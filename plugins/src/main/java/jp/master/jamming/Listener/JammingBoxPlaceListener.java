package jp.master.jamming.listener;

import jp.master.jamming.box.JammingBox;
import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;

public class JammingBoxPlaceListener implements Listener {

    private final JammingBoxManager manager;

    public JammingBoxPlaceListener(JammingBoxManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!manager.isReplaceEnabled()) return;

        JammingBox box = manager.getBox();
        if (box == null) return;

        if (!box.isInside(event.getBlockPlaced().getLocation())) return;

        int y = event.getBlockPlaced().getY();
        int minY = box.getCenter().getBlockY() - box.getHalf() + 1;
        int maxY = box.getCenter().getBlockY() + box.getHalf() - 1;

        if (y < minY || y > maxY) return;

        Material converted = manager.getReplaceBlockType(
                box,
                event.getBlockPlaced().getY()
        );

        event.getBlockPlaced().setType(converted);
    }
}
