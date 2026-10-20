package org.bukkit.inventory;

import org.bukkit.Material;

public class PlayerInventory {
    private ItemStack inMainHand = new ItemStack(Material.AIR, 1);
    private final ItemStack[] storageContents = new ItemStack[36];

    public ItemStack getItemInMainHand() {
        return inMainHand;
    }

    public void setItemInMainHand(ItemStack stack) {
        this.inMainHand = stack == null ? new ItemStack(Material.AIR, 1) : stack;
    }

    public ItemStack[] getStorageContents() {
        return storageContents;
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < storageContents.length) {
            storageContents[slot] = stack;
        }
    }
}
