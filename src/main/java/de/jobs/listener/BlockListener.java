package de.jobs.listener;

import de.jobs.Job;
import de.jobs.JobsPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockListener implements Listener {

    private final JobsPlugin plugin;

    public BlockListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        // Jeder vom Spieler gesetzte Block wird gemerkt. So bringt Setzen und Abbauen kein Geld.
        plugin.getPlacedBlocks().markPlaced(block);

        Player player = event.getPlayer();
        if (!JobsPlugin.isSurvival(player)) return;

        // Farmer: Anpflanzen
        Double plantReward = plugin.getRewards().plantReward(block.getType());
        if (plantReward != null) {
            plugin.getJobManager().reward(player, Job.FARMER, plantReward);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Shop-Truhen dürfen nur Admins abbauen
        if (plugin.getShopManager().categoryAt(block) != null) {
            if (!player.hasPermission("jobs.admin")) {
                event.setCancelled(true);
                player.sendMessage(JobsPlugin.MM.deserialize("<red>Diese Shop-Truhe kann nicht abgebaut werden.</red>"));
                return;
            }
            plugin.getShopManager().remove(block);
        }

        boolean placedByPlayer = plugin.getPlacedBlocks().removeIfPlaced(block);
        if (!JobsPlugin.isSurvival(player)) return;

        // Farmer: Ernten. Auch eigene Pflanzen zählen, deshalb vor der Prüfung auf selbst gesetzte Blöcke.
        Double harvestReward = plugin.getRewards().harvestReward(block.getType());
        if (harvestReward != null && isFullyGrown(block, placedByPlayer)) {
            plugin.getJobManager().reward(player, Job.FARMER, harvestReward);
        }

        // Selbst gesetzte Blöcke bringen beim Abbau kein Geld mehr
        if (placedByPlayer) return;

        Double woodReward = plugin.getRewards().woodcutterReward(block.getType());
        if (woodReward != null) {
            plugin.getJobManager().reward(player, Job.WOODCUTTER, woodReward);
        }

        Double minerReward = plugin.getRewards().minerReward(block.getType());
        if (minerReward != null) {
            plugin.getJobManager().reward(player, Job.MINER, minerReward);
        }
    }

    /**
     * Ausgewachsene Feldfrüchte zählen. Melonen und Kürbisse nur, wenn sie
     * natürlich entstanden sind (sonst könnte man sie aus Melonenblöcken selbst bauen).
     */
    private static boolean isFullyGrown(Block block, boolean placedByPlayer) {
        if (block.getBlockData() instanceof Ageable ageable) {
            return ageable.getAge() >= ageable.getMaximumAge();
        }
        return !placedByPlayer;
    }
}
