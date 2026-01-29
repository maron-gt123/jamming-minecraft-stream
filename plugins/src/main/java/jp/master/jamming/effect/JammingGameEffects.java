package jp.master.jamming.effect;

import jp.master.jamming.box.JammingBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
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
    // ===== ドラゴン演出 =====
    public void playDragonEffect(
            Player player,
            JammingBox box,
            JavaPlugin plugin,
            Runnable onFinish
    ) {
        World world = box.getWorld();
        Location center = box.getCenter();

        double radius = box.getHalf() + 3;
        Location spawn = center.clone().add(0, 12, radius);

        EnderDragon dragon = (EnderDragon) world.spawnEntity(spawn, EntityType.ENDER_DRAGON);
        dragon.setAI(false);
        dragon.setSilent(true);
        dragon.setInvulnerable(true);
        dragon.setGravity(false);

        // プレイヤーTP
        if (player != null && player.isOnline()) {
            Location tpLoc = dragon.getLocation().clone().add(3, 6, -10);
            tpLoc.setPitch(60f);
            player.teleport(tpLoc);
        }

        // 向き調整
        Location dragonLoc = dragon.getLocation();
        Vector dir = center.toVector().subtract(dragonLoc.toVector());
        dragonLoc.setYaw((float) Math.toDegrees(Math.atan2(dir.getX(), dir.getZ())));
        dragonLoc.setPitch((float) Math.toDegrees(
                Math.atan2(dir.getY(), Math.sqrt(dir.getX()*dir.getX() + dir.getZ()*dir.getZ()))
        ));
        dragon.teleport(dragonLoc);

        // サウンド
        world.playSound(center, Sound.ENTITY_WITHER_AMBIENT, 4.0f, 0.6f);
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 4.0f, 0.8f);

        // クリスタル
        Entity crystal = world.spawnEntity(
                center.clone().add(0, 1, 0),
                EntityType.ENDER_CRYSTAL
        );

        // ビーム演出
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!crystal.isValid()) {
                task.cancel();
                return;
            }

            Vector v = center.toVector().subtract(crystal.getLocation().toVector()).normalize();
            for (double i = 0; i < 6; i += 0.5) {
                Location p = crystal.getLocation().clone().add(v.clone().multiply(i));
                world.spawnParticle(Particle.END_ROD, p, 1, 0, 0, 0, 0);
            }
        }, 0L, 2L);

        // 終了処理
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            world.spawnParticle(Particle.EXPLOSION_HUGE, center, 1);
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);

            crystal.remove();
            dragon.remove();

            onFinish.run(); // ← 破壊は呼び出し元に任せる
        }, 60L);
    }

    // ===== ウィザー演出 =====
    public void playWitherEffect(
            Player player,
            JammingBox box,
            JavaPlugin plugin,
            Runnable onFinish
    ) {
        World world = box.getWorld();
        Location center = box.getCenter();

        Location spawn = center.clone().add(0, 8, 0);
        Wither wither = (Wither) world.spawnEntity(spawn, EntityType.WITHER);

        wither.setAI(false);
        wither.setInvulnerable(true);
        wither.setGravity(false);

        player.teleport(spawn.clone().add(3, 6, -6));

        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 4.0f, 0.6f);

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                if (!wither.isValid()) {
                    cancel();
                    return;
                }

                ticks += 5;
                world.spawnParticle(
                        Particle.SMOKE_LARGE,
                        wither.getLocation().add(0, 2, 0),
                        30, 0.6, 0.6, 0.6, 0.01
                );

                if (ticks >= 40) {
                    world.spawnParticle(Particle.EXPLOSION_HUGE, wither.getLocation(), 1);
                    world.playSound(wither.getLocation(), Sound.ENTITY_WITHER_DEATH, 4.0f, 0.5f);

                    wither.remove();
                    onFinish.run(); // ← ここだけ
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
