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

    public static boolean isAutoConvertEnabled() {
        return plugin.getConfig().getBoolean("jammingbox.autocvt.enabled", false);
    }

    public static String getAutoConvertBottom() {
        return plugin.getConfig().getString("jammingbox.autocvt.bottom");
    }

    public static String getAutoConvertMiddle() {
        return plugin.getConfig().getString("jammingbox.autocvt.middle");
    }

    public static String getAutoConvertTop() {
        return plugin.getConfig().getString("jammingbox.autocvt.top");
    }
    // ===== reload =====
    public static void reload() {
        plugin.reloadConfig();
        loadConfig(plugin);
    }
}
