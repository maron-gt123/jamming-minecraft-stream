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

/*
 * ============================================
 * JammingBoxManager
 * --------------------------------------------
 * JammingBox（ジャミング用の箱構造）を
 * ・生成
 * ・削除
 * ・内部ブロックの制御
 * ・壁ブロックの管理
 * をまとめて管理するクラス
 *
 * 「今、箱が存在するか？」
 * 「どこに置いたブロックか？」
 * といった状態管理もここで行う
 * ============================================
 */

public class JammingBoxManager {

    /** 現在存在している JammingBox（未生成の場合は null） */
    private JammingBox box = null;

    // 最新のcreate基準
    private Location basePos1;
    private Location basePos2;
    // reset用
    private Location resetPos1;
    private Location resetPos2;

    private Material baseMaterial;

    /** このマネージャーが「自分で設置した」ブロック一覧 → 後で一括削除・保護判定に使う */
    private final Set<BlockKey> placedBlocks = new HashSet<>();

    /** 内部ブロック置換機能が有効かどうか */
    private boolean replaceEnabled;
    /** 内部下段・中段・上段に使用する置換ブロック */
    private Material replaceBottom;
    private Material replaceMiddle;
    private Material replaceTop;
    /** プラグイン本体（スケジューラ等で使用） */
    private final JavaPlugin plugin;

    /*
     * ============================================
     * コンストラクタ
     * --------------------------------------------
     * プラグイン起動時に呼ばれる
     * 設定ファイルから置換設定を読み込む
     * ============================================
     */
    public JammingBoxManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.replaceEnabled = ConfigManager.getReplaceEnabledDefault();
        loadReplaceConfig();
    }

    // =========================================================
    // Box basic（箱そのものの生成・状態管理）
    // =========================================================

    /** 箱を新規作成する。すでに箱が存在する場合は何もしない */
    public void createBox(Location pos1, Location pos2, Material material) {
        if (pos1.getWorld() != pos2.getWorld()) {
            throw new IllegalArgumentException("pos1 and pos2 must be in same world");
        }
        if (box != null) return;

        // 常に更新（今の箱の基準）
        this.basePos1 = pos1.clone();
        this.basePos2 = pos2.clone();
        // ★ reset用は「createコマンドのときだけ更新」
        this.resetPos1 = pos1.clone();
        this.resetPos2 = pos2.clone();

        this.baseMaterial = material;

        box = new JammingBox(pos1, pos2);
        buildWalls(box, material);
    }
    /** 箱の現在サイズ取得用 */
    public int getCurrentSizeXZ() {
        return box != null ? box.getSizeXZ() : 0;
    }

    public int getCurrentHeight() {
        return box != null ? box.getHeight() : 0;
    }

    /** 箱を完全に削除する。壁・床ブロックもすべて消去する */
    public void removeBox() {
        if (box == null) return;
        clearWalls();
        box = null;
    }

    /** 箱が存在しているかどうか */
    public boolean hasBox() {
        return box != null;
    }

    /**
     * アクティブな箱を Optional で取得する
     * ・箱が存在する
     * ・かつ active 状態である
     */
    public Optional<JammingBox> getActiveBox() {
        if (box != null && box.isActive()) return Optional.of(box);
        return Optional.empty();
    }

    /** 現在の箱をそのまま返す ※ null の可能性あり */
    public JammingBox getBox() {
        return box;
    }

    /** 指定した Location がこのマネージャーが設置したブロックか判定する */
    public boolean isProtected(Location loc) {
        return loc != null && placedBlocks.contains(new BlockKey(loc));
    }

    /** 箱の内部からランダムな座標を取得する（箱がアクティブな場合のみ）*/
    public Optional<Location> getRandomInnerLocation() {
        if (box == null) return Optional.empty();
        return Optional.of(box.getRandomInnerLocation());
    }

    // =========================================================
    // Replace（内部ブロック置換設定）
    // =========================================================

    /** 設定ファイルから置換用ブロックを読み込む。読み込み失敗時はデフォルト値を使用する */
    private void loadReplaceConfig() {
        replaceBottom = Material.matchMaterial(ConfigManager.getReplaceBottom());
        replaceMiddle = Material.matchMaterial(ConfigManager.getReplaceMiddle());
        replaceTop = Material.matchMaterial(ConfigManager.getReplaceTop());

        if (replaceBottom == null) replaceBottom = Material.IRON_BLOCK;
        if (replaceMiddle == null) replaceMiddle = Material.GOLD_BLOCK;
        if (replaceTop == null) replaceTop = Material.DIAMOND_BLOCK;
    }

    /** 箱の高さ（Y座標）に応じて 使用する置換ブロックを決定する */
    public Material getReplaceBlockType(JammingBox box, int y) {
        int minY = box.getMinY() + 1;
        int maxY = box.getMaxY() - 1;
        int height = maxY - minY + 1;
        int relativeY = y - minY;
        int layerHeight = height / 3;

        if (relativeY >= 2 * layerHeight) return replaceTop;
        else if (relativeY >= layerHeight) return replaceMiddle;
        else return replaceBottom;
    }

    /** 内部ブロック置換機能が有効かどうか */
    public boolean isReplaceEnabled() {
        return replaceEnabled;
    }

    /** 内部ブロック置換機能の ON / OFF を切り替える */
    public void setReplaceEnabled(boolean enabled) {
        this.replaceEnabled = enabled;
    }

    // =========================================================
    // Inner blocks（箱の内部ブロック操作）
    // =========================================================

    /** 置換設定に関係なく、内部を強制的に埋める */
    public void fillInsideForce() {
        if (!hasBox()) return;
        fillInside(box);
    }

    /** 置換機能が有効な場合のみ内部を埋める */
    public void fillInsideWithReplace() {
        if (!hasBox() || !replaceEnabled) return;
        fillInside(box);
    }

    /** 箱の内部をすべて空気ブロックにする */
    public void clearInside() {
        if (!hasBox()) return;
        clearInside(box);
    }

    /** 内部ブロックを置換用ブロックで埋める */
    private void fillInside(JammingBox box) {
        for (Location loc : box.getInnerBlocks()) {
            loc.getBlock().setType(getReplaceBlockType(box, loc.getBlockY()), false);
        }
    }

    /** 内部ブロックをすべて削除する */
    private void clearInside(JammingBox box) {
        for (Location loc : box.getInnerBlocks()) {
            loc.getBlock().setType(Material.AIR, false);
        }
    }

    /** AIR の最下部を起点に指定レベル分埋める */
    public void fillColumnsFromAir(int levels, Material material) {
        if (!hasBox()) return;
        JammingBox b = box;

        World w = b.getWorld();

        int minX = b.getMinX() + 1;
        int maxX = b.getMaxX() - 1;
        int minZ = b.getMinZ() + 1;
        int maxZ = b.getMaxZ() - 1;

        int floorY = b.getMinY();
        int maxY = b.getMaxY();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                int startY = -1;

                for (int y = floorY + 1; y <= maxY - 1; y++) {
                    if (w.getBlockAt(x, y, z).getType() == Material.AIR) {
                        startY = y;
                        break;
                    }
                }

                if (startY == -1) continue;

                for (int y = startY; y < startY + levels; y++) {
                    if (y > maxY - 1) break;

                    Material blockType = isReplaceEnabled()
                            ? getReplaceBlockType(b, y)
                            : material;

                    w.getBlockAt(x, y, z).setType(blockType, false);
                }
            }
        }
    }
    // =========================================================
    // Walls（壁・床ブロック管理）
    // =========================================================

    /** 箱の壁と床を構築する+天井は作らない+設置したブロックは placedBlocks に記録する */
    private void buildWalls(JammingBox box, Material material) {

        for (int x = box.getMinX(); x <= box.getMaxX(); x++) {
            for (int y = box.getMinY(); y <= box.getMaxY(); y++) {
                for (int z = box.getMinZ(); z <= box.getMaxZ(); z++) {

                    boolean wall = x == box.getMinX() || x == box.getMaxX()
                            || z == box.getMinZ() || z == box.getMaxZ();

                    boolean floor = y == box.getMinY();
                    boolean ceiling = y == box.getMaxY();

                    if (ceiling) continue;

                    if (wall || floor) {
                        Location loc = new Location(box.getWorld(), x, y, z);
                        loc.getBlock().setType(material);
                        placedBlocks.add(new BlockKey(loc));
                    }
                }
            }
        }
    }

    /** 記録してある壁・床ブロックをすべて削除する */
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
    // BlockKey（ブロック識別用キー）
    // =========================================================

    /** World + 座標（x, y, z）でブロックを一意に識別するためのキー */
    private static class BlockKey {
        /** ワールドID */
        private final UUID worldId;
        /** ブロック座標 */
        private final int x, y, z;

        /** Location から BlockKey を生成する */
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

    /** heightup */
    public void Heightup(int delta) {
        if (box == null) return;

        int current = box.getHeight();
        int newHeight = current + delta;

        // =========================
        // バリデーション
        // =========================
        if (newHeight < 3) {
            return;
        }

        Location pos1 = new Location(box.getWorld(), box.getMinX(), box.getMinY(), box.getMinZ());
        Location pos2 = new Location(box.getWorld(), box.getMaxX(), box.getMaxY() + delta, box.getMaxZ());

        removeBox();
        this.basePos1 = pos1.clone();
        this.basePos2 = pos2.clone();

        box = new JammingBox(pos1, pos2);
        buildWalls(box, baseMaterial);
    }
    /** heightdown */
    public void Heightdown(int delta) {
        if (box == null) return;

        if (delta < 0) delta = -delta;

        int current = box.getHeight();
        int newHeight = current - delta;

        // =========================
        // バリデーション
        // =========================
        if (newHeight < 3) {
            return;
        }

        Location clearPos1 = new Location(
                box.getWorld(),
                box.getMinX(),
                box.getMaxY() - delta + 1,
                box.getMinZ()
        );
        Location clearPos2 = new Location(
                box.getWorld(),
                box.getMaxX(),
                box.getMaxY(),
                box.getMaxZ()
        );
        clearRegion(clearPos1, clearPos2);

        Location pos1 = new Location(
                box.getWorld(),
                box.getMinX(),
                box.getMinY(),
                box.getMinZ()
        );

        Location pos2 = new Location(
                box.getWorld(),
                box.getMaxX(),
                box.getMaxY() - delta,
                box.getMaxZ()
        );

        removeBox();

        this.basePos1 = pos1.clone();
        this.basePos2 = pos2.clone();

        box = new JammingBox(pos1, pos2);
        buildWalls(box, baseMaterial);
    }

    /** SizeupXZ */
    public void SizeupXZ(int delta) {
        if (box == null) return;

        int current = box.getSizeXZ();
        int newSize = current + delta * 2;

        // =========================
        // バリデーション
        // =========================
        if (newSize < 5) {
            return; // またはメッセージ送信
        }

        Location pos1 = new Location(
                box.getWorld(),
                box.getMinX() - delta,
                box.getMinY(),
                box.getMinZ() - delta
        );

        Location pos2 = new Location(
                box.getWorld(),
                box.getMaxX() + delta,
                box.getMaxY(),
                box.getMaxZ() + delta
        );

        removeBox();
        this.basePos1 = pos1.clone();
        this.basePos2 = pos2.clone();

        box = new JammingBox(pos1, pos2);
        buildWalls(box, baseMaterial);
    }
    /** SizedownXZ */
    public void SizedownXZ(int delta) {
        if (box == null) return;

        if (delta < 0) delta = -delta;

        int current = box.getSizeXZ();
        int newSize = current - delta * 2;

        // =========================
        // バリデーション
        // =========================
        if (newSize < 5) {
            return;
        }

        clearRegion(
                new Location(box.getWorld(), box.getMinX(), box.getMinY(), box.getMinZ()),
                new Location(box.getWorld(), box.getMinX() + delta - 1, box.getMaxY(), box.getMaxZ())
        );
        clearRegion(
                new Location(box.getWorld(), box.getMaxX() - delta + 1, box.getMinY(), box.getMinZ()),
                new Location(box.getWorld(), box.getMaxX(), box.getMaxY(), box.getMaxZ())
        );
        clearRegion(
                new Location(box.getWorld(), box.getMinX(), box.getMinY(), box.getMinZ()),
                new Location(box.getWorld(), box.getMaxX(), box.getMaxY(), box.getMinZ() + delta - 1)
        );
        clearRegion(
                new Location(box.getWorld(), box.getMinX(), box.getMinY(), box.getMaxZ() - delta + 1),
                new Location(box.getWorld(), box.getMaxX(), box.getMaxY(), box.getMaxZ())
        );

        Location pos1 = new Location(
                box.getWorld(),
                box.getMinX() + delta,
                box.getMinY(),
                box.getMinZ() + delta
        );

        Location pos2 = new Location(
                box.getWorld(),
                box.getMaxX() - delta,
                box.getMaxY(),
                box.getMaxZ() - delta
        );

        removeBox();

        this.basePos1 = pos1.clone();
        this.basePos2 = pos2.clone();

        box = new JammingBox(pos1, pos2);
        buildWalls(box, baseMaterial);
    }

    /** down reset */
    private void clearRegion(Location pos1, Location pos2) {
        World world = pos1.getWorld();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, false);
                }
            }
        }
    }

    /** SizeXYZ RESET */
    public void resetSize() {
        if (box == null) return;
        if (resetPos1 == null || resetPos2 == null) return;

        removeBox();

        // ★ reset用に戻す
        box = new JammingBox(resetPos1, resetPos2);
        buildWalls(box, baseMaterial);

        this.basePos1 = resetPos1.clone();
        this.basePos2 = resetPos2.clone();
    }
}
