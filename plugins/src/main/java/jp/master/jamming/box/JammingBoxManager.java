package jp.master.jamming.box;

import jp.master.jamming.effect.JammingGameEffects;
import jp.master.jamming.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.DragonFireball;
import org.bukkit.Particle;

import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;

public class JammingBoxManager {

    // =========================================================
    // Effect
    // =========================================================
    private final JammingGameEffects effects = new JammingGameEffects();
    // =========================================================
    // Box state
    // =========================================================
    private JammingBox box = null;
    private final Set<BlockKey> placedBlocks = new HashSet<>();
    // =========================================================
    // replase settings
    // =========================================================
    private boolean replaceEnabled;
    private Material replaceBottom;
    private Material replaceMiddle;
    private Material replaceTop;
    // =========================================================
    // Game state
    // =========================================================
    private boolean gameActive = false;
    private long gameStartTime = 0L;
    private boolean clearSequenceRunning = false;
    private int clearCountdownSeconds;
    private BukkitTask clearCheckTask;
    // =========================================================
    // Tasks / Plugin
    // =========================================================
    private BukkitTask actionBarTask;
    private final JavaPlugin plugin;
    private BukkitTask countdownTask;
    private BukkitTask clearCountdownTask;

    // =========================================================

    // =========================================================
    // config load
    // =========================================================
    public JammingBoxManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.replaceEnabled = ConfigManager.getReplaceEnabledDefault();
        loadReplaceConfig();
        loadClearConfig();
    }
    // =========================================================
    // ボックス操作
    // =========================================================
    /** ボックス生成（壁・床を構築） */
    public void createBox(Location center, int size, Material material) {
        if (box != null) {
            return;
        }
        box = new JammingBox(center, size);
        buildWalls(box, material);
    }
    /** ボックス削除（壁撤去＋ゲーム停止） */
    public void removeBox() {
        if (box == null) return;
        clearWalls(box);
        box = null;
    }
    /** ボックス存在確認 */
    public boolean hasBox() {
        return box != null;
    }
    /** アクティブなボックス取得 */
    public Optional<JammingBox> getActiveBox() {
        if (box != null && box.isActive()) return Optional.of(box);
        return Optional.empty();
    }
    /** ボックス内部のランダム座標取得 */
    public Optional<Location> getRandomInnerLocation() {
        Optional<JammingBox> box = getActiveBox();
        return box.map(JammingBox::getRandomInnerLocation);
    }
    /** 管理下ブロックかどうか */
    public boolean isProtected(Location loc) {
        if (loc == null) return false;
        return placedBlocks.contains(new BlockKey(loc));
    }
    /** 現在のボックス取得 */
    public JammingBox getBox() {
        return box;
    }
    // =========================================================
    // 内部ブロック操作
    // =========================================================
    /** jammingbox内部を強制充填 */
    public void fillInsideForce() {
        if (!hasBox()) return;
        fillInside(box);
    }
    /** replace 有効時の内部充填 */
    public void fillInsideWithReplace() {
        if (!hasBox()) return;
        if (!replaceEnabled) return;
        fillInside(box);
    }
    /** 内部ブロック全消去 */
    public void clearInside() {
        if (!hasBox()) return;
        if (box != null) clearInside(box);
    }
    // =========================================================
    // replase
    // =========================================================
    /** config情報取得 */
    private void loadReplaceConfig() {
        replaceBottom = Material.matchMaterial(ConfigManager.getReplaceBottom());
        replaceMiddle = Material.matchMaterial(ConfigManager.getReplaceMiddle());
        replaceTop = Material.matchMaterial(ConfigManager.getReplaceTop());

        if (replaceBottom == null) replaceBottom = Material.IRON_BLOCK;
        if (replaceMiddle == null) replaceMiddle = Material.GOLD_BLOCK;
        if (replaceTop == null) replaceTop = Material.DIAMOND_BLOCK;
    }
    /** 高さに応じたブロック種別取得 */
    public Material getReplaceBlockType(JammingBox box, int y) {
        int minY = box.getCenter().getBlockY() - box.getHalf() + 1;
        int maxY = box.getCenter().getBlockY() + box.getHalf();

        int height = maxY - minY;
        int relativeY = y - minY;
        int layerHeight = height / 3;

        if (relativeY >= 2 * layerHeight) return replaceTop;
        else if (relativeY >= layerHeight) return replaceMiddle;
        else return replaceBottom;
    }
    public boolean isReplaceEnabled() {
        return replaceEnabled;
    }
    public void setReplaceEnabled(boolean enabled) {
        this.replaceEnabled = enabled;
    }
    // =========================================================
    // ゲーム制御
    // =========================================================
    public boolean isGameActive() {
        return gameActive;
    }
    /** config情報取得 */
    private void loadClearConfig() {
        clearCountdownSeconds = ConfigManager.getClearCountdown();
    }
    /** ゲーム開始 */
    public void startGame() {
        if (box != null) {
            box.activate();
        }
        clearSequenceRunning = false;
        gameActive = true;
        gameStartTime = System.currentTimeMillis();
        startActionBar();
        startClearConditionWatcher();
    }
    /** ゲーム開始カウントダウン */
    public void startGameWithCountdown(int seconds) {
        if (gameActive) return;
        if (seconds <= 0) {
            startGame();
            effects.playGameStart();
            return;
        }
        stopActionBar();
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int timeLeft = seconds;
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    countdownTask.cancel();
                    countdownTask = null;
                    // ゲーム開始
                    startGame();
                    effects.playGameStart();
                    return;
                }
                // カウントダウン表示
                for (Player player : Bukkit.getOnlinePlayers()) {
                    effects.showActionBar("§eゲーム開始まで §c" + timeLeft + " §e秒");
                    playCountdownSound(player);
                }
                timeLeft--;
            }
        }, 0L, 20L);
    }
    /** 経過秒数取得 */
    public long getElapsedSeconds() {
        if (!gameActive) return 0;
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }
    /** ゲーム開始カウントダウン音 */
    private void playCountdownSound(Player player) {
        player.playSound(
                player.getLocation(),
                org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                0.6f,
                1.8f
        );
    }

    /** ゲーム停止 */
    public void stopGame() {
        if (box != null) {
            box.deactivate();
        }
        this.gameActive = false;
        this.gameStartTime = 0L;
        stopActionBar();
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        stopClearConditionWatcher();
        effects.playGameStop();
    }
    /** ゲームクリア判定 */
    private boolean isBoxFullyFilled() {
        if (!hasBox()) return false;

        JammingBox box = this.box;
        World world = box.getWorld();
        Location center = box.getCenter();
        int half = box.getHalf();

        int minX = center.getBlockX() - half + 1;
        int maxX = center.getBlockX() + half - 1;
        int minY = center.getBlockY() - half + 1;
        int maxY = center.getBlockY() + half - 1;
        int minZ = center.getBlockZ() - half + 1;
        int maxZ = center.getBlockZ() + half - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (world.getBlockAt(x, y, z).getType() == Material.AIR) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    /** ゲーム内監視 */
    private void startClearConditionWatcher() {
        if (clearCheckTask != null) return;

        clearCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameActive || !hasBox()) return;

            // ▼ クリアカウントダウン中
            if (clearSequenceRunning) {
                if (!isBoxFullyFilled()) {
                    cancelClearCountdown();
                }
                return;
            }

            // ▼ まだクリア演出に入っていない
            if (isBoxFullyFilled()) {
                onGameClear();
            }

        }, 0L, 10L);
    }
    /** クリアキャンセル */
    private void cancelClearCountdown() {
        if (clearCountdownTask != null) {
            clearCountdownTask.cancel();
            clearCountdownTask = null;
        }
        clearSequenceRunning = false;
        effects.playClearCanceled();
    }
    /** 監視停止 */
    private void stopClearConditionWatcher() {
        if (clearCheckTask != null) {
            clearCheckTask.cancel();
            clearCheckTask = null;
        }
    }
    // =========================================================
    // 壁・床構築
    // =========================================================
    private void buildWalls(JammingBox box, Material material) {
        Location center = box.getCenter();
        World world = box.getWorld();
        int half = box.getHalf();

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        int minX = cx - half;
        int maxX = cx + half;
        int minY = cy - half;
        int maxY = cy + half;
        int minZ = cz - half;
        int maxZ = cz + half;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    boolean isWall = x == minX || x == maxX || z == minZ || z == maxZ;
                    boolean isFloor = y == minY;
                    boolean isCeiling = y == maxY;

                    if (isCeiling) continue;

                    if (isWall || isFloor) {
                        Location loc = new Location(world, x, y, z);
                        world.getBlockAt(loc).setType(material);
                        placedBlocks.add(new BlockKey(loc));
                    }
                }
            }
        }
    }

    private void clearWalls(JammingBox box) {
        for (BlockKey key : placedBlocks) {
            World world = Bukkit.getWorld(key.worldId);
            if (world != null) {
                world.getBlockAt(key.x, key.y, key.z)
                        .setType(Material.AIR, false);
            }
        }
        placedBlocks.clear();
    }

    // =========================================================
    // 内部ブロック処理
    // =========================================================
    private void fillInside(JammingBox box) {
        for (Location loc : box.getInnerBlocks()) {
            Material mat = getReplaceBlockType(box, loc.getBlockY());
            loc.getBlock().setType(mat, false);
        }
    }
    private void onGameClear() {
        clearSequenceRunning = true;
        startClearCountdown(clearCountdownSeconds);
    }
    private void clearInside(JammingBox box) {
        for (Location loc : box.getInnerBlocks()) {
            loc.getBlock().setType(Material.AIR, false);
        }
    }

    // =========================================================
    // ActionBar / UI
    // =========================================================
    private void startActionBar() {
        if (actionBarTask != null) return;

        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameActive) return;

            long seconds = getElapsedSeconds();
            String time = formatTime(seconds);

            for (Player player : Bukkit.getOnlinePlayers()) {
                effects.showActionBar("§a⏱ 経過時間: §e" + time);
            }
        }, 0L, 20L); // 1秒ごと
    }
    private void stopActionBar() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }
    private String formatTime(long seconds) {
        long min = seconds / 60;
        long sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    /** ゲームクリアアクション */
    private void startClearCountdown(int seconds) {
        if (clearCountdownTask != null) {
            clearCountdownTask.cancel();
        }

        clearCountdownTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    clearCountdownTask.cancel();
                    clearCountdownTask = null;
                    onGameClearComplete();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    effects.showTitle(
                            "§aクリアまで",
                            "§e" + timeLeft + " §a秒",
                            0, 20, 0
                    );
                    effects.playSoundAll(
                            Sound.BLOCK_NOTE_BLOCK_PLING,
                            0.8f,
                            1.5f
                    );
                }
                timeLeft--;
            }
        }, 0L, 20L);
    }
    private void onGameClearComplete() {
        effects.playClear();
        clearInsideWithFireworks(box);
        clearSequenceRunning = false;
    }
    // =========================================================
    // 内部クラス
    // =========================================================
    /** 設置ブロック識別用キー（World + XYZ） */
    private static class BlockKey {
        private final UUID worldId;
        private final int x, y, z;

        BlockKey(Location loc) {
            this.worldId = loc.getWorld().getUID();
            this.x = loc.getBlockX();
            this.y = loc.getBlockY();
            this.z = loc.getBlockZ();
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BlockKey)) return false;
            BlockKey key = (BlockKey) o;
            return x == key.x && y == key.y && z == key.z && worldId.equals(key.worldId);
        }
        @Override
        public int hashCode() {
            return Objects.hash(worldId, x, y, z);
        }
    }
    // =========================================================
    // 演出関連
    // =========================================================
    /** ゲームクリア時の花火演出 */
    private void clearInsideWithFireworks(JammingBox box) {
        Location center = box.getCenter();
        World world = box.getWorld();
        int half = box.getHalf();

        int minX = center.getBlockX() - half + 1;
        int maxX = center.getBlockX() + half - 1;
        int minY = center.getBlockY() - half + 1;
        int maxY = center.getBlockY() + half - 1;
        int minZ = center.getBlockZ() - half + 1;
        int maxZ = center.getBlockZ() + half - 1;

        // ブロック削除
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }

        // 花火を複数ランダムに打ち上げ
        for (int i = 0; i < 50; i++) {
            Location fwLoc = getRandomInnerLocation()
                    .orElse(center)
                    .clone()
                    .add(0.5, 1.0, 0.5);

            Firework fw = world.spawn(fwLoc, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();

            meta.addEffect(
                    FireworkEffect.builder()
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .withColor(Color.LIME, Color.YELLOW, Color.AQUA)
                            .withFade(Color.WHITE)
                            .flicker(true)
                            .trail(true)
                            .build()
            );
            meta.setPower(1);
            fw.setFireworkMeta(meta);
        }
    }
    /** エンダードラゴン破壊演出 */
    public void resetByDragon(Player player) {
        effects.resetByDragon(player, this.box, this.plugin);
    }
}
