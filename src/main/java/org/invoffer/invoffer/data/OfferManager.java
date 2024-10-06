package org.invoffer.invoffer.data;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
//import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.*;

import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

public class OfferManager {

    private static final String DATABASE_URL = "jdbc:sqlite:plugins/InvOffer/invoffer.db";
    private static final String ACCEPT_TITLE = ChatColor.GOLD + "InvOffer GUI: Accept";
    private static final String CANCEL_TITLE = ChatColor.GOLD + "InvOffer GUI: Cancel";
    private final InventoryManager inventoryManager = new InventoryManager();
    private Connection connection;


    public OfferManager() {
        try {
            connect();
            initializeDatabase();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to initialize database connection", e);
        }
    }

    public void connect() throws SQLException {
        File pluginFolder = new File("plugins/InvOffer");
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs(); // Create the directory if it doesn't exist
        }

        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DATABASE_URL);
                Bukkit.getLogger().info("Database connection established.");
            } catch (ClassNotFoundException e) {
                Bukkit.getLogger().log(Level.SEVERE, "SQLite JDBC driver not found!", e);
            }
        }
    }


    private void initializeDatabase() throws SQLException {
        Statement statement = connection.createStatement();
        String createTableSQL = "CREATE TABLE IF NOT EXISTS offers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender_uuid TEXT NOT NULL, " +
                "target_uuid TEXT NOT NULL, " +
                "offer_inventory TEXT NOT NULL," +
                "offer_status TEXT NOT NULL DEFAULT 'pending')";
        statement.execute(createTableSQL);
        statement.close();
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }

    public boolean hasActiveOffer(UUID senderUUID, UUID targetUUID) {
        String query = "SELECT COUNT(*) FROM offers WHERE sender_uuid = ? AND target_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, senderUUID.toString());
            statement.setString(2, targetUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while checking for active offer", e);
        }
        return false;
    }

    public void savePendingOffer(UUID senderUUID, UUID targetUUID, Inventory offerInventory) {
        if (hasActiveOffer(senderUUID, targetUUID)) {
            // If a pending offer already exists, return without adding and saving new entry to file.
            return;
        }
        String insertSQL = "INSERT INTO offers (sender_uuid, target_uuid, offer_inventory, offer_status) VALUES (?, ?, ?, 'pending')";
        try (PreparedStatement statement = connection.prepareStatement(insertSQL)) {
            statement.setString(1, senderUUID.toString());
            statement.setString(2, targetUUID.toString());
            statement.setString(3, inventoryManager.serializeInventory(offerInventory)); // Converts inventory data to string
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while saving pending offer", e);
        }
    }

    public void updatePendingOffer(UUID senderUUID, UUID targetUUID, Inventory offerInventory) {
        String updateSQL = "UPDATE offers SET offer_inventory = ? WHERE sender_uuid = ? AND target_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(updateSQL)) {
            statement.setString(1, inventoryManager.serializeInventory(offerInventory));
            statement.setString(2, senderUUID.toString());
            statement.setString(3, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while updating pending offer", e);
        }
    }

    public void resolvePendingOffer(UUID senderUUID, UUID targetUUID) {
        String deleteSQL = "DELETE FROM offers WHERE sender_uuid = ? AND target_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteSQL)) {
            statement.setString(1, senderUUID.toString());
            statement.setString(2, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while resolving pending offer", e);
        }
    }

    public void processPendingOffer(UUID senderUUID, UUID targetUUID, Inventory offerInventory) {
        // Check if the target player is online
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            String senderName = Bukkit.getOfflinePlayer(senderUUID).getName();
            if (senderName != null) {
                // Get the summarized inventory contents
                String inventorySummary = inventoryManager.getInventoryContentsSummary(offerInventory);
                // Create the initial message
                TextComponent message = new TextComponent(ChatColor.YELLOW + "You have received an InvOffer from " + ChatColor.WHITE + senderName + ChatColor.YELLOW + ".\n" +
                        "You can accept this offer by typing or clicking: ");
                // Create the clickable command text
                TextComponent clickableText = new TextComponent(ChatColor.GREEN + "/acceptoffer " + senderName);
                // Set the click event for the clickable text
                clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptoffer " + senderName));
                // Append the clickable text to the message
                message.addExtra(clickableText);
                // Add hover event to show the inventory summary
                BaseComponent[] hoverText = new ComponentBuilder(inventorySummary).create();
                clickableText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

                // Send the message to the target player
                targetPlayer.spigot().sendMessage(message);
            }
        } else {
            // Notify the sender that the target player is offline
            Player senderPlayer = Bukkit.getPlayer(senderUUID);
            if (senderPlayer != null && senderPlayer.isOnline()) {
                senderPlayer.sendMessage(ChatColor.RED + "Your target player is offline, you cannot send offers to offline players.");
            }
        }
    }

    public void acceptOffer(UUID playerUUID, UUID senderUUID) {
        String selectSQL = "SELECT offer_inventory FROM offers WHERE sender_uuid = ? AND target_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            statement.setString(1, senderUUID.toString());
            statement.setString(2, playerUUID.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String serializedInventory = resultSet.getString("offer_inventory");

                // Create a new inventory with the fixed size of 9
                Inventory inventory = inventoryManager.deserializeInventory(serializedInventory, ACCEPT_TITLE);

                if (inventory != null) { // Check if inventory is not null
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null) {
                        player.openInventory(inventory);
                    } else {
                        Bukkit.getLogger().warning("Player is not found.");
                    }
                } else {
                    Bukkit.getLogger().warning("Deserialized inventory is null.");
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while accepting offer", e);
        }
    }

    public void cancelOffer (UUID senderUUID, UUID targetUUID) {
        String selectSQL = "SELECT offer_inventory FROM offers WHERE sender_uuid = ? AND target_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            statement.setString(1, senderUUID.toString());
            statement.setString(2, targetUUID.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String serializedInventory = resultSet.getString("offer_inventory");

                Inventory inventory = inventoryManager.deserializeInventory(serializedInventory, CANCEL_TITLE);

                if (inventory != null) {
                    Player player = Bukkit.getPlayer(senderUUID);
                    if (player != null) {
                        player.openInventory(inventory);
                    } else {
                        Bukkit.getLogger().warning("Player is not found");
                    }
                } else {
                    Bukkit.getLogger().warning("Deserialized inventory is null.");
                }
            }


        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while canceling the offer", e);
        }
    }

    public void updateOfferStatus(UUID senderUUID, UUID targetUUID, String status) {
        String updateSQL = "UPDATE offers SET offer_status = ? WHERE sender_uuid = ? AND target_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(updateSQL)) {
            statement.setString(1, status);
            statement.setString(2, senderUUID.toString());
            statement.setString(3, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while updating offer status", e);
        }
    }

    public String getOfferStatus(UUID senderUUID, UUID targetUUID) {
        String status = null;
        String query = "Select offer_status FROM offers WHERE sender_uuid = ? AND target_uuid = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, senderUUID.toString());
            statement.setString(2, targetUUID.toString());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                status = resultSet.getString("offer_status");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while retrieving offer status", e);
        }

        return status;
    }
    public void resetAcceptingStatus() {
        String updateSQL = "UPDATE offers SET offer_status = 'pending' WHERE offer_status = 'accepting'";
        try (PreparedStatement statement = connection.prepareStatement(updateSQL)) {
            int rowsUpdated = statement.executeUpdate();
            Bukkit.getLogger().info(rowsUpdated + "Offers have been reset from 'accepting' to 'pending'.");
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while resetting accepting offers to pending.");
        }
    }

    public List<String> getOffersReceivedByPlayer(UUID playerUUID) {
        List<String> senderNames = new ArrayList<>();
        String query = "SELECT sender_uuid FROM offers WHERE target_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                UUID senderUUID = UUID.fromString(resultSet.getString("sender_uuid"));
                String senderName = Bukkit.getOfflinePlayer(senderUUID).getName();
                if (senderName != null) {
                    senderNames.add(senderName);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE,"SQL Exception occurred while getting list of received offers", e );
        }
        return senderNames;
    }

    public List<String> getOffersSentByPlayer(UUID playerUUID) {
        List<String> targetNames = new ArrayList<>();
        String query = "SELECT target_uuid FROM offers WHERE sender_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                UUID targetUUID = UUID.fromString(resultSet.getString("target_uuid"));
                String targetName = Bukkit.getOfflinePlayer(targetUUID).getName();
                if (targetName != null) {
                    targetNames.add(targetName);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while getting list of sent offers.", e);
        }
        return targetNames;
    }



}
