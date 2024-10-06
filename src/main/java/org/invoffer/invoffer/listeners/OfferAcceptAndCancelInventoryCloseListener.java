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
import org.invoffer.invoffer.commands.CancelOfferCommand;
import org.invoffer.invoffer.data.OfferManager;

import java.util.UUID;

public class OfferAcceptAndCancelInventoryCloseListener implements Listener {

    private final OfferManager offerManager;
    private final AcceptOfferCommand acceptOfferCommand;
    private final CancelOfferCommand cancelOfferCommand;

    public OfferAcceptAndCancelInventoryCloseListener(OfferManager offerManager, AcceptOfferCommand acceptOfferCommand, CancelOfferCommand cancelOfferCommand) {
        this.offerManager = offerManager;
        this.acceptOfferCommand = acceptOfferCommand;
        this.cancelOfferCommand = cancelOfferCommand;
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
        String title = event.getView().getTitle();
        Inventory inventory = event.getInventory();
        //Check the title and inventory size to accept or cancel offer
        if (title.equals(ChatColor.GOLD + "InvOffer GUI: Accept") && inventory.getSize() == 9){
            handleAccept(event);
        } else if (title.equals(ChatColor.GOLD + "InvOffer GUI: Cancel") && inventory.getSize() == 9) {
            handleCancel(event);
        }
    }
    private void handleAccept(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID senderUUID = acceptOfferCommand.getSenderUUID(player); // Pass the player instance here
        UUID targetUUID = player.getUniqueId();
        Player senderPlayer = Bukkit.getPlayer(senderUUID);
        Inventory offerInventory = event.getInventory();

        if (isInventoryEmpty(offerInventory)) {
            // If the inventory is empty, delete/resolve the offer
            offerManager.resolvePendingOffer(senderUUID, targetUUID);
            player.sendMessage(ChatColor.GREEN + "You have fully accepted the offer.");
            if (senderPlayer != null && senderPlayer.isOnline()) {
                senderPlayer.sendMessage(ChatColor.GREEN + "Your offer to " + ChatColor.WHITE + player.getName() + ChatColor.GREEN + " has been fully accepted.");
            }
        } else {
            // If the inventory is NOT empty, update the offer with the remaining items
            offerManager.updatePendingOffer(senderUUID, targetUUID, offerInventory);
            offerManager.updateOfferStatus(senderUUID, targetUUID, "pending");
            if (senderPlayer != null && senderPlayer.isOnline()) {
                senderPlayer.sendMessage(ChatColor.YELLOW + "Your offer to " + ChatColor.WHITE + player.getName() + ChatColor.YELLOW + " was viewed or partially accepted.");
            }
            player.sendMessage(ChatColor.YELLOW + "There were still items remaining to accept.");
            player.sendMessage(ChatColor.YELLOW + "You can claim them by resending that command again.");
        }
    }

    private void handleCancel(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID targetUUID = cancelOfferCommand.getTargetUUID(player);
        UUID senderUUID = player.getUniqueId();
        Player senderPlayer = Bukkit.getPlayer(targetUUID);
        Inventory offerInventory = event.getInventory();

        if (isInventoryEmpty(offerInventory)) {
            //If the inventory is empty, fully cancel the offer
            offerManager.resolvePendingOffer(senderUUID,targetUUID);
            player.sendMessage(ChatColor.GREEN + "You have fully canceled the offer.");
            if (senderPlayer != null && senderPlayer.isOnline()) {
                senderPlayer.sendMessage(ChatColor.YELLOW + "Your pending offer from " + ChatColor.WHITE + player.getName() + ChatColor.YELLOW + " has been fully canceled.");
            }
        } else {
            // Handle the remaining items for canceling
            offerManager.updatePendingOffer(senderUUID, targetUUID, offerInventory);
            player.sendMessage(ChatColor.YELLOW + "There were still items remaining to reclaim and cancel.");
            player.sendMessage(ChatColor.YELLOW + "You can claim them by resending that command again.");
            if (senderPlayer != null && senderPlayer.isOnline())
                senderPlayer.sendMessage(ChatColor.YELLOW + "Your pending offer from " + ChatColor.WHITE + player.getName() + ChatColor.YELLOW + " has begun cancellation and is no longer acceptable.");
        }

    }
}
