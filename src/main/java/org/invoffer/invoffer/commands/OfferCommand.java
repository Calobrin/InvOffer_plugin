package org.invoffer.invoffer.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.invoffer.invoffer.data.DataManager;
import org.invoffer.invoffer.listeners.OfferInventoryCloseListener;

import java.util.UUID;


public class OfferCommand implements CommandExecutor {

    private final DataManager dataManager;
    private final OfferInventoryCloseListener offerInventoryCloseListener;
    private UUID targetUUID;


    public OfferCommand(DataManager dataManager) {
        this.dataManager = dataManager;
        this.offerInventoryCloseListener = new OfferInventoryCloseListener(dataManager,this);
    }
    public OfferInventoryCloseListener getOfferInventoryCloseListener() {
        return offerInventoryCloseListener;
    }

    public UUID getTargetUUID() {
        return targetUUID;
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
                } else {
                    this.targetUUID = target.getUniqueId();
                    // Checks if you already have an active offer to player.
                    if (dataManager.hasActiveOffer(player.getUniqueId(), target.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You already have an active offer to " + ChatColor.WHITE + target.getName() + ChatColor.RED+ "." );
                        player.sendMessage(ChatColor.YELLOW + "You may only have one active offer for one player each.");
                        return true;
                    }
                    // Create the offer inventory window
                    Inventory offerMenu = Bukkit.createInventory(player, 9, ChatColor.GOLD + "InvOffer GUI: Send");
                    player.openInventory(offerMenu);
                }
            } else { //In the off chance anything else is incorrect...
                player.sendMessage(ChatColor.RED + "Too many arguments.");
                player.sendMessage(ChatColor.YELLOW + "Usage: /invoffer <player>");
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "Command only usable by players");
        }

        return true;
    }
}
