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
    // エンダードラゴン破壊演出
    public void resetByDragon(Player player, JammingBox box, JavaPlugin plugin) {

        World world = box.getWorld();
        Location center = box.getCenter();

        // 箱の外周にスポーン
        double radius = box.getHalf() + 3;
        Location spawn = center.clone().add(0, 12, radius);

        EnderDragon dragon = (EnderDragon) world.spawnEntity(spawn, EntityType.ENDER_DRAGON);

        dragon.setAI(false);
        dragon.setSilent(true);
        dragon.setInvulnerable(true);
        dragon.setGravity(false);

        // プレイヤーをドラゴンの上へTP
        Location dragonLoc = dragon.getLocation();
        Location tpLoc = dragonLoc.clone().add(3, 6, -10);
        tpLoc.setPitch(60f);
        tpLoc.setYaw(dragonLoc.getYaw());
        player.teleport(tpLoc);

        // 箱へ向ける（ここが重要）
        double dx = center.getX() - dragon.getLocation().getX();
        double dy = center.getY() - dragon.getLocation().getY();
        double dz = center.getZ() - dragon.getLocation().getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        dragonLoc.setYaw(yaw);
        dragonLoc.setPitch(pitch);
        dragon.teleport(dragonLoc);

        // 鳴き声（絶望系）
        world.playSound(center, Sound.ENTITY_WITHER_AMBIENT, 4.0f, 0.6f);
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 4.0f, 0.8f);
        world.playSound(center, Sound.BLOCK_PORTAL_TRAVEL, 2.0f, 0.5f);

        // クリスタルをスポーン（Entityとして）
        Location crystalLoc = center.clone().add(0, 1, 0);
        Entity crystal = world.spawnEntity(crystalLoc, EntityType.ENDER_CRYSTAL);

        // ビーム演出（パーティクルで代用）
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!crystal.isValid()) return;

            Location from = crystal.getLocation();
            Location to = center.clone().add(0, 1, 0);

            Vector vec = to.toVector().subtract(from.toVector());
            double length = vec.length();
            vec.normalize();

            int beams = 4;
            for (int b = 0; b < beams; b++) {
                Vector offset = vec.clone().rotateAroundY((b - beams / 2.0) * 0.1);

                for (double i = 0; i < length; i += 0.5) {
                    Location point = from.clone().add(offset.clone().multiply(i));
                    world.spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
                }
            }

        }, 0L, 2L);

        // 破壊までの遅延
        long delayTicks = 60L; // 3秒

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            world.createExplosion(center, 0F, false, false);
            world.spawnParticle(Particle.EXPLOSION_HUGE, center, 200, 2, 2, 2, 0.05);
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);

            // 内部破壊
            for (Location loc : box.getInnerBlocks()) {
                loc.getBlock().setType(Material.AIR, false);
            }

            // クリスタル削除
            crystal.remove();

            // ドラゴン削除
            dragon.remove();

        }, delayTicks);
    }
    // ウィザー破壊演出
    public void resetByWither(Player player, JammingBox box, JavaPlugin plugin) {

        World world = box.getWorld();
        Location center = box.getCenter();

        // 箱の真上にスポーン
        Location spawn = center.clone().add(0, 8, 0);
        Wither wither = (Wither) world.spawnEntity(spawn, EntityType.WITHER);

        wither.setAI(false);
        wither.setInvulnerable(true);
        wither.setGravity(false);

        // プレイヤーを少し上＆斜め後ろにTP（全体が見える）
        Location tpLoc = spawn.clone().add(3, 6, -6);
        tpLoc.setPitch(60f);
        tpLoc.setYaw(tpLoc.getYaw());
        player.teleport(tpLoc);

        // 登場音（絶望）
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 4.0f, 0.6f);
        world.playSound(center, Sound.BLOCK_PORTAL_TRAVEL, 2.0f, 0.5f);

        // ===== チャージ演出 =====
        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                if (!wither.isValid()) {
                    cancel();
                    return;
                }

                ticks += 5;

                // 煙と低音
                world.spawnParticle(
                        Particle.SMOKE_LARGE,
                        wither.getLocation().add(0, 2, 0),
                        30, 0.6, 0.6, 0.6, 0.01
                );

                world.playSound(
                        wither.getLocation(),
                        Sound.BLOCK_NOTE_BLOCK_BASS,
                        1.0f,
                        0.5f + ticks * 0.02f
                );

                // ===== 自爆 =====
                if (ticks >= 40) { // 約2秒
                    explode();
                    cancel();
                }
            }

            private void explode() {
                Location loc = wither.getLocation();

                world.spawnParticle(Particle.EXPLOSION_HUGE, loc, 3);
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 0.7f);
                world.playSound(loc, Sound.ENTITY_WITHER_DEATH, 4.0f, 0.5f);

                // 内部破壊
                for (Location l : box.getInnerBlocks()) {
                    l.getBlock().setType(Material.AIR, false);
                }

                wither.remove();
            }

        }.runTaskTimer(plugin, 0L, 5L);
    }
}
