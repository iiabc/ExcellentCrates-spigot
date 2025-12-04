package su.nightexpress.excellentcrates.crate.cost.entry.impl;

import net.milkbowl.vault2.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.Placeholders;
import su.nightexpress.excellentcrates.crate.cost.entry.AbstractCostEntry;
import su.nightexpress.excellentcrates.crate.cost.type.impl.VaultUnlockedCostType;
import su.nightexpress.excellentcrates.dialog.CrateDialogs;
import su.nightexpress.excellentcrates.hooks.impl.VaultUnlockedHook;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class VaultUnlockedCostEntry extends AbstractCostEntry<VaultUnlockedCostType> {

    private String currency;
    private BigDecimal amount;

    public VaultUnlockedCostEntry(@NotNull VaultUnlockedCostType type, @NotNull String currency, double amount) {
        super(type);
        this.setCurrency(currency);
        this.setAmount(BigDecimal.valueOf(amount));
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Currency", this.currency);
        config.set(path + ".Amount", this.amount.doubleValue());
    }

    @Override
    public void openEditor(@NotNull Player player, @Nullable Runnable callback) {
        CrateDialogs.VAULT_UNLOCKED_COST_OPTIONS.ifPresent(dialog -> dialog.show(player, this, callback));
    }

    @Nullable
    private Economy getEconomy() {
        return VaultUnlockedHook.getEconomy();
    }

    @NotNull
    private String getPluginName() {
        return VaultUnlockedHook.getPluginName();
    }

    @Override
    @NotNull
    public NightItem getEditorIcon() {
        Economy economy = getEconomy();
        boolean validCurrency = economy != null && (economy.hasCurrency(this.currency) || !economy.hasMultiCurrencySupport());

        return NightItem.fromType(validCurrency ? Material.GOLD_INGOT : Material.BARRIER)
            .localized(VaultUnlockedCostType.LOCALE_EDIT_BUTTON)
            .replacement(replacer -> replacer
                .replace(Placeholders.GENERIC_ID, () -> validCurrency ? CoreLang.goodEntry(this.currency) : CoreLang.badEntry(this.currency))
                .replace(Placeholders.GENERIC_AMOUNT, () -> this.amount.compareTo(BigDecimal.ZERO) > 0 ? CoreLang.goodEntry(this.amount.toPlainString()) : CoreLang.badEntry(this.amount.toPlainString()))
                .replace(Placeholders.GENERIC_NAME, () -> {
                    if (economy == null) return this.currency;
                    if (economy.hasMultiCurrencySupport()) {
                        return this.currency;
                    }
                    return economy.getDefaultCurrency(getPluginName());
                })
            )
            .hideAllComponents();
    }

    @Override
    @NotNull
    public String format() {
        Economy economy = getEconomy();
        if (economy == null) {
            return this.amount.toPlainString() + " " + this.currency;
        }

        if (economy.hasMultiCurrencySupport()) {
            return economy.format(getPluginName(), this.amount, this.currency);
        } else {
            return economy.format(getPluginName(), this.amount);
        }
    }

    @Override
    public boolean isValid() {
        Economy economy = getEconomy();
        if (economy == null || this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (economy.hasMultiCurrencySupport()) {
            return economy.hasCurrency(this.currency);
        }

        return true;
    }

    @Override
    public int countPossibleOpenings(@NotNull Player player) {
        Economy economy = getEconomy();
        if (economy == null || this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal balance = getBalance(economy, player);
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        return balance.divide(this.amount, 0, RoundingMode.FLOOR).intValue();
    }

    @Override
    public boolean hasEnough(@NotNull Player player) {
        Economy economy = getEconomy();
        if (economy == null) {
            return false;
        }

        BigDecimal balance = getBalance(economy, player);
        return balance.compareTo(this.amount) >= 0;
    }

    @Override
    public void take(@NotNull Player player) {
        Economy economy = getEconomy();
        if (economy == null) {
            return;
        }

        String worldName = player.getWorld().getName();

        if (economy.hasMultiCurrencySupport()) {
            economy.withdraw(getPluginName(), player.getUniqueId(), worldName, this.currency, this.amount);
        } else {
            economy.withdraw(getPluginName(), player.getUniqueId(), worldName, this.amount);
        }
    }

    @Override
    public void refund(@NotNull Player player) {
        Economy economy = getEconomy();
        if (economy == null) {
            return;
        }

        String worldName = player.getWorld().getName();

        if (economy.hasMultiCurrencySupport()) {
            economy.deposit(getPluginName(), player.getUniqueId(), worldName, this.currency, this.amount);
        } else {
            economy.deposit(getPluginName(), player.getUniqueId(), worldName, this.amount);
        }
    }

    @NotNull
    private BigDecimal getBalance(@NotNull Economy economy, @NotNull Player player) {
        String worldName = player.getWorld().getName();

        if (economy.hasMultiCurrencySupport()) {
            if (economy.accountSupportsCurrency(getPluginName(), player.getUniqueId(), this.currency, worldName)) {
                return economy.balance(getPluginName(), player.getUniqueId(), worldName, this.currency);
            }
            return BigDecimal.ZERO;
        } else {
            return economy.balance(getPluginName(), player.getUniqueId(), worldName);
        }
    }

    @NotNull
    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(@NotNull String currency) {
        this.currency = currency;
    }

    @NotNull
    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(@NotNull BigDecimal amount) {
        this.amount = amount.max(BigDecimal.ZERO);
    }

    public void setAmount(double amount) {
        this.setAmount(BigDecimal.valueOf(amount));
    }

    @Override
    public String toString() {
        return "[" + "currency='" + currency + '\'' + ", amount=" + amount + ']';
    }
}

