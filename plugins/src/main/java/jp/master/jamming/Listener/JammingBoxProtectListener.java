package jp.master.jamming.listener;

import jp.master.jamming.box.JammingBoxManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class JammingBoxProtectListener implements Listener {

    private final JammingBoxManager manager;

    public JammingBoxProtectListener(JammingBoxManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!manager.hasBox()) return;

        if (manager.isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
