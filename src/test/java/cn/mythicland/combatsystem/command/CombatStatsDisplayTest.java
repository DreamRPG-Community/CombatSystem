package cn.mythicland.combatsystem.command;

import cn.mythicland.combatsystem.api.CombatStatsSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatStatsDisplayTest {

    @Test
    void rendersTheConfiguredAttributePanel() {
        CombatStatsSnapshot stats = CombatStatsSnapshot.builder()
                .damageMinimum(100.0D)
                .damageMaximum(100.0D)
                .hasDamage(true)
                .defensePercent(100.0D)
                .hasDefense(true)
                .health(100.0D)
                .healthRegenPercent(100.0D)
                .build();

        assertEquals(List.of(
                "§8§lCombatSystem §f| §7战斗属性",
                "  §f",
                "  §4伤害: §f100",
                "  §b防御: §f100",
                "  §a生命值: §f100",
                "  §2生命回复: §f100%",
                "  §c暴击几率: §f0%",
                "  §4暴击伤害: §f0%",
                "  §f"
        ), CombatStatsDisplay.render(stats));
    }
}
