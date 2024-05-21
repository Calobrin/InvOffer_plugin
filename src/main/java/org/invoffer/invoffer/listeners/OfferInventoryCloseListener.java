package org.invoffer.invoffer.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.invoffer.invoffer.commands.OfferCommand;
import org.invoffer.invoffer.data.DataManager;

import java.util.UUID;


public class OfferInventoryCloseListener implements Listener {

    private final DataManager dataManager;
    private final OfferCommand offerCommand;

    public OfferInventoryCloseListener(DataManager dataManager, OfferCommand offerCommand) {
        this.dataManager = dataManager;
        this.offerCommand = offerCommand;
        System.out.println("OfferInventoryCloseListener constructor called with OfferCommand instance: " + offerCommand);
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
            // Check if the inventory closed was the Offer Menu
            if (event.getView().getTitle().equals(ChatColor.GOLD + "InvOffer GUI: Send")) {
                // Process the pending offer
                UUID senderUUID = event.getPlayer().getUniqueId();
                UUID targetUUID = offerCommand.getTargetUUID();
                Inventory offerInventory = event.getInventory();
                //System.out.println("OfferInventoryCloseListener: TargetUUID: " + targetUUID);
                if (targetUUID != null) {
                    if (isInventoryEmpty(offerInventory)) {
                        //If inventory is empty, do NOT save the offer(s)
                        event.getPlayer().sendMessage(ChatColor.RED + "The InvOffer GUI was empty, the offer has not been sent.");
                        return;
                    }
                    dataManager.processPendingOffer(senderUUID, targetUUID, offerInventory);
                    // After processing the offer, it saves the offer (conditions for correct offer sending done by process method)
                    dataManager.savePendingOffer(senderUUID, targetUUID, offerInventory);
                } else {
                    //Log a warning if target UUID is null
                    System.out.println("Warning: TargetUUID is null, cannot save offer");
                }
            }
        if (event.getView().getTitle().equals(ChatColor.GOLD + "InvOffer GUI: Accept")) {
            // Things and stuff
        }
        }
    }

