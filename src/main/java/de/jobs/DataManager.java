package de.jobs;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hält die Daten aller online Spieler im Speicher und schreibt sie
 * in plugins/JobsPlugin/players/<uuid>.yml.
 */
public final class DataManager {

    private final JobsPlugin plugin;
    private final File folder;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public DataManager(JobsPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /** Gibt die Daten eines Spielers zurück und lädt sie bei Bedarf. */
    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadFromDisk);
    }

    public void load(UUID uuid) {
        get(uuid);
    }

    /** Speichert und entfernt die Daten aus dem Speicher (beim Verlassen). */
    public void unload(UUID uuid) {
        save(uuid);
        cache.remove(uuid);
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) {
            save(uuid);
        }
    }

    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("money", data.getMoney());
        for (Job job : Job.values()) {
            PlayerData.JobProgress progress = data.getProgress(job);
            yaml.set("jobs." + job.name() + ".level", progress.getLevel());
            yaml.set("jobs." + job.name() + ".xp", progress.getXp());
        }
        yaml.set("visited-chunks", new ArrayList<>(data.getVisitedChunks()));

        try {
            yaml.save(fileOf(uuid));
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte Daten von " + uuid + " nicht speichern: " + e.getMessage());
        }
    }

    private PlayerData loadFromDisk(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File file = fileOf(uuid);
        if (!file.exists()) return data;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        data.setMoney(yaml.getDouble("money", 0));
        for (Job job : Job.values()) {
            PlayerData.JobProgress progress = data.getProgress(job);
            progress.setLevel(Math.max(1, yaml.getInt("jobs." + job.name() + ".level", 1)));
            progress.setXp(yaml.getDouble("jobs." + job.name() + ".xp", 0));
        }
        data.getVisitedChunks().addAll(yaml.getStringList("visited-chunks"));
        return data;
    }

    private File fileOf(UUID uuid) {
        return new File(folder, uuid + ".yml");
    }
}
