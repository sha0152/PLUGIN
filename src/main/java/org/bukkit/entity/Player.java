package org.bukkit.entity;

import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.PlayerInventory;

public class Player extends OfflinePlayer implements CommandSender {
    private final PlayerInventory inventory = new PlayerInventory();

    public Player(String name) {
        super(name);
    }

    public UUID getUniqueId() {
        return super.getUniqueId();
    }

    public PlayerInventory getInventory() {
        return inventory;
    }
}
