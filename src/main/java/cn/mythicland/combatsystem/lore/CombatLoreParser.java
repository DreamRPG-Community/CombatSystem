package cn.mythicland.combatsystem.lore;

import cn.mythicland.combatsystem.config.CombatSettings;
import cn.mythicland.lib.item.LoreAttributeLine;
import cn.mythicland.lib.item.LoreAttributeParseResult;
import cn.mythicland.lib.item.LoreAttributeParser;
import cn.mythicland.lib.item.NumericRange;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class CombatLoreParser {

    private static final double MAX_ATTRIBUTE_VALUE = 1_000_000_000.0D;

    private final Map<String, CombatStat> aliases;

    public CombatLoreParser(CombatSettings settings) {
        CombatSettings validatedSettings = Objects.requireNonNull(settings, "settings");
        Map<String, CombatStat> mutableAliases = new HashMap<>();
        for (CombatStat stat : CombatStat.values()) {
            for (String alias : stat.aliases()) {
                mutableAliases.putIfAbsent(normalize(alias), stat);
            }
            mutableAliases.putIfAbsent(normalize(validatedSettings.labelText(stat)), stat);
        }
        aliases = Map.copyOf(mutableAliases);
    }

    public CombatItemStats parse(List<String> lore) {
        EnumMap<CombatStat, NumericRange> values = new EnumMap<>(CombatStat.class);
        List<String> invalidLines = new java.util.ArrayList<>();
        for (String line : lore) {
            LoreAttributeParseResult result = LoreAttributeParser.parseLine(line);
            if (result.status() == LoreAttributeParseResult.Status.NOT_ATTRIBUTE) continue;

            CombatStat stat = result.label()
                    .map(this::normalize)
                    .map(aliases::get)
                    .orElse(null);
            if (stat == null) continue;

            if (!result.isValid()) {
                invalidLines.add(line + " (" + result.error().orElse("格式无效") + ")");
                continue;
            }

            LoreAttributeLine attribute = result.attribute().orElseThrow();
            String validationError = validate(stat, attribute.value());
            if (validationError != null) {
                invalidLines.add(line + " (" + validationError + ")");
                continue;
            }
            values.merge(stat, attribute.value(), CombatLoreParser::sum);
        }
        return new CombatItemStats(values, invalidLines);
    }

    private static NumericRange sum(NumericRange first, NumericRange second) {
        return new NumericRange(
                first.minimum() + second.minimum(),
                first.maximum() + second.maximum(),
                first.percent() || second.percent()
        );
    }

    private static String validate(CombatStat stat, NumericRange value) {
        if (value.minimum() <= 0.0D || value.maximum() <= 0.0D) {
            return "属性数值必须为正数";
        }
        if (!stat.percentageAttribute() && value.percent()) {
            return "该属性不应带百分号";
        }
        if (value.maximum() > MAX_ATTRIBUTE_VALUE) {
            return "属性数值超过安全上限";
        }
        return null;
    }

    private String normalize(String label) {
        return LoreAttributeParser.normalizeLabel(label).toLowerCase(Locale.ROOT);
    }
}
