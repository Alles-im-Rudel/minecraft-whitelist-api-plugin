package dev.vollborn.WhitelistApiFetchPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class WhitelistApiFetchPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        this.checkOrCreateConfig();

        FileConfiguration config = getConfig();

        WhitelistRunnable runnable = new WhitelistRunnable(getConfig());
        runnable.runTaskTimer(this, 0, 20L * config.getInt("delay"));
    }

    public void checkOrCreateConfig() {
        FileConfiguration config = getConfig();

        String url = config.getString("url");
        if (url == null) {
            config.set("url", "");
        }

        int delay = config.getInt("delay");
        if (delay == 0) {
            config.set("delay", 60);
        }

        saveConfig();
    }
}
