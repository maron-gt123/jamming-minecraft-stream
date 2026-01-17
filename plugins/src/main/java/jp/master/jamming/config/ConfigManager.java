package jp.master.jamming.config;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private static int port;
    private static String path;

    public static void loadConfig(JavaPlugin plugin) {
        port = plugin.getConfig().getInt("http.port");
        path = plugin.getConfig().getString("http.path");
    }
    public static int getPort() {
        return port;
    }
    public static String getPath() {
        return path;
    }
}
