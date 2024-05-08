package org.invoffer.invoffer.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;


public class OfferCommand implements CommandExecutor {
    //private final OfferManager offerManager;

    // offer <playerName> :  Sends an item(s) offer to a player online.
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
                String playerName = args[0];

                // Creates variable to store target player name from list of online players.
                Player target = Bukkit.getServer().getPlayerExact(playerName);
                if(target == null){ // Checks if the target is offline, or the argument is empty. (Incorrect_
                    player.sendMessage(ChatColor.RED + "Invalid target - You cannot target offline players.");
                    player.sendMessage(ChatColor.YELLOW + "Usage: /invoffer <player>.");
                } else if(target == player){ // Checks if you are using the command on yourself (Incorrect)
                    player.sendMessage(ChatColor.RED + "Invalid target - You cannot use offer on yourself.");
                    player.sendMessage(ChatColor.YELLOW + "Usage: /invoffer <player>.");
                } else { //
                    Inventory offerMenu = Bukkit.createInventory(player, 9, ChatColor.GOLD + "InvOffer GUI");
                    player.openInventory(offerMenu);
                    // TODO: Create code that will save inventory data to send to other player
                }
            } else { //In the off chance anything else is incorrect...
                player.sendMessage(ChatColor.RED + "Too many arguments.");
                player.sendMessage(ChatColor.YELLOW + "Usage: /invoffer <player>");
            }
        }

        return true;
    }
}
