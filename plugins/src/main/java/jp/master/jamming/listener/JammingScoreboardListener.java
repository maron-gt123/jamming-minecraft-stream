package jp.master.jamming.listener;

import jp.master.jamming.scoreboard.JammingScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JammingScoreboardListener implements Listener {

    private final JammingScoreboardManager scoreboardManager;

    public JammingScoreboardListener(JammingScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        scoreboardManager.show(e.getPlayer());
    }
}