package org.invoffer.invoffer.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OfferInventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the clicked inventory window is the InvOffer GUI for Accepting offers.
        if (event.getView().getTitle().equals(ChatColor.GOLD + "InvOffer GUI: Accept")) {
            Inventory clickedInventory = event.getClickedInventory();
            Inventory topInventory = event.getView().getTopInventory();
            ItemStack clickedItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            // Check if the click is in the offer inventory (top inventory)
            if (clickedInventory != null && clickedInventory.equals(topInventory)) {
                // Prevent adding items to the offer inventory
                if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    event.setCancelled(true);
                    return;
                }
                // Allow removing items from the offer inventory
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    event.setCancelled(false);
                    return;
                }
            }
            // Prevent shift-clicking items into the offer inventory
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
        }
    }

    // This additional eventHandler may be the KEY to preventing spam clicking to add items.
    // Before this simple thing was added, all my robust efforts to nullify adding items to the GUI met in failure.
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Prevent dragging items into the InvOffer GUI
        if (event.getView().getTitle().equals(ChatColor.GOLD + "InvOffer GUI: Accept")) {
            event.setCancelled(true);
        }
    }
}
