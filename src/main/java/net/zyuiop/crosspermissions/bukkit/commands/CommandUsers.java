package net.zyuiop.crosspermissions.bukkit.commands;

import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.api.permissions.PermissionGroup;
import net.zyuiop.crosspermissions.api.permissions.PermissionUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zyuiop on 26/08/14.
 */
public class CommandUsers implements CommandExecutor {

    private PermissionsAPI api = null;

    public CommandUsers(PermissionsAPI api) {
        this.api = api;
    }

    protected boolean canDo(CommandSender sender, String command, String[] args) {
        if (sender instanceof Player) {
            String basePerm = "permissions.bukkit.users";
            PermissionUser u = api.getUser(((Player) sender).getUniqueId());
            if (u.hasPermission("permissions.bukkit.*") || u.hasPermission("permissions.*")) return true;
            if (u.hasPermission(basePerm + ".*")) return true;
            if (u.hasPermission(basePerm + "." + command + ".*")) return true;

            ArrayList<String> uniperm = new ArrayList<>();
            uniperm.add("info");
            uniperm.add("allinfos");
            uniperm.add("help");
            uniperm.add("add");
            uniperm.add("del");
            uniperm.add("setoption");
            uniperm.add("deloption");

            if (uniperm.contains(command) && u.hasPermission(basePerm + "." + command))
                return true;
            else if (args != null && args.length != 0) {
                UUID player = getPlayerID(args[0]);
                return (u.hasPermission(basePerm + "." + command + "." + args[1]));
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public UUID getPlayerID(String name) {
        return api.getTranslator().getUUID(name, false);
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(org.bukkit.ChatColor.GOLD + "CrossPermissions - A simple lightweight permissions plugin");
            sender.sendMessage(org.bukkit.ChatColor.GOLD + "Bukkit/Spigot edition - (C) zyuiop 2015");
            return;
        }

        if (!canDo(sender, args[0], Arrays.copyOfRange(args, 1, args.length))) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire cela.");
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GOLD + "Aide de /users :");
            HashMap<String, String> com = new HashMap<>();
            com.put("addgroup <name> <group> [duration]", "Adds the group to the user (if duration is given, the group will be deleted after [duration] seconds)");
            com.put("setgroup <name> <group> [duration]", "Replaces the user's group by the given one (if duration is given, the group will be deleted after [duration] seconds)");
            com.put("delgroup <group> <group>", "Removes the user from the given group");
            com.put("add <name> <permission>", "Adds a permission. If you prefix the permission with \"-\", the permission will be a forbidden");
            com.put("del <name> <permission>", "Removes a permission");
            com.put("setoption <name> <option> <valeur>", "Défines an option");
            com.put("deloption <name> <option>", "Removes an option.");
            com.put("info <name>", "Displays some information about the user");
            com.put("allinfos <name>", "Displays all the information about the user, including the inherited information");

            for (String command : com.keySet()) {
                sender.sendMessage(ChatColor.GOLD + "/users " + command + ChatColor.WHITE + " : " + com.get(command));
            }
        } else {
            String command = args[0];
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Invalid syntax. Please run /users help");
                return;
            }

            if (command.equalsIgnoreCase("addgroup")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));
                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "The parent group doesn't exist");
                    return;
                }

                if (args.length == 4) {
                    int duration = Integer.decode(args[3]);
                    u.addParent(parent, duration);
                } else {
                    u.addParent(parent);
                }


                sender.sendMessage(ChatColor.GREEN + "The user was added to the group successfully");
            } else if (command.equalsIgnoreCase("setgroup")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));
                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "The parent group doesn't exist");
                    return;
                }

                List<PermissionGroup> gpes = u.getParents().stream().collect(Collectors.toList());

                gpes.forEach(u::removeParent);

                if (args.length == 4) {
                    int duration = Integer.decode(args[3]);
                    u.addParent(parent, duration);
                } else {
                    u.addParent(parent);
                }

                sender.sendMessage(ChatColor.GREEN + "The user's group was changed.");
            } else if (command.equalsIgnoreCase("delgroup")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));
                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "The parent group doesn't exist");
                    return;
                }
                u.removeParent(parent);
                sender.sendMessage(ChatColor.GREEN + "User was successfully removed from his group.");
            } else if (command.equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                // Ajout de permission.
                String permission = args[2];
                boolean value = !permission.startsWith("-");
                if (!value) permission = permission.substring(1); // On vire le "-"

                u.setPermission(permission, value);
                sender.sendMessage(ChatColor.GREEN + "The permission was defined.");
            } else if (command.equalsIgnoreCase("setoption")) {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                String option = args[2];
                String value = args[3];

                u.setProperty(option, value);
                sender.sendMessage(ChatColor.GREEN + "The option was defined.");
            } else if (command.equalsIgnoreCase("deloption")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                u.deleteProperty(args[2]);
                sender.sendMessage(ChatColor.GREEN + "The option was deleted.");
            } else if (command.equalsIgnoreCase("del")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }

                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                u.deletePermission(args[2]);
                sender.sendMessage(ChatColor.GREEN + "The permission was deleted");
            } else if (command.equalsIgnoreCase("info")) {
                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                sender.sendMessage(ChatColor.GOLD + "Player " + u.getEntityID());
                sender.sendMessage(ChatColor.GREEN + "PARENTS :");
                for (PermissionGroup parent : u.getParents()) {
                    sender.sendMessage(" => " + parent.getGroupName() + " - Rank " + parent.getLadder());
                }

                sender.sendMessage(ChatColor.GREEN + "PERMISSIONS :");
                for (String perm : u.getEntityPermissions().keySet()) {
                    sender.sendMessage(" => " + ((u.getPermissions().get(perm)) ? org.bukkit.ChatColor.GREEN : org.bukkit.ChatColor.RED) + perm);
                }

                sender.sendMessage(ChatColor.GREEN + "OPTIONS :");
                for (String option : u.getEntityProperties().keySet()) {
                    sender.sendMessage(" => " + option + " - val : " + u.getProperty(option));
                }
            } else if (command.equalsIgnoreCase("allinfos")) {
                PermissionUser u = api.getManager().getUser(getPlayerID(args[1]));

                sender.sendMessage(ChatColor.GOLD + "Player " + u.getEntityID());
                sender.sendMessage(ChatColor.GOLD + "Complete data, including inherited data");
                sender.sendMessage(ChatColor.GREEN + "PARENTS :");
                for (PermissionGroup parent : u.getParents()) {
                    sender.sendMessage(" => " + parent.getGroupName() + " - Rank " + parent.getLadder());
                }

                sender.sendMessage(ChatColor.GREEN + "PERMISSIONS :");
                for (String perm : u.getPermissions().keySet()) {
                    sender.sendMessage(" => " + ((u.getPermissions().get(perm)) ? org.bukkit.ChatColor.GREEN : org.bukkit.ChatColor.RED) + perm);
                }

                sender.sendMessage(ChatColor.GREEN + "OPTIONS :");
                for (String option : u.getProperties().keySet()) {
                    sender.sendMessage(" => " + option + " - val : " + u.getProperty(option));
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        this.execute(commandSender, strings);
        return true;
    }
}
