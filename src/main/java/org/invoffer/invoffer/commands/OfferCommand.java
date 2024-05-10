package org.invoffer.invoffer.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.invoffer.invoffer.data.DataManager;


public class OfferCommand implements CommandExecutor {

    private final DataManager dataManager;

    public OfferCommand(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    // /offer <playerName> :  Sends an item(s) offer to a player online.
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
        // Checks if sender is player or not - only players may use this command.
        if (sender instanceof Player player) {
            // Check if the command has the correct number of arguments
            // Checks if no argument is used (Incorrect, it needs a target play)
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Missing target. ");
                player.sendMessage(ChatColor.YELLOW + "Usage: /invoffer <player>");
                return true;
            }
            // Checks if only ONE argument is used (Correct)
            else if (args.length == 1) {
                String targetPlayerName = args[0];
                // Creates a variable to store target player name from list of online players.
                Player target = Bukkit.getServer().getPlayerExact(targetPlayerName);
                if(target == null){ // Checks if the target is offline, or the argument is empty. (Incorrect_
                    player.sendMessage(ChatColor.RED + "Invalid target - You cannot target offline players.");
                    player.sendMessage(ChatColor.YELLOW + "Usage: /invoffer <player>.");
                } else if(target.equals(player)){ // Checks if you are using the command on yourself (Incorrect)
                    player.sendMessage(ChatColor.RED + "Invalid target - You cannot use offer on yourself.");
                    player.sendMessage(ChatColor.YELLOW + "Usage: /invoffer <player>.");
                } else { //
                    // Checks if you already have an active offer to player. (should.. at least..)
                    if (dataManager.hasActiveOffer(player.getUniqueId(), target.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You already have an active offer.");
                        return true;
                    }else {
                        player.sendMessage("The else was called for hasActiveOffer.");
                    }

                    Inventory offerMenu = Bukkit.createInventory(player, 9, ChatColor.GOLD + "InvOffer GUI");
                    player.openInventory(offerMenu);
                    dataManager.saveActiveOffers(player.getUniqueId(), target.getUniqueId());
                    System.out.println("It SHOULD have saved the offer. Did it?");
                    // TODO: Incorporate the saveActiveOffers method from DataManager to keep track of active offers.

                }
            } else { //In the off chance anything else is incorrect...
                player.sendMessage(ChatColor.RED + "Too many arguments.");
                player.sendMessage(ChatColor.YELLOW + "Usage: /invoffer <player>");
            }
        }
        else {
            System.out.println("Command only usable by players");
        }

        return true;
    }
}
