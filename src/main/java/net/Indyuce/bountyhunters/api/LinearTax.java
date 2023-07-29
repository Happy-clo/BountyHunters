package net.Indyuce.bountyhunters.api;

import net.Indyuce.bountyhunters.util.UtilityMethods;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Tax that is composed of a flat component and a scaling
 * component.
 * <p>
 * Example: $100 + 3% of the amount involved
 * - flat component is 100
 * - scale component is 3%
 */
public class LinearTax {
    private final double flat, scale;

    public LinearTax(double flat, double scale) {
        this.flat = flat;
        this.scale = scale;

        Validate.isTrue(scale >= 0 && scale <= 100, "Scale must be between 0 and 100");
    }

    public LinearTax(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");

        this.flat = config.getDouble("flat");
        this.scale = config.getDouble("scale");

        Validate.isTrue(scale >= 0 && scale <= 100, "Scale must be between 0 and 100");
    }

    @Deprecated
    public double getTax(double bountyReward) {
        return calculate(bountyReward, false);
    }

    public double calculate(double bountyReward, boolean clamp) {
        final double brut = UtilityMethods.truncate(flat + scale * bountyReward / 100d, 1);
        return clamp ? Math.max(0, Math.min(brut, bountyReward)) : brut;
    }
}
