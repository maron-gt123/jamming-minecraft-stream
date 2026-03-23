package jp.master.jamming.scoreboard;

import jp.master.jamming.game.JammingGameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JammingScoreboardManager {

    private final JavaPlugin plugin;
    private final JammingGameManager gameManager;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public JammingScoreboardManager(JavaPlugin plugin, JammingGameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    // =========================
    // プレイヤーに表示
    // =========================
    public void show(Player player) {

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        // 既存Objective全部削除（超重要）
        for (DisplaySlot slot : DisplaySlot.values()) {
            Objective o = board.getObjective(slot);
            if (o != null) {
                o.unregister();
            }
        }

        Objective obj = board.registerNewObjective("jamming", "dummy", "§aJamming");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        boards.put(player.getUniqueId(), board);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setScoreboard(board);
        }, 1L);

        update(player);
    }

    // =========================
    // 全体更新
    // =========================
    public void updateAll() {
        for (UUID uuid : boards.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                update(player);
            }
        }
    }

    // =========================
    // 個別更新
    // =========================
    private void update(Player player) {
        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) return;

        Objective obj = board.getObjective("jamming");
        if (obj == null) return;

        // リセット
        for (String e : board.getEntries()) {
            board.resetScores(e);
        }

        String entry = "§0"; // ← ここ重要

        Team team = board.getTeam("clear");
        if (team == null) {
            team = board.registerNewTeam("clear");
            team.addEntry(entry);
        }

        team.setPrefix("§bクリア回数: §e" + gameManager.getClearCount());

        obj.getScore(entry).setScore(1);
    }
}