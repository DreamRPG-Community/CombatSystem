package cn.mythicland.combatsystem.combat;

import cn.mythicland.combatsystem.api.CombatStatsSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatFormulaTest {

    @Test
    void selectsDamageInsideConfiguredRange() {
        CombatStatsSnapshot stats = CombatStatsSnapshot.builder()
                .damageMinimum(400.0D)
                .damageMaximum(450.0D)
                .hasDamage(true)
                .build();

        assertEquals(400.0D, CombatFormula.damage(stats, 0.0D));
        assertEquals(425.0D, CombatFormula.damage(stats, 0.5D));
        assertEquals(450.0D, CombatFormula.damage(stats, 1.0D));
    }

    @Test
    void appliesCriticalMultiplierAndDefensePercentage() {
        CombatStatsSnapshot stats = CombatStatsSnapshot.builder()
                .damageMinimum(10.0D)
                .damageMaximum(10.0D)
                .hasDamage(true)
                .defensePercent(50.0D)
                .hasDefense(true)
                .critChancePercent(30.0D)
                .critDamagePercent(30.0D)
                .build();

        assertTrue(CombatFormula.critical(stats, 29.99D));
        assertFalse(CombatFormula.critical(stats, 30.0D));
        assertEquals(1.3D, CombatFormula.criticalMultiplier(stats, true));
        assertEquals(5.0D, CombatFormula.applyDefense(10.0D, stats.defensePercent()));
        assertEquals(1.0D, CombatFormula.criticalMultiplier(CombatStatsSnapshot.empty(), true));
    }
}
