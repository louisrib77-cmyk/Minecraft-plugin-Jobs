package de.jobs.listener;

import de.jobs.JobsPlugin;
import de.jobs.ShopGui.ShopHolder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ShopListener implements Listener {

    private final JobsPlugin plugin;

    public ShopListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    /** Rechtsklick auf eine Shop-Truhe öffnet das Shop-Menü statt der Truhe. */
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return; // sonst wird zweimal ausgelöst

        Block block = event.getClickedBlock();
        if (block == null) return;

        String category = plugin.getShopManager().categoryAt(block);
        if (category == null) return;

        Player player = event.getPlayer();
        // Schleichen + Rechtsklick öffnet die echte Truhe (praktisch für Admins)
        if (player.isSneaking()) return;

        event.setCancelled(true);
        plugin.getShopGui().open(player, category);
    }

    /** Im Shop-Menü darf nichts verschoben werden, nur über den Kauf-Klick. */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ShopHolder holder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        if (!event.isLeftClick()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        Double price = holder.getPrices().get(clicked.getType());
        if (price == null) return;

        int amount = event.isShiftClick() ? 16 : 1;
        plugin.getShopGui().buy(player, clicked.getType(), price, amount);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ShopHolder) {
            event.setCancelled(true);
        }
    }
}
