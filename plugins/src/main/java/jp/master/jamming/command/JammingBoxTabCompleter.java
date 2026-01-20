package jp.master.jamming.command;

import jp.master.jamming.box.JammingBoxManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class JammingBoxTabCompleter implements TabCompleter {

    private final JammingBoxManager manager;

    public JammingBoxTabCompleter(JammingBoxManager manager) {
        this.manager = manager;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        List<String> result = new ArrayList<>();

        // /jammingbox <ここ>
        if (args.length == 1) {
            result.add("create");
            result.add("remove");
            result.add("start");
            result.add("stop");
            result.add("replace");
            result.add("fill");
            result.add("clear");
            result.add("set_block_interaction_range");
            return result;
        }

        // /jammingbox create
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            result.add("7");
            result.add("9");
            result.add("11");
            result.add("13");
            return result;
        }

        // /jammingbox replace
        if (args.length == 2 && args[0].equalsIgnoreCase("replace")) {
            result.add("true");
            result.add("false");
            return result;
        }

        // /jammingbox start
        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            result.add("3");
            result.add("5");
            result.add("10");
            return result;
        }

        // /jammingbox set_block_interaction_range
        if (args.length == 2
                && args[0].equalsIgnoreCase("set_block_interaction_range")) {
            result.add("5");
            result.add("10");
            result.add("15");
            result.add("20");
            return result;
        }

        return result;
    }
}
