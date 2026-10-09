package de.jobs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Seitenleiste mit dem Geld aller online Spieler, sortiert nach Geld.
 * Alle Spieler sehen dieselbe Rangliste.
 */
public final class MoneyScoreboard {

    private final JobsPlugin plugin;
    private final Scoreboard board;
    private final Objective objective;

    public MoneyScoreboard(JobsPlugin plugin) {
        this.plugin = plugin;
        this.board = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = board.registerNewObjective("money", Criteria.DUMMY,
                JobsPlugin.MM.deserialize("<gold><bold>Geld-Rangliste</bold></gold>"));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /** Zeigt die Rangliste dem Spieler an. */
    public void show(Player player) {
        player.setScoreboard(board);
        update(player);
    }

    /** Setzt den Eintrag des Spielers auf sein aktuelles Geld (ganzzahlig, abgerundet). */
    public void update(Player player) {
        int value = (int) Math.floor(plugin.getDataManager().get(player.getUniqueId()).getMoney());
        objective.getScore(player.getName()).setScore(value);
    }

    /** Entfernt den Eintrag, wenn der Spieler den Server verlässt. */
    public void remove(Player player) {
        board.resetScores(player.getName());
    }

    /** Aktualisiert alle online Spieler. Wird regelmäßig vom Scheduler aufgerufen. */
    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
    }
}
