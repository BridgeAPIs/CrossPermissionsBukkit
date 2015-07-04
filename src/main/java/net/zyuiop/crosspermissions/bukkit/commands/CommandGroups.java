package net.zyuiop.crosspermissions.bukkit.commands;

import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.api.permissions.PermissionGroup;
import net.zyuiop.crosspermissions.api.permissions.PermissionUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

// This command is originally an BungeeCord command
// TODO : split each sub command to a different file
// TODO : allow plugins to add their own subcommands
public class CommandGroups implements CommandExecutor {
    private PermissionsAPI api = null;

    public CommandGroups(PermissionsAPI api) {
        this.api = api;
    }

    protected boolean canDo(CommandSender sender, String command, String[] args) {
        if (sender instanceof Player) {
            String basePerm = "permissions.bukkit.groups";
            PermissionUser u = api.getUser(((Player) sender).getUniqueId());
            if (u.hasPermission("permissions.bukkit.*") || u.hasPermission("permissions.*")) return true;
            if (u.hasPermission(basePerm + ".*")) return true;
            if (u.hasPermission(basePerm + "." + command + ".*")) return true;

            ArrayList<String> uniperm = new ArrayList<String>();
            uniperm.add("list");
            uniperm.add("create");
            uniperm.add("info");
            uniperm.add("allinfos");
            uniperm.add("help");

            if (uniperm.contains(command) && u.hasPermission(basePerm + "." + command))
                return true;
            else if (args != null && args.length != 0)
                return (u.hasPermission(basePerm + "." + command + "." + args[0]));
            else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "CrossPermissions - A simple lightweight permissions plugin");
            sender.sendMessage(ChatColor.GOLD + "Bukkit/Spigot edition - (C) zyuiop 2015");
            return;
        }

        if (!canDo(sender, args[0], Arrays.copyOfRange(args, 1, args.length))) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire cela.");
            return;
        }

        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GOLD + "Aide de /groups :");
            HashMap<String, String> com = new HashMap<>();
            com.put("list", "Lists the existing groups.");
            com.put("create <group> <rank>", "créates the group");
            com.put("addparent <group> <parent>", "Adds the group <parent> in the parents of the group <group>");
            com.put("delparent <group> <parent>", "Removes the group <parent> from the parents of the group <group>");
            com.put("deletegroup <group>", "Delets the group " + ChatColor.RED + "(Warning : might cause bugs)");
            com.put("add <group> <permission>", "Adds a permission. If you prefix the permission with \"-\", the permission will be a forbidden");
            com.put("del <group> <permission>", "Removes a permission");
            com.put("setoption <group> <option> <valeur>", "Défines an option");
            com.put("deloption <group> <option>", "Removes an option.");
            com.put("info <group>", "Displays some information about the group");
            com.put("allinfos <group>", "Displays all the information about the group, including the inherited information");
            com.put("rename <group> <new name>", "Renomme le groupe");


            for (String command : com.keySet()) {
                sender.sendMessage(ChatColor.GOLD + "/groups " + command + ChatColor.WHITE + " : " + com.get(command));
            }
        } else {
            String command = args[0];

            if (command.equalsIgnoreCase("list")) {
                for (PermissionGroup g : api.getManager().getGroupsCache().values()) {
                    sender.sendMessage(g.getGroupName() + " (Rank : " + g.getLadder() + ") ");
                }
                return;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Invalid command : please run /groups help");
                return;
            }

            if (command.equalsIgnoreCase("create")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "/groups create <nom> <rank>");
                    return;
                }
                if (api.getManager().getGroup(args[1]) != null) {
                    sender.sendMessage(ChatColor.RED + "The groups already exists.");
                    return;
                }

                PermissionGroup group = new PermissionGroup(api.getManager(), UUID.randomUUID(), Integer.decode(args[2]), args[1]);
                group.create(); // Sauvegarde le groupe
                api.getManager().refreshGroups();
            } else if (command.equalsIgnoreCase("addparent")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "This group doesn't exist");
                    return;
                }

                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "The parent group doesn't exist");
                    return;
                }

                g.addParent(parent);
                sender.sendMessage(ChatColor.GREEN + parent.getGroupName() + " is now a parent of " + g.getGroupName() +" !");
            } else if (command.equalsIgnoreCase("delparent")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "This group doesn't exist");
                    return;
                }

                PermissionGroup parent = api.getManager().getGroup(args[2]);
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "The parent group doesn't exist");
                    return;
                }

                g.removeParent(parent);
                sender.sendMessage(ChatColor.GREEN + parent.getGroupName() + " is not a parent of " + g.getGroupName() +" anymore.");
            } else if (command.equalsIgnoreCase("deletegroup")) {
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "The parent group doesn't exist.");
                    return;
                }

                g.remove();
                api.getManager().refreshGroups();
            } else if (command.equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "The group doesn't exist");
                    return;
                }

                // Ajout de permission.
                String permission = args[2];
                boolean value = !permission.startsWith("-");
                if (!value) permission = permission.substring(1); // On vire le "-"

                g.setPermission(permission, value);
                sender.sendMessage(ChatColor.GREEN + "The permission was added.");
            } else if (command.equalsIgnoreCase("setoption")) {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "The group doesn't exist");
                    return;
                }

                String option = args[2];
                String value = args[3];

                g.setProperty(option, value);
                sender.sendMessage(ChatColor.GREEN + "The option was defined.");
            } else if (command.equalsIgnoreCase("deloption")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "The group doesn't exist");
                    return;
                }

                g.deleteProperty(args[2]);
                sender.sendMessage(ChatColor.GREEN + "The option was deleted");
            } else if (command.equalsIgnoreCase("rename")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "The group doesn't exist");
                    return;
                }

                api.getManager().moveGroup(args[1], args[2]);
                sender.sendMessage(ChatColor.GREEN + "The group was renamed !");
            } else if (command.equalsIgnoreCase("del")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Missing arguments");
                    return;
                }
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "The group doesn't exist");
                    return;
                }

                g.deletePermission(args[2]);
                sender.sendMessage(ChatColor.GREEN + "The permission was deleted");
            } else if (command.equalsIgnoreCase("info")) {
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "The group doesn't exist");
                    return;
                }

                sender.sendMessage(ChatColor.GOLD + "Group " + g.getGroupName() + " (rank : " + g.getLadder() + ")");
                sender.sendMessage(ChatColor.GREEN + "PARENTS :");
                for (PermissionGroup parent : g.getParents()) {
                    if (parent != null)
                        sender.sendMessage(" => " + parent.getGroupName() + " - Rank " + parent.getLadder());
                }

                sender.sendMessage(ChatColor.GREEN + "PERMISSIONS :");
                for (String perm : g.getEntityPermissions().keySet()) {
                    sender.sendMessage(" => " + ((g.getPermissions().get(perm)) ? ChatColor.GREEN : ChatColor.RED) + perm);
                }

                sender.sendMessage(ChatColor.GREEN + "OPTIONS :");
                for (String option : g.getEntityProperties().keySet()) {
                    sender.sendMessage(" => " + option + " - val : " + g.getProperty(option));
                }
            } else if (command.equalsIgnoreCase("allinfos")) {
                PermissionGroup g = api.getManager().getGroup(args[1]);
                if (g == null) {
                    sender.sendMessage(ChatColor.RED + "The group doesn't exist");
                    return;
                }

                sender.sendMessage(ChatColor.GOLD + "Group " + g.getGroupName() + " (rank : " + g.getLadder() + ", id " + g.getEntityID() + ")");
                sender.sendMessage(ChatColor.GOLD + "Complete data, including inherited data.");
                sender.sendMessage(ChatColor.GREEN + "PARENTS :");
                for (PermissionGroup parent : g.getParents()) {
                    sender.sendMessage(" => " + parent.getGroupName() + " - Rank " + parent.getLadder());
                }

                sender.sendMessage(ChatColor.GREEN + "PERMISSIONS :");
                for (String perm : g.getPermissions().keySet()) {
                    sender.sendMessage(" => " + ((g.getPermissions().get(perm)) ? ChatColor.GREEN : ChatColor.RED) + perm);
                }

                sender.sendMessage(ChatColor.GREEN + "OPTIONS :");
                for (String option : g.getProperties().keySet()) {
                    sender.sendMessage(" => " + option + " - val : " + g.getProperty(option));
                }
            } else {
                sender.sendMessage(ChatColor.RED + "This command doesn't exist.");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        execute(commandSender, strings);
        return true;
    }
}
