package org.invoffer.invoffer;

import org.bukkit.plugin.java.JavaPlugin;
import org.invoffer.invoffer.commands.AcceptOfferCommand;
import org.invoffer.invoffer.commands.OfferCommand;
import org.invoffer.invoffer.data.DataManager;
import org.invoffer.invoffer.listeners.OfferAcceptInventoryCloseListener;
import org.invoffer.invoffer.listeners.OfferInventoryClickListener;
import org.invoffer.invoffer.listeners.OfferInventoryCloseListener;

public final class InvOffer extends JavaPlugin {

    private static InvOffer instance;
    private static OfferCommand offerCommand;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        System.out.println("InvOffer has started! This plugin is still in development...");

        dataManager = new DataManager();

        // Register the OfferCommand
        offerCommand = new OfferCommand(dataManager);
        AcceptOfferCommand acceptOfferCommand = new AcceptOfferCommand(dataManager);
        getCommand("invOffer").setExecutor(offerCommand);
        getCommand("acceptOffer").setExecutor(acceptOfferCommand);

        // Register the OfferInventoryCloseListener
        OfferInventoryCloseListener offerInventoryCloseListener = offerCommand.getOfferInventoryCloseListener();
        getServer().getPluginManager().registerEvents(offerInventoryCloseListener, this);

        // Register the OfferAcceptInventoryCloseListener for accepting offers
        OfferAcceptInventoryCloseListener acceptInventoryCloseListener = acceptOfferCommand.getOfferAcceptInventoryCloseListener();
        getServer().getPluginManager().registerEvents(acceptInventoryCloseListener, this);

        OfferInventoryClickListener offerInventoryClickListener = new OfferInventoryClickListener();
        getServer().getPluginManager().registerEvents(offerInventoryClickListener, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        dataManager.closeConnection();
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
