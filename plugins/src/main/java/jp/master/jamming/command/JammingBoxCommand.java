package jp.master.jamming.command;

import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.config.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JammingBoxCommand implements CommandExecutor {

    private final JammingBoxManager manager;

    public JammingBoxCommand(JammingBoxManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ実行できます");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args);
            case "remove" -> handleRemove(player);
            case "start"  -> handleStart(player, args);
            case "stop"   -> handleStop(player);
            case "replace" -> handleReplace(player, args);
            case "fill" -> handleFill(player);
            case "clear" -> handleClear(player);
            case "reset" -> handleReset(player, args);
            case "set_block_interaction_range" -> handleSetBlockInteractionRange(player, args);
            default -> sendHelp(player);
        }
        return true;
    }

    /* =======================
       create
       ======================= */
    private void handleCreate(Player player, String[] args) {

        if (manager.hasBox()) {
            player.sendMessage("§cすでにjammingboxが存在します。先に /jammingbox remove を実行してください");
            return;
        }

        int size = 9;
        if (args.length >= 2) {
            try {
                size = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cサイズは数値で指定してください");
                return;
            }
        }

        if (size < 5) {
            player.sendMessage("§cサイズは5以上を指定してください");
            return;
        }

        if (size % 2 == 0) {
            player.sendMessage("§cサイズは奇数で指定してください（例: 7, 9, 11）");
            return;
        }

        Location center = player.getLocation();
        Material material = Material.GLASS; // ← 後でconfig化可

        manager.createBox(center, size, material);

        player.sendMessage("§ajammingboxを作成しました");
        player.sendMessage("§7サイズ: " + size + " / 素材: " + material.name());
    }

    /* =======================
       remove
       ======================= */
    private void handleRemove(Player player) {

        if (!manager.hasBox()) {
            player.sendMessage("§ejammingboxは存在しません");
            return;
        }
        if (manager.isGameActive()) {
            player.sendMessage("§cゲーム中は削除できません。先に stop してください");
            return;
        }
        manager.removeBox();
        player.sendMessage("§ajammingboxを削除しました");
    }
    /* =======================
       start
       ======================= */
    private void handleStart(Player player, String[] args) {

        if (!manager.hasBox()) {
            player.sendMessage("§c先に /jammingbox create を実行してください");
            return;
        }

        if (manager.isGameActive()) {
            player.sendMessage("§eゲームはすでに開始されています");
            return;
        }

        int countdownSeconds = 0;
        if (args.length >= 2) {
            try {
                countdownSeconds = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§c秒数は数字で指定してください: /jammingbox start <秒数>");
                return;
            }
        }

        manager.startGameWithCountdown(countdownSeconds);
        player.sendMessage("§aゲーム開始準備中...");
    }

    /* =======================
       stop
       ======================= */
    private void handleStop(Player player) {

        if (!manager.isGameActive()) {
            player.sendMessage("§eゲームはすでに停止しています");
            return;
        }
        long seconds = manager.getElapsedSeconds();
        manager.stopGame();
        player.getWorld().playSound(
                player.getLocation(),
                Sound.UI_TOAST_CHALLENGE_COMPLETE,
                1.0f,
                1.0f
        );
        player.sendMessage("§cゲームを停止しました");
    }
    /* =======================
       replace
       ======================= */
    private void handleReplace(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§e/jammingbox replace true|false");
            return;
        }
        boolean enabled = args[1].equalsIgnoreCase("true");
        manager.setReplaceEnabled(enabled);

        player.sendMessage(
                "§aJammingBox内ブロック置換: " + (enabled ? "有効" : "無効")
        );
    }

    /* =======================
       fill
       ======================= */
    private void handleFill(Player player) {
        if (!manager.hasBox()) {
            player.sendMessage("§cJammingBoxが存在しません");
            return;
        }
        manager.fillInsideForce();
        player.sendMessage("§aJammingBoxを自動変換ルールで埋めました");
    }

    /* =======================
       clear
       ======================= */
    private void handleClear(Player player) {
        if (!manager.hasBox()) {
            player.sendMessage("§cJammingBoxが存在しません");
            return;
        }
        manager.clearInside();
        player.sendMessage("§aJammingBox内のブロックを削除しました");
    }
    /* =======================
    reset
    ======================= */
    private void handleReset(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("§e/jammingbox reset <dragon|wither>");
            return;
        }

        if (!manager.hasBox()) {
            player.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        if (!manager.isGameActive()) {
            player.sendMessage("§cゲーム中のみ実行できます");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "dragon" -> {
                player.sendMessage("§cドラゴンが接近しています…");
                manager.resetByDragon(player); // ★ 作成済み処理を呼ぶ
            }
            case "wither" -> {
                player.sendMessage("§cウィザーが接近しています…");
                manager.resetByWither(player);
            }
            default -> {
                player.sendMessage("§e/jammingbox reset <dragon|wither>");
            }
        }
    }
    /* =======================
   set_block_interaction_range
   ======================= */
    private void handleSetBlockInteractionRange(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("§e/jammingbox set_block_interaction_range <数値>");
            return;
        }

        double range;
        try {
            range = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c数値を指定してください");
            return;
        }

        // ★ Minecraft 標準 attribute を実行
        player.getServer().dispatchCommand(
                player.getServer().getConsoleSender(),
                "attribute " + player.getName()
                        + " minecraft:block_interaction_range base set "
                        + range
        );

        player.sendMessage("§aブロック操作距離を " + range + " に設定しました");
    }
    /* =======================
       help
       ======================= */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6==== JammingBox ====");
        sender.sendMessage("§e/jammingbox create [size] §7- jammingboxを作成");
        sender.sendMessage("§e/jammingbox remove        §7- jammingboxを削除");
        sender.sendMessage("§e/jammingbox start [count] §7- ゲーム開始");
        sender.sendMessage("§e/jammingbox stop         §7- ゲーム停止");
        sender.sendMessage("§e/jammingbox replace true | false §7- ブロック置換切替");
        sender.sendMessage("§e/jammingbox fill         §7- jammingboxを埋める");
        sender.sendMessage("§e/jammingbox clear        §7- jammingboxを空にする");
        sender.sendMessage("§e/jammingbox reset <dragon|wither> §7- 演出付きリセット");
        sender.sendMessage("§e/jammingbox set_block_interaction_range <value> §7- 操作距離変更");
    }
}
