package de.jobs;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;

/** Verteilt Belohnungen, zählt XP und lässt Jobs im Level aufsteigen. */
public final class JobManager {

    private final JobsPlugin plugin;
    private final int maxLevel;
    private final double xpBase;
    private final double xpPerLevel;
    private final double bonusPerLevel;

    public JobManager(JobsPlugin plugin) {
        this.plugin = plugin;
        this.maxLevel = plugin.getConfig().getInt("leveling.max-level", 100);
        this.xpBase = plugin.getConfig().getDouble("leveling.xp-base", 100);
        this.xpPerLevel = plugin.getConfig().getDouble("leveling.xp-per-level", 50);
        this.bonusPerLevel = plugin.getConfig().getDouble("leveling.bonus-per-level", 0.02);
    }

    /** XP, die man braucht, um von diesem Level auf das nächste zu kommen. */
    public double xpNeeded(int level) {
        return xpBase + (level - 1) * xpPerLevel;
    }

    /** Geld-Multiplikator: Level 1 = 1.0x, jedes weitere Level erhöht um bonusPerLevel. */
    public double multiplier(int level) {
        return 1.0 + (level - 1) * bonusPerLevel;
    }

    /**
     * Gibt einem Spieler für eine Aktion Geld und XP.
     *
     * @param baseMoney Basislohn vor dem Level-Bonus. Die XP entsprechen diesem Wert.
     */
    public void reward(Player player, Job job, double baseMoney) {
        if (baseMoney <= 0) return;

        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        PlayerData.JobProgress progress = data.getProgress(job);

        double earned = baseMoney * multiplier(progress.getLevel());
        data.addMoney(earned);
        progress.addXp(baseMoney);

        boolean leveledUp = false;
        while (progress.getLevel() < maxLevel && progress.getXp() >= xpNeeded(progress.getLevel())) {
            progress.addXp(-xpNeeded(progress.getLevel()));
            progress.setLevel(progress.getLevel() + 1);
            leveledUp = true;
        }

        if (leveledUp) {
            player.sendMessage(JobsPlugin.MM.deserialize(String.format(Locale.GERMAN,
                    "<gold>Level-Up! </gold><yellow>%s</yellow> <gray>ist jetzt Level %d.</gray>",
                    job.getDisplayName(), progress.getLevel())));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }

        String xpText = progress.getLevel() >= maxLevel
                ? "MAX"
                : String.format(Locale.GERMAN, "%.0f/%.0f XP", progress.getXp(), xpNeeded(progress.getLevel()));

        player.sendActionBar(JobsPlugin.MM.deserialize(String.format(Locale.GERMAN,
                "<green>+%s $</green> <dark_gray>|</dark_gray> <gray>%s Lv. %d (%s)</gray>",
                JobsPlugin.money(earned), job.getDisplayName(), progress.getLevel(), xpText)));
    }
}
