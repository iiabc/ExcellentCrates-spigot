package su.nightexpress.excellentcrates.crate.cost.type.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.excellentcrates.api.cost.CostEntry;
import su.nightexpress.excellentcrates.crate.cost.CostTypeId;
import su.nightexpress.excellentcrates.crate.cost.entry.impl.VaultUnlockedCostEntry;
import su.nightexpress.excellentcrates.crate.cost.type.AbstractCostType;
import su.nightexpress.excellentcrates.hooks.impl.VaultUnlockedHook;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import static su.nightexpress.excellentcrates.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class VaultUnlockedCostType extends AbstractCostType implements LangContainer {

    public static final TextLocale LOCALE_NAME = LangEntry.builder("Costs.VaultUnlocked.Name").text(GREEN.wrap("[VU]") + " " + WHITE.wrap("VaultUnlocked Currency"));

    public static final IconLocale LOCALE_EDIT_BUTTON = LangEntry.iconBuilder("Costs.VaultUnlocked.EditButton")
        .rawName(YELLOW.and(BOLD).wrap("VaultUnlocked Cost") + GRAY.wrap(" - ") + WHITE.wrap(GENERIC_NAME))
        .rawLore(ITALIC.and(DARK_GRAY).wrap("Press " + SOFT_RED.wrap(TagWrappers.KEY.apply("key.drop")) + " key to delete.")).br()
        .appendCurrent("Currency ID", GENERIC_ID)
        .appendCurrent("Amount", GENERIC_AMOUNT).br()
        .appendClick("Click to edit")
        .build();

    public VaultUnlockedCostType(@NotNull CratesPlugin plugin) {
        super(CostTypeId.VAULT_UNLOCKED);
        plugin.injectLang(this);
    }

    @Override
    public boolean isAvailable() {
        return VaultUnlockedHook.isAvailable();
    }

    @Override
    @NotNull
    public String getName() {
        return LOCALE_NAME.text();
    }

    @Override
    @NotNull
    public CostEntry load(@NotNull FileConfig config, @NotNull String path) {
        String currency = ConfigValue.create(path + ".Currency", "default").read(config);
        double amount = ConfigValue.create(path + ".Amount", 0D).read(config);

        return new VaultUnlockedCostEntry(this, currency, amount);
    }

    @Override
    @NotNull
    public VaultUnlockedCostEntry createEmpty() {
        return new VaultUnlockedCostEntry(this, "default", 0);
    }
}

