package org.invoffer.invoffer.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class DataManager {

    private final File file;
    private final FileConfiguration config;
    private final HashMap<UUID, UUID> activeOffers;
    private final HashMap<UUID, PendingOffer> pendingInventoryOffers;

    public DataManager(File activeOffersFile) {
        this.activeOffers = new HashMap<>();
        this.pendingInventoryOffers = new HashMap<>();
        this.file = activeOffersFile;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }
    public boolean hasActiveOffer(UUID senderUUID, UUID targetUUID) {
        if (activeOffers.containsKey(senderUUID)) {
            UUID storedTargetUUID = activeOffers.get(senderUUID);
            return !senderUUID.equals(storedTargetUUID) && targetUUID.equals(storedTargetUUID);
        }
        return false;
    }
    public void saveActiveOffers(UUID senderUUID, UUID targetUUID) {
        // Clear existing data in the configuration
        config.set("activeOffers." + senderUUID.toString(), targetUUID.toString());

        // Save the configuration to the file
        try {
            config.save(file);
        } catch (IOException e) {
            //Using a basic system out print statement if the save failed. May want something more robust later.
            System.out.println("Failed to save active offers: " + e.getMessage());
        }
    }
    //Method to load the active offers data from file.
    public HashMap<UUID, UUID> loadActiveOffers() {
        HashMap<UUID, UUID> activeOffers = new HashMap<>();

        // Read data from the configuration
        for (String senderUUIDString : config.getKeys(false)) {
            String targetUUIDString = config.getString(senderUUIDString);
            if (targetUUIDString != null) {
                UUID senderUUID = UUID.fromString(senderUUIDString);
                UUID targetUUID = UUID.fromString(targetUUIDString);
                activeOffers.put(senderUUID, targetUUID);
            } else {
                //Handle cases where target is null.
                System.out.println("There is some error in reading the file. Temporary error message");
            }
        }

        return activeOffers;
    }

    public void storePendingOffer(UUID playerUUID, UUID targetUUID, Inventory offerInventory) {
        // Store the pending offer details in the pendingOffers map
        pendingInventoryOffers.put(playerUUID, new PendingOffer(targetUUID, offerInventory));
    }

    public void processPendingOffer(UUID playerUUID, Inventory offerInventory) {
        // Retrieve the pending offer details
        PendingOffer pendingOffer = pendingInventoryOffers.get(playerUUID);
        if (pendingOffer == null) {
            // There is no pending offer found for the player
            return;
        }
        // Validating the offer
        if (offerInventory.getSize() == 0) {
            // Offer inventory is empty, cancel the offer.
            pendingInventoryOffers.remove(playerUUID);
            return;
        }

        // Send notification to the target player
        UUID targetUUID = pendingOffer.getTargetUUID();
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            String playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
            if (playerName != null){
                targetPlayer.sendMessage(ChatColor.GOLD+ "You have received an InvOffer from " + ChatColor.WHITE + playerName + ChatColor.GOLD + ".");
                targetPlayer.sendMessage(ChatColor.GOLD + "You can accept this offer by typing " + ChatColor.WHITE + "/acceptOffer " + playerName + ChatColor.GOLD + ".");
            }
        } else {
            // Target player is offline, cancel the offer
            pendingInventoryOffers.remove(playerUUID);
            return;
        }


    }

    public static class PendingOffer {
        private final UUID targetUUID;
        private final Inventory offerInventory;

        public PendingOffer(UUID targetUUID, Inventory offerInventory) {
            this.targetUUID = targetUUID;
            this.offerInventory = offerInventory;
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }

        public Inventory getOfferInventory() {
            return offerInventory;
        }
    }
}
