package jp.master.jamming.command;

import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.game.JammingGameManager;
import jp.master.jamming.config.ConfigManager;
import jp.master.jamming.listener.JammingBoxClickDelay;
import jp.master.jamming.listener.JammingDoublePlaceListener;
import jp.master.jamming.prison.JammingPrisonManager;
import jp.master.jamming.JammingStream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Random;
import net.kyori.adventure.text.Component;

public class JammingBoxCommand implements CommandExecutor {

    private final JammingBoxManager manager;
    private final JammingGameManager gameManager;
    private final JammingBoxClickDelay clickDelay;
    private final JammingPrisonManager prisonManager;
    private final Random random = new Random();
    private final JammingStream plugin;
    private final JammingDoublePlaceListener doublePlace;

    public JammingBoxCommand(
            JammingStream plugin,
            JammingBoxManager manager,
            JammingGameManager gameManager,
            JammingBoxClickDelay clickDelay,
            JammingPrisonManager prisonManager,
            JammingDoublePlaceListener doublePlace
    ) {
        this.plugin = plugin;
        this.manager = manager;
        this.gameManager = gameManager;
        this.clickDelay = clickDelay;
        this.prisonManager = prisonManager;
        this.doublePlace = doublePlace;
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
            case "reload" -> handleReload(sender);
            case "create" -> handleCreate(player, args);
            case "remove" -> handleRemove(player);
            case "start"  -> handleStart(player, args);
            case "stop"   -> handleStop(player);
            case "replace" -> handleReplace(player, args);
            case "clear" -> handleClear(player);
            case "goal" -> handleTarget(player, args);
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
            case "fill" -> handleFill(sender, args);
            case "fillblock" -> handleFillBlock(sender, args);
            case "prison" -> handlePrison(sender, args);
            case "addclear" -> handleAddClear(sender, args);
            case "delclear" -> handleDelClear(sender, args);
            case "rocket" -> handleRocket(sender, args);
            case "doubleplace" -> handleDoublePlace(sender, args);
            case "heightup" -> handleHeightUp(sender, args);
            case "sizeup" -> handleSizeUp(sender, args);
            case "size_reset" -> handleSizeReset(sender);
            default -> sendHelpPage2(sender);
        }
        return true;
    }
    /* =======================
       reload
       ======================= */
    private void handleReload(CommandSender sender) {
        ConfigManager.reload();
        if (plugin.getHttpServerManager() != null) {
            plugin.getHttpServerManager().stop();
            plugin.getHttpServerManager().start();
        }
        sender.sendMessage("§aConfigを再読み込みしました");
    }

    /* =======================
       create
       ======================= */
    private void handleCreate(Player player, String[] args) {

        if (manager.hasBox()) {
            player.sendMessage("§cすでにjammingboxが存在します。先に /jammingbox remove を実行してください");
            return;
        }

        int sizeXZ = 9;
        int height = 9;
        Material material = Material.GLASS;

        // =====================
        // サイズ取得
        // =====================
        try {
            if (args.length >= 2) {
                sizeXZ = Integer.parseInt(args[1]);
            }

            if (args.length >= 3) {
                height = Integer.parseInt(args[2]);
            } else {
                height = sizeXZ; // 指定なしならXZと同じ
            }

        } catch (NumberFormatException e) {
            player.sendMessage("§cサイズは数値で指定してください");
            return;
        }

        // =====================
        // バリデーション
        // =====================
        if (sizeXZ < 5 || height < 3) {
            player.sendMessage("§cサイズが小さすぎます（XZ>=5, Y>=3）");
            return;
        }

        if (sizeXZ % 2 == 0) {
            player.sendMessage("§cXZサイズは奇数で指定してください（例: 7, 9, 11）");
            return;
        }

        if (height % 2 == 0) {
            player.sendMessage("§cYサイズは奇数で指定してください（例: 7, 9, 11）");
            return;
        }

        // =====================
        // Material取得
        // =====================
        int materialIndex = (args.length >= 3) ? 3 : 2;

        if (args.length > materialIndex) {
            Material m = Material.matchMaterial(args[materialIndex]);
            if (m != null && m.isBlock()) {
                material = m;
            } else {
                player.sendMessage("§c無効なブロック素材です: " + args[materialIndex]);
                return;
            }
        }

        // =====================
        // 作成
        // =====================
        Location center = player.getLocation();
        manager.createBox(center, sizeXZ, height, material);

        player.sendMessage("§aJammingBoxを作成しました");
        player.sendMessage("§7XZ: " + sizeXZ + " / Y: " + height + " / 素材: " + material.name());
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
       goal (クリア目標数設定)
       ======================= */
    private void handleTarget(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§e/jammingbox goal <数値>  - クリア目標数を設定");
            return;
        }

        int target;
        try {
            target = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c数値を指定してください");
            return;
        }

        if (target <= 0) {
            player.sendMessage("§cクリア目標数は1以上を指定してください");
            return;
        }

        gameManager.setClearGoal(target); // JammingGameManager 側のメソッド呼び出し
        player.sendMessage("§aクリア目標数を " + target + " に設定しました");
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

        gameManager.spawnTnt(count, false);
        sender.sendMessage("§c§l[TNT] §f" + count + " 個投下");
    }

    private void handleEXTnt(CommandSender sender, String[] args) {
        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        int count = 1;
        if (args.length >= 2) {
            try {
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c個数は数値で指定してください");
                return;
            }
        }

        gameManager.spawnTnt(count, true);
        sender.sendMessage("§c§l[EXTNT] §f" + count + " 個投下 爆発力固定: 8.0");
    }

    /* =======================
       fill
       ======================= */
    private void handleFill(CommandSender sender, String[] args) {
        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }
        manager.fillInsideForce();
    }

    /* =======================
       fillblock
       ======================= */
    private void handleFillBlock(CommandSender sender, String[] args) {
        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        int levels = 1; // デフォルト1列
        if (args.length >= 2) {
            try {
                levels = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c埋める段数は 1～3 の数字で指定してください");
                return;
            }
        }

        if (levels < 1) levels = 1;

        manager.fillColumnsFromAir(levels, Material.STONE);

        sender.sendMessage("§aJammingBoxを下から " + levels + " 列分埋めました" +
                (manager.isReplaceEnabled() ? "（自動置換ルール適用）" : ""));
    }
    /* =======================
       Prison
       ======================= */
    private void handlePrison(CommandSender sender, String[] args) {

        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        if (!gameManager.isGameActive()) {
            sender.sendMessage("§cゲーム中のみ実行できます");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§e/jammingevent prison <秒数>");
            return;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c秒数は数値で指定してください");
            return;
        }

        Player player;
        if (sender instanceof Player p) {
            player = p;
        } else {
            player = sender.getServer().getOnlinePlayers()
                    .stream().findFirst().orElse(null);
        }
        if (player == null) return;

        var box = manager.getBox();
        var c = box.getCenter();
        int halfXZ = box.getHalfXZ();
        int hy = box.getHalfY();
        int hx = halfXZ;
        int hz = halfXZ;

        // 天井左上
        Location base = new Location(
                box.getWorld(),
                c.getBlockX() - hx,
                c.getBlockY() + hy,
                c.getBlockZ() - hz
        );

        // 20マス範囲
        Location prisonCenter = base.clone().add(
                1 + random.nextInt(20), // X
                10,                       // Y: 箱の上端+10
                1 + random.nextInt(20)  // Z
        );

        prisonManager.imprison(player, prisonCenter, seconds);

        sender.sendMessage("§c§l[PRISON] §f" + seconds + "秒間投獄 ⛓");
    }
    /* =======================
       addclear
       ======================= */
    private void handleAddClear(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ実行可能です");
            return;
        }
        if (!gameManager.isGameActive()) {
            sender.sendMessage("§cゲーム中のみクリア数を変更できます");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§e/jammingbox addclear <数>");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数値を指定してください");
            return;
        }
        if (amount <= 0) {
            sender.sendMessage("§c1以上の数値を指定してください");
            return;
        }
        gameManager.addClearCount(amount);
        sender.sendMessage("§aクリア数を " + amount + " 増加させました §e現在: " + gameManager.getClearCount());
    }

    /* =======================
       delclear
       ======================= */
    private void handleDelClear(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ実行可能です");
            return;
        }
        if (!gameManager.isGameActive()) {
            sender.sendMessage("§cゲーム中のみクリア数を変更できます");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§e/jammingbox delclear <数>");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数値を指定してください");
            return;
        }
        if (amount <= 0) {
            sender.sendMessage("§c1以上の数値を指定してください");
            return;
        }
        gameManager.addClearCount(-amount);
        sender.sendMessage("§cクリア数を " + amount + " 減少させました §e現在: " + gameManager.getClearCount());
    }

    /* =======================
       rocket
       ======================= */
    private void handleRocket(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;

        // 回数を取得（引数チェックのみ）
        int count = 1;
        if (args.length >= 2) {
            try {
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§c回数は数字で指定してください");
                return;
            }
        }

        gameManager.launchRocket(player, count, 3.0);
    }
    /* =======================
       eDoublePlace
       ======================= */
    private void handleDoublePlace(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage("§e/jammingevent doubleplace time <秒> | clear");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "time" -> {
                if (args.length < 3) {
                    sender.sendMessage("§e/jammingevent doubleplace time <秒>");
                    return;
                }
                int sec;
                try {
                    sec = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c秒数は数値で指定");
                    return;
                }
                doublePlace.enableWithTime(sec);
            }
            case "clear" -> {
                doublePlace.enableClearMode();
            }
            default -> sender.sendMessage("§e/jammingevent doubleplace time <秒> | clear");
        }
    }
    /* =======================
       box size
       ======================= */
    private void handleHeightUp(CommandSender sender, String[] args) {

        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§e/jammingevent heightup <数値>");
            return;
        }

        int value;
        try {
            value = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数値を指定してください");
            return;
        }

        manager.addHeight(value);

        sender.sendMessage("§a高さを +" + value + " しました");
    }
    private void handleSizeUp(CommandSender sender, String[] args) {

        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§e/jammingevent sizeup <数値>");
            return;
        }

        int value;
        try {
            value = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数値を指定してください");
            return;
        }

        manager.addSizeXZ(value);

        sender.sendMessage("§aXZサイズを +" + value + " しました");
    }
    private void handleSizeReset(CommandSender sender) {

        if (!manager.hasBox()) {
            sender.sendMessage("§cJammingBoxが存在しません");
            return;
        }

        manager.resetSize();

        sender.sendMessage("§aサイズを初期値に戻しました");
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
        sender.sendMessage("§e/jammingbox reload        §7- configを再読込");
        sender.sendMessage("§e/jammingbox create [XZ] [Y] [BLOCK]§7- jammingboxを作成");
        sender.sendMessage("§e/jammingbox remove        §7- jammingboxを削除");
        sender.sendMessage("§e/jammingbox start [time] §7- ゲーム開始");
        sender.sendMessage("§e/jammingbox stop         §7- ゲーム停止");
        sender.sendMessage("§e/jammingbox replace true | false §7- ブロック置換切替");
        sender.sendMessage("§e/jammingbox clear        §7- jammingboxを空にする");
        sender.sendMessage("§e/jammingbox goal <数値>  §7- クリア目標数を設定");
        sender.sendMessage("§e/jammingbox set_block_interaction_range <v>  §7- ブロック設置の長さ");
        sender.sendMessage("§e/jammingbox clickdelay <true|false> §7- クリック遅延の有効化/無効化");
    }
    private void sendHelpPage2(CommandSender sender) {
        sender.sendMessage("§6==== JammingEvent Help ====");
        sender.sendMessage("§e/jammingevent text <msg> §7- 全体メッセージ");
        sender.sendMessage("§e/jammingevent title <msg> §7- タイトル表示");
        sender.sendMessage("§e/jammingevent tnt <1|2|3> §7- TNT投下");
        sender.sendMessage("§e/jammingevent extnt <1|2|3> §7- 強化版TNT投下");
        sender.sendMessage("§e/jammingevent reset <dragon|wither> §7- 演出付きリセット");
        sender.sendMessage("§e/jammingbox fill         §7- jammingboxを埋める");
        sender.sendMessage("§e/jammingevent fillblock <1|2|3> §7- JammingBox内の下から指定列数を埋める");
        sender.sendMessage("§e/jammingevent prison [time] §7- 牢獄に投獄");
        sender.sendMessage("§e/jammingevent addclear <1|2|3> §7- クリア数増加");
        sender.sendMessage("§e/jammingevent delclear <1|2|3> §7- クリア数減少");
        sender.sendMessage("§e/jammingevent rocket <1|2|3> §7- プレイヤーをロケットで打ち上げ");
        sender.sendMessage("§e/jammingevent doubleplace <time> §7- 同時に2個ブロック設置");
        sender.sendMessage("§e/jammingevent heightup <数値> §7- 高さを拡張");
        sender.sendMessage("§e/jammingevent sizeup <数値> §7- 横サイズを拡張");
        sender.sendMessage("§e/jammingevent size_reset §7- サイズを初期化");
        sender.sendMessage("§7◀ help 1   help 3 ▶");
    }
    private void sendHelpPage3(CommandSender sender) {
        sender.sendMessage("§6==== JammingBox Help (3/3) ====");
        sender.sendMessage("§7◀ /jammingbox help 2");
    }
}
