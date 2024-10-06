package org.invoffer.invoffer.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.invoffer.invoffer.data.OfferManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AcceptOfferCommand implements CommandExecutor {

    private final OfferManager offerManager;
    private final Map<UUID, UUID> playerOffers = new HashMap<>();

    public AcceptOfferCommand(OfferManager offerManager) {
        this.offerManager = offerManager;
    }
    public void setSenderUUID(Player player, UUID senderUUID) {
        playerOffers.put(player.getUniqueId(), senderUUID);
    }

    public UUID getSenderUUID(Player player){
        return playerOffers.get(player.getUniqueId());
    }
    /*
    For the sake of my own mentality:
    PLAYER is the person RUNNING this command.
    SENDER is the person they received the offer from.
    */


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
        UUID senderUUID = Bukkit.getOfflinePlayer(senderName).getUniqueId();

        if (senderUUID == null || !offerManager.hasActiveOffer(senderUUID, player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Player '" + senderName + "' not found or you do not have an active offer from them.");
            return true;
        }

        if (player.getUniqueId().equals(senderUUID)) {
            player.sendMessage(ChatColor.RED + "Invalid target - You cannot accept offers from yourself.");
            player.sendMessage(ChatColor.YELLOW + "Usage: /acceptoffer <player>");
            return true;
        }
        // Check the status of an offer - you cannot accept an offer that has been canceled.
        String currentStatus = offerManager.getOfferStatus(senderUUID, player.getUniqueId());

        if (currentStatus == null) {
            player.sendMessage(ChatColor.RED + "An error occurred while checking the offer status for accepting an offer");
            return true;
        }

        if (!"pending".equals(currentStatus)) {
            player.sendMessage(ChatColor.RED + "This offer cannot be accepted, it is either no longer pending, or is being canceled");
            return true;
        }
        // Store the senderUUID associated with this player
        setSenderUUID(player, senderUUID);

        // Proceed with accepting the offer
        offerManager.updateOfferStatus(senderUUID,player.getUniqueId(), "accepting");
        offerManager.acceptOffer(player.getUniqueId(), senderUUID);
        return true;
    }

}