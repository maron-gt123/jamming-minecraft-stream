package jp.master.jamming;

import org.bukkit.plugin.java.JavaPlugin;
import jp.master.jamming.config.ConfigManager;
import jp.master.jamming.http.HttpServerManager;

public final class JammingStream extends JavaPlugin {

    private HttpServerManager httpServerManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager.loadConfig(this);

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
}