package cn.mythicland.combatsystem.lore;

import cn.mythicland.lib.item.NumericRange;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable attributes parsed from one item stack.
 *
 * @param values       supported attributes and their numeric values
 * @param invalidLines supported attribute lines that were skipped
 */
public record CombatItemStats(Map<CombatStat, NumericRange> values, List<String> invalidLines) {

    public CombatItemStats {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(invalidLines, "invalidLines");
        EnumMap<CombatStat, NumericRange> copy = new EnumMap<>(CombatStat.class);
        copy.putAll(values);
        values = Collections.unmodifiableMap(copy);
        invalidLines = List.copyOf(invalidLines);
    }

    /**
     * Returns one parsed attribute when present.
     *
     * @param stat the requested attribute
     * @return the parsed value
     */
    public Optional<NumericRange> value(CombatStat stat) {
        return Optional.ofNullable(values.get(Objects.requireNonNull(stat, "stat")));
    }

    /**
     * Returns whether this item contributes the requested attribute.
     *
     * @param stat the requested attribute
     * @return true when the attribute exists
     */
    public boolean has(CombatStat stat) {
        return values.containsKey(Objects.requireNonNull(stat, "stat"));
    }
}
