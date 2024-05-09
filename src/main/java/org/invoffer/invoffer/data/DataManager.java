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
    }

    public DataManager(File file, HashMap<UUID, UUID> activeOffers) {
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
        this.activeOffers = activeOffers;
    }
    public boolean hasActiveOffer(UUID senderUUID, UUID targetUUID) {
        return activeOffers.containsKey(senderUUID) && activeOffers.get(senderUUID).equals(targetUUID);
    }

    public void saveActiveOffers(HashMap<String, UUID> activeOffers) {
        // Clear existing data in the configuration
        config.set(null, null);

        // Save activeOffers to the configuration
        for (String senderName : activeOffers.keySet()) {
            UUID targetUUID = activeOffers.get(senderName);
            config.set(senderName, targetUUID.toString());
        }

        // Save the configuration to the file
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Method to load the active offers data from file.
    public HashMap<String, UUID> loadActiveOffers() {
        HashMap<String, UUID> activeOffers = new HashMap<>();

        // Read data from the configuration
        for (String senderName : config.getKeys(false)) {
            String targetUUIDString = config.getString(senderName);
            UUID targetUUID = UUID.fromString(targetUUIDString);
            activeOffers.put(senderName, targetUUID);
        }

        return activeOffers;
    }
}
