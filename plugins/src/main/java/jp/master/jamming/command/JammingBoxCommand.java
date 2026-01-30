package jp.master.jamming.command;

import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.game.JammingGameManager;
import jp.master.jamming.config.ConfigManager;
import jp.master.jamming.listener.JammingBoxClickDelay;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.Component;

public class JammingBoxCommand implements CommandExecutor {

    private final JammingBoxManager manager;
    private final JammingGameManager gameManager;
    private final JammingBoxClickDelay clickDelay;

    public JammingBoxCommand(
            JammingBoxManager manager,
            JammingGameManager gameManager,
            JammingBoxClickDelay clickDelay
    ) {
        this.manager = manager;
        this.gameManager = gameManager;
        this.clickDelay = clickDelay;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String root = command.getName().toLowerCase();
        if (root.equals("jammingbox")) {
            return handleJammingBox(sender, args);
        }
        if (root.equals("jammingevent")) {
            return handleJammingEvent(sender, args);
        }
        return true;
    }

    private boolean handleJammingBox(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ実行できます");
            return true;
        }
        if (args.length == 0) {
            sendHelpPage1(sender);
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
            case "set_block_interaction_range" -> handleSetBlockInteractionRange(player, args);
            case "clickdelay" -> handleClickDelay(player, args);
            default -> sendHelpPage1(sender);
        }
        return true;
    }

    private boolean handleJammingEvent(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelpPage2(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "text" -> handleText(sender, args);
            case "title" -> handleTitle(sender, args);
            case "tnt"  -> handleTnt(sender, args);
            case "extnt" -> handleEXTnt(sender, args);
            case "reset" -> handleReset(sender, args);
            default -> sendHelpPage2(sender);
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
        Material material = Material.GLASS;
        if (args.length >= 3) {
            Material m = Material.matchMaterial(args[2]);
            if (m != null && m.isBlock()) {
                material = m;
            } else {
                player.sendMessage("§c無効なブロック素材です: " + args[2]);
                return;
            }
        }

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
        if (gameManager.isGameActive()) {
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

        if (gameManager.isGameActive()) {
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

        gameManager.startGameWithCountdown(countdownSeconds);
        player.sendMessage("§aゲーム開始準備中...");
    }

    /* =======================
       stop
       ======================= */
    private void handleStop(Player player) {

        if (!gameManager.isGameActive()) {
            player.sendMessage("§eゲームはすでに停止しています");
            return;
        }
        long seconds = gameManager.getElapsedSeconds();
        gameManager.stopGame();
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
    private void handleReset(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage("§e/jammingevent reset <dragon|wither>");
            return;
        }

        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        if (!gameManager.isGameActive()) {
            sender.sendMessage("§cゲーム中のみ実行できます");
            return;
        }

        Player player;
        if (sender instanceof Player p) {
            player = p;
        } else {
            // Console / HTTP から来た場合
            player = sender.getServer().getOnlinePlayers()
                    .stream()
                    .findFirst()
                    .orElse(null);
        }

        switch (args[1].toLowerCase()) {
            case "dragon" -> {
                sender.sendMessage("§cドラゴンが接近しています…");
                gameManager.resetBoxByDragon(player);
            }
            case "wither" -> {
                sender.sendMessage("§cウィザーが接近しています…");
                gameManager.resetBoxByWither(player);
            }
            default -> sender.sendMessage("§e/jammingevent reset <dragon|wither>");
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
   clickdelay
   ======================= */
    private void handleClickDelay(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§e/jammingbox clickdelay <true|false>");
            return;
        }

        if (args[1].equalsIgnoreCase("true")) {
            clickDelay.enable(player);
            player.sendMessage("§aクリック遅延を有効化しました（固定5ティック）");
        } else if (args[1].equalsIgnoreCase("false")) {
            clickDelay.disable(player);
            player.sendMessage("§cクリック遅延を無効化しました");
        } else {
            player.sendMessage("§e/jammingbox clickdelay <true|false>");
        }
    }
    /* =======================
   text
   ======================= */
    private void handleText(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage("§e/jammingevent text <message>");
            return;
        }

        String rawMessage = String.join(
                " ",
                java.util.Arrays.copyOfRange(args, 1, args.length)
        );

        String nickname = ConfigManager.getLastNickname();
        if (nickname == null) nickname = "???";

        String message = rawMessage.replace("{nickname}", nickname);

        String result = "§6§l[jammingbox] §f" + message;

        sender.getServer().broadcastMessage(result);
    }
    /* =======================
       Title
       ======================= */
    private void handleTitle(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage("§e/jammingevent title <message>");
            return;
        }

        String rawMessage = String.join(
                " ",
                java.util.Arrays.copyOfRange(args, 1, args.length)
        );

        String nickname = ConfigManager.getLastNickname();
        if (nickname == null) nickname = "???";

        String message = rawMessage.replace("{nickname}", nickname);

        for (Player player : sender.getServer().getOnlinePlayers()) {
            player.sendTitle(
                    "§6§l" + message,   // 上段：置換済み message
                    "§c§l" + nickname,  // 下段：nickname 固定
                    10,
                    40,
                    10
            );
        }
    }
    /* =======================
    tnt
    ======================= */
    private void handleTnt(CommandSender sender, String[] args) {

        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        int count = 1;
        if (args.length >= 2) {
            try {
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c数は数値で指定してください");
                return;
            }
        }

        for (int i = 0; i < count; i++) {

            Location inner = manager.getRandomInnerLocation().orElse(null);
            if (inner == null) continue;

            Location spawn = inner.clone().add(
                    Math.random() * 3 - 1.5,
                    10,
                    Math.random() * 3 - 1.5
            );

            TNTPrimed tnt = spawn.getWorld().spawn(spawn, TNTPrimed.class);
            tnt.setFuseTicks(60); // 3秒
        }

        sender.sendMessage("§c§l[TNT] §f" + count + " 個投下 💣");
    }
    private void handleEXTnt(CommandSender sender, String[] args) {

        final double exPower = 8.0; // 強化版TNTの固定爆発力

        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        int count = 1; // 投下個数デフォルト1
        if (args.length >= 2) {
            try {
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c個数は数値で指定してください");
                return;
            }
        }

        for (int i = 0; i < count; i++) {
            Location inner = manager.getRandomInnerLocation().orElse(null);
            if (inner == null) continue;

            Location spawn = inner.clone().add(
                    Math.random() * 3 - 1.5,
                    10,
                    Math.random() * 3 - 1.5
            );

            TNTPrimed tnt = spawn.getWorld().spawn(spawn, TNTPrimed.class);
            tnt.setFuseTicks(60); // 3秒
            tnt.setYield((float) exPower); // 強化TNT
        }

        sender.sendMessage("§c§l[EXTNT] §f" + count + " 個投下 💣 爆発力固定: " + exPower);
    }
    /* =======================
       help
       ======================= */
    private void handleHelp(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cページ番号は数値で指定してください");
                return;
            }
        }
        switch (page) {
            case 1 -> sendHelpPage1(sender);
            case 2 -> sendHelpPage2(sender);
            case 3 -> sendHelpPage3(sender);
            default -> sender.sendMessage("§cそのページは存在しません");
        }
    }
    private void sendHelpPage1(CommandSender sender) {
        sender.sendMessage("§6==== JammingBox Help (1/3) ====");
        sender.sendMessage("§e/jammingbox create [size] §7- jammingboxを作成");
        sender.sendMessage("§e/jammingbox remove        §7- jammingboxを削除");
        sender.sendMessage("§e/jammingbox start [count] §7- ゲーム開始");
        sender.sendMessage("§e/jammingbox stop         §7- ゲーム停止");
        sender.sendMessage("§e/jammingbox replace true | false §7- ブロック置換切替");
        sender.sendMessage("§e/jammingbox fill         §7- jammingboxを埋める");
        sender.sendMessage("§e/jammingbox clear        §7- jammingboxを空にする");
        sender.sendMessage("§e/jammingbox set_block_interaction_range <v>  §7- ブロック設置の長さ");
        sender.sendMessage("§e/jammingbox clickdelay <true|false> §7- クリック遅延の有効化/無効化");
    }
    private void sendHelpPage2(CommandSender sender) {
        sender.sendMessage("§6==== JammingEvent Help ====");
        sender.sendMessage("§e/jammingevent text <msg> §7- 全体メッセージ");
        sender.sendMessage("§e/jammingevent title <msg> §7- タイトル表示");
        sender.sendMessage("§e/jammingevent tnt [count] §7- TNT投下");
        sender.sendMessage("§e/jammingevent extnt [count] §7- 強化版TNT投下");
        sender.sendMessage("§e/jammingevent reset <dragon|wither> §7- 演出付きリセット");
        sender.sendMessage("§7◀ help 1   help 3 ▶");
    }
    private void sendHelpPage3(CommandSender sender) {
        sender.sendMessage("§6==== JammingBox Help (3/3) ====");
        sender.sendMessage("§7◀ /jammingbox help 2");
    }
}
