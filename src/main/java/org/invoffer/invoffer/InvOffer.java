package org.invoffer.invoffer;

import org.bukkit.plugin.java.JavaPlugin;
import org.invoffer.invoffer.commands.OfferCommand;
import org.invoffer.invoffer.data.DataManager;
import org.invoffer.invoffer.listeners.OfferInventoryCloseListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public final class InvOffer extends JavaPlugin {

    private static InvOffer instance;
    private static OfferCommand offerCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        System.out.println("InvOffer has started! This plugin is still in development...");
        // Create a directory for data (active offers) if it doesn't exist.
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // Creates the directory (or any other necessary directories to do it)
        }
        // Specify the file path for the new active offers data
        File activeOffersFile = new File(dataFolder, "active_offers.yml");

        DataManager dataManager = new DataManager(dataFolder);

        // Specify the file path for the new pending offers data
        File pendingOffersFile = new File(dataFolder, "pending_offers.yml");

        HashMap<UUID, UUID> activeOffers = dataManager.loadActiveOffers();
        // Register the OfferCommand
        offerCommand = new OfferCommand(dataManager);
        getCommand("invOffer").setExecutor(offerCommand);
        // Register the OfferInventoryCloseListener
        OfferInventoryCloseListener offerInventoryCloseListener = offerCommand.getOfferInventoryCloseListener();
        getServer().getPluginManager().registerEvents(offerInventoryCloseListener, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        instance = null;
        System.out.println("InvOffer has shut down.");

    }
    public static InvOffer getInstance(){
        return instance;
    }

    public static OfferCommand getOfferCommand() {
        return offerCommand;
    }
}
