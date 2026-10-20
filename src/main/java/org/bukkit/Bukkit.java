package org.bukkit;

import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public final class Bukkit {
    private static final BukkitScheduler SCHEDULER = new BukkitScheduler();

    private Bukkit() {}

    public static Collection<Player> getOnlinePlayers() {
        return new ArrayList<>();
    }

    public static Player getPlayerExact(String name) {
        return null;
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        return new OfflinePlayer(name);
    }

    public static BukkitScheduler getScheduler() {
        return SCHEDULER;
    }
}
