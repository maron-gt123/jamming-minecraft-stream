package jp.master.jamming.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class JammingScoreboardManager {

    private Scoreboard board;
    private Objective objective;

    // UID → 名前
    private final Map<String, String> names = new HashMap<>();

    // UID → ダイヤ累計
    private final Map<String, Integer> totals = new HashMap<>();

    // =========================
    // 初期化
    // =========================
    public void init() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();

        objective = board.registerNewObjective("giftRank", "dummy", "§6§l★ RANKING ★");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    // =========================
    // 全プレイヤーに表示
    // =========================
    public void show(Player player) {
        if (board == null) return;
        player.setScoreboard(board);
    }
    public void showAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }

    // =========================
    // ギフト更新（HTTPから呼ぶ）
    // =========================
    public void updateGift(String user, String nickname, int diamondTotal) {

        if (nickname == null || nickname.isEmpty()) {
            nickname = user;
        }

        names.put(user, nickname);
        totals.put(user, diamondTotal);

        updateBoard();
    }

    // =========================
    // スコアボード更新
    // =========================
    private void updateBoard() {
        if (board == null || objective == null) return;

        // 全削除
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        // チーム削除（重要）
        for (Team team : board.getTeams()) {
            team.unregister();
        }

        // ===== ランキング作成 =====
        List<Map.Entry<String, Integer>> ranking = totals.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .toList();

        int score = 15;

        for (int i = 0; i < Math.min(5, ranking.size()); i++) {

            Map.Entry<String, Integer> entry = ranking.get(i);

            String user = entry.getKey();
            int diamond = entry.getValue();
            String name = names.getOrDefault(user, user);

            int rank = i + 1;

            String entryKey = "§" + i;

            Team team = board.registerNewTeam("rank_" + i);
            team.addEntry(entryKey);

            // 左側
            team.setPrefix("§e" + rank + "位 §f" + name);

            // 右側に付く文字
            team.setSuffix(" §bpt.");

            // 数値（中央右）
            objective.getScore(entryKey).setScore(diamond);
        }
    }
    // =========================
    // リセット（配信終了用）
    // =========================
    public void reset() {
        names.clear();
        totals.clear();

        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
    }
}