package de.jobs.listener;

import de.jobs.Job;
import de.jobs.JobsPlugin;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.persistence.PersistentDataType;

public final class EntityListener implements Listener {

    private final JobsPlugin plugin;

    public EntityListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    /** Tiere aus Spawnern werden markiert, damit Spawner-Farmen kein Geld bringen. */
    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            event.getEntity().getPersistentDataContainer()
                    .set(plugin.getSpawnerKey(), PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler
    public void onTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) return;
        if (!JobsPlugin.isSurvival(player)) return;

        plugin.getJobManager().reward(player, Job.FARMER, plugin.getRewards().tameReward());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Animals)) return;

        // Haustiere und Spawner-Tiere bringen kein Geld
        if (entity instanceof Tameable tameable && tameable.isTamed()) return;
        if (entity.getPersistentDataContainer().has(plugin.getSpawnerKey(), PersistentDataType.BYTE)) return;

        Player killer = entity.getKiller();
        if (killer == null || !JobsPlugin.isSurvival(killer)) return;

        plugin.getJobManager().reward(killer, Job.FARMER, plugin.getRewards().killReward(entity.getType()));
    }
}
