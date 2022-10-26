package me.itsmas.forgemodblocker.util;

import me.itsmas.forgemodblocker.ForgeModBlocker;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Chat and formatting utilities
 */
public final class FormattingUtil {
    private FormattingUtil() {
    }

    /**
     * The plugin prefix
     */
    public static String PREFIX = null;

    /**
     * Sets the plugin prefix
     */
    public static void setPrefix() {
        assert PREFIX == null : "Prefix is already set";

        ForgeModBlocker plugin = UtilServer.getPlugin();

        PREFIX = FormattingUtil.colour(plugin.getConfig("prefix", "&c[&6FMB&c]&e")) + " ";
    }

    /**
     * Colours a string
     *
     * @param string The string to colour
     * @return The coloured string
     */
    @Contract("_ -> new")
    public static @NotNull String colour(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
