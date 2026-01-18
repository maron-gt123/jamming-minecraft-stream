package jp.master.jamming.listener;

import jp.master.jamming.box.JammingBox;
import jp.master.jamming.box.JammingBoxManager;
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
        if (!manager.isGameActive()) return;
        if (!manager.isAutoConvertEnabled()) return;
        Optional<JammingBox> boxOpt = manager.getActiveBox();
        if (boxOpt.isEmpty()) return;
        JammingBox box = boxOpt.get();
        if (!box.isInside(event.getBlockPlaced().getLocation())) return;
        Material converted = getAutoBlockType(box, event.getBlockPlaced().getY());
        event.getBlockPlaced().setType(converted);
    }

    private Material getAutoBlockType(JammingBox box, int y) {
        int minY = box.getCenter().getBlockY() - box.getHalf() + 1;
        int maxY = box.getCenter().getBlockY() + box.getHalf();

        int height = maxY - minY;
        int relativeY = y - minY;
        int layerHeight = height / 3;

        if (relativeY >= 2 * layerHeight) return Material.DIAMOND_BLOCK;
        else if (relativeY >= layerHeight) return Material.GOLD_BLOCK;
        else return Material.IRON_BLOCK;
    }
}
