package com.velora.smp;

import com.velora.smp.config.Settings;
import com.velora.smp.pricing.PriceService;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PriceServicePolicyTest {

    @Test
    void defaultFallbackWorks() throws Exception {
        File data = Files.createTempDirectory("velora-prices-test").toFile();
        File prices = new File(data, "prices.yml");
        Files.writeString(prices.toPath(), "defaults:\n  fallback: \"5.00\"\n  safe-fallback: \"1.00\"\noverrides: {}\ncategories: {}\nexclusions: []\n");

        TestPlugin plugin = new TestPlugin(data);
        Settings settings = new Settings("Velora SMP", "<gold>Velora</gold>", Set.of("selling"), 2,
                new BigDecimal("1000.00"), false, new BigDecimal("8.00"), new BigDecimal("1.00"), Set.of(), false, false,
                new BigDecimal("0.01"), new BigDecimal("1000000.00"), "velora.db", "backups", 10, 60);

        PriceService service = new PriceService(plugin, settings);
        Assertions.assertTrue(service.priceFor(Material.DIRT).isPresent());
        Assertions.assertEquals("5.00", service.priceFor(Material.DIRT).orElseThrow().toPlainString());
    }

    private static class TestPlugin extends JavaPlugin {
        private final File dataFolder;

        private TestPlugin(File dataFolder) {
            this.dataFolder = dataFolder;
        }

        @Override
        public File getDataFolder() {
            return dataFolder;
        }
    }
}
