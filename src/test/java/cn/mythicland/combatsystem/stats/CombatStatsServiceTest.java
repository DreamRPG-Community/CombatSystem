package cn.mythicland.combatsystem.stats;

import cn.mythicland.combatsystem.api.CombatStatsSnapshot;
import cn.mythicland.combatsystem.lore.CombatItemStats;
import cn.mythicland.combatsystem.lore.CombatStat;
import cn.mythicland.lib.item.NumericRange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatStatsServiceTest {

    @Test
    void aggregatesDamageRangesAndClampsPercentageAttributes() {
        CombatItemStats mainHand = new CombatItemStats(Map.of(
                CombatStat.DAMAGE, new NumericRange(400.0D, 450.0D, false),
                CombatStat.DEFENSE, new NumericRange(70.0D, 70.0D, true),
                CombatStat.CRIT_CHANCE, new NumericRange(60.0D, 60.0D, true),
                CombatStat.CRIT_DAMAGE, new NumericRange(30.0D, 30.0D, true)
        ), List.of());
        CombatItemStats armor = new CombatItemStats(Map.of(
                CombatStat.DAMAGE, new NumericRange(10.0D, 20.0D, false),
                CombatStat.DEFENSE, new NumericRange(50.0D, 50.0D, true),
                CombatStat.HEALTH, new NumericRange(1700.0D, 1700.0D, false),
                CombatStat.HEALTH_REGEN, new NumericRange(10.0D, 10.0D, true),
                CombatStat.CRIT_CHANCE, new NumericRange(60.0D, 60.0D, true)
        ), List.of());

        CombatStatsSnapshot result = CombatStatsService.aggregate(List.of(mainHand, armor));

        assertEquals(410.0D, result.damageMinimum());
        assertEquals(470.0D, result.damageMaximum());
        assertEquals(100.0D, result.defensePercent());
        assertEquals(1700.0D, result.health());
        assertEquals(10.0D, result.healthRegenPercent());
        assertEquals(100.0D, result.critChancePercent());
        assertEquals(30.0D, result.critDamagePercent());
        assertTrue(result.hasDamage());
        assertTrue(result.hasDefense());
    }
}
