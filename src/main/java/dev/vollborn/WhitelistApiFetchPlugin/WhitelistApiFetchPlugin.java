package dev.vollborn.WhitelistApiFetchPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class WhitelistApiFetchPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        this.checkOrCreateConfig();

        getServer().getPluginManager().registerEvents(new WhitelistListener(getConfig()), this);
    }

    public void checkOrCreateConfig() {
        FileConfiguration config = getConfig();

        String url = config.getString("url");
        if (url == null) {
            config.set("url", "");
        }

        saveConfig();
    }
}
