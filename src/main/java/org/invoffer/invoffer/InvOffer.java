package org.invoffer.invoffer;

import org.bukkit.plugin.java.JavaPlugin;
import org.invoffer.invoffer.commands.AcceptOfferCommand;
import org.invoffer.invoffer.commands.CancelOfferCommand;
import org.invoffer.invoffer.commands.ListOffersCommand;
import org.invoffer.invoffer.commands.OfferCommand;
import org.invoffer.invoffer.data.OfferManager;
import org.invoffer.invoffer.listeners.OfferAcceptAndCancelInventoryCloseListener;
import org.invoffer.invoffer.listeners.OfferInventoryClickListener;
import org.invoffer.invoffer.listeners.OfferInventoryCloseListener;

public final class InvOffer extends JavaPlugin {

    private static InvOffer instance;
    private static OfferCommand offerCommand;
    private OfferManager offerManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        getLogger().info("InvOffer has started! This plugin is still in development...");

        offerManager = new OfferManager();

        // Register the OfferCommand, AcceptOfferCommand, and ListOffersCommand
        offerCommand = new OfferCommand(offerManager);
        AcceptOfferCommand acceptOfferCommand = new AcceptOfferCommand(offerManager);
        CancelOfferCommand cancelOfferCommand = new CancelOfferCommand(offerManager);
        ListOffersCommand listOffersCommand = new ListOffersCommand(offerManager);

        getCommand("invoffer").setExecutor(offerCommand);
        getCommand("acceptOffer").setExecutor(acceptOfferCommand);
        getCommand("cancelOffer").setExecutor(cancelOfferCommand);
        getCommand("listOffers").setExecutor(listOffersCommand);

        // Register the OfferInventoryCloseListener
        OfferInventoryCloseListener offerInventoryCloseListener = new OfferInventoryCloseListener(offerManager, offerCommand);
        getServer().getPluginManager().registerEvents(offerInventoryCloseListener, this);

        // Register the OfferAcceptAndCancelInventoryCloseListener for accepting offers
        OfferAcceptAndCancelInventoryCloseListener offerAcceptAndCancelInventoryCloseListener = new OfferAcceptAndCancelInventoryCloseListener(offerManager, acceptOfferCommand, cancelOfferCommand);
        getServer().getPluginManager().registerEvents(offerAcceptAndCancelInventoryCloseListener, this);

        OfferInventoryClickListener offerInventoryClickListener = new OfferInventoryClickListener();
        getServer().getPluginManager().registerEvents(offerInventoryClickListener, this);

        offerManager.resetAcceptingStatus();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        offerManager.closeConnection();
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
