package jp.master.jamming.command;

import jp.master.jamming.box.JammingBoxManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JammingBoxCommand implements CommandExecutor {

    private final JammingBoxManager manager;

    public JammingBoxCommand(JammingBoxManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみ実行できます");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args);
            case "remove" -> handleRemove(player);
            default -> sendHelp(player);
        }
        return true;
    }

    /* =======================
       create
       ======================= */
    private void handleCreate(Player player, String[] args) {

        if (manager.hasBox()) {
            player.sendMessage("§cすでにjammingboxが存在します。先に /jammingbox remove を実行してください");
            return;
        }

        int size = 9;
        if (args.length >= 2) {
            try {
                size = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cサイズは数値で指定してください");
                return;
            }
        }

        if (size < 5) {
            player.sendMessage("§cサイズは5以上を指定してください");
            return;
        }

        if (size % 2 == 0) {
            player.sendMessage("§cサイズは奇数で指定してください（例: 7, 9, 11）");
            return;
        }

        Location center = player.getLocation();
        Material material = Material.GLASS; // ← 後でconfig化可

        manager.createBox(center, size, material);

        player.sendMessage("§ajammingboxを作成しました");
        player.sendMessage("§7サイズ: " + size + " / 素材: " + material.name());
    }

    /* =======================
       remove
       ======================= */
    private void handleRemove(Player player) {

        if (!manager.hasBox()) {
            player.sendMessage("§ejammingboxは存在しません");
            return;
        }

        manager.removeBox();
        player.sendMessage("§ajammingboxを削除しました");
    }

    /* =======================
       help
       ======================= */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6==== JammingBox ====");
        sender.sendMessage("§e/jammingbox create [size] §7- jammingboxを作成");
        sender.sendMessage("§e/jammingbox remove        §7- jammingboxを削除");
    }
}
