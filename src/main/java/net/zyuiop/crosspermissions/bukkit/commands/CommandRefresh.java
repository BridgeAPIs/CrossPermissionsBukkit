package net.zyuiop.crosspermissions.bukkit.commands;

import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.bukkit.PermissionsBukkit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by zyuiop on 27/08/14.
 */
public class CommandRefresh implements CommandExecutor {

    protected final PermissionsAPI api;
    protected final PermissionsBukkit plugin;

    public CommandRefresh(PermissionsAPI api, PermissionsBukkit plugin) {
        this.api = api;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, Command command, String s, String[] strings) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            commandSender.sendMessage(ChatColor.GREEN+"Début du raffraichissement du cache de permissions local...");
            api.getManager().refresh();
            commandSender.sendMessage(ChatColor.GREEN+"Les permissions locales ont été raffraichies !");
        });
        return true;
    }
}
