package dev.vollborn.WhitelistApiFetchPlugin;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WhitelistListener implements Listener {
    final private FileConfiguration config;
    final private Logger logger;

    public WhitelistListener(FileConfiguration config) {
        this.config = config;
        this.logger = Bukkit.getLogger();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        String configUrl = this.config.getString("url");

        if (configUrl == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Der Whitelist-Server scheint nicht richtig konfiguriert zu sein.");
            return;
        }

        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder(URI.create(configUrl))
                .header("accept", "application/json")
                .build();

        @SuppressWarnings("rawtypes")
        HttpResponse response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "Could not download the whitelist.");
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Der Whitelist-Server scheint nicht erreichbar zu sein.");

            return;
        }

        Gson gson = new Gson();
        try {
            String[] players = gson.fromJson(response.body().toString(), String[].class);
            if (players == null || !isOnWhitelist(players, player)) {
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Bitte frag ein Mitglied von Alles im Rudel, dich auf die Whitelist zu setzen.");
            }
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "The downloaded whitelist does not seem to be valid.");
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "Der Whitelist-Server scheint nicht erreichbar zu sein.");
        }
    }

    private boolean isOnWhitelist(String[] players, Player player) {
        var playerName = player.getName();

        for (String s : players) {
            if (playerName.equals(s)) {
                return true;
            }
        }

        return false;
    }
}
