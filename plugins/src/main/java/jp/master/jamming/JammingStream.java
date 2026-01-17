package jp.master.jamming;

import jp.master.jamming.box.JammingBoxManager;
import org.bukkit.plugin.java.JavaPlugin;
import jp.master.jamming.config.ConfigManager;
import jp.master.jamming.http.HttpServerManager;

public final class JammingStream extends JavaPlugin {

    private HttpServerManager httpServerManager;
    private JammingBoxManager boxManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager.loadConfig(this);

        boxManager = new JammingBoxManager();

        httpServerManager = new HttpServerManager(this);
        httpServerManager.start();

        getLogger().info("JammingStream enabled");
    }

    @Override
    public void onDisable() {
        if (httpServerManager != null) {
            httpServerManager.stop();
        }
        getLogger().info("JammingStream disabled");
    }
    public JammingBoxManager getBoxManager() {
        return boxManager;
    }
}