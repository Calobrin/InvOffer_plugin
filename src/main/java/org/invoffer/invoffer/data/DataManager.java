package org.invoffer.invoffer.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {

    private static final String PENDING_OFFERS_FILE_NAME = "pending_offers.yml";
    private static final String PENDING_OFFERS_KEY = "pending_offers";
    private final File pendingOffersFile;
    private final FileConfiguration pendingOffersConfig;

    public DataManager(File dataFolder) {
        this.pendingOffersFile = new File(dataFolder, PENDING_OFFERS_FILE_NAME);

        createFile(pendingOffersFile);

        this.pendingOffersConfig = YamlConfiguration.loadConfiguration(pendingOffersFile);
    }
    private boolean isInventoryEmpty(Inventory inventory) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                return false;
            }
        }
        return true;
    }
    private void removePendingOffer(UUID senderUUID, UUID targetUUID) {
        // Get the pending offer list from the YAML file
        List<Map<String, Object>> pendingOffersList = (List<Map<String, Object>>) pendingOffersConfig.getList(PENDING_OFFERS_KEY);
        if (pendingOffersList != null) {
            // Remove any existing offer with the same sender and target UUIDs
            pendingOffersList.removeIf(offerData -> {
                String existingSenderUUID = (String) offerData.get("sender_uuid");
                String existingTargetUUID = (String) offerData.get("target_uuid");
                return existingSenderUUID.equals(senderUUID.toString()) && existingTargetUUID.equals(targetUUID.toString());
            });
        }
    }
    private void createFile(File file) {
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    System.err.println("Failed to create the file: " + file.getPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasActiveOffer(UUID senderUUID, UUID targetUUID) {
        List<Map<String, Object>> pendingOffersList = loadPendingOffers();
        if (pendingOffersList != null) {
            for (Map<String, Object> offerData : pendingOffersList) {
                String existingSenderUUID = (String) offerData.get("sender_uuid");
                String existingTargetUUID = (String) offerData.get("target_uuid");
                if (existingSenderUUID != null && existingSenderUUID.equals(senderUUID.toString()) && existingTargetUUID.equals(targetUUID.toString())) {
                    // Check if the offer inventory exists and is not empty
                    List<Map<String, Object>> offerInventory = (List<Map<String, Object>>) offerData.get("offer_inventory");
                    return offerInventory != null && !offerInventory.isEmpty();
                }
            }
        }
        return false;
    }
    public UUID getPlayerUUIDFromStorage(String playerName) {
        List<Map<String, Object>> pendingOffersList = loadPendingOffers();
        if (pendingOffersList != null) {
            for (Map<String, Object> offerData : pendingOffersList) {
                String existingSenderUUID = (String) offerData.get("sender_uuid");
                String existingTargetUUID = (String) offerData.get("target_uuid");
                if (playerName.equalsIgnoreCase(Bukkit.getOfflinePlayer(UUID.fromString(existingSenderUUID)).getName())) {
                    return UUID.fromString(existingSenderUUID);
                } else if (playerName.equalsIgnoreCase(Bukkit.getOfflinePlayer(UUID.fromString(existingTargetUUID)).getName())) {
                    return UUID.fromString(existingTargetUUID);
                }
            }
        }
        return null; // Player UUID not found
    }

    public void savePendingOffer(UUID senderUUID, UUID targetUUID, Inventory offerInventory) {
        if (hasActiveOffer(senderUUID, targetUUID)) {
            // If a pending offer already exists, return without adding and saving new entry to file.
            return;
        }
        // Removes the offer data previously
        removePendingOffer(senderUUID, targetUUID);
        // Construct the data to be saved
        Map<String, Object> offerData = new LinkedHashMap<>();
        offerData.put("sender_uuid", senderUUID.toString());
        offerData.put("target_uuid", targetUUID.toString());

        // Serialize the offer inventory contents
        List<Map<String, Object>> serializedContents = new ArrayList<>();
        for (ItemStack itemStack : offerInventory.getContents()) {
            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                serializedContents.add(itemStack.serialize());
            }
        }
        offerData.put("offer_inventory", serializedContents);

        // Get the pending offer list from the YAML file
        List<Map<String, Object>> pendingOffersList = (List<Map<String, Object>>) pendingOffersConfig.getList(PENDING_OFFERS_KEY);
        if (pendingOffersList == null) {
            pendingOffersList = new ArrayList<>();
        }

        // Create a new list to store the offer data
        List<Map<String, Object>> newPendingOffersList = new ArrayList<>(pendingOffersList);

        // Add the new offer data to the list
        newPendingOffersList.add(offerData);
        // Save the updated pending offers list to the YAML file
        pendingOffersConfig.set(PENDING_OFFERS_KEY, newPendingOffersList);
        try {
            pendingOffersConfig.save(pendingOffersFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save pending offer: " + e.getMessage());
        }
    }
    // Method to load the pending offer data from a YML
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadPendingOffers() {
        // Retrieve the pending offers list from the YAML file
        return (List<Map<String, Object>>) pendingOffersConfig.getList(PENDING_OFFERS_KEY, new ArrayList<Map<String, Object>>());
    }

    public void processPendingOffer(UUID senderUUID, UUID targetUUID, Inventory offerInventory) {
        // Check if the target player is online
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            String senderName = Bukkit.getOfflinePlayer(senderUUID).getName();
            if (senderName != null) {
                targetPlayer.sendMessage(ChatColor.YELLOW + "You have received an InvOffer from " + ChatColor.WHITE + senderName + ChatColor.YELLOW + ".");
                targetPlayer.sendMessage(ChatColor.YELLOW + "You can accept this offer by typing " + ChatColor.WHITE + "/acceptOffer " + senderName + ChatColor.YELLOW + ".");
            }
        } else {
            // Notify the sender that the target player is offline
            Player senderPlayer = Bukkit.getPlayer(senderUUID);
            if (senderPlayer != null && senderPlayer.isOnline()) {
                senderPlayer.sendMessage(ChatColor.RED + "Your target player is offline, you cannot send offers to offline players.");
            }
        }
    }

    public void updatePendingOffer(UUID senderUUID, UUID targetUUID, Inventory offerInventory) {
        List<Map<String, Object>> pendingOffersList = loadPendingOffers();
        if (pendingOffersList != null) {
            // Find the offer data for the sender and target
            for (Map<String, Object> offerData : pendingOffersList) {
                UUID existingSenderUUID = UUID.fromString((String) offerData.get("sender_uuid"));
                UUID existingTargetUUID = UUID.fromString((String) offerData.get("target_uuid"));
                System.out.println("Existing senderUUID: " + existingSenderUUID);
                System.out.println("Existing targetUUID: " + existingTargetUUID);

                System.out.println("Provided senderUUID: " + senderUUID.toString());
                System.out.println("Provided targetUUID: " + targetUUID.toString());
                if (existingSenderUUID.equals(senderUUID) && existingTargetUUID.equals(targetUUID)) {
                    // Update the offer inventory with the remaining items
                    List<Map<String, Object>> serializedContents = new ArrayList<>();
                    for (ItemStack itemStack : offerInventory.getContents()) {
                        if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                            serializedContents.add(itemStack.serialize());
                        }
                    }
                    offerData.put("offer_inventory", serializedContents);

                    // Save the updated pending offers list to the YAML file
                    pendingOffersConfig.set(PENDING_OFFERS_KEY, pendingOffersList);
                    try {
                        pendingOffersConfig.save(pendingOffersFile);
                        System.out.println("Successfully updated pending offer in file.");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Failed to update pending offer: " + e.getMessage());
                    }
                    return; // Exit the method after updating the offer
                }
            }
            System.out.println("Pending offer not found for update.");
        } else {
            System.out.println("No pending offers found for update.");
        }
    }



    public void resolvePendingOffer(UUID senderUUID, UUID targetUUID) {
        List<Map<String, Object>> pendingOffersList = loadPendingOffers();
        if (pendingOffersList != null) {
            // Find the offer data for the sender and target
            for (Map<String,Object> offerData: pendingOffersList) {
                String existingSenderUUID = (String) offerData.get("sender_uuid");
                String existingTargetUUID = (String) offerData.get("target_uuid");
                if (existingSenderUUID != null && existingSenderUUID.equals(senderUUID.toString()) && existingTargetUUID.equals(targetUUID.toString())) {
                    // Remove the offer inventory from the map
                    offerData.remove("offer_inventory");

                    pendingOffersConfig.set(PENDING_OFFERS_KEY, pendingOffersList);
                    try {
                        pendingOffersConfig.save(pendingOffersFile);
                        System.out.println("Successfully deleted pending offer data from file");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Failed to delete the pending offer: " + e.getMessage());
                    }
                    return;
                }
            }
           System.out.println("Pending offer not found for deletion");
        } else {
            System.out.println("No pending offers found for deletion");
        }
    }

    public void acceptOffer(UUID playerUUID, UUID senderUUID) {
        List<Map<String, Object>> pendingOffersList = loadPendingOffers();

        if (pendingOffersList == null) {
            System.out.println("The pendingOffersList was null (Inside AcceptOfferCommand)");
            return;
        }

        Map<String, Object> pendingOfferData = null;
        for (Map<String, Object> offerData : pendingOffersList) {
            UUID offerSenderUUID = UUID.fromString((String) offerData.get("sender_uuid"));
            UUID offerTargetUUID = UUID.fromString((String) offerData.get("target_uuid"));
            if (offerSenderUUID.equals(senderUUID) && offerTargetUUID.equals(playerUUID)) {
                pendingOfferData = offerData;
                break;
            }
        }

        if (pendingOfferData == null) {
            System.out.println("The pendingOfferData was null (Inside AcceptOfferCommand)");
            return;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            // Create and open the inventory
            Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.GOLD + "InvOffer GUI: Accept");

            // Fill the inventory with the contents from the offerData
            List<Map<String, Object>> offerInventoryContents = (List<Map<String, Object>>) pendingOfferData.get("offer_inventory");
            if (offerInventoryContents != null) {
                for (Map<String, Object> itemData : offerInventoryContents) {
                    ItemStack item = ItemStack.deserialize(itemData);
                    inventory.addItem(item);
                }
            }

            // Open the inventory for the player
            player.openInventory(inventory);
        } else {
            System.out.println("Player is not found");
        }
    }




}
