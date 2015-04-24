package net.zyuiop.crosspermissions.bukkit;

import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.api.database.JedisDatabase;
import net.zyuiop.crosspermissions.api.database.JedisSentinelDatabase;
import net.zyuiop.crosspermissions.api.permissions.PermissionEntity;
import net.zyuiop.crosspermissions.api.rawtypes.RawPlayer;
import net.zyuiop.crosspermissions.api.rawtypes.RawPlugin;
import net.zyuiop.crosspermissions.bukkit.commands.CommandGroups;
import net.zyuiop.crosspermissions.bukkit.commands.CommandRefresh;
import net.zyuiop.crosspermissions.bukkit.commands.CommandUsers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * This file is licensed under MIT License
 * A copy of the license is provided with the source
 * (C) zyuiop 2015
 */
public class PermissionsBukkit extends JavaPlugin implements RawPlugin {

    protected static PermissionsAPI api = null;
    protected HashMap<UUID, VirtualPlayer> players = new HashMap<UUID, VirtualPlayer>();
    protected ArrayList<BukkitTask> tasks = new ArrayList<>();
    protected boolean isLobby = false;
    protected static PermissionsBukkit instance;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        FileConfiguration config = this.getConfig();
        logInfo("Loading database configuration...");

        if (config.getBoolean("redis-sentinel.enabled", false)) {
            logInfo("Trying to load API with database mode : REDIS SENTINEL.");
            String master = config.getString("redis-sentinel.master");
            String auth = config.getString("redis-sentinel.auth");
            List<String> ips = config.getStringList("redis-sentinel.sentinels");

            if (master == null || auth == null || ips == null) {
                logSevere("Configuration is not complete. Plugin failed to load.");
                getPluginLoader().disablePlugin(this);
                return;
            } else {
                try {
                    Set<String> iplist = new HashSet<>();
                    iplist.addAll(ips);
                    JedisSentinelDatabase database = new JedisSentinelDatabase(iplist, master, auth);
                    api = new PermissionsAPI(this, config.getString("default-group"), database);
                } catch (Exception e) {
                    logSevere("Configuration is not correct. Plugin failed to load.");
                    e.printStackTrace();
                    getPluginLoader().disablePlugin(this);
                    return;
                }
            }
            /*
            redis:
               enabled: false
               auth: authpassword
               address: ip
               port: port
             */
        } else if (config.getBoolean("redis.enabled", false)) {
            logInfo("Trying to load API with database mode : REDIS.");
            String address = config.getString("redis.address");
            String auth = config.getString("redis.auth");
            int port = config.getInt("redis.port", 6379);

            if (address == null || auth == null) {
                logSevere("Configuration is not complete. Plugin failed to load.");
                getPluginLoader().disablePlugin(this);
                return;
            } else {
                try {
                    JedisDatabase database = new JedisDatabase(address, port, auth);
                    api = new PermissionsAPI(this, config.getString("default-group"), database);
                } catch (Exception e) {
                    logSevere("Configuration is not correct. Plugin failed to load.");
                    e.printStackTrace();
                    getPluginLoader().disablePlugin(this);
                    return;
                }
            }
        } else {
            logSevere("ERROR : NO DATABASE BACKEND ENABLED.");
            logSevere("To use this plugin, you have to enable redis or redis sentinel");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        api.getManager().refreshGroups();

        this.getCommand("refresh").setExecutor(new CommandRefresh(api, this));
        this.getCommand("groups").setExecutor(new CommandGroups(api));
        this.getCommand("users").setExecutor(new CommandUsers(api));
        this.getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
    }

    @Override
    public void onDisable() {
        for (BukkitTask task : tasks) {
            if (task != null)
                task.cancel();
        }

        logInfo("Task cancelled and DB connexion closed.");
        logInfo("CrossPermissionsBukkit disabled.");
    }

    @Override
    public void logSevere(String log) {
        Bukkit.getLogger().severe(log);
    }

    @Override
    public void logWarning(String log) {
        Bukkit.getLogger().warning(log);
    }

    @Override
    public void logInfo(String log) {
        Bukkit.getLogger().info(log);
    }

    @Override
    public void runRepeatedTaskAsync(Runnable task, long delay, long timeBeforeRun) {
        tasks.add(Bukkit.getScheduler().runTaskTimerAsynchronously(this, task, timeBeforeRun, delay));
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(this, task);
    }

    @Override
    public boolean isOnline(UUID player) {
        Player p = Bukkit.getPlayer(player);
        return (p != null && p.isOnline());
    }

    @Override
    public RawPlayer getPlayer(UUID player) {
        Player p = Bukkit.getPlayer(player);
        if (p == null)
            return null;
        if (players.containsKey(player))
            return players.get(player);

        VirtualPlayer pl = new VirtualPlayer(p, this);
        players.put(player, pl);
        return pl;
    }

    @Override
    public UUID getPlayerId(String name) {
        Player pl = Bukkit.getPlayer(name);
        return (pl == null) ? null : pl.getUniqueId();
    }

    @Override
    public String getPlayerName(UUID id) {
        Player pl = Bukkit.getPlayer(id);
        return (pl == null) ? null : pl.getName();
    }

    public static PermissionsAPI getApi() {
        return api;
    }

    public static String getPrefix(PermissionEntity entity) {
        String prefix = entity.getProperty("prefix");
        if (prefix == null)
            return null;
        prefix = prefix.replaceAll("&s", " ");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        return prefix;
    }

    public static String getSuffix(PermissionEntity entity) {
        String suffix = entity.getProperty("suffix");
        if (suffix == null)
            return null;
        suffix = suffix.replaceAll("&s", " ");
        suffix = ChatColor.translateAlternateColorCodes('&', suffix);
        return suffix;
    }

    public static boolean hasPermission(PermissionEntity entity, String permission) {
        return entity.hasPermission(permission);
    }

    /**
     * Only works for onlineplayers.
     * @param player UUID for the player. Must be online
     * @param permission The permission to check
     * @return
     */
    public static boolean hasPermission(UUID player, String permission) {
        PermissionEntity entity = api.getManager().getUserFromCache(player);
        if (entity == null) {
            Bukkit.getLogger().warning("Entity "+player+" is not found in cache.");
            return false;
        }
        return entity.hasPermission(permission);
    }

    public static boolean hasPermission(Player player, String permission) {
        return hasPermission(player.getUniqueId(), permission);
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof ConsoleCommandSender)
            return true;
        if (sender instanceof Player)
            return hasPermission((Player) sender, permission);
        return false;
    }
}