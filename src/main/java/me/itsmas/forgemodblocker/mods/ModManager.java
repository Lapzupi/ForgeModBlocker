package me.itsmas.forgemodblocker.mods;

import com.google.common.collect.Lists;
import me.itsmas.forgemodblocker.ForgeModBlocker;
import me.itsmas.forgemodblocker.messaging.JoinListener;
import me.itsmas.forgemodblocker.messaging.MessageListener;
import me.itsmas.forgemodblocker.util.C;
import me.itsmas.forgemodblocker.util.Permission;
import me.itsmas.forgemodblocker.util.UtilServer;
import me.itsmas.forgemodblocker.util.UtilString;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Handles caching of player mods
 */
public class ModManager {
    /**
     * The plugin instance
     */
    private final ForgeModBlocker plugin;

    public ModManager(ForgeModBlocker plugin) {
        this.plugin = plugin;

        new JoinListener(plugin);
        new MessageListener(plugin);

        loadConfigValues();
    }

    /**
     * Loads needed configuration values
     */
    public void loadConfigValues() {
        loadMode();

        blockForge = plugin.getConfig("block-forge", false);
        modList = plugin.getConfig("mod-list", new ArrayList<>());

        disallowedCommands = plugin.getConfig("disallowed-mods-commands", Lists.newArrayList("kick %player% &cIllegal Mods - %disallowed_mods%"));
        disallowedCommands.replaceAll(C::colour);
    }

    /**
     * Loads the whitelist/blacklist mode
     */
    private void loadMode() {
        Mode tempMode = EnumUtils.getEnum(Mode.class, ((String) plugin.getConfig("mode")).toUpperCase());
        this.mode = tempMode == null ? Mode.BLACKLIST : tempMode;
    }

    /**
     * Plugin modes
     */
    private enum Mode {
        WHITELIST((mod, modList) -> modList.contains(mod)),
        BLACKLIST((mod, modList) -> !modList.contains(mod));

        Mode(BiFunction<String, List<String>, Boolean> function) {
            this.function = function;
        }

        private final BiFunction<String, List<String>, Boolean> function;

        /**
         * Determines whether a mod is allowed
         *
         * @param mod     The mod to check
         * @param modList The mod list
         * @return Whether the mod is allowed
         */
        public boolean isAllowed(String mod, List<String> modList) {
            if (mod.equals("FML") || mod.equals("mcp") || mod.equals("Forge")) {
                return true;
            }

            return function.apply(mod, modList);
        }
    }

    /**
     * The mode the plugin is running in
     */
    private Mode mode;

    /**
     * Determines whether a mod is disallowed
     *
     * @param mod The mod
     * @return Whether the mod is disallowed
     */
    private boolean isDisallowed(String mod) {
        return !mode.isAllowed(mod, modList);
    }

    /**
     * Whether to block all Forge clients
     */
    private boolean blockForge;

    /**
     * The list of whitelisted/blacklisted mods
     */
    private List<String> modList;

    /**
     * The commands to execute on players using disallowed mods
     */
    private List<String> disallowedCommands;

    /**
     * Map of players to their {@link ModData} object
     */
    private final Map<Player, ModData> playerData = new HashMap<>();

    /**
     * Determines whether a player is using Forge or not
     *
     * @param player The player to check
     * @return Whether the player is using forge
     */
    public boolean isUsingForge(Player player) {
        return playerData.containsKey(player);
    }

    /**
     * Gets a player's {@link ModData} object
     *
     * @param player The player
     * @return The ModData object
     */
    public ModData getModData(Player player) {
        return playerData.get(player);
    }

    /**
     * Adds a player to the data map
     *
     * @param player The player to add
     * @param data   The player's {@link ModData}
     * @see #playerData
     */
    public void addPlayer(Player player, ModData data) {
        playerData.put(player, data);

        checkForDisallowed(player, data.getMods());
    }

    /**
     * Checks whether a player is using any disallowed mods
     *
     * @param player The player
     * @param mods   The player's mods
     */
    private void checkForDisallowed(Player player, Set<String> mods) {
        if (Permission.hasPermission(player, Permission.BYPASS)) {
            return;
        }

        Set<String> disallowed = mods.stream().filter(this::isDisallowed).collect(Collectors.toSet());

        if (!disallowed.isEmpty() || (!mods.isEmpty() && blockForge)) {
            // Player is using disallowed mods
            String modsString = String.join(", ", mods);
            String disallowedString = String.join(", ", disallowed);

            sendDisallowedCommand(player, modsString, disallowedString);
        }
    }

    /**
     * Sends the disallowed mods command for a player using illegal mods
     *
     * @param player         The player
     * @param mods           The player's mods as a string
     * @param disallowedMods The player's disallowed mods as a string
     */
    private void sendDisallowedCommand(Player player, String mods, String disallowedMods) {
        disallowedCommands.forEach(command ->
        {
            command = formatCommand(command, player, mods, disallowedMods);

            String[] args = command.split(" ");

            if (args[0].equalsIgnoreCase("[bungeekick]")) {
                String reason = UtilString.combine(args, 1);
                UtilServer.writeBungee("KickPlayer", player.getName(), reason);

                return;
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        });
    }

    /**
     * Formats a command using placeholders
     *
     * @param command        The command to be executed
     * @param player         The player to substitute
     * @param mods           The mods the player is using
     * @param disallowedMods The disallowed mods the player is using
     * @return The formatted command
     */
    private @NotNull String formatCommand(@NotNull String command, @NotNull Player player, String mods, String disallowedMods) {
        return command.replace("%player%", player.getName()).replace("%mods%", mods).replace("%disallowed_mods%", disallowedMods);
    }

    /**
     * Removes a player from the data map
     *
     * @param player The player to remove
     * @see #playerData
     */
    public void removePlayer(Player player) {
        playerData.remove(player);
    }
}
