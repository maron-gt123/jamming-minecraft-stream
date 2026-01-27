package jp.master.jamming.listener;

import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.game.JammingGameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

// =========================================================
// JammingBox に対するあらゆる破壊行為を防ぐための保護リスナー
//   * 対象：
//     - プレイヤーによるブロック破壊
//     - TNT / クリーパー等の爆発
//     - エンダードラゴン・ウィザー等の Mob によるブロック干渉
// =========================================================
public class JammingBoxProtectListener implements Listener {

    /** JammingBox の状態管理・保護判定を行うマネージャ */
    private final JammingBoxManager manager;
    private final JammingGameManager gameManager;

    public JammingBoxProtectListener(
            JammingBoxManager manager,
            JammingGameManager gameManager
    ) {
        this.manager = manager;
        this.gameManager = gameManager;
    }

    // プレイヤー破壊
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!gameManager.isGameActive()) return;
        if (!manager.hasBox()) return;
        if (manager.isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
    // TNT・クリーパー等の爆発
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!gameManager.isGameActive()) return;
        if (!manager.hasBox()) return;
        event.blockList().removeIf(block ->
                manager.isProtected(block.getLocation())
        );
    }
    // エンダードラゴン等のブロック干渉
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!gameManager.isGameActive()) return;
        if (!manager.hasBox()) return;

        if (manager.isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}