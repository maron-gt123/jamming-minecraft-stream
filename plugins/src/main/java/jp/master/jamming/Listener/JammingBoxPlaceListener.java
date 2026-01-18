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

        Material converted = manager.getAutoBlockType(
                box,
                event.getBlockPlaced().getY()
        );

        event.getBlockPlaced().setType(converted);
    }
}
