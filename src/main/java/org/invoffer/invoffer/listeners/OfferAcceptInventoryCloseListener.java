package org.invoffer.invoffer.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.invoffer.invoffer.commands.AcceptOfferCommand;
import org.invoffer.invoffer.data.DataManager;

import java.util.UUID;

public class OfferAcceptInventoryCloseListener implements Listener {

    private final DataManager dataManager;
    private final AcceptOfferCommand acceptOfferCommand;

    public OfferAcceptInventoryCloseListener(DataManager dataManager, AcceptOfferCommand acceptOfferCommand) {
        this.dataManager = dataManager;
        this.acceptOfferCommand = acceptOfferCommand;
    }

    private boolean isInventoryEmpty(Inventory inventory){
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && !itemStack.getType().isAir()) {
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Checks if the inventory closed was the offer Accept menu.
        if (event.getView().getTitle().equals(ChatColor.GOLD + "InvOffer GUI: Accept")) {
            Player player = (Player) event.getPlayer();
            UUID senderUUID = acceptOfferCommand.getSenderUUID();
            UUID targetUUID = player.getUniqueId();
            Player senderPlayer = Bukkit.getPlayer(senderUUID);
            Inventory offerInventory = event.getInventory();

            if (isInventoryEmpty(offerInventory)) {
                // If the inventory is empty, delete/resolve the offer
                dataManager.resolvePendingOffer(senderUUID, targetUUID);
                player.sendMessage(ChatColor.GREEN + "You have fully accepted the offer.");
                if (senderPlayer != null && senderPlayer.isOnline()) {
                    senderPlayer.sendMessage(ChatColor.GREEN + "Your offer to " + ChatColor.WHITE + player.getName() + ChatColor.GREEN + " has been fully accepted.");
                }

                System.out.println("The inventory was empty, it should have deleted the pending offer.");
            } else {
                // If the inventory is NOT empty, update the offer with the remaining items
                dataManager.updatePendingOffer(senderUUID, targetUUID, offerInventory);
                if (senderPlayer != null && senderPlayer.isOnline()) {
                    senderPlayer.sendMessage(ChatColor.YELLOW + "Your offer to " + ChatColor.WHITE + player.getName() + ChatColor.YELLOW + " was partially accepted.");
                }
                player.sendMessage(ChatColor.YELLOW + "There were still items remaining in the offer. You can claim them by resending that command again.");
                System.out.println("The inventory was not empty, it should have updated the offer with the items that remained.");
            }
        }
        else {
            return;
        }
    }
}
