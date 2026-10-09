package de.jobs.listener;

import de.jobs.Job;
import de.jobs.JobsPlugin;
import de.jobs.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class MoveListener implements Listener {

    /** Teleports und Enderperlen: Sprünge über diese Strecke pro Bewegung zählen nicht. */
    private static final double MAX_STEP = 10.0;

    /** Nach dieser Strecke gibt es einen Lohn. */
    private static final double DISTANCE_UNIT = 100.0;

    private final JobsPlugin plugin;

    public MoveListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;

        Player player = event.getPlayer();
        if (!JobsPlugin.isSurvival(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getWorld() != to.getWorld()) return;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double step = Math.sqrt(dx * dx + dz * dz);
        if (step > MAX_STEP) return;

        PlayerData data = plugin.getDataManager().get(player.getUniqueId());

        // Strecke: nach jeweils 100 Blöcken gibt es einen Lohn
        data.addDistance(step);
        while (data.getDistanceBuffer() >= DISTANCE_UNIT) {
            data.addDistance(-DISTANCE_UNIT);
            plugin.getJobManager().reward(player, Job.EXPLORER, plugin.getRewards().explorerPer100Blocks());
        }

        // Neuer Chunk: nur das erste Betreten zählt, dauerhaft pro Spieler gespeichert
        String chunkKey = to.getWorld().getName() + ";" + (to.getBlockX() >> 4) + ";" + (to.getBlockZ() >> 4);
        if (data.getVisitedChunks().add(chunkKey)) {
            plugin.getJobManager().reward(player, Job.EXPLORER, plugin.getRewards().newChunkReward());
        }
    }
}
