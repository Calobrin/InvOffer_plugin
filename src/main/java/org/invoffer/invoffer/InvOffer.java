package org.invoffer.invoffer;

import org.bukkit.plugin.java.JavaPlugin;
import org.invoffer.invoffer.commands.AcceptOfferCommand;
import org.invoffer.invoffer.commands.OfferCommand;
import org.invoffer.invoffer.data.DataManager;
import org.invoffer.invoffer.listeners.OfferInventoryClickListener;
import org.invoffer.invoffer.listeners.OfferInventoryCloseListener;

import java.io.File;

public final class InvOffer extends JavaPlugin {

    private static InvOffer instance;
    private static OfferCommand offerCommand;
    private static AcceptOfferCommand acceptOfferCommand;

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

        // Register the OfferCommand
        offerCommand = new OfferCommand(dataManager);
        acceptOfferCommand = new AcceptOfferCommand(dataManager);
        getCommand("invOffer").setExecutor(offerCommand);
        getCommand("acceptOffer").setExecutor(acceptOfferCommand);
        // Register the OfferInventoryCloseListener
        OfferInventoryCloseListener offerInventoryCloseListener = offerCommand.getOfferInventoryCloseListener();
        getServer().getPluginManager().registerEvents(offerInventoryCloseListener, this);
        OfferInventoryClickListener offerInventoryClickListener = new OfferInventoryClickListener();
        getServer().getPluginManager().registerEvents(offerInventoryClickListener, this);

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
