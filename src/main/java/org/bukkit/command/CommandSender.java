package org.bukkit.command;

public interface CommandSender {
    default void sendMessage(String message) {
        System.out.println(message);
    }

    default boolean hasPermission(String permission) {
        return true;
    }
}
