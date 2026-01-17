package jp.master.jamming.box;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

public class JammingBoxManager {

    private final List<JammingBox> boxes = new ArrayList<>();
    private final Set<BlockKey> placedBlocks = new HashSet<>();

    private boolean autoConvertEnabled = false;

    public boolean isAutoConvertEnabled() {
        return autoConvertEnabled;
    }

    public void setAutoConvertEnabled(boolean enabled) {
        this.autoConvertEnabled = enabled;
    }

    public void addBox(JammingBox box) {
        boxes.add(box);
    }

    public void removeBox(JammingBox box) {
        if (box == null) return;
        clearWalls(box);
        boxes.remove(box);
    }

    public List<JammingBox> getBoxes() {
        return Collections.unmodifiableList(boxes);
    }

    public boolean hasBox() {
        return !boxes.isEmpty();
    }

    public Optional<JammingBox> getActiveBox() {
        return boxes.stream().filter(JammingBox::isActive).findFirst();
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
        JammingBox box = new JammingBox(center, size);
        addBox(box);
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
        placedBlocks.clear();
    }

    public void fillInsideWithAutoConvert() {
        if (!autoConvertEnabled) return;
        if (!hasBox()) return;

        for (JammingBox box : boxes) {
            fillInside(box);
        }
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

    private Material getAutoBlockType(JammingBox box, int y) {
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

        for (JammingBox box : boxes) {
            clearInside(box);
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
