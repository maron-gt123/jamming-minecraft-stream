package jp.master.jamming.listener;

import jp.master.jamming.scoreboard.JammingScoreboardManager;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class JammingScoreboardListener implements Listener {

    private final JammingScoreboardManager scoreboardManager;

    public JammingScoreboardListener(JammingScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        scoreboardManager.show(e.getPlayer());
    }

    // サーバー再読み込み対策（任意）
    public void applyToOnlinePlayers() {
        for (Player p : scoreboardManager.getPlugin().getServer().getOnlinePlayers()) {
            scoreboardManager.show(p);
        }
    }
}