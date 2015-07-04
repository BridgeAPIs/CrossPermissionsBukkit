package net.zyuiop.crosspermissions.bukkit.tags;

import net.zyuiop.crosspermissions.api.permissions.PermissionUser;
import net.zyuiop.crosspermissions.bukkit.PermissionsBukkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author zyuiop
 */
public class ChatListener implements Listener {

    private final String format;

    public ChatListener(PermissionsBukkit plugin, String chatFormat) {
        this.format = chatFormat;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        PermissionUser user = PermissionsBukkit.getApi().getUser(p.getUniqueId());

        String prefix = PermissionsBukkit.getPrefix(user);
        if (prefix == null)
            prefix = "";
        String suffix = PermissionsBukkit.getSuffix(user);
        if (suffix == null)
            suffix = "";

        String tmp = format;
        tmp = tmp.replaceAll("\\{PREFIX\\}", prefix);
        tmp = tmp.replaceAll("\\{NAME\\}", p.getName());
        tmp = tmp.replaceAll("\\{SUFFIX\\}", suffix);
        tmp = tmp.replaceAll("\\{MESSAGE\\}", event.getMessage());

        event.setFormat(tmp.replace("%", "%%"));
    }
}
