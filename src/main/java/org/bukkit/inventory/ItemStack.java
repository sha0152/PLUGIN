package org.bukkit.inventory;

import org.bukkit.Material;

public class ItemStack implements Cloneable {
    private Material type;
    private int amount;

    public ItemStack(Material type) {
        this(type, 1);
    }

    public ItemStack(Material type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public Material getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public ItemStack clone() {
        return new ItemStack(type, amount);
    }
}
