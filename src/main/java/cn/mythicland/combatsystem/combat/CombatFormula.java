package cn.mythicland.combatsystem.combat;

import cn.mythicland.combatsystem.api.CombatStatsSnapshot;

public final class CombatFormula {

    private static final double BASE_CRITICAL_DAMAGE_PERCENT = 100.0D;

    private CombatFormula() {
    }

    public static double damage(CombatStatsSnapshot stats, double randomUnit) {
        if (!stats.hasDamage()) return 0.0D;
        double unit = Math.clamp(randomUnit, 0.0D, 1.0D);
        return stats.damageMinimum()
                + (stats.damageMaximum() - stats.damageMinimum()) * unit;
    }

    public static boolean critical(CombatStatsSnapshot stats, double randomPercent) {
        return stats.critChancePercent() > 0.0D
                && randomPercent >= 0.0D
                && randomPercent < stats.critChancePercent();
    }

    public static double criticalMultiplier(CombatStatsSnapshot stats, boolean critical) {
        return critical
                ? (BASE_CRITICAL_DAMAGE_PERCENT + stats.critDamagePercent()) / 100.0D
                : 1.0D;
    }

    public static double applyDefense(double damage, double defensePercent) {
        return Math.max(0.0D, damage * (1.0D - Math.clamp(defensePercent, 0.0D, 100.0D) / 100.0D));
    }
}
