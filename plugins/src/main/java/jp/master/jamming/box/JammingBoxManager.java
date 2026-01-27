package jp.master.jamming.box;

import jp.master.jamming.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;

public class JammingBoxManager {

    private JammingBox box = null;
    private final Set<BlockKey> placedBlocks = new HashSet<>();

    private boolean replaceEnabled;
    private Material replaceBottom;
    private Material replaceMiddle;
    private Material replaceTop;
    private final JavaPlugin plugin;

    public JammingBoxManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.replaceEnabled = ConfigManager.getReplaceEnabledDefault();
        loadReplaceConfig();
    }

    // =========================================================
    // Box basic
    // =========================================================
    public void createBox(Location center, int size, Material material) {
        if (box != null) return;
        box = new JammingBox(center, size);
        buildWalls(box, material);
    }

    public void removeBox() {
        if (box == null) return;
        clearWalls();
        box = null;
    }

    public boolean hasBox() {
        return box != null;
    }

    public Optional<JammingBox> getActiveBox() {
        if (box != null && box.isActive()) return Optional.of(box);
        return Optional.empty();
    }

    public JammingBox getBox() {
        return box;
    }

    public boolean isProtected(Location loc) {
        return loc != null && placedBlocks.contains(new BlockKey(loc));
    }

    public Optional<Location> getRandomInnerLocation() {
        return getActiveBox().map(JammingBox::getRandomInnerLocation);
    }

    // =========================================================
    // Replace
    // =========================================================
    private void loadReplaceConfig() {
        replaceBottom = Material.matchMaterial(ConfigManager.getReplaceBottom());
        replaceMiddle = Material.matchMaterial(ConfigManager.getReplaceMiddle());
        replaceTop = Material.matchMaterial(ConfigManager.getReplaceTop());

        if (replaceBottom == null) replaceBottom = Material.IRON_BLOCK;
        if (replaceMiddle == null) replaceMiddle = Material.GOLD_BLOCK;
        if (replaceTop == null) replaceTop = Material.DIAMOND_BLOCK;
    }

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
    // Inner blocks
    // =========================================================
    public void fillInsideForce() {
        if (!hasBox()) return;
        fillInside(box);
    }

    public void fillInsideWithReplace() {
        if (!hasBox() || !replaceEnabled) return;
        fillInside(box);
    }

    public void clearInside() {
        if (!hasBox()) return;
        clearInside(box);
    }

    private void fillInside(JammingBox box) {
        for (Location loc : box.getInnerBlocks()) {
            loc.getBlock().setType(getReplaceBlockType(box, loc.getBlockY()), false);
        }
    }

    private void clearInside(JammingBox box) {
        for (Location loc : box.getInnerBlocks()) {
            loc.getBlock().setType(Material.AIR, false);
        }
    }

    // =========================================================
    // Walls
    // =========================================================
    private void buildWalls(JammingBox box, Material material) {
        Location c = box.getCenter();
        World w = box.getWorld();
        int h = box.getHalf();

        for (int x = c.getBlockX() - h; x <= c.getBlockX() + h; x++) {
            for (int y = c.getBlockY() - h; y <= c.getBlockY() + h; y++) {
                for (int z = c.getBlockZ() - h; z <= c.getBlockZ() + h; z++) {
                    boolean wall = x == c.getBlockX() - h || x == c.getBlockX() + h
                            || z == c.getBlockZ() - h || z == c.getBlockZ() + h;
                    boolean floor = y == c.getBlockY() - h;
                    boolean ceiling = y == c.getBlockY() + h;

                    if (ceiling) continue;
                    if (wall || floor) {
                        Location loc = new Location(w, x, y, z);
                        w.getBlockAt(loc).setType(material);
                        placedBlocks.add(new BlockKey(loc));
                    }
                }
            }
        }
    }

    private void clearWalls() {
        for (BlockKey key : placedBlocks) {
            World world = org.bukkit.Bukkit.getWorld(key.worldId);
            if (world != null) {
                world.getBlockAt(key.x, key.y, key.z).setType(Material.AIR, false);
            }
        }
        placedBlocks.clear();
    }

    // =========================================================
    // BlockKey
    // =========================================================
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
            BlockKey k = (BlockKey) o;
            return x == k.x && y == k.y && z == k.z && worldId.equals(k.worldId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(worldId, x, y, z);
        }
    }
}
