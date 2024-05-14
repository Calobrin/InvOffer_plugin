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

    private static final String ACTIVE_OFFERS_FILE_NAME = "active_offers.yml";
    private static final String PENDING_OFFERS_FILE_NAME = "pending_offers.yml";
    private static final String PENDING_OFFERS_KEY = "pending_offers";

    private final File activeOffersFile;
    private final File pendingOffersFile;
    private final FileConfiguration activeOffersConfig;
    private final FileConfiguration pendingOffersConfig;
    private final HashMap<UUID, UUID> activeOffers;


    public DataManager(File dataFolder) {
        this.activeOffers = new HashMap<>();
        this.activeOffersFile = new File(dataFolder, ACTIVE_OFFERS_FILE_NAME);
        this.pendingOffersFile = new File(dataFolder, PENDING_OFFERS_FILE_NAME);

        createFile(activeOffersFile);
        createFile(pendingOffersFile);

        this.activeOffersConfig = YamlConfiguration.loadConfiguration(activeOffersFile);
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

    // Method for checking if an active offer exists (simply by checking who sent the offer, and the target)
    public boolean hasActiveOffer(UUID senderUUID, UUID targetUUID) {
        if (activeOffers.containsKey(senderUUID)) {
            UUID storedTargetUUID = activeOffers.get(senderUUID);
            return !senderUUID.equals(storedTargetUUID) && targetUUID.equals(storedTargetUUID);
        }
        return false;
    }
    // Method for saving the active offer (simply for who sent the offer, and the target)
    public void saveActiveOffers(UUID senderUUID, UUID targetUUID) {
        // Clear existing data in the configuration
        activeOffersConfig.set("activeOffers." + senderUUID.toString(), targetUUID.toString());

        // Save the configuration to the file
        try {
            activeOffersConfig.save(activeOffersFile);
        } catch (IOException e) {
            //Using a basic system out print statement if the save failed. May want something more robust later.
            System.out.println("Failed to save active offers: " + e.getMessage());
        }
    }
    //Method to load the active offers data from file.
    public HashMap<UUID, UUID> loadActiveOffers() {
        HashMap<UUID, UUID> activeOffers = new HashMap<>();

        // Read data from the configuration
        for (String senderUUIDString : activeOffersConfig.getKeys(false)) {
            String targetUUIDString = activeOffersConfig.getString(senderUUIDString);
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
    private boolean hasPendingOffer(UUID senderUUID, UUID targetUUID) {
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
        if (hasPendingOffer(senderUUID, targetUUID)) {
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



}
