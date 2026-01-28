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
    public void createBox(Location center, int size, Material material) {
        if (box != null) return;
        box = new JammingBox(center, size);
        buildWalls(box, material);
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
        return getActiveBox().map(JammingBox::getRandomInnerLocation);
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
        int minY = box.getCenter().getBlockY() - box.getHalf() + 1;
        int maxY = box.getCenter().getBlockY() + box.getHalf();

        int height = maxY - minY;
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

    // =========================================================
    // Walls（壁・床ブロック管理）
    // =========================================================

    /** 箱の壁と床を構築する+天井は作らない+設置したブロックは placedBlocks に記録する */
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
}
