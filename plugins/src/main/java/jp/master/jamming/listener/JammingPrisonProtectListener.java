package jp.master.jamming.listener;

import jp.master.jamming.prison.JammingPrisonManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class JammingPrisonProtectListener implements Listener {

    private final JammingPrisonManager prisonManager;

    public JammingPrisonProtectListener(JammingPrisonManager prisonManager) {
        this.prisonManager = prisonManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (prisonManager.isPrisonBlock(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }
}
