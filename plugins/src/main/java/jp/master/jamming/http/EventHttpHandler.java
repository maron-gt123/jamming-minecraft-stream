package jp.master.jamming.http;

import jp.master.jamming.config.ConfigManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class EventHttpHandler implements HttpHandler {

    private final JavaPlugin plugin;
    private final Gson gson = new Gson();

    public EventHttpHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStream body = exchange.getRequestBody();
            String json = new String(body.readAllBytes(), StandardCharsets.UTF_8);

            Map<String, Object> payload = gson.fromJson(
                    json,
                    new TypeToken<Map<String, Object>>(){}.getType()
            );

            String eventType = (String) payload.get("event_type");
            Object data = payload.get("data");

            if (eventType == null) {
                exchange.sendResponseHeaders(400, 0);
                exchange.getResponseBody().write("Missing event_type".getBytes());
                exchange.close();
                return;
            }

            if (!ConfigManager.isEventEnabled(eventType)) {
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().write("OK".getBytes());
                exchange.close();
                return;
            }

            plugin.getLogger().info("Event received: " + eventType + " -> " + gson.toJson(data));

            executeCommands(eventType, data);

            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("OK".getBytes());
            exchange.close();

        } catch (Exception e) {
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (Exception ignored) {}
            plugin.getLogger().severe("Failed to handle request: " + e.getMessage());
        }
    }

    private void executeCommands(String eventType, Object dataObj) {
        Map<String, Object> data = dataObj instanceof Map ? (Map<String, Object>) dataObj : Map.of();

        if (eventType.equals("connect") ||
                eventType.equals("online") ||
                eventType.equals("offline")) {

            handleStreamStatus(eventType, data);
            return;
        }

        // ===== スコアボード更新（追加）=====
        if (eventType.equals("gift")) {

            String user = (String) data.getOrDefault("user", "unknown");
            String nickname = (String) data.getOrDefault("nickname", user);
            int diamondTotal = ((Number) data.getOrDefault("diamond_total", 0)).intValue();

            if (plugin instanceof jp.master.jamming.JammingStream stream) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    stream.getScoreboardManager().updateGift(user, nickname, diamondTotal);
                });
            }
        }
        // ===== comment 強制チャット表示 =====
        if (eventType.equals("comment")) {

            String nickname = (String) data.getOrDefault("nickname", "???");
            String comment = (String) data.getOrDefault("comment", "");

            String command = "tellraw @a [" +
                    "{\"text\":\"[\",\"color\":\"red\"}," +
                    "{\"text\":\"T\",\"color\":\"red\"}," +
                    "{\"text\":\"i\",\"color\":\"gold\"}," +
                    "{\"text\":\"k\",\"color\":\"yellow\"}," +
                    "{\"text\":\"T\",\"color\":\"green\"}," +
                    "{\"text\":\"o\",\"color\":\"aqua\"}," +
                    "{\"text\":\"k\",\"color\":\"light_purple\"}," +
                    "{\"text\":\"]\",\"color\":\"red\"}," +
                    "{\"text\":\"【" + nickname + "】\",\"color\":\"green\"}," +
                    "{\"text\":\" " + comment + "\",\"color\":\"white\"}" +
                    "]";

            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {

                    if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                        return;
                    }

                    plugin.getServer().dispatchCommand(
                            plugin.getServer().getConsoleSender(),
                            command
                    );

                }
            });
            return;
        }
        if (plugin instanceof jp.master.jamming.JammingStream stream) {
            if (!stream.getGameManager().isGameActive()) {
                plugin.getLogger().info(
                        "Event ignored (game not active): " + eventType
                );
                return;
            }
        }
        List<Map<String, Object>> commands =
                ConfigManager.getCommands(eventType);
        if (commands.isEmpty()) return;

        ConfigManager.setLastNickname(
                (String) data.getOrDefault("nickname", "???")
        );
        for (Map<String, Object> cmdConfig : commands) {
            if (!checkCondition(eventType, cmdConfig, data)) continue;
            int repeat = 1;
            // count回実行
            if (eventType.equals("gift")) {
                repeat = ((Number) data.getOrDefault("count", 1)).intValue();
            }

            for (int i = 0; i < repeat; i++) {
                if (cmdConfig.containsKey("commands")) {
                    List<String> commandList = (List<String>) cmdConfig.get("commands");
                    for (String rawCmd : commandList) {
                        final String command =
                                replacePlaceholders(rawCmd, data);
                        plugin.getServer().getScheduler().runTask(plugin, () ->
                                plugin.getServer().dispatchCommand(
                                        plugin.getServer().getConsoleSender(), command
                                )
                        );
                    }
                }
                else if (cmdConfig.containsKey("command")) {
                    final String command =
                            replacePlaceholders((String) cmdConfig.get("command"), data);
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            plugin.getServer().dispatchCommand(
                                    plugin.getServer().getConsoleSender(), command
                            )
                    );
                }
            }
        }
    }

    private boolean checkCondition(String eventType, Map<String, Object> cmdConfig, Map<String, Object> data) {

        // ===== like rule =====
        if (eventType.equals("like")) {
            if (cmdConfig.containsKey("count")) {
                double required = ((Number) cmdConfig.get("count")).doubleValue();
                double prevTotal = ((Number) data.getOrDefault("prev_total", 0)).doubleValue();
                double userTotal = ((Number) data.getOrDefault("user_total", 0)).doubleValue();

                // prev_total < required <= user_total
                return prevTotal < required && required <= userTotal;
            }
        }

        // ===== gift rule =====
        if (eventType.equals("gift")) {
            if (cmdConfig.containsKey("gift_name")) {
                String requiredName = (String) cmdConfig.get("gift_name");
                String giftName = (String) data.getOrDefault("gift_name", "");
                if (!requiredName.equals(giftName)) return false;
            }
        }

        return true;
    }

    private String replacePlaceholders(String command, Map<String, Object> data) {
        if (data == null) return command;

        if (data.containsKey("nickname")) {
            command = command.replace("{nickname}", (String) data.get("nickname"));
        }
        if (data.containsKey("count")) {
            command = command.replace("{count}", String.valueOf(data.get("count")));
        }
        if (data.containsKey("user_total")) {
            command = command.replace("{user_total}", String.valueOf(data.get("user_total")));
        }
        if (data.containsKey("gift_name")) {
            command = command.replace("{gift_name}", (String) data.get("gift_name"));
        }
        if (data.containsKey("comment")) {
            command = command.replace("{comment}", (String) data.get("comment"));
        }

        return command;
    }

    private void handleStreamStatus(String eventType, Map<String, Object> data) {

        String user = (String) data.getOrDefault("user", "unknown");

        if (eventType.equals("connect") || eventType.equals("online")) {

            String command =
                    "tellraw @a {\"text\":\"[TikTok] " + user + " が配信を開始\",\"color\":\"green\"}";

            plugin.getServer().getScheduler().runTask(plugin, () ->
                    plugin.getServer().dispatchCommand(
                            plugin.getServer().getConsoleSender(),
                            command
                    )
            );
            return;
        }

        if (eventType.equals("offline")) {

            String command =
                    "tellraw @a {\"text\":\"[TikTok] " + user + " の配信終了\",\"color\":\"red\"}";

            plugin.getServer().getScheduler().runTask(plugin, () ->
                    plugin.getServer().dispatchCommand(
                            plugin.getServer().getConsoleSender(),
                            command
                    )
            );
        }
    }
}