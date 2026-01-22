package jp.master.jamming.box;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Random;

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

    /** コンストラクタ */
    public JammingBox(Location center, int size) {
        this.center = center.getBlock().getLocation();
        this.size = size;
        this.half = size / 2;
        this.world = center.getWorld();
    }
    /** 箱がアクティブか確認 */
    public boolean isActive() {
        return active;
    }
    /** 箱をアクティブにする */
    public void activate() {
        this.active = true;
    }
    /** 箱を非アクティブにする */
    public void deactivate() {
        this.active = false;
    }
    /** 中心位置を取得（外部で変更されないようcloneして返す） */
    public Location getCenter() {
        return center.clone();
    }
    /** 箱のサイズを取得 */
    public int getSize() {
        return size;
    }
    /** 箱の半分サイズを取得 */
    public int getHalf() {
        return half;
    }
    /** 箱のワールドを取得 */
    public World getWorld() {
        return world;
    }
    /** 指定した座標が箱の内側にあるか判定 */
    public boolean isInside(Location loc) {
        if (loc == null || loc.getWorld() != world) return false;

        // 比較をブロック単位に揃えるためブロック位置を取得
        Location b = loc.getBlock().getLocation();

        int x = b.getBlockX();
        int y = b.getBlockY();
        int z = b.getBlockZ();

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // 中心から半分以内なら箱の中
        return x >= cx - half && x <= cx + half
                && y >= cy - half && y <= cy + half
                && z >= cz - half && z <= cz + half;
    }
    /** 箱の内部（外壁を除いた範囲）からランダムな位置を返す */
    public Location getRandomInnerLocation() {
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // 内部範囲を計算（外壁を除くため +1 / -1）
        int minX = cx - half + 1;
        int maxX = cx + half - 1;
        int minY = cy - half + 1;
        int maxY = cy + half - 1;
        int minZ = cz - half + 1;
        int maxZ = cz + half - 1;

        // その範囲内でランダムな座標を生成
        int x = randomBetween(minX, maxX);
        int y = randomBetween(minY, maxY);
        int z = randomBetween(minZ, maxZ);

        // ランダムな位置を返す
        return new Location(world, x, y, z);
    }

    /** min〜maxの範囲でランダムな整数を返す */
    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
