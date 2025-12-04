package su.nightexpress.excellentcrates.dialog.cost;

import net.milkbowl.vault2.economy.Economy;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.crate.cost.entry.impl.VaultUnlockedCostEntry;
import su.nightexpress.excellentcrates.dialog.CrateDialog;
import su.nightexpress.excellentcrates.hooks.impl.VaultUnlockedHook;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class VaultUnlockedCostOptionsDialog extends CrateDialog<VaultUnlockedCostEntry> {

    private static final String INPUT_CURRENCY = "currency";
    private static final String INPUT_AMOUNT = "amount";

    private static final TextLocale TITLE = LangEntry.builder("Dialog.CostEntry.VaultUnlocked.Title").text(title("Cost Entry", "VaultUnlocked Options"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.CostEntry.VaultUnlocked.Body").dialogElement(400,
        "Specify which currency and how much of it are required to open the crate.",
        "",
        "This uses VaultUnlockedAPI with multi-currency support."
    );

    private static final TextLocale LABEL_CURRENCY = LangEntry.builder("Dialog.CostEntry.VaultUnlocked.Input.Currency").text(SOFT_YELLOW.wrap("Currency"));
    private static final TextLocale LABEL_AMOUNT = LangEntry.builder("Dialog.CostEntry.VaultUnlocked.Input.Amount").text("Amount");

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull VaultUnlockedCostEntry entry) {
        Economy economy = VaultUnlockedHook.getEconomy();
        List<WrappedSingleOptionEntry> currencyOptions = new ArrayList<>();

        if (economy != null) {
            if (economy.hasMultiCurrencySupport()) {
                currencyOptions = economy.currencies().stream()
                    .map(currency -> new WrappedSingleOptionEntry(currency, currency, entry.getCurrency().equalsIgnoreCase(currency)))
                    .collect(Collectors.toList());
            } else {
                // Single currency mode - use default currency
                String defaultCurrency = economy.getDefaultCurrency(VaultUnlockedHook.getPluginName());
                currencyOptions.add(new WrappedSingleOptionEntry(defaultCurrency, defaultCurrency, entry.getCurrency().equalsIgnoreCase(defaultCurrency)));
            }
        }

        // Fallback if economy is not available
        if (currencyOptions.isEmpty()) {
            currencyOptions.add(new WrappedSingleOptionEntry(entry.getCurrency(), entry.getCurrency(), true));
        }

        final List<WrappedSingleOptionEntry> finalCurrencyOptions = currencyOptions;
        String currentCurrency = entry.getCurrency();
        String currentAmount = entry.getAmount().toPlainString();

        return Dialogs.create(builder -> {
            builder.base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.singleOption(INPUT_CURRENCY, LABEL_CURRENCY, finalCurrencyOptions).build(),
                    DialogInputs.text(INPUT_AMOUNT, LABEL_AMOUNT).maxLength(20).initial(currentAmount).build()
                )
                .build()
            );

            builder.type(DialogTypes.multiAction(DialogButtons.ok())
                .exitAction(DialogButtons.back())
                .build()
            );

            builder.handleResponse(DialogActions.OK, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String currency = nbtHolder.getText(INPUT_CURRENCY, currentCurrency);
                String amountText = nbtHolder.getText(INPUT_AMOUNT).orElse(currentAmount);

                BigDecimal amount;
                try {
                    amount = new BigDecimal(amountText);
                    if (amount.compareTo(BigDecimal.ZERO) < 0) {
                        amount = BigDecimal.ZERO;
                    }
                } catch (NumberFormatException e) {
                    amount = BigDecimal.ZERO;
                }

                entry.setCurrency(currency);
                entry.setAmount(amount);

                user.callback();
            });
        });
    }
}

