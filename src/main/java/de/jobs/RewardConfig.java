package de.jobs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/** Liest alle Belohnungen aus der config.yml ein. */
public final class RewardConfig {

    private final Map<Material, Double> woodcutter;
    private final Map<Material, Double> miner;
    private final Map<Material, Double> plant;
    private final Map<Material, Double> harvest;
    private final Map<EntityType, Double> kill;
    private final double killDefault;
    private final double tame;
    private final double explorerPer100;
    private final double newChunk;

    public RewardConfig(JobsPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        Logger logger = plugin.getLogger();

        woodcutter = loadMaterials(config, "woodcutter.blocks", logger);
        miner = loadMaterials(config, "miner.blocks", logger);
        plant = loadMaterials(config, "farmer.plant", logger);
        harvest = loadMaterials(config, "farmer.harvest", logger);
        kill = loadEntities(config, "farmer.kill", logger);

        killDefault = config.getDouble("farmer.kill-default", 1.0);
        tame = config.getDouble("farmer.tame", 5.0);
        explorerPer100 = config.getDouble("explorer.per-100-blocks", 1.0);
        newChunk = config.getDouble("explorer.new-chunk", 1.0);
    }

    private static Map<Material, Double> loadMaterials(FileConfiguration config, String path, Logger logger) {
        Map<Material, Double> result = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) return result;

        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null) {
                logger.warning("Unbekanntes Material in config.yml (" + path + "): " + key);
                continue;
            }
            result.put(material, section.getDouble(key));
        }
        return result;
    }

    private static Map<EntityType, Double> loadEntities(FileConfiguration config, String path, Logger logger) {
        Map<EntityType, Double> result = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) return result;

        for (String key : section.getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(key.toUpperCase(Locale.ROOT));
                result.put(type, section.getDouble(key));
            } catch (IllegalArgumentException e) {
                logger.warning("Unbekanntes Tier in config.yml (" + path + "): " + key);
            }
        }
        return result;
    }

    /** Lohn für einen Holzblock, oder null wenn der Block kein belohnter Holzblock ist. */
    public Double woodcutterReward(Material material) {
        return woodcutter.get(material);
    }

    /** Lohn für einen Stein- oder Erzblock, oder null. */
    public Double minerReward(Material material) {
        return miner.get(material);
    }

    /** Lohn beim Anpflanzen, oder null. */
    public Double plantReward(Material material) {
        return plant.get(material);
    }

    /** Lohn beim Ernten, oder null. */
    public Double harvestReward(Material material) {
        return harvest.get(material);
    }

    public double killReward(EntityType type) {
        return kill.getOrDefault(type, killDefault);
    }

    public double tameReward() {
        return tame;
    }

    public double explorerPer100Blocks() {
        return explorerPer100;
    }

    public double newChunkReward() {
        return newChunk;
    }
}
