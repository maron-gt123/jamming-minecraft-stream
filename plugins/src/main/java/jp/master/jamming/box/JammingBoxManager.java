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
    public boolean isAutoConvertEnabled() {
        return autoConvertEnabled;
    }
    public void setAutoConvertEnabled(boolean enabled) {
        this.autoConvertEnabled = enabled;
    }
    private boolean gameActive = false;
    private long gameStartTime = 0L;
    private BukkitTask actionBarTask;
    private final JavaPlugin plugin;
    private BukkitTask countdownTask;

    public void removeBox() {
        if (box == null) return;
        clearWalls(box);
        box = null;
        stopGame();
    }

    public boolean hasBox() {
        return box != null;
    }

    public Optional<JammingBox> getActiveBox() {
        if (box != null && box.isActive()) return Optional.of(box);
        return Optional.empty();
    }

    public Optional<Location> getRandomInnerLocation() {
        Optional<JammingBox> box = getActiveBox();
        return box.map(JammingBox::getRandomInnerLocation);
    }

    public boolean isProtected(Location loc) {
        if (loc == null) return false;
        return placedBlocks.contains(new BlockKey(loc));
    }

    public void createBox(Location center, int size, Material material) {
        if (box != null) {
            return;
        }
        box = new JammingBox(center, size);
        buildWalls(box, material);
    }

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

    public void fillInsideWithAutoConvert() {
        if (!autoConvertEnabled) return;
        if (!hasBox()) return;
        if (box != null) fillInside(box);
    }

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

    public Material getAutoBlockType(JammingBox box, int y) {
        int minY = box.getCenter().getBlockY() - box.getHalf() + 1;
        int maxY = box.getCenter().getBlockY() + box.getHalf();

        int height = maxY - minY;
        int relativeY = y - minY;
        int layerHeight = height / 3;

        if (relativeY >= 2 * layerHeight) return Material.DIAMOND_BLOCK;
        else if (relativeY >= layerHeight) return Material.GOLD_BLOCK;
        else return Material.IRON_BLOCK;
    }

    public void clearInside() {
        if (!hasBox()) return;
        if (box != null) clearInside(box);
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

    public boolean isGameActive() {
        return gameActive;
    }

    public void startGame() {
        if (box != null) {
            box.activate();
        }
        gameActive = true;
        gameStartTime = System.currentTimeMillis();
        startActionBar();
    }
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
    public long getElapsedSeconds() {
        if (!gameActive) return 0;
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }
    public JammingBoxManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void startActionBar() {
        // 二重起動防止
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
    private void playCountdownSound(Player player) {
        player.playSound(
                player.getLocation(),
                org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                0.6f,
                1.8f
        );
    }
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
}
