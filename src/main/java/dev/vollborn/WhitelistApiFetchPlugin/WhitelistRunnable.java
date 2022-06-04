package dev.vollborn.WhitelistApiFetchPlugin;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WhitelistRunnable extends BukkitRunnable {
    final private FileConfiguration config;
    final private Logger logger;

    public WhitelistRunnable(FileConfiguration config) {
        this.config = config;
        this.logger = Bukkit.getLogger();
    }

    @Override
    public void run() {
        // this.logger.info("Fetching whitelist...");

        String configUrl = this.config.getString("url");

        var client = HttpClient.newHttpClient();

        if (configUrl == null) {
            this.logger.log(Level.SEVERE, "URL is not set.");
            return;
        }

        var request = HttpRequest.newBuilder(URI.create(configUrl))
                .header("accept", "application/json")
                .build();

        @SuppressWarnings("rawtypes")
        HttpResponse response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "Could not download the whitelist.", e);
            return;
        }

        Gson gson = new Gson();
        try {
            String[] players = gson.fromJson(response.body().toString(), String[].class);
            if (players != null) {
                syncWhitelist(players);
            }
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "The downloaded whitelist does not seem to be valid.", e);
        }
    }

    public void syncWhitelist(String[] playersArray) {
        Server server = Bukkit.getServer();
        Set<OfflinePlayer> whitelistedPlayersSet = Bukkit.getWhitelistedPlayers();

        List<String> players = new ArrayList<>(Arrays.asList(playersArray));
        List<String> whitelistedPlayers = new ArrayList<>();

        whitelistedPlayersSet.forEach((offlinePlayer) -> {
            whitelistedPlayers.add(offlinePlayer.getName());
        });

        players.forEach((player) -> {
            if (!whitelistedPlayers.contains(player)) {
                server.dispatchCommand(server.getConsoleSender(), "whitelist add " + player);
            }
        });

        whitelistedPlayers.forEach((player) -> {
            if (!players.contains(player)) {
                server.dispatchCommand(server.getConsoleSender(), "whitelist remove " + player);

                Player playerObject = Bukkit.getPlayerExact(player);
                if (playerObject != null) {
                    playerObject.kickPlayer("You got automatically removed from the whitelist.");
                }
            }
        });
    }
}
