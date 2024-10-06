package org.invoffer.invoffer.data;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;

public class InventoryManager {

    //    private boolean isInventoryEmpty(Inventory inventory) {
//        for (ItemStack itemStack : inventory.getContents()) {
//            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
//                return false;
//            }
//        }
//        return true;
//    }

    public String serializeInventory(Inventory inventory) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            // Write the inventory contents to the output stream
            oos.writeObject(inventory.getContents());
            // Convert the byte array to a Base64-encoding string
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize inventory", e);
            return null;
        }
    }

    public Inventory deserializeInventory(String base64, String title) {
        final int INVENTORY_SIZE = 9; // Fixed size for offer inventories
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
            // Read the serialized inventory data from the input stream
            ItemStack[] contents = (ItemStack[]) ois.readObject();
            // Create a new inventory and set its contents, by adding the inventory data from the database to this window.
            Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title); // Fixed size
            inventory.setContents(contents);
            return inventory;
        } catch (IOException | ClassNotFoundException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize inventory", e);
            return null;
        }
    }

    // Method to create a summary of the inventory contents of an offer for hover-able text
    public String getInventoryContentsSummary(Inventory inventory) {
        StringBuilder summary = new StringBuilder();
        for (ItemStack item: inventory.getContents()) {
            if (item != null) {
                String baseName = item.getType().toString().toLowerCase().replace("_", " ");
                String customName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                                    ? item.getItemMeta().getDisplayName()
                                    : null;
                boolean isEnchanted = item.getItemMeta() != null && item.getItemMeta().hasEnchants();
                int quantity = item.getAmount();

                if (customName != null && isEnchanted) {
                    summary.append(customName).append(" (enchanted ").append(baseName).append(")");
                } else if (isEnchanted) {
                    summary.append(baseName).append(" (enchanted)");
                } else if (customName != null) {
                    summary.append(customName).append(" (").append(baseName).append(")");
                } else {
                    summary.append(baseName);
                }

                summary.append(", x").append(quantity);
                summary.append("\n"); // Add new line after each item entry.
            }
        }
        return summary.toString().trim();
    }


}
