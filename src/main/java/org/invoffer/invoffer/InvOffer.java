package org.invoffer.invoffer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.invoffer.invoffer.commands.OfferCommand;

public final class InvOffer extends JavaPlugin {
    //private OfferManager offerManager;

    @Override
    public void onEnable() {
        //offerManager = new OfferManager();
        // Plugin startup logic
        System.out.println("InvOffer has started!");

        getCommand("invOffer").setExecutor(new OfferCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("InvOffer has shut down.");
    }
}
