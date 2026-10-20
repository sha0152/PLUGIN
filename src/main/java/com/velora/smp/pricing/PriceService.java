package com.velora.smp.pricing;

import com.velora.smp.config.Settings;
import com.velora.smp.util.MoneyUtil;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PriceService {
    private final Settings settings;
    private final Map<Material, BigDecimal> overrides = new HashMap<>();
    private final Map<Material, BigDecimal> categoryPrices = new HashMap<>();
    private final Set<Material> exclusions = new HashSet<>();
    private BigDecimal fallback;
    private BigDecimal safeFallback;

    public PriceService(JavaPlugin plugin, Settings settings) {
        this.settings = settings;
        load(plugin);
    }

    private void load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "prices.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        this.fallback = parse(yml.getString("defaults.fallback", settings.defaultPrice().toPlainString()), settings.defaultPrice());
        this.safeFallback = parse(yml.getString("defaults.safe-fallback", settings.safeFallbackPrice().toPlainString()), settings.safeFallbackPrice());

        ConfigurationSection overrideSection = yml.getConfigurationSection("overrides");
        if (overrideSection != null) {
            for (String key : overrideSection.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material != null && material.isItem() && !material.isAir()) {
                    overrides.put(material, parse(overrideSection.getString(key), safeFallback));
                }
            }
        }

        ConfigurationSection categories = yml.getConfigurationSection("categories");
        if (categories != null) {
            for (String categoryName : categories.getKeys(false)) {
                String path = "categories." + categoryName;
                BigDecimal categoryPrice = parse(yml.getString(path + ".price", fallback.toPlainString()), fallback);
                List<String> materials = yml.getStringList(path + ".materials");
                for (String m : materials) {
                    Material material = Material.matchMaterial(m);
                    if (material != null && material.isItem() && !material.isAir()) {
                        categoryPrices.put(material, categoryPrice);
                    }
                }
            }
        }

        List<String> configExclusions = new ArrayList<>();
        configExclusions.addAll(yml.getStringList("exclusions"));
        configExclusions.addAll(settings.sellExclusions());
        for (String value : configExclusions) {
            Material material = Material.matchMaterial(value.toUpperCase(Locale.ROOT));
            if (material != null) {
                exclusions.add(material);
            }
        }
    }

    public Optional<BigDecimal> priceFor(Material material) {
        if (material == null || !material.isItem() || material.isAir()) {
            return Optional.empty();
        }
        if (exclusions.contains(material)) {
            return Optional.empty();
        }
        BigDecimal price = overrides.get(material);
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            return Optional.of(price);
        }
        price = categoryPrices.get(material);
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            return Optional.of(price);
        }
        if (fallback.compareTo(BigDecimal.ZERO) > 0) {
            return Optional.of(fallback);
        }
        return Optional.of(safeFallback.compareTo(BigDecimal.ZERO) > 0 ? safeFallback : BigDecimal.ONE);
    }

    public BigDecimal setOverride(Material material, String amount) {
        BigDecimal price = parse(amount, safeFallback);
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        overrides.put(material, price);
        return price;
    }

    public void resetOverride(Material material) {
        overrides.remove(material);
    }

    public Map<Material, BigDecimal> listOverrides() {
        return Map.copyOf(overrides);
    }

    public boolean allValidMaterialsHavePrice() {
        for (Material material : Material.values()) {
            if (material.isItem() && !material.isAir() && !exclusions.contains(material)) {
                Optional<BigDecimal> p = priceFor(material);
                if (p.isEmpty() || p.get().compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private BigDecimal parse(String input, BigDecimal fallbackValue) {
        try {
            BigDecimal parsed = MoneyUtil.parseNonNegative(input, 2);
            if (parsed.compareTo(BigDecimal.ZERO) == 0) {
                return fallbackValue;
            }
            return parsed;
        } catch (Exception ignored) {
            return fallbackValue;
        }
    }
}
