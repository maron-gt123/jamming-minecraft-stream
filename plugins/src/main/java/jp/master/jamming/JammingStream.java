package jp.master.jamming;

import org.bukkit.plugin.java.JavaPlugin;
import jp.master.jamming.config.ConfigManager;
import jp.master.jamming.http.HttpServerManager;
import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.command.JammingBoxCommand;
import jp.master.jamming.listener.JammingBoxPlaceListener;
import jp.master.jamming.listener.JammingBoxProtectListener;

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

        getCommand("jammingbox").setExecutor(new JammingBoxCommand(boxManager));

        getServer().getPluginManager().registerEvents(
                new JammingBoxProtectListener(boxManager), this
        );
        getServer().getPluginManager().registerEvents(
                new JammingBoxPlaceListener(boxManager), this
        );

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