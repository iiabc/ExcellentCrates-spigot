package su.nightexpress.excellentcrates.hooks.impl;

import net.milkbowl.vault2.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.hooks.HookId;
import su.nightexpress.nightcore.util.Plugins;

import java.util.logging.Level;

public class VaultUnlockedHook {

    private static Economy economy;
    private static final String PLUGIN_NAME = "ExcellentCrates";

    public static void setup(@NotNull CratesPlugin plugin) {
        // Try to get the economy service from services manager
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return; // Service not available
        }

        economy = rsp.getProvider();
        if (economy == null) {
            return;
        }

        if (!economy.isEnabled()) {
            economy = null;
            return;
        }

        // Check if this is VaultUnlocked by checking for multi-currency support
        // VaultUnlocked typically supports multi-currency
        plugin.info("Successfully hooked into economy: " + economy.getName() + " (Multi-currency: " + economy.hasMultiCurrencySupport() + ")");
    }

    public static void shutdown() {
        economy = null;
    }

    public static boolean isAvailable() {
        return economy != null && economy.isEnabled();
    }

    @Nullable
    public static Economy getEconomy() {
        return economy;
    }

    @NotNull
    public static String getPluginName() {
        return PLUGIN_NAME;
    }
}

