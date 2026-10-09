package de.jobs.listener;

import de.jobs.JobsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinQuitListener implements Listener {

    private final JobsPlugin plugin;

    public JoinQuitListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDataManager().load(player.getUniqueId());
        plugin.getMoneyScoreboard().show(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getMoneyScoreboard().remove(player);
        plugin.getDataManager().unload(player.getUniqueId());
    }
}
