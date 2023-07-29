package net.Indyuce.bountyhunters.leaderboard.profile;

import net.Indyuce.bountyhunters.BountyHunters;
import net.Indyuce.bountyhunters.api.Bounty;
import net.Indyuce.bountyhunters.util.PlayerProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public class BountyProfile extends PlayerProfile {
    private final double currentBounty;

    public BountyProfile(OfflinePlayer player) {
        super(player);

        Optional<Bounty> opt = BountyHunters.getInstance().getBountyManager().getBounty(player);
        this.currentBounty = opt.isPresent() ? opt.get().getReward() : 0;
    }

    public BountyProfile(ConfigurationSection config) {
        super(config);

        this.currentBounty = config.getDouble("bounty");
    }

    public double getCurrentBounty() {
        return currentBounty;
    }

    @Override
    public void whenSaved(ConfigurationSection config) {
        config.set("bounty", currentBounty);
    }
}
