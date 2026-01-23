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

    // ゲーム開始音
    public void playGameStart() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(),
                    Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.2f);
        }
    }
    // ゲーム停止音
    public void playGameStop() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(),
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
