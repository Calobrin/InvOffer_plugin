package org.invoffer.invoffer.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class DataManager {

    private File file;
    private FileConfiguration config;
    private final HashMap<UUID, UUID> activeOffers;

    public DataManager(File activeOffersFile) {
        this.activeOffers = new HashMap<>();
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

    public DataManager(File file, HashMap<UUID, UUID> activeOffers) {
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
        this.activeOffers = activeOffers;
    }
    public boolean hasActiveOffer(UUID senderUUID, UUID targetUUID) {
        System.out.println("hasActiveOffer was called, were you able to resume otherwise?");
        return activeOffers.containsKey(senderUUID) && activeOffers.get(senderUUID).equals(targetUUID);
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
    public HashMap<String, UUID> loadActiveOffers() {
        HashMap<String, UUID> activeOffers = new HashMap<>();

        // Read data from the configuration
        for (String senderName : config.getKeys(false)) {
            String targetUUIDString = config.getString(senderName);
            if (targetUUIDString != null) {
                UUID targetUUID = UUID.fromString(targetUUIDString);
                activeOffers.put(senderName, targetUUID);
            } else {
                //Handle cases where target is null.
                System.out.println("Ya done fucked up. Somehow targetUUID was set to null. lol go ask ChatGPT");
            }
        }

        return activeOffers;
    }
}
