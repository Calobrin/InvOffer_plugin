package org.invoffer.invoffer.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.invoffer.invoffer.data.DataManager;

import java.util.UUID;

public class AcceptOfferCommand implements CommandExecutor {

    private final DataManager dataManager;

    public AcceptOfferCommand(DataManager dataManager) {
        this.dataManager = dataManager;
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

        Player targetPlayer = player.getServer().getPlayer(senderName);
        UUID senderUUID = null;
            if (targetPlayer != null) {
                senderUUID = targetPlayer.getUniqueId();
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
        // Proceed with accepting the offer
        dataManager.acceptOffer(player.getUniqueId(), senderUUID);
        return true;
    }

}