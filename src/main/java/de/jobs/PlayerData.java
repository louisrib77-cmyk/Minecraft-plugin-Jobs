package de.jobs;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Alle Daten eines Spielers: Geld, Job-Fortschritt und besuchte Chunks. */
public final class PlayerData {

    private final UUID uuid;
    private double money;
    private final Map<Job, JobProgress> progress = new EnumMap<>(Job.class);
    private final Set<String> visitedChunks = new HashSet<>();

    /** Nur zur Laufzeit, wird nicht gespeichert: Strecke seit dem letzten Lohn. */
    private double distanceBuffer;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        for (Job job : Job.values()) {
            progress.put(job, new JobProgress());
        }
    }

    public UUID getUuid() { return uuid; }

    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }
    public void addMoney(double amount) { this.money += amount; }
    public void removeMoney(double amount) { this.money -= amount; }

    public JobProgress getProgress(Job job) { return progress.get(job); }

    /** Format: "welt;chunkX;chunkZ" */
    public Set<String> getVisitedChunks() { return visitedChunks; }

    public double getDistanceBuffer() { return distanceBuffer; }
    public void addDistance(double amount) { this.distanceBuffer += amount; }

    /** Fortschritt eines einzelnen Jobs. */
    public static final class JobProgress {

        private int level = 1;
        private double xp;

        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }

        public double getXp() { return xp; }
        public void setXp(double xp) { this.xp = xp; }
        public void addXp(double amount) { this.xp += amount; }
    }
}
