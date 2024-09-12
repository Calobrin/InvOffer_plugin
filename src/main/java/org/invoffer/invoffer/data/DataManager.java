package org.invoffer.invoffer.data;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DataManager {

    private static final String DATABASE_URL = "jdbc:sqlite:plugins/InvOffer/invoffer.db";
    private final Gson gson = new Gson();
    private Connection connection;


    public DataManager() {
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
                "offer_inventory TEXT NOT NULL)";
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

    private boolean isInventoryEmpty(Inventory inventory) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                return false;
            }
        }
        return true;
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
        String insertSQL = "INSERT INTO offers (sender_uuid, target_uuid, offer_inventory) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSQL)) {
            statement.setString(1, senderUUID.toString());
            statement.setString(2, targetUUID.toString());
            statement.setString(3, serializeInventory(offerInventory)); // Converts inventory data to string
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Exception occurred while saving pending offer", e);
        }
    }

    public void updatePendingOffer(UUID senderUUID, UUID targetUUID, Inventory offerInventory) {
        String updateSQL = "UPDATE offers SET offer_inventory = ? WHERE sender_uuid = ? AND target_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(updateSQL)) {
            statement.setString(1, serializeInventory(offerInventory));
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
                targetPlayer.sendMessage(ChatColor.YELLOW + "You have received an InvOffer from " + ChatColor.WHITE + senderName + ChatColor.YELLOW + ".");
                targetPlayer.sendMessage(ChatColor.YELLOW + "You can accept this offer by typing " + ChatColor.WHITE + "/acceptOffer " + senderName + ChatColor.YELLOW + ".");
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
                Inventory inventory = deserializeInventory(serializedInventory);

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


    public String serializeInventory(Inventory inventory) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeObject(inventory.getContents());
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize inventory", e);
            return null;
        }
    }

    public Inventory deserializeInventory(String base64) {
        final int INVENTORY_SIZE = 9; // Fixed size for offer inventories
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
            ItemStack[] contents = (ItemStack[]) ois.readObject();
            Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, ChatColor.GOLD + "InvOffer GUI: Accept"); // Fixed size
            inventory.setContents(contents);
            return inventory;
        } catch (IOException | ClassNotFoundException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize inventory", e);
            return null;
        }
    }



}
