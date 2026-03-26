package jp.master.jamming.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class JammingDoublePlaceListener implements Listener {

    private final JavaPlugin plugin;

    private boolean enabledByTime = false;
    private boolean enabledByClear = false;

    private BukkitTask task;
    private BukkitTask actionBarTask;

    public JammingDoublePlaceListener() {
        this.plugin = JavaPlugin.getProvidingPlugin(getClass());
    }

    // =========================
    // 時間指定ON
    // =========================
    public void enableWithTime(int seconds) {
        enabledByTime = true;

        startActionBar();

        if (task != null) task.cancel();

        task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            enabledByTime = false;

            if (!enabledByClear) stopActionBar();

        }, seconds * 20L);
    }

    // =========================
    // クリアまでON
    // =========================
    public void enableClearMode() {
        enabledByClear = true;

        startActionBar();

    }

    // =========================
    // 強制OFF
    // =========================
    public void disableAll() {
        enabledByTime = false;
        enabledByClear = false;

        if (task != null) task.cancel();

        stopActionBar(); // ★追加
    }

    // =========================
    // クリア時解除
    // =========================
    public void clear() {
        if (!enabledByClear) return;

        Bukkit.getLogger().info("DoublePlace CLEAR CALLED");

        enabledByClear = false;

        if (!enabledByTime) stopActionBar(); // ★追加

    }

    // =========================
    // ActionBar制御
    // =========================
    private void startActionBar() {
        if (actionBarTask != null) actionBarTask.cancel();

        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (enabledByTime || enabledByClear) {
                Bukkit.getOnlinePlayers().forEach(p ->
                        p.sendActionBar("§d⛏ 2ブロック設置 適用中")
                );
            }

        }, 0L, 20L);
    }

    private void stopActionBar() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }

    // =========================
    // 本体ロジック
    // =========================
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!(enabledByTime || enabledByClear)) return;

        Block placed = e.getBlockPlaced();
        Block above = placed.getRelative(0, 1, 0);

        if (above.getType() == Material.AIR) {
            above.setType(placed.getType());
        }

        e.getPlayer().getWorld().spawnParticle(
                Particle.VILLAGER_HAPPY,
                placed.getLocation().add(0.5, 1, 0.5),
                10
        );
    }
}