package jp.master.jamming.http;

import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.InetSocketAddress;

import jp.master.jamming.config.ConfigManager;

public class HttpServerManager {

    private final JavaPlugin plugin;
    private HttpServer server;

    public HttpServerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            int port = ConfigManager.getPort();
            String path = ConfigManager.getPath();

            server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext(path, new EventHttpHandler(plugin));

            server.setExecutor(null);
            server.start();

            plugin.getLogger().info("HTTP Server started at port " + port + " path " + path);
        } catch (Exception e) {
            plugin.getLogger().severe("HTTP Server failed: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("HTTP Server stopped");
        }
    }
}
