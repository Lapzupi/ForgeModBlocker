package me.itsmas.forgemodblocker.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import me.itsmas.forgemodblocker.ForgeModBlocker;
import me.itsmas.forgemodblocker.mods.ModData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles {@link PlaceholderAPI} hooks
 */
public class Placeholders extends PlaceholderExpansion {
    /**
     * The plugin instance
     */
    private final ForgeModBlocker plugin;

    public Placeholders(ForgeModBlocker plugin) {
        this.plugin = plugin;
        this.register();
    }


    @Override
    public @NotNull String getIdentifier() {
        return "forgemodblocker";
    }

    @Override
    public @NotNull String getAuthor() {
        return "sarhatabaot";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return null;
        }

        if (identifier.equals("forge")) {
            return Boolean.toString(plugin.getModManager().isUsingForge(player));
        }

        if (identifier.equals("mods")) {
            ModData data = plugin.getModManager().getModData(player);

            return data == null ? "" : String.join(", ", data.getMods());
        }

        return null;
    }
}
