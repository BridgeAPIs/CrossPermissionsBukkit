package net.zyuiop.crosspermissions.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.RegisteredListener;

import java.util.UUID;

/**
 * This file is licensed under MIT License
 * A copy of the license is provided with the source
 * (C) zyuiop 2015
 */
public class PlayerListeners implements Listener {

    protected PermissionsBukkit plugin;

    public PlayerListeners(PermissionsBukkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(final AsyncPlayerPreLoginEvent ev) {
        plugin.api.getManager().getUser(ev.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent ev) {
        if (!plugin.isLobby)
            plugin.api.getManager().refreshPerms(ev.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLeave(PlayerQuitEvent ev) {
        disconnect(ev.getPlayer().getUniqueId());
    }

    public void disconnect(UUID player) {
        plugin.players.remove(player);
    }

    @EventHandler
    public void onKick(PlayerKickEvent ev) {
        disconnect(ev.getPlayer().getUniqueId());
    }

}
