package net.zyuiop.crosspermissions.bukkit;

import net.zyuiop.crosspermissions.api.rawtypes.RawPlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This file is licensed under MIT License
 * A copy of the license is provided with the source
 * (C) zyuiop 2015
 */
public class VirtualPlayer implements RawPlayer {

    protected Player player;
    protected UUID id;
    protected PermissionAttachment attachment;

    public VirtualPlayer(Player p, PermissionsBukkit plugin) {
        this.player = p;
        this.id = p.getUniqueId();
        this.attachment = player.addAttachment(plugin);
    }

    @Override
    public void setPermission(String permission, boolean value) {
        if (player != null) {
            attachment.setPermission(permission, value);
        }
    }

    @Override
    public UUID getUniqueId() {
        return id;
    }

    @Override
    public void clearPermissions() {
        ArrayList<String> perms = new ArrayList<>();
        for (String perm : attachment.getPermissions().keySet())
            perms.add(perm);

        for (String perm : perms)
            attachment.unsetPermission(perm);
    }
}
