package jp.master.jamming.listener;

import jp.master.jamming.box.JammingBox;
import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import java.util.Optional;

// =========================================================
// JammingBox 内でブロックが置かれた際に
// 高さなどの条件に応じてブロックを自動変換するリスナー
// =========================================================
public class JammingBoxPlaceListener implements Listener {

    // JammingBox 全体の管理クラス
    private final JammingBoxManager manager;

    // コンストラクタ
    public JammingBoxPlaceListener(JammingBoxManager manager) {
        this.manager = manager;
    }

    /**
     * ブロック設置時に呼ばれるイベント
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // 自動置換機能が無効なら何もしない
        if (!manager.isReplaceEnabled()) return;

        // 現在の JammingBox を取得
        JammingBox box = manager.getBox();
        if (box == null) return;

        // 設置されたブロックが箱の中でなければ無視
        if (!box.isInside(event.getBlockPlaced().getLocation())) return;

        // 設置されたブロックのY座標
        int y = event.getBlockPlaced().getY();
        // 箱の内部Y範囲（外壁を除外）
        int minY = box.getCenter().getBlockY() - box.getHalf() + 1;
        int maxY = box.getCenter().getBlockY() + box.getHalf() - 1;

        // 内部Y範囲外なら変換しない
        if (y < minY || y > maxY) return;

        // 設置された高さに応じて置換後のブロックタイプを取得
        Material converted = manager.getReplaceBlockType(
                box,
                event.getBlockPlaced().getY()
        );

        // 実際にブロックを置換する
        event.getBlockPlaced().setType(converted);
    }
}
