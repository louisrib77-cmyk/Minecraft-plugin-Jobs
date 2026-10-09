package de.jobs;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Öffnet die Shop-Menüs und wickelt die Käufe ab. */
public final class ShopGui {

    private final JobsPlugin plugin;

    public ShopGui(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String categoryKey) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("shop.categories." + categoryKey);
        if (section == null) {
            player.sendMessage(JobsPlugin.MM.deserialize("<red>Diesen Shop gibt es nicht."));
            return;
        }

        Map<Material, Double> prices = new LinkedHashMap<>();
        ConfigurationSection items = section.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material == null || !material.isItem()) {
                    plugin.getLogger().warning("Ungültiger Shop-Artikel in config.yml: " + key);
                    continue;
                }
                prices.put(material, items.getDouble(key));
            }
        }

        // 1 bis 6 Zeilen, je nach Anzahl der Artikel
        int rows = Math.max(1, Math.min(6, (prices.size() + 8) / 9));
        int size = rows * 9;

        ShopHolder holder = new ShopHolder(categoryKey, prices);
        Component title = JobsPlugin.MM.deserialize(section.getString("title", "<gold>Shop</gold>"));
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);

        int slot = 0;
        for (Map.Entry<Material, Double> entry : prices.entrySet()) {
            if (slot >= size) break;
            inventory.setItem(slot++, createDisplayItem(entry.getKey(), entry.getValue()));
        }

        player.openInventory(inventory);
    }

    /** Kauft eine Menge eines Artikels, wenn das Geld reicht. */
    public void buy(Player player, Material material, double unitPrice, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        double total = unitPrice * amount;

        if (data.getMoney() < total) {
            player.sendMessage(JobsPlugin.MM.deserialize(
                    "<red>Nicht genug Geld. Du brauchst " + JobsPlugin.money(total) + " $.</red>"));
            return;
        }

        data.removeMoney(total);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(material, amount));
        // Passt nichts mehr ins Inventar, wird der Rest vor dem Spieler abgelegt
        for (ItemStack item : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        player.sendMessage(JobsPlugin.MM.deserialize("<green>Gekauft: " + amount + "x "
                + JobsPlugin.prettyName(material) + " für " + JobsPlugin.money(total) + " $</green>"));
    }

    private ItemStack createDisplayItem(Material material, double price) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.lore(List.of(
                    JobsPlugin.MM.deserialize("<gray>Preis: <gold>" + JobsPlugin.money(price) + " $</gold></gray>"),
                    JobsPlugin.MM.deserialize("<dark_gray>Linksklick: 1 kaufen</dark_gray>"),
                    JobsPlugin.MM.deserialize("<dark_gray>Shift + Linksklick: 16 kaufen</dark_gray>")
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /** Erkennt Shop-Menüs am Inventar-Besitzer und speichert die Preise. */
    public static final class ShopHolder implements InventoryHolder {

        private final String category;
        private final Map<Material, Double> prices;
        private Inventory inventory;

        public ShopHolder(String category, Map<Material, Double> prices) {
            this.category = category;
            this.prices = prices;
        }

        void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public String getCategory() {
            return category;
        }

        public Map<Material, Double> getPrices() {
            return prices;
        }
    }
}
