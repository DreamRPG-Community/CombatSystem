package cn.mythicland.combatsystem.command;

import cn.mythicland.combatsystem.api.CombatStatsSnapshot;
import cn.mythicland.lib.text.LegacyText;

import java.util.List;
import java.util.Locale;

/**
 * Renders the player-facing CombatSystem attribute panel.
 */
final class CombatStatsDisplay {

    private CombatStatsDisplay() {
    }

    static List<String> render(CombatStatsSnapshot stats) {
        return List.of(
                colorize("&8&lCombatSystem &f| &7战斗属性"),
                colorize("  &f"),
                valueLine("&4", "伤害", damageValue(stats)),
                valueLine("&b", "防御", stats.hasDefense() ? format(stats.defensePercent()) : "原版护甲"),
                valueLine("&a", "生命值", format(stats.health())),
                valueLine("&2", "生命回复", format(stats.healthRegenPercent()) + "%"),
                valueLine("&c", "暴击几率", format(stats.critChancePercent()) + "%"),
                valueLine("&4", "暴击伤害", format(stats.critDamagePercent()) + "%"),
                colorize("  &f")
        );
    }

    private static String valueLine(String labelColor, String label, String value) {
        return colorize("  " + labelColor + label + ": &f" + value);
    }

    private static String damageValue(CombatStatsSnapshot stats) {
        if (!stats.hasDamage()) return "原版伤害";
        if (Double.compare(stats.damageMinimum(), stats.damageMaximum()) == 0) {
            return format(stats.damageMinimum());
        }
        return format(stats.damageMinimum()) + " - " + format(stats.damageMaximum());
    }

    private static String colorize(String text) {
        return LegacyText.colorize(text);
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("\\.?0+$", "");
    }
}
