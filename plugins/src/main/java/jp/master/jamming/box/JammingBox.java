package jp.master.jamming.box;

import org.bukkit.Location;
import org.bukkit.World;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/*
 * ============================================
 * JammingBox
 * --------------------------------------------
 * 「箱そのもの」を表すクラス
 *
 * このクラスの責務：
 * ・箱の中心・サイズ・ワールド情報を保持する
 * ・指定した座標が箱の内側かどうか判定する
 * ・箱の内部座標を計算する
 * ・箱内部のランダム位置を提供する
 *
 * ※ ブロックの設置・削除は行わない
 *    → 実際の操作は JammingBoxManager が担当
 * ============================================
 */
public class JammingBox {

    /** 箱の中心位置 */
    private final Location center;
    /** 箱のサイズ（1辺の長さ） */
    private final int size;
    /** サイズの半分（中心から端までの距離） */
    private final int half;
    /** 箱が存在するワールド */
    private final World world;
    /** 箱がアクティブかどうか（ゲーム中かどうか） */
    private boolean active = false;
    /** ランダム生成用 */
    private final Random random = new Random();

    /*
     * ============================================
     * コンストラクタ
     * --------------------------------------------
     * 箱の中心位置とサイズを指定して生成する
     *
     * ・中心座標はブロック座標に揃える
     * ・world 情報もここで確定させる
     * ============================================
     */
    public JammingBox(Location center, int size) {
        this.center = center.getBlock().getLocation();
        this.size = size;
        this.half = size / 2;
        this.world = center.getWorld();
    }
    // =========================================================
    // State（状態管理）
    // =========================================================

    /** 箱がアクティブ状態かどうかを返す */
    public boolean isActive() {
        return active;
    }
    /** 箱をアクティブ状態にする */
    public void activate() {
        this.active = true;
    }
    /** 箱を非アクティブ状態にする */
    public void deactivate() {
        this.active = false;
    }

    // =========================================================
    // Getter（外部参照用）
    // =========================================================

    /** 箱の中心位置を取得する 外部から Location を変更されないよう clone を返す */
    public Location getCenter() {
        return center.clone();
    }
    /** 箱のサイズを取得する */
    public int getSize() {
        return size;
    }
    /** 箱の半分サイズ（中心から端まで）を取得する */
    public int getHalf() {
        return half;
    }
    /** 箱が存在するワールドを取得する */
    public World getWorld() {
        return world;
    }

    // =========================================================
    // 判定ロジック
    // =========================================================

    /**
     * 指定した Location が箱の内側にあるか判定する
     * ・外壁を含めた範囲を「箱の内側」とする
     * ・異なるワールドの場合は常に false
     */
    public boolean isInside(Location loc) {
        if (loc == null || loc.getWorld() != world) return false;

        // 比較をブロック単位に揃える
        Location b = loc.getBlock().getLocation();

        int x = b.getBlockX();
        int y = b.getBlockY();
        int z = b.getBlockZ();

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // 中心から half 以内であれば箱の中
        return x >= cx - half && x <= cx + half
                && y >= cy - half && y <= cy + half
                && z >= cz - half && z <= cz + half;
    }

    // =========================================================
    // 内部座標取得
    // =========================================================

    /** 箱の内部（外壁を除いた範囲）からランダムなブロック座標を1つ返す */
    public Location getRandomInnerLocation() {
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // 外壁を除くため +1 / -1
        int minX = cx - half + 1;
        int maxX = cx + half - 1;
        int minY = cy - half + 1;
        int maxY = cy + half - 1;
        int minZ = cz - half + 1;
        int maxZ = cz + half - 1;

        int x = randomBetween(minX, maxX);
        int y = randomBetween(minY, maxY);
        int z = randomBetween(minZ, maxZ);

        return new Location(world, x, y, z);
    }

    /** 箱の内部（外壁を除いた範囲）のすべてのブロック座標をリストとして返す */
    public List<Location> getInnerBlocks() {
        List<Location> blocks = new ArrayList<>();

        for (int x = center.getBlockX() - half + 1; x <= center.getBlockX() + half - 1; x++) {
            for (int y = center.getBlockY() - half + 1; y <= center.getBlockY() + half - 1; y++) {
                for (int z = center.getBlockZ() - half + 1; z <= center.getBlockZ() + half - 1; z++) {
                    blocks.add(new Location(world, x, y, z));
                }
            }
        }
        return blocks;
    }
    // =========================================================
    // Utility
    // =========================================================

    /** min〜max の範囲でランダムな整数を返す */
    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
