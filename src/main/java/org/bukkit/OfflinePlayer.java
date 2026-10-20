package org.bukkit;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class OfflinePlayer {
    private final UUID uniqueId;
    private final String name;

    public OfflinePlayer(String name) {
        this.name = name;
        this.uniqueId = UUID.nameUUIDFromBytes(("offline:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }
}
