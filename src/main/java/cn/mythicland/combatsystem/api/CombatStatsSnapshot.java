package cn.mythicland.combatsystem.api;

import cn.mythicland.combatsystem.lore.CombatStat;
import java.util.Objects;

/**
 * Immutable effective Combat attributes for one living entity.
 */
public final class CombatStatsSnapshot {

    private final double damageMinimum;
    private final double damageMaximum;
    private final boolean hasDamage;
    private final double defensePercent;
    private final boolean hasDefense;
    private final double health;
    private final double healthRegenPercent;
    private final double critChancePercent;
    private final double critDamagePercent;

    private CombatStatsSnapshot(Builder builder) {
        validateNonNegative(builder.damageMinimum, "damageMinimum");
        validateNonNegative(builder.damageMaximum, "damageMaximum");
        if (builder.damageMinimum > builder.damageMaximum) {
            throw new IllegalArgumentException("damageMinimum cannot exceed damageMaximum");
        }
        validateNonNegative(builder.defensePercent, "defensePercent");
        validateNonNegative(builder.health, "health");
        validateNonNegative(builder.healthRegenPercent, "healthRegenPercent");
        validateNonNegative(builder.critChancePercent, "critChancePercent");
        validateNonNegative(builder.critDamagePercent, "critDamagePercent");

        damageMinimum = builder.damageMinimum;
        damageMaximum = builder.damageMaximum;
        hasDamage = builder.hasDamage;
        defensePercent = builder.defensePercent;
        hasDefense = builder.hasDefense;
        health = builder.health;
        healthRegenPercent = builder.healthRegenPercent;
        critChancePercent = builder.critChancePercent;
        critDamagePercent = builder.critDamagePercent;
    }

    /**
     * Creates a builder with safe defaults. Critical damage is stored as a bonus above the
     * base 100% multiplier, so the default bonus is 0% and the effective multiplier is 1.0x.
     *
     * @return a new snapshot builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns an empty snapshot that leaves vanilla combat untouched.
     *
     * @return an empty snapshot
     */
    public static CombatStatsSnapshot empty() {
        return builder().build();
    }

    public double damageMinimum() {
        return damageMinimum;
    }

    public double damageMaximum() {
        return damageMaximum;
    }

    public boolean hasDamage() {
        return hasDamage;
    }

    public double defensePercent() {
        return defensePercent;
    }

    public boolean hasDefense() {
        return hasDefense;
    }

    public double health() {
        return health;
    }

    public double healthRegenPercent() {
        return healthRegenPercent;
    }

    public double critChancePercent() {
        return critChancePercent;
    }

    /**
     * Returns the extra critical-damage percentage, excluding the base 100% damage.
     *
     * @return the extra critical-damage percentage
     */
    public double critDamagePercent() {
        return critDamagePercent;
    }

    /**
     * Returns a value by public stat identifier.
     *
     * @param stat the requested attribute
     * @return the effective value
     */
    public double value(CombatStat stat) {
        return switch (Objects.requireNonNull(stat, "stat")) {
            case DAMAGE -> damageMinimum;
            case DEFENSE -> defensePercent;
            case HEALTH -> health;
            case HEALTH_REGEN -> healthRegenPercent;
            case CRIT_CHANCE -> critChancePercent;
            case CRIT_DAMAGE -> critDamagePercent;
        };
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CombatStatsSnapshot snapshot)) return false;
        return Double.compare(damageMinimum, snapshot.damageMinimum) == 0
                && Double.compare(damageMaximum, snapshot.damageMaximum) == 0
                && hasDamage == snapshot.hasDamage
                && Double.compare(defensePercent, snapshot.defensePercent) == 0
                && hasDefense == snapshot.hasDefense
                && Double.compare(health, snapshot.health) == 0
                && Double.compare(healthRegenPercent, snapshot.healthRegenPercent) == 0
                && Double.compare(critChancePercent, snapshot.critChancePercent) == 0
                && Double.compare(critDamagePercent, snapshot.critDamagePercent) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                damageMinimum,
                damageMaximum,
                hasDamage,
                defensePercent,
                hasDefense,
                health,
                healthRegenPercent,
                critChancePercent,
                critDamagePercent
        );
    }

    @Override
    public String toString() {
        return "CombatStatsSnapshot["
                + "damageMinimum=" + damageMinimum
                + ", damageMaximum=" + damageMaximum
                + ", hasDamage=" + hasDamage
                + ", defensePercent=" + defensePercent
                + ", hasDefense=" + hasDefense
                + ", health=" + health
                + ", healthRegenPercent=" + healthRegenPercent
                + ", critChancePercent=" + critChancePercent
                + ", critDamagePercent=" + critDamagePercent
                + ']';
    }

    private static void validateNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }

    /**
     * Named construction API for the snapshot's multiple values and flags.
     */
    public static final class Builder {

        private double damageMinimum;
        private double damageMaximum;
        private boolean hasDamage;
        private double defensePercent;
        private boolean hasDefense;
        private double health;
        private double healthRegenPercent;
        private double critChancePercent;
        private double critDamagePercent;

        private Builder() {
        }

        public Builder damageMinimum(double value) {
            damageMinimum = value;
            return this;
        }

        public Builder damageMaximum(double value) {
            damageMaximum = value;
            return this;
        }

        public Builder hasDamage(boolean value) {
            hasDamage = value;
            return this;
        }

        public Builder defensePercent(double value) {
            defensePercent = value;
            return this;
        }

        public Builder hasDefense(boolean value) {
            hasDefense = value;
            return this;
        }

        public Builder health(double value) {
            health = value;
            return this;
        }

        public Builder healthRegenPercent(double value) {
            healthRegenPercent = value;
            return this;
        }

        public Builder critChancePercent(double value) {
            critChancePercent = value;
            return this;
        }

        public Builder critDamagePercent(double value) {
            critDamagePercent = value;
            return this;
        }

        public CombatStatsSnapshot build() {
            return new CombatStatsSnapshot(this);
        }
    }
}
