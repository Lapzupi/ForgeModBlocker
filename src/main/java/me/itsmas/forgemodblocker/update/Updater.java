package me.itsmas.forgemodblocker.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.itsmas.forgemodblocker.ForgeModBlocker;
import me.itsmas.forgemodblocker.util.FormattingUtil;
import me.itsmas.forgemodblocker.util.Logs;
import me.itsmas.forgemodblocker.util.Permission;
import me.itsmas.forgemodblocker.util.UtilHttp;
import me.itsmas.forgemodblocker.util.UtilServer;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scans for available plugin updates
 */
public class Updater implements Listener {
    /**
     * Spiget API query URLs
     */
    private static final String RESOURCE_VERSIONS = "https://api.spiget.org/v2/resources/ForgeModBlocker/versions?size=1&spiget__ua=ForgeModBlocker&sort=-name";
    private static final String RESOURCE_UPDATES = "https://api.spiget.org/v2/resources/ForgeModBlocker/updates?size=1&spiget__ua=ForgeModBlocker&sort=-date";

    /**
     * The delay after which update checking should begin
     */
    private static final long UPDATE_CHECK_DELAY = 20L * 60L;

    /**
     * The amount of time between update checks
     */
    private final long updateCheckInterval;

    /**
     * The plugin instance
     */
    private final ForgeModBlocker plugin;

    /**
     * Latest update data
     */
    private Object[] data;

    public Updater(@NotNull ForgeModBlocker plugin) {
        this.plugin = plugin;

        this.updateCheckInterval = 20L * 60L * (int) plugin.getConfig("update-check-interval", 20L);
        this.currentVersion = Integer.parseInt(plugin.getDescription().getVersion().replaceAll("\\.", ""));

        UtilServer.registerListener(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (checkUpdates()) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, UPDATE_CHECK_DELAY, updateCheckInterval);
    }

    /**
     * The current plugin version
     */
    private final int currentVersion;

    /**
     * Periodically checks for updates
     *
     * @return Whether an update was found
     */
    private boolean checkUpdates() {
        JsonElement versionElement = UtilHttp.getJsonFromUrl(RESOURCE_VERSIONS);
        JsonElement updateElement = UtilHttp.getJsonFromUrl(RESOURCE_UPDATES);

        if (versionElement == null || updateElement == null) {
            return false;
        }

        JsonArray versionArray = versionElement.getAsJsonArray();
        JsonArray updateArray = updateElement.getAsJsonArray();

        JsonObject latestVersion = versionArray.get(0).getAsJsonObject();
        JsonObject latestUpdate = updateArray.get(0).getAsJsonObject();

        String versionName = latestVersion.get("name").getAsString();
        int version = Integer.parseInt(versionName.replaceAll("\\.", ""));

        if (version > currentVersion) {
            String updateTitle = latestUpdate.get("title").getAsString();

            data = new Object[]{versionName, updateTitle};

            announceUpdate();
            attemptDownload();

            return true;
        }

        return false;
    }

    /**
     * The message to send to operators when they join
     */
    private String joinMessage;

    /**
     * Periodically broadcasts new updates
     */
    private void announceUpdate() {
        String[] messages = new String[]
                {
                        "An update is available:",
                        "Version: " + data[0] + " (current: " + plugin.getDescription().getVersion() + ")",
                        "Title: " + data[1]
                };

        Logs.info(messages);
        setJoinMessage(messages);

        UtilServer.broadcast(Permission.UPDATE_NOTIFICATION, false, joinMessage);
    }

    /**
     * Sets the join message
     *
     * @param messages The separated messages
     * @see #joinMessage
     */
    private void setJoinMessage(String @NotNull ... messages) {
        for (int i = 0; i < messages.length; i++) {
            messages[i] = FormattingUtil.PREFIX + messages[i];
        }

        joinMessage = String.join("\n", messages);
    }

    /**
     * Sends the join message to a player
     *
     * @param player The player
     */
    private void sendJoinMessage(@NotNull Player player) {
        player.sendMessage(joinMessage);
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (joinMessage != null && Permission.hasPermission(player, Permission.UPDATE_NOTIFICATION)) {
            sendJoinMessage(player);
        }
    }

    /**
     * The amount of failed update attempts
     */
    private final AtomicInteger updateAttempts = new AtomicInteger();

    /**
     * The location of the latest plugin jar
     */
    private final String fileAddress = "https://api.spiget.org/v2/resources/ForgeModBlocker/download";

    /**
     * Attempts to download the latest plugin update
     */
    private void attemptDownload() {
        Logs.info("Attempting update download");
        UtilServer.broadcast(Permission.UPDATE_NOTIFICATION, "Attempting to download plugin update...");

        File updateDir = new File("plugins" + File.separator + "");

        if (!updateDir.exists()) {
            updateDir.mkdir();
        }

        File pluginFile = new File(updateDir, "ForgeModBlocker.jar");

        try {
            URL updateUrl = new URL(fileAddress);
            HttpURLConnection connection = (HttpURLConnection) updateUrl.openConnection();

            connection.setRequestProperty("User-Agent", UtilHttp.USER_AGENT);

            FileUtils.copyInputStreamToFile(connection.getInputStream(), pluginFile);

            Logs.info("Update download successful");

            UtilServer.broadcast(Permission.UPDATE_NOTIFICATION,
                    ChatColor.GREEN + "Update downloaded successfully",
                    ChatColor.GREEN + "Updates will take effect when the server is restarted"
            );

            setJoinMessage(ChatColor.GREEN + "A new update was downloaded", ChatColor.GREEN + "Restart the server for it to take effect");
        } catch (IOException ex) {
            Logs.severe("Update download failed");
            UtilServer.broadcast(Permission.UPDATE_NOTIFICATION, ChatColor.RED + "Update download failed");

            ex.printStackTrace();

            if (updateAttempts.incrementAndGet() <= 3) {
                // Re-attempts the update later
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        attemptDownload();
                    }
                }.runTaskLaterAsynchronously(plugin, UPDATE_CHECK_DELAY);
            }
        }
    }
}
