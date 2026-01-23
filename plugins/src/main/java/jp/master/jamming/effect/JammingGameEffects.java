package jp.master.jamming.effect;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

// =========================================================
// ゲーム中に発生する「演出（エフェクト）」専用クラス
//  役割：
//   - サウンド再生
//   - タイトル表示
//   - アクションバー表示
// =========================================================
public class JammingGameEffects {
    // ゲーム開始
    public void playGameStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§aSTART!", "§fゲーム開始", 10, 40, 10);
            player.playSound(player.getLocation(),
                    Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }
    }
    // ゲーム終了
    public void playGameStop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§cSTOP", "§7ゲーム終了", 10, 40, 10);
            player.playSound(player.getLocation(),
                    Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 0.8f);
        }
    }
    // アクションバー表示
    public void showActionBar(String text) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent(text)
            );
        }
    }
    // タイトル表示（汎用）
    public void showTitle(String title, String sub, int fadeIn, int stay, int fadeOut) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(title, sub, fadeIn, stay, fadeOut);
        }
    }
    // サウンド再生（汎用）
    public void playSoundAll(Sound sound, float volume, float pitch) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }
    // ゲームクリア時の演出
    public void playClear() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§6§lGAME CLEAR!", "§eおめでとう！",
                    10, 60, 20);
            p.playSound(p.getLocation(),
                    Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }
    // ゲームクリア中断時の演出
    public void playClearCanceled() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§cクリア中断", "§7ブロックが壊されました",
                    5, 30, 10);
            p.playSound(p.getLocation(),
                    Sound.BLOCK_ANVIL_LAND, 0.6f, 0.8f);
        }
    }
}
