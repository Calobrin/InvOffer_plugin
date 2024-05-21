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
                if (existingSenderUUID.equals(senderUUID.toString()) && existingTargetUUID.equals(targetUUID.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void savePendingOffer(UUID senderUUID, UUID targetUUID, Inventory offerInventory) {
        if (hasActiveOffer(senderUUID, targetUUID)) {
            // If a pending offer already exists, return without adding and saving new entry to file.
            return;
        }
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
        // Add the new offer data to the list
        pendingOffersList.add(offerData);
        // Save the updated pending offers list to the YAML file
        pendingOffersConfig.set(PENDING_OFFERS_KEY, pendingOffersList);
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
                targetPlayer.sendMessage(ChatColor.GOLD + "You have received an InvOffer from " + ChatColor.WHITE + senderName + ChatColor.GOLD + ".");
                targetPlayer.sendMessage(ChatColor.GOLD + "You can accept this offer by typing " + ChatColor.WHITE + "/acceptOffer " + senderName + ChatColor.GOLD + ".");
            }
        } else {
            // Notify the sender that the target player is offline
            Player senderPlayer = Bukkit.getPlayer(senderUUID);
            if (senderPlayer != null && senderPlayer.isOnline()) {
                senderPlayer.sendMessage(ChatColor.RED + "Your target player is offline, you cannot send offers to offline players.");
            }
        }
    }

    public void deletePendingOffer(UUID senderUUID, UUID targetUUID) {
        List<Map<String, Object>> pendingOffersList = loadPendingOffers();
        if (pendingOffersList != null) {
            pendingOffersList.removeIf(offerData -> {
                String existingSenderUUID = (String) offerData.get("sender_uuid");
                String existingTargetUUID = (String) offerData.get("target_uuid");
                return existingSenderUUID.equals(senderUUID.toString()) && existingTargetUUID.equals(targetUUID.toString());
            });
            pendingOffersConfig.set(PENDING_OFFERS_KEY, pendingOffersList);
            try {
                pendingOffersConfig.save(pendingOffersFile);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to remove pending offer: " + e.getMessage());
            }
        }
    }

    public void acceptOffer(UUID playerUUID, UUID senderUUID) {
        // Retrieve the pending offer data associated with the pair of UUID's
        List<Map<String, Object>> pendingOffersList = loadPendingOffers();

        // Checks if there are any pending offers.
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
            System.out.println("The pendingOfferData was null (Inside AcceptOfferCommand");
            return;
        }

        // Open an inventory window with offer items from pending offer
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            List<Map<String, Object>> offerInventoryContents = (List<Map<String, Object>>) pendingOfferData.get("offer_inventory");
            if (offerInventoryContents != null) {
                Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.GOLD + "InvOffer GUI: Accept");

                //Fill the inventory with the contents from the offerInventoryContents.
                for (Map<String, Object> itemData : offerInventoryContents) {
                    ItemStack item = ItemStack.deserialize(itemData);
                    inventory.addItem(item);
                }
                //Open the inventory for the player
                player.openInventory(inventory);
            } else {
                System.out.println("Offer Inventory contents are null or empty");
            }
        } else {
            System.out.print("Player is not found");
        }
    }



}
