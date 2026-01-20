package jp.master.jamming.box;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;

public class JammingBoxManager {

    private JammingBox box = null;
    private final Set<BlockKey> placedBlocks = new HashSet<>();
    private boolean autoConvertEnabled = false;
    private Material autoBottom;
    private Material autoMiddle;
    private Material autoTop;
    private boolean gameActive = false;
    private long gameStartTime = 0L;
    private BukkitTask actionBarTask;
    private final JavaPlugin plugin;
    private BukkitTask countdownTask;

    // =========================================================
    // config load
    // =========================================================
    public JammingBoxManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadAutoConvertConfig();
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
        stopGame();
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
    public void fillInsideWithAutoConvert() {
        if (!hasBox()) return;
        if (box != null) fillInside(box);
    }
    /** 内部ブロック全消去 */
    public void clearInside() {
        if (!hasBox()) return;
        if (box != null) clearInside(box);
    }
    // =========================================================
    // AutoConvert
    // =========================================================
    public boolean isAutoConvertEnabled() {
        return autoConvertEnabled;
    }
    public void setAutoConvertEnabled(boolean enabled) {
        this.autoConvertEnabled = enabled;
    }
    private void loadAutoConvertConfig() {
        autoBottom = loadMaterial("jammingbox.autocvt.bottom", Material.IRON_BLOCK);
        autoMiddle = loadMaterial("jammingbox.autocvt.middle", Material.GOLD_BLOCK);
        autoTop = loadMaterial("jammingbox.autocvt.top", Material.DIAMOND_BLOCK);
    }
    private Material loadMaterial(String path, Material def) {
        String name = plugin.getConfig().getString(path);
        if (name == null) return def;
        Material mat = Material.matchMaterial(name.toUpperCase());
        return mat != null ? mat : def;
    }
    /** 高さに応じたブロック種別取得 */
    public Material getAutoBlockType(JammingBox box, int y) {
        int minY = box.getCenter().getBlockY() - box.getHalf() + 1;
        int maxY = box.getCenter().getBlockY() + box.getHalf();

        int height = maxY - minY;
        int relativeY = y - minY;
        int layerHeight = height / 3;

        if (relativeY >= 2 * layerHeight) return autoTop;
        else if (relativeY >= layerHeight) return autoMiddle;
        else return autoBottom;
    }

    // =========================================================
    // ゲーム制御
    // =========================================================
    public boolean isGameActive() {
        return gameActive;
    }
    /** ゲーム開始 */
    public void startGame() {
        if (box != null) {
            box.activate();
        }
        gameActive = true;
        gameStartTime = System.currentTimeMillis();
        startActionBar();
    }
    /** ゲーム開始カウントダウン */
    public void startGameWithCountdown(int seconds) {
        if (gameActive) return;
        if (seconds <= 0) {
            startGame();
            playGameStartSound();
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
                    playGameStartSound();
                    return;
                }
                // カウントダウン表示
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new TextComponent("§eゲーム開始まで §c" + timeLeft + " §e秒")
                    );
                    playCountdownSound(player);
                }
                timeLeft--;
            }
        }, 0L, 20L);
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
        playGameStopSound();
    }
    /** 経過秒数取得 */
    public long getElapsedSeconds() {
        if (!gameActive) return 0;
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }
    /** ゲームスタート音 */
    private void playGameStartSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE,
                    0.9f,
                    1.2f
            );
        }
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
    /** ゲーム終了音 */
    private void playGameStopSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE,
                    0.8f,
                    0.8f
            );
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
        Location center = box.getCenter();
        World world = box.getWorld();
        int half = box.getHalf();

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        int minX = cx - half + 1;
        int maxX = cx + half - 1;
        int minY = cy - half + 1;
        int maxY = cy + half - 1;
        int minZ = cz - half + 1;
        int maxZ = cz + half - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Material mat = getAutoBlockType(box, y);
                    world.getBlockAt(x, y, z).setType(mat, false);
                }
            }
        }
    }
    private void clearInside(JammingBox box) {
        Location center = box.getCenter();
        World world = box.getWorld();
        int half = box.getHalf();

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        int minX = cx - half + 1;
        int maxX = cx + half - 1;
        int minY = cy - half + 1;
        int maxY = cy + half - 1;
        int minZ = cz - half + 1;
        int maxZ = cz + half - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
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
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent("§a⏱ 経過時間: §e" + time)
                );
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
}
