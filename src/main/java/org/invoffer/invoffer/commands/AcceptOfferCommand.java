package org.invoffer.invoffer.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.invoffer.invoffer.data.DataManager;
import org.invoffer.invoffer.listeners.OfferAcceptInventoryCloseListener;

import java.util.UUID;

public class AcceptOfferCommand implements CommandExecutor {

    private final DataManager dataManager;
    private final OfferAcceptInventoryCloseListener offerAcceptInventoryCloseListener;
    private UUID senderUUID;

    public AcceptOfferCommand(DataManager dataManager) {
        this.dataManager = dataManager;
        this.offerAcceptInventoryCloseListener = new OfferAcceptInventoryCloseListener(dataManager, this);
    }
    public OfferAcceptInventoryCloseListener getOfferAcceptInventoryCloseListener() {
        return offerAcceptInventoryCloseListener;
    }

    public UUID getSenderUUID(){
        return senderUUID;
    }
    /*
    For the sake of my own mentality:
    PLAYER is the person RUNNING this command.
    SENDER is the person they received the offer from.
    */


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Checks if the sender of command is a player or not - only players can use this command.
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Command only usable by players");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Incorrect command usage.");
            player.sendMessage(ChatColor.YELLOW + "Usage: /acceptoffer <player>");
            return true;
        }
        String senderName = args[0];
        UUID senderUUID = dataManager.getPlayerUUIDFromStorage(senderName);
        if (senderUUID == null) {
            player.sendMessage(ChatColor.RED + "Player '" + senderName + "' not found.");
            return true;
        }

        UUID playerUUID = player.getUniqueId();

        if (player.getUniqueId().equals(senderUUID)) {
            player.sendMessage(ChatColor.RED + "Invalid target - You cannot accept offers from yourself. ");
            player.sendMessage(ChatColor.YELLOW + "Usage: /acceptoffer <player>");
            return true;
        }
        if (!dataManager.hasActiveOffer(senderUUID, playerUUID)) {
            player.sendMessage(ChatColor.RED + "You do not have an active offer from " + ChatColor.WHITE + senderName + ChatColor.RED + ".");
            return true;
        }

        this.senderUUID = senderUUID;

        // Proceed with accepting the offer
        dataManager.acceptOffer(player.getUniqueId(), senderUUID);
        return true;
    }

}