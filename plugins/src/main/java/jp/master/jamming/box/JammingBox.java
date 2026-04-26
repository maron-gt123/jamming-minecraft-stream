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

    /** 箱の座標 */
    private final int minX, maxX;
    private final int minY, maxY;
    private final int minZ, maxZ;

    /** 箱が存在するワールド */
    private final World world;
    /** 箱がアクティブかどうか（ゲーム中かどうか） */
    private boolean active = false;
    /** ランダム生成用 */
    private static final Random random = new Random();

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
    public JammingBox(Location pos1, Location pos2) {
        this.world = pos1.getWorld();

        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());

        this.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());

        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        if (maxX - minX < 2 || maxY - minY < 2 || maxZ - minZ < 2) {
            throw new IllegalArgumentException("Box must be at least 3x3x3");
        }
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

    public int getMinX() { return minX; }
    public int getMaxX() { return maxX; }

    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }

    public int getMinZ() { return minZ; }
    public int getMaxZ() { return maxZ; }


    /** 箱の各種サイズを取得する */
    public int getSizeXZ() {
        return maxX - minX + 1;
    }

    public int getHeight() {
        return maxY - minY + 1;
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
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= getMinX() && x <= getMaxX()
                && y >= getMinY() && y <= getMaxY()
                && z >= getMinZ() && z <= getMaxZ();
    }

    // =========================================================
    // 内部座標取得
    // =========================================================

    /** 箱の内部（外壁を除いた範囲）からランダムなブロック座標を1つ返す */
    public Location getRandomInnerLocation() {
        int x = randomBetween(minX + 1, maxX - 1);
        int y = randomBetween(minY + 1, maxY - 1);
        int z = randomBetween(minZ + 1, maxZ - 1);

        return new Location(world, x, y, z);
    }

    /** 箱の内部（外壁を除いた範囲）のすべてのブロック座標をリストとして返す */
    public List<Location> getInnerBlocks() {
        List<Location> blocks = new ArrayList<>();

        for (int x = minX + 1; x <= maxX - 1; x++) {
            for (int y = minY + 1; y <= maxY - 1; y++) {
                for (int z = minZ + 1; z <= maxZ - 1; z++) {
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
