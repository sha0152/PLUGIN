package com.velora.smp;

import com.velora.smp.command.BalanceCommand;
import com.velora.smp.command.BaltopCommand;
import com.velora.smp.command.EcoCommand;
import com.velora.smp.command.ModuleAwareInfoCommand;
import com.velora.smp.command.PayCommand;
import com.velora.smp.command.PriceCommand;
import com.velora.smp.command.PricesCommand;
import com.velora.smp.command.SellCommand;
import com.velora.smp.command.VeloraCommand;
import com.velora.smp.command.WorthCommand;
import com.velora.smp.config.Settings;
import com.velora.smp.database.DatabaseManager;
import com.velora.smp.economy.EconomyService;
import com.velora.smp.integration.PlaceholderApiHook;
import com.velora.smp.pricing.PriceService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class VeloraCore extends JavaPlugin {

    private Settings settings;
    private DatabaseManager databaseManager;
    private EconomyService economyService;
    private PriceService priceService;
    private PlaceholderApiHook placeholderApiHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");
        saveResourceIfMissing("prices.yml");
        saveResourceIfMissing("shops.yml");
        saveResourceIfMissing("auction.yml");
        saveResourceIfMissing("orders.yml");
        saveResourceIfMissing("crates.yml");
        saveResourceIfMissing("rewards.yml");
        saveResourceIfMissing("quests.yml");
        saveResourceIfMissing("ranks.yml");
        saveResourceIfMissing("menus.yml");
        saveResourceIfMissing("database.yml");

        this.settings = Settings.load(this);
        this.databaseManager = new DatabaseManager(this, settings);
        databaseManager.start();

        this.economyService = new EconomyService(this, settings, databaseManager);
        this.priceService = new PriceService(this, settings);

        registerCommands();

        this.placeholderApiHook = new PlaceholderApiHook(this, settings, economyService);
        placeholderApiHook.tryRegister();

        getLogger().info("VeloraCore enabled.");
    }

    @Override
    public void onDisable() {
        if (placeholderApiHook != null) {
            placeholderApiHook.unregister();
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    public void reloadSafely() {
        reloadConfig();
        this.settings = Settings.load(this);
        this.priceService = new PriceService(this, settings);
        this.economyService.reloadSettings(settings);
        this.databaseManager.reloadSettings(settings);
        if (placeholderApiHook != null) {
            placeholderApiHook.unregister();
        }
        this.placeholderApiHook = new PlaceholderApiHook(this, settings, economyService);
        placeholderApiHook.tryRegister();
    }

    private void registerCommands() {
        BalanceCommand balanceCommand = new BalanceCommand(this);
        PayCommand payCommand = new PayCommand(this);
        EcoCommand ecoCommand = new EcoCommand(this);
        BaltopCommand baltopCommand = new BaltopCommand(this);
        SellCommand sellCommand = new SellCommand(this);
        WorthCommand worthCommand = new WorthCommand(this);
        PricesCommand pricesCommand = new PricesCommand(this);
        PriceCommand priceCommand = new PriceCommand(this);
        VeloraCommand veloraCommand = new VeloraCommand(this);

        register("bal", balanceCommand);
        register("pay", payCommand);
        register("eco", ecoCommand);
        register("baltop", baltopCommand);
        register("sell", sellCommand);
        register("worth", worthCommand);
        register("prices", pricesCommand);
        register("price", priceCommand);
        register("velora", veloraCommand);

        register("ah", new ModuleAwareInfoCommand(this, "auction", "Auction House module is available but minimal in this build."));
        register("orders", new ModuleAwareInfoCommand(this, "orders", "Buy Orders module is available but minimal in this build."));
        register("crates", new ModuleAwareInfoCommand(this, "crates", "Crates module is available but minimal in this build."));
        register("rank", new ModuleAwareInfoCommand(this, "ranks", "Ranks module is available but minimal in this build."));
        register("playtime", new ModuleAwareInfoCommand(this, "playtime-rewards", "Playtime module is available but minimal in this build."));
        register("quests", new ModuleAwareInfoCommand(this, "quests", "Quests module is available but minimal in this build."));
        register("daily", new ModuleAwareInfoCommand(this, "daily-rewards", "Daily rewards module is available but minimal in this build."));
        register("bounty", new ModuleAwareInfoCommand(this, "bounties", "Bounty module is available but minimal in this build."));
        register("bounties", new ModuleAwareInfoCommand(this, "bounties", "Bounty module is available but minimal in this build."));
        register("stats", new ModuleAwareInfoCommand(this, "pvp-stats", "PvP stats module is available but minimal in this build."));
    }

    private void register(String command, org.bukkit.command.TabExecutor executor) {
        PluginCommand pluginCommand = getCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(executor);
            pluginCommand.setTabCompleter(executor);
        } else {
            getLogger().warning("Missing command declaration for: " + command);
        }
    }

    private void saveResourceIfMissing(String resource) {
        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }
        if (!new java.io.File(getDataFolder(), resource).exists()) {
            saveResource(resource, false);
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public PriceService getPriceService() {
        return priceService;
    }
}
