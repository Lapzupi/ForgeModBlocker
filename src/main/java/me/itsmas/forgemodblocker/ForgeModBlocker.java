package me.itsmas.forgemodblocker;

import me.itsmas.forgemodblocker.command.MainCommand;
import me.itsmas.forgemodblocker.command.ModsCommand;
import me.itsmas.forgemodblocker.mods.ModManager;
import me.itsmas.forgemodblocker.placeholder.Placeholders;
import me.itsmas.forgemodblocker.update.Updater;
import me.itsmas.forgemodblocker.util.C;
import me.itsmas.forgemodblocker.util.Message;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Main class for the ForgeModBlocker plugin
 *
 * @author Mas281
 * @version 1.2.5
 * @since 1.0
 */
public class ForgeModBlocker extends JavaPlugin {
    private ModManager modManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initConfig();

        boolean placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        if (placeholderAPI) {
            new Placeholders(this);
        }

        getCommand("fmb").setExecutor(new MainCommand(this));
        getCommand("mods").setExecutor(new ModsCommand(this));

        new Metrics(this, 1334).addCustomChart(new SimplePie("using_placeholderapi", () -> Boolean.toString(placeholderAPI)));
        new Updater(this);

        modManager = new ModManager(this);
    }

    @Override
    public void onDisable() {
        destroyTasks();
    }

    /**
     * Reloads the plugin
     */
    public void reload() {
        reloadConfig();

        initConfig();
        getModManager().loadConfigValues();
    }

    /**
     * Initialises caching of config values
     */
    private void initConfig() {
        C.setPrefix();
        Message.init(this);
    }

    /**
     * Cancels all ongoing plugin tasks
     */
    private void destroyTasks() {
        Bukkit.getScheduler().getPendingTasks().stream().filter(task -> task.getOwner() == this).forEach(BukkitTask::cancel);
    }

    /**
     * Fetches a value from the config
     *
     * @param path The path to the value
     * @return The value at the path or null if not present
     */
    public <T> T getConfig(String path) {
        return getConfig(path, null);
    }

    /**
     * Fetches a value from the config
     *
     * @param path         The path to the value
     * @param defaultValue The default value to return
     * @return The value at the path
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String path, Object defaultValue) {
        return (T) getConfig().get(path, defaultValue);
    }

    public ModManager getModManager() {
        return modManager;
    }
}
