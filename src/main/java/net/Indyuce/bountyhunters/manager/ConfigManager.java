package net.Indyuce.bountyhunters.manager;

import net.Indyuce.bountyhunters.BountyHunters;
import net.Indyuce.bountyhunters.api.ConfigFile;
import net.Indyuce.bountyhunters.api.LinearTax;
import net.Indyuce.bountyhunters.api.account.BankAccount;
import net.Indyuce.bountyhunters.api.account.PlayerBankAccount;
import net.Indyuce.bountyhunters.api.account.SimpleBankAccount;
import net.Indyuce.bountyhunters.api.language.Language;
import net.Indyuce.bountyhunters.api.language.Message;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class ConfigManager {
    @Nullable
    public LinearTax bountyCreationTax, bountyRemovalTax, targetSetTax;
    public long bountyInactivityTime;
    public boolean formattedNumbers, trackingParticles;
    @Nullable
    public BankAccount taxBankAccount;

    public void reload() {
        final ConfigurationSection config = BountyHunters.getInstance().getConfig();

        // Taxes
        bountyCreationTax = new LinearTax(config.getConfigurationSection("bounty-tax.bounty-creation"));
        bountyRemovalTax = new LinearTax(config.getConfigurationSection("bounty-tax.bounty-removal"));
        targetSetTax = new LinearTax(config.getConfigurationSection("bounty-tax.target-set"));

        formattedNumbers = config.getBoolean("formatted-numbers");
        bountyInactivityTime = config.getLong("inactive-bounty-removal.time") * 60 * 60 * 1000;
        trackingParticles = config.getBoolean("player-tracking.target-particles");

        if (!config.getString("tax-bank-account.name").isEmpty()) try {
            String type = config.getString("tax-bank-account.type"), name = config.getString("tax-bank-account.name");
            taxBankAccount = type.equalsIgnoreCase("player") ? new PlayerBankAccount(name) : type.equalsIgnoreCase("account") ? new SimpleBankAccount(name) : null;
            Validate.notNull(taxBankAccount, "Account type must be either 'player' or 'account'");
        } catch (IllegalArgumentException exception) {
            BountyHunters.getInstance().getLogger().log(Level.WARNING, "Could not load tax bank account: " + exception.getMessage());
        }

        FileConfiguration messages = new ConfigFile("/language", "messages").getConfig();
        for (Message message : Message.values())
            message.update(messages.getConfigurationSection(message.getPath()));

        FileConfiguration language = new ConfigFile("/language", "language").getConfig();
        for (Language key : Language.values())
            key.update(language.getString(key.getPath()));
    }
}
