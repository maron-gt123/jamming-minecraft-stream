package jp.master.jamming.listener;

import jp.master.jamming.box.JammingBox;
import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.game.JammingGameManager;
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
    private final JammingGameManager gameManager;

    // コンストラクタ
    public JammingBoxPlaceListener(
            JammingBoxManager manager,
            JammingGameManager gameManager
    ) {
        this.manager = manager;
        this.gameManager = gameManager;
    }

    /**
     * ブロック設置時に呼ばれるイベント
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (!manager.isReplaceEnabled()) return;
        if (!gameManager.isGameActive()) return;

        JammingBox box = manager.getBox();
        if (box == null) return;

        // 箱内判定
        if (!box.isInside(event.getBlockPlaced().getLocation())) return;

        int y = event.getBlockPlaced().getY();

        // ★ 完全に min/max ベースに統一
        int minY = box.getMinY() + 1;
        int maxY = box.getMaxY() - 1;

        if (y < minY || y > maxY) return;

        Material converted = manager.getReplaceBlockType(box, y);

        event.getBlockPlaced().setType(converted);
    }
}
