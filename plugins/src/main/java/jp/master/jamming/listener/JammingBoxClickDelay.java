package jp.master.jamming.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class JammingBoxClickDelay implements Listener {

    // 有効化されているプレイヤー
    private final Set<Player> enabledPlayers = new HashSet<>();

    private final int FIXED_DELAY_TICKS = 1;

    /**
     * プレイヤーのクリック遅延を有効化
     */
    public void enable(Player player) {
        enabledPlayers.add(player);
    }

    /**
     * プレイヤーのクリック遅延を無効化
     */
    public void disable(Player player) {
        enabledPlayers.remove(player);
    }

    /**
     * 遅延が有効かどうか
     */
    public boolean isEnabled(Player player) {
        return enabledPlayers.contains(player);
    }

    // プレイヤーごとの最終クリック時刻
    private final java.util.Map<Player, Long> lastClickMap = new java.util.HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (!isEnabled(player)) return; // 無効なら何もしない

        long now = System.currentTimeMillis();
        long lastClick = lastClickMap.getOrDefault(player, 0L);

        // Minecraft 1ティック = 50ms
        if ((now - lastClick) < FIXED_DELAY_TICKS * 50L) {
            e.setCancelled(true); // クリック無効化
            return;
        }

        lastClickMap.put(player, now);
    }
}
