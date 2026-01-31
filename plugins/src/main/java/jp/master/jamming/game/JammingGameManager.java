package jp.master.jamming.game;

import jp.master.jamming.box.JammingBox;
import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.effect.JammingGameEffects;
import jp.master.jamming.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class JammingGameManager {

    private final JavaPlugin plugin;
    private final JammingBoxManager boxManager;
    private final JammingGameEffects effects = new JammingGameEffects();

    private boolean gameActive = false;
    private long gameStartTime = 0L;
    private boolean clearSequenceRunning = false;

    private BukkitTask actionBarTask;
    private BukkitTask countdownTask;
    private BukkitTask clearCheckTask;
    private BukkitTask clearCountdownTask;

    private final int clearCountdownSeconds;

    public JammingGameManager(JavaPlugin plugin, JammingBoxManager boxManager) {
        this.plugin = plugin;
        this.boxManager = boxManager;
        this.clearCountdownSeconds = ConfigManager.getClearCountdown();
    }

    public boolean isGameActive() {
        return gameActive;
    }

    // =========================================================
    // Game control
    // =========================================================
    public void startGame() {
        boxManager.getActiveBox().ifPresent(JammingBox::activate);
        gameActive = true;
        gameStartTime = System.currentTimeMillis();
        startActionBar();
        startClearConditionWatcher();
    }

    public void startGameWithCountdown(int seconds) {
        if (gameActive) return;

        if (seconds <= 0) {
            startGame();
            effects.playGameStart();
            return;
        }

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int t = seconds;
            @Override
            public void run() {
                if (t <= 0) {
                    countdownTask.cancel();
                    startGame();
                    effects.playGameStart();
                    return;
                }
                effects.showActionBar("§eゲーム開始まで §c" + t + " §e秒");
                Bukkit.getOnlinePlayers().forEach(p ->
                        p.playSound(p.getLocation(),
                                Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.8f));
                t--;
            }
        }, 0L, 20L);
    }

    public void stopGame() {
        gameActive = false;
        gameStartTime = 0;
        stopActionBar();
        stopClearConditionWatcher();
        effects.playGameStop();
    }

    // =========================================================
    // Clear check
    // =========================================================
    private void startClearConditionWatcher() {
        clearCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameActive || !boxManager.hasBox()) return;
            if (clearSequenceRunning) return;
            if (isBoxFullyFilled()) startClearCountdown();
        }, 0L, 10L);
    }

    private void stopClearConditionWatcher() {
        if (clearCheckTask != null) clearCheckTask.cancel();
    }

    private boolean isBoxFullyFilled() {
        JammingBox box = boxManager.getBox();
        World w = box.getWorld();
        Location c = box.getCenter();
        int h = box.getHalf();

        for (int x = c.getBlockX() - h + 1; x <= c.getBlockX() + h - 1; x++)
            for (int y = c.getBlockY() - h + 1; y <= c.getBlockY() + h - 1; y++)
                for (int z = c.getBlockZ() - h + 1; z <= c.getBlockZ() + h - 1; z++)
                    if (w.getBlockAt(x, y, z).getType() == Material.AIR)
                        return false;
        return true;
    }

    // =========================================================
    // Clear sequence
    // =========================================================
    private void startClearCountdown() {
        clearSequenceRunning = true;

        clearCountdownTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int t = clearCountdownSeconds;

            @Override
            public void run() {
                if (!isBoxFullyFilled()) {
                    clearCountdownTask.cancel();
                    clearSequenceRunning = false;
                    effects.playClearCanceled();
                    return;
                }
                if (t <= 0) {
                    clearCountdownTask.cancel();
                    onGameClearComplete();
                    return;
                }
                effects.showTitle("§aクリアまで", "§e" + t + " §a秒", 0, 20, 0);
                effects.playSoundAll(Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
                t--;
            }
        }, 0L, 20L);
    }

    private void onGameClearComplete() {
        effects.playClear();
        effects.playClearFireworks(boxManager.getBox(), plugin);
        boxManager.clearInside();
        clearSequenceRunning = false;
    }

    // =========================================================
    // UI
    // =========================================================
    private void startActionBar() {
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameActive) return;
            long sec = (System.currentTimeMillis() - gameStartTime) / 1000;
            effects.showActionBar("§a⏱ 経過時間: §e" + String.format("%02d:%02d", sec / 60, sec % 60));
        }, 0L, 20L);
    }

    private void stopActionBar() {
        if (actionBarTask != null) actionBarTask.cancel();
    }
    public void resetBoxByDragon(Player player) {
        JammingBox box = boxManager.getBox();
        effects.playDragonEffect(player, box, plugin, () -> {
            boxManager.clearInside();
        });
    }

    public void resetBoxByWither(Player player) {
        JammingBox box = boxManager.getBox();
        effects.playWitherEffect(player, box, plugin, () -> {
            boxManager.clearInside();
        });
    }
    public long getElapsedSeconds() {
        if (!isGameActive()) return 0;
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }
}
