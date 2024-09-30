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
        String title = event.getView().getTitle();
        Inventory topInventory = event.getView().getTopInventory();
        // Check if the clicked inventory window is the InvOffer GUI for Accepting or Canceling offers
        if ((title.equals(ChatColor.GOLD + "InvOffer GUI: Accept") ||
            title.equals(ChatColor.GOLD + "InvOffer GUI: Cancel")) &&
            topInventory.getSize() == 9)  {

            Inventory clickedInventory = event.getClickedInventory();
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
        String title = event.getView().getTitle();
        Inventory topInventory = event.getView().getTopInventory();
        // Prevent dragging items into the InvOffer GUI for both the Accept and Cancel windows
        if ((title.equals(ChatColor.GOLD + "InvOffer GUI: Accept") ||
            title.equals(ChatColor.GOLD + "InvOffer GUI: Cancel")) &&
            topInventory.getSize() == 9) {
            event.setCancelled(true);
        }
    }
}
