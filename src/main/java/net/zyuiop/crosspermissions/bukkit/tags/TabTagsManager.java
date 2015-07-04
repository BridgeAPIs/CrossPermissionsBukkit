package net.zyuiop.crosspermissions.bukkit.tags;

import net.zyuiop.crosspermissions.api.permissions.PermissionGroup;
import net.zyuiop.crosspermissions.api.permissions.PermissionUser;
import net.zyuiop.crosspermissions.api.rawtypes.RefreshHook;
import net.zyuiop.crosspermissions.bukkit.PermissionsBukkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;

import java.util.HashMap;

/**
 * @author zyuiop
 */
public class TabTagsManager implements RefreshHook, Listener {
    private HashMap<String, Integer> teamsIds   = new HashMap<>();
    private HashMap<Integer, Team>   teams      = new HashMap<>();
    private Integer lastTeamId = 1;
    private Scoreboard               scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    private final PermissionsBukkit instance;

    public TabTagsManager(PermissionsBukkit instance) {
        this.instance = instance;
        Bukkit.getPluginManager().registerEvents(this, instance);
        //Objective objective = scoreboard.registerNewObjective("teams", "dummy");
        //objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    /**
     * Called after the refresh
     * May be usefull if you want to do some stuff AFTER the refresh
     */
    @Override
    public void onRefreshHook() {
        for (PermissionGroup group : PermissionsBukkit.getApi().getManager().getGroupsCache().values()) {
            String prefix = PermissionsBukkit.getPrefix(group);
            String suffix = PermissionsBukkit.getSuffix(group);

            prefix = (prefix == null) ? "" : prefix;
            suffix = (suffix == null) ? "" : suffix;

            if (suffix.equals("") && prefix.equals(""))
                continue;

            getTeam(prefix, suffix);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            PermissionUser user = PermissionsBukkit.getApi().getUser(player.getUniqueId());
            String prefix = PermissionsBukkit.getPrefix(user);
            String suffix = PermissionsBukkit.getSuffix(user);
            prefix = (prefix == null) ? "" : prefix;
            suffix = (suffix == null) ? "" : suffix;

            if (suffix.equals("") && prefix.equals(""))
                continue;

            Team team = getTeam(prefix, suffix);
            team.addPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            PermissionUser user = PermissionsBukkit.getApi().getUser(event.getPlayer().getUniqueId());
            String prefix = PermissionsBukkit.getPrefix(user);
            String suffix = PermissionsBukkit.getSuffix(user);
            prefix = (prefix == null) ? "" : prefix;
            suffix = (suffix == null) ? "" : suffix;

            if (suffix.equals("") && prefix.equals(""))
                return;

            Team team = getTeam(prefix, suffix);
            Bukkit.getScheduler().runTask(instance, () ->  team.addPlayer(event.getPlayer()));
        });
        event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private Team getTeam(String prefix, String suffix) {
        if (prefix.length() > 16)
            prefix = prefix.substring(0, 16);
        if (suffix.length() > 16)
            suffix = suffix.substring(0, 16);

        String identification = prefix + ">" + suffix;
        Integer id = teamsIds.get(identification);
        if (id == null) {
            id = lastTeamId ++;
            Team team = scoreboard.registerNewTeam("Tid" + id);
            team.setPrefix(prefix);
            team.setSuffix(suffix);
            team.setNameTagVisibility(NameTagVisibility.ALWAYS);
            teams.put(id, team);
            return team;
        } else {
            return teams.get(id);
        }
    }
}
