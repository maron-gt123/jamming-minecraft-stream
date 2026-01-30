package jp.master.jamming;

import org.bukkit.plugin.java.JavaPlugin;
import jp.master.jamming.config.ConfigManager;
import jp.master.jamming.game.JammingGameManager;
import jp.master.jamming.http.HttpServerManager;
import jp.master.jamming.box.JammingBoxManager;
import jp.master.jamming.command.JammingBoxCommand;
import jp.master.jamming.command.JammingBoxTabCompleter;
import jp.master.jamming.listener.JammingBoxPlaceListener;
import jp.master.jamming.listener.JammingBoxProtectListener;
import jp.master.jamming.listener.JammingBoxClickDelay;

public final class JammingStream extends JavaPlugin {

    private HttpServerManager httpServerManager;
    private JammingGameManager gameManager;
    private JammingBoxManager boxManager;
    private JammingBoxClickDelay clickDelay;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager.loadConfig(this);

        boxManager = new JammingBoxManager(this);
        gameManager = new JammingGameManager(this, boxManager);
        clickDelay = new JammingBoxClickDelay();

        httpServerManager = new HttpServerManager(this);
        httpServerManager.start();

        JammingBoxCommand command = new JammingBoxCommand(boxManager, gameManager, clickDelay);
        getCommand("jammingbox").setExecutor(command);
        getCommand("jammingevent").setExecutor(command);
        getCommand("jammingbox").setTabCompleter(new JammingBoxTabCompleter(boxManager));
        getCommand("jammingevent").setTabCompleter(new JammingBoxTabCompleter(boxManager));

        getServer().getPluginManager().registerEvents(
                new JammingBoxProtectListener(boxManager, gameManager), this
        );
        getServer().getPluginManager().registerEvents(
                new JammingBoxPlaceListener(boxManager, gameManager), this
        );
        getServer().getPluginManager().registerEvents(clickDelay, this);

        getLogger().info("JammingStream enabled");
    }

    @Override
    public void onDisable() {
        if (httpServerManager != null) {
            httpServerManager.stop();
        }
        getLogger().info("JammingStream disabled");
    }
    public JammingGameManager getGameManager() {
        return gameManager;
    }
    public JammingBoxClickDelay getClickDelay() {
        return clickDelay;
    }
}