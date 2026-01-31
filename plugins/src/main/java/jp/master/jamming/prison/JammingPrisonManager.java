package jp.master.jamming.prison;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class JammingPrisonManager {

    private final JavaPlugin plugin;
    private final Set<Location> prisonBlocks = new HashSet<>();

    public JammingPrisonManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void imprison(Player player, Location center, int seconds) {
        World world = center.getWorld();
        int r = 5;

        // TP
        player.teleport(center);

        // 牢獄生成
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {

                    boolean wallX = Math.abs(x) == r;
                    boolean wallY = Math.abs(y) == r;
                    boolean wallZ = Math.abs(z) == r;

                    Location loc = center.clone().add(x, y, z);

                    if (wallY) {
                        loc.getBlock().setType(Material.OBSIDIAN, false);
                        prisonBlocks.add(loc);
                    } else if (wallX || wallZ) {
                        loc.getBlock().setType(Material.BLACK_STAINED_GLASS, false);
                        prisonBlocks.add(loc);
                    }
                }
            }
        }

        // 時間経過で解除
        Bukkit.getScheduler().runTaskLater(plugin, this::release, seconds * 20L);
    }

    public boolean isPrisonBlock(Location loc) {
        return prisonBlocks.contains(loc.getBlock().getLocation());
    }

    private void release() {
        for (Location loc : prisonBlocks) {
            loc.getBlock().setType(Material.AIR, false);
        }
        prisonBlocks.clear();
    }
}
