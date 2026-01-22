package jp.master.jamming.config;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class ConfigManager {

    private static JavaPlugin plugin;
    // ===== http =====
    private static int port;
    private static String path;

    // ===== load =====
    public static void loadConfig(JavaPlugin pluginInstance) {
        plugin = pluginInstance;

        port = plugin.getConfig().getInt("http.port", 8080);
        path = plugin.getConfig().getString("http.path", "/event");
    }

    // ===== http =====
    public static int getPort() {
        return port;
    }

    public static String getPath() {
        return path;
    }
    // ===== events =====
    public static boolean isEventEnabled(String eventType) {
        return plugin.getConfig().getBoolean(
                "events." + eventType + ".enabled", false
        );
    }
    // ===== commands =====
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getCommands(String eventType) {
        List<?> list = plugin.getConfig().getList("commands." + eventType);
        if (list == null) return Collections.emptyList();
        return (List<Map<String, Object>>) list;
    }
    // ===== jammingbox =====
    public static int getClearCountdown() {
        return plugin.getConfig().getInt("jammingbox.clear.countdown", 15);
    }

    public static boolean getReplaceEnabledDefault() {
        return plugin.getConfig().getBoolean(
                "jammingbox.replace.enabled",
                false
        );
    }

    public static boolean isReplaceEnabled() {
        return plugin.getConfig().getBoolean("jammingbox.replace.enabled", false);
    }

    public static String getReplaceBottom() {
        return plugin.getConfig().getString("jammingbox.replace.bottom");
    }

    public static String getReplaceMiddle() {
        return plugin.getConfig().getString("jammingbox.replace.middle");
    }

    public static String getReplaceTop() {
        return plugin.getConfig().getString("jammingbox.replace.top");
    }
    // ===== reload =====
    public static void reload() {
        plugin.reloadConfig();
        loadConfig(plugin);
    }
}
