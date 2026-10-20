package org.bukkit;

import java.util.Locale;

public enum Material {
    AIR(false, true),
    DIRT(true, false),
    COBBLESTONE(true, false),
    STONE(true, false),
    COAL(true, false),
    IRON_INGOT(true, false),
    GOLD_INGOT(true, false),
    DIAMOND(true, false),
    NETHERITE_INGOT(true, false),
    EMERALD(true, false),
    ANCIENT_DEBRIS(true, false),
    NETHERITE_SCRAP(true, false),
    WHEAT(true, false),
    CARROT(true, false),
    POTATO(true, false),
    BEETROOT(true, false),
    MELON_SLICE(true, false),
    ROTTEN_FLESH(true, false),
    BONE(true, false),
    STRING(true, false),
    GUNPOWDER(true, false),
    ENDER_PEARL(true, false),
    BLAZE_ROD(true, false),
    GHAST_TEAR(true, false),
    CHORUS_FRUIT(true, false),
    SHULKER_SHELL(true, false);

    private final boolean item;
    private final boolean air;

    Material(boolean item, boolean air) {
        this.item = item;
        this.air = air;
    }

    public boolean isItem() {
        return item;
    }

    public boolean isAir() {
        return air;
    }

    public static Material matchMaterial(String name) {
        if (name == null) return null;
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return null;
        }
    }
}
