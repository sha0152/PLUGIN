package com.velora.smp.integration;

import com.velora.smp.VeloraCore;
import com.velora.smp.config.Settings;
import com.velora.smp.economy.EconomyService;

public final class PlaceholderApiHook {
    public PlaceholderApiHook(VeloraCore plugin, Settings settings, EconomyService economyService) {
    }

    public void tryRegister() {
        // Optional integration intentionally no-op in this environment.
    }

    public void unregister() {
        // Optional integration intentionally no-op in this environment.
    }
}
