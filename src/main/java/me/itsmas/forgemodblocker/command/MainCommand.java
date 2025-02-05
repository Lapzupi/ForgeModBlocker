package me.itsmas.forgemodblocker.command;

import me.itsmas.forgemodblocker.ForgeModBlocker;
import me.itsmas.forgemodblocker.util.Message;
import me.itsmas.forgemodblocker.util.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {
    private final ForgeModBlocker plugin;

    public MainCommand(@NotNull ForgeModBlocker plugin) {
        this.plugin = plugin;

        PluginDescriptionFile description = plugin.getDescription();
        this.msg = String.format("%s%s version %s%s %sby %s%s", ChatColor.GREEN, plugin.getName(), ChatColor.YELLOW, description.getVersion(), ChatColor.GREEN, ChatColor.YELLOW, description.getAuthors().get(0));
    }

    private final String msg;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!Permission.hasPermission(sender, Permission.MAIN_COMMAND)) {
            Message.send(sender, Message.NO_PERMISSION);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(msg);
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!Permission.hasPermission(sender, Permission.RELOAD_COMMAND)) {
                Message.send(sender, Message.NO_PERMISSION);
                return true;
            }

            plugin.reload();
            Message.send(sender, Message.PLUGIN_RELOADED);
        }

        return true;
    }
}
