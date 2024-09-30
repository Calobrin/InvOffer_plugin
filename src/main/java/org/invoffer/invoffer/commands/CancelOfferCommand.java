package org.invoffer.invoffer.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.invoffer.invoffer.data.DataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CancelOfferCommand implements CommandExecutor {
    private final DataManager dataManager;
    private final Map<UUID, UUID> cancelOfferTargets = new HashMap<>(); // Maps sender UUID -> target UUID

    public CancelOfferCommand(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    // Method to set the target UUID (the player who received the offer) for this player
    public void setTargetUUID(Player player, UUID targetUUID) {
        cancelOfferTargets.put(player.getUniqueId(), targetUUID);
    }
    //Method to retrieve the target UUID for this player from the HashMap
    public UUID getTargetUUID(Player player) {
        return cancelOfferTargets.get(player.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage((ChatColor.RED + "Command only usable by players."));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Incorrect command usage.");
            player.sendMessage(ChatColor.YELLOW + "Usage: /canceloffer <player>");
            return true;
        }

        String targetName = args[0];
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        if (targetUUID == null || !dataManager.hasActiveOffer(player.getUniqueId(), targetUUID)) {
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' not found or you do not have an active offer to cancel.");
            return true;
        }

        if (player.getUniqueId().equals(targetUUID)) {
            player.sendMessage(ChatColor.RED + "Invalid target - You cannot cancel offers from yourself.");
            player.sendMessage(ChatColor.YELLOW + "Usage: /canceloffer <player>");
            return true;
        }
        // Check the current status of the offer being canceled
        String currentStatus = dataManager.getOfferStatus(player.getUniqueId(), targetUUID);
        if (currentStatus == null) {
            player.sendMessage(ChatColor.RED + "An error occurred while checking the offer status for canceling an offer");
            return true;
        }
        if ("accepting".equals(currentStatus)) {
            player.sendMessage(ChatColor.RED + "Cannot cancel the offer because it is currently being accepted");
            return true;
        }

        // Store the target UUID for this player
        setTargetUUID(player, targetUUID);

        // Proceed with canceling the offer
        dataManager.updateOfferStatus(player.getUniqueId(), targetUUID, "canceling");
        dataManager.cancelOffer(player.getUniqueId(), targetUUID);
        return true;
    }
}
