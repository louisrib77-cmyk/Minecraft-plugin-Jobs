package de.jobs;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Verwaltet, welche Truhen in der Welt Shops sind. Gespeichert in shops.yml. */
public final class ShopManager {

    private final JobsPlugin plugin;
    private final File file;

    /** Schlüssel: "welt;x;y;z", Wert: Kategorie, z. B. "erz" */
    private final Map<String, String> shops = new HashMap<>();

    public ShopManager(JobsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shops.yml");
        load();
    }

    private void load() {
        if (!file.exists()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String line : yaml.getStringList("shops")) {
            int index = line.lastIndexOf(';');
            if (index > 0) {
                shops.put(line.substring(0, index), line.substring(index + 1));
            }
        }
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : shops.entrySet()) {
            lines.add(entry.getKey() + ";" + entry.getValue());
        }

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("shops", lines);
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte shops.yml nicht speichern: " + e.getMessage());
        }
    }

    /** Kategorie des Shops an diesem Block oder null, wenn es keiner ist. */
    public String categoryAt(Block block) {
        return shops.get(key(block));
    }

    public void set(Block block, String category) {
        shops.put(key(block), category);
        save();
    }

    public void remove(Block block) {
        if (shops.remove(key(block)) != null) {
            save();
        }
    }

    private static String key(Block block) {
        return block.getWorld().getName() + ";" + block.getX() + ";" + block.getY() + ";" + block.getZ();
    }
}
