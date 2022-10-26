package me.itsmas.forgemodblocker.util;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.itsmas.forgemodblocker.ForgeModBlocker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 * Server utility methods
 */
public final class UtilServer {
    private UtilServer() {
    }

    /**
     * The plugin instance
     */
    private static final ForgeModBlocker plugin = JavaPlugin.getPlugin(ForgeModBlocker.class);

    /**
     * Fetches the plugin instance
     *
     * @return The plugin instance
     * @see #plugin
     */
    static ForgeModBlocker getPlugin() {
        return plugin;
    }

    /**
     * Broadcasts a set of messages to all players with a {@link Permission}
     *
     * @param permission The required permission
     * @param messages   The messages to send
     * @see #broadcast(Permission, boolean, String...)
     */
    public static void broadcast(Permission permission, String... messages) {
        broadcast(permission, true, messages);
    }

    /**
     * Broadcasts a set of messages to all players with a {@link Permission}
     *
     * @param permission The required permission
     * @param messages   The messages to send
     * @param prefix     Whether to prefix the messages with the plugin prefix
     * @see FormattingUtil#PREFIX
     */
    public static void broadcast(Permission permission, boolean prefix, String... messages) {
        if (prefix) {
            for (int i = 0; i < messages.length; i++) {
                messages[i] = FormattingUtil.PREFIX + messages[i];
            }
        }

        Bukkit.getOnlinePlayers().stream().filter(player -> Permission.hasPermission(player, permission)).forEach(player -> player.sendMessage(messages));
    }

    /**
     * Registers a {@link Listener}
     *
     * @param listener The listener
     */
    public static void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    /**
     * Registers an outgoing message channel
     *
     * @param channel The channel name
     */
    public static void registerOutgoingChannel(String channel) {
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, channel);
    }

    /**
     * Registers an incoming message channel
     *
     * @param channel         The channel name
     * @param messageListener The {@link PluginMessageListener} listening to the channel
     */
    public static void registerIncomingChannel(String channel, PluginMessageListener messageListener) {
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, channel, messageListener);
    }

    /**
     * Forwards a message to BungeeCord through the plugin messaging channel
     *
     * @param args The data to send
     */
    public static void writeBungee(String... args) {
        assert args.length > 0 : "Args length must be at least 1";

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player == null) {
            return;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        for (String arg : args) {
            out.writeUTF(arg);
        }

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
