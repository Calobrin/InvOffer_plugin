package org.invoffer.invoffer.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.invoffer.invoffer.commands.OfferCommand;
import org.invoffer.invoffer.data.DataManager;

import java.util.UUID;


public class OfferInventoryCloseListener implements Listener {

    private final DataManager dataManager;
    private final OfferCommand offerCommand;

    public OfferInventoryCloseListener(DataManager dataManager, OfferCommand offerCommand) {
        this.dataManager = dataManager;
        this.offerCommand = offerCommand;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
            // Check if the inventory closed was the Offer Menu
            if (event.getView().getTitle().equals(ChatColor.GOLD + "InvOffer GUI")) {
                // Process the pending offer
                UUID senderUUID = event.getPlayer().getUniqueId();
                UUID targetUUID = offerCommand.getTargetUUID();
                if (targetUUID != null) {
                    dataManager.processPendingOffer(senderUUID, event.getInventory());
                    dataManager.saveActiveOffers(senderUUID, targetUUID);
                } else {
                    //Log a warning if target UUID is null
                    System.out.println("Warning: TargetUUID is null, cannot save offer");
                }
            }
        }
    }

