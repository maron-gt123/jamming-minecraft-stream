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
        String root = command.getName().toLowerCase();

        // /jammingbox
        if (root.equals("jammingbox")) {
            if (args.length == 1) {
                result.add("create");
                result.add("remove");
                result.add("start");
                result.add("stop");
                result.add("replace");
                result.add("fill");
                result.add("clear");
                result.add("set_block_interaction_range");
                result.add("clickdelay");
                return result;
            }
            // /jammingbox create <size>
            if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
                result.add("7");
                result.add("9");
                result.add("11");
                result.add("13");
                return result;
            }

            // /jammingbox create <size> <material>
            if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
                result.add("GLASS");
                result.add("RED_STAINED_GLASS");
                result.add("IRON_BLOCK");
                result.add("STONE");
                result.add("OBSIDIAN");
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

            // /jammingbox clickdelay
            if (args.length == 2
                    && args[0].equalsIgnoreCase("clickdelay")) {
                result.add("true");
                result.add("false");
                return result;
            }
        }
        // /jammingevent
        if (root.equals("jammingevent")) {

            if (args.length == 1) {
                result.add("text");
                result.add("title");
                result.add("tnt");
                result.add("extnt");
                result.add("reset");
                result.add("fillblock");
                return result;
            }

            // /jammingevent text <message>
            if (args.length == 2 && args[0].equalsIgnoreCase("text")) {
                result.add("<message>");
                return result;
            }

            // /jammingevent title <message>
            if (args.length == 2 && args[0].equalsIgnoreCase("title")) {
                result.add("<message>");
                return result;
            }

            // /jammingevent tnt [count]
            if (args.length == 2 && args[0].equalsIgnoreCase("tnt")) {
                result.add("1");
                result.add("3");
                result.add("5");
                return result;
            }

            // /jammingevent extnt [count]
            if (args.length == 2 && args[0].equalsIgnoreCase("extnt")) {
                result.add("1");
                result.add("3");
                result.add("5");
                return result;
            }

            // /jammingevent reset <dragon|wither>
            if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
                result.add("dragon");
                result.add("wither");
                return result;
            }

            // /jammingevent fillblock
            if (args.length == 2 && args[0].equalsIgnoreCase("fillblock")) {
                result.add("1");
                result.add("3");
                result.add("5");
                return result;
            }
        }
        return result;
    }
}