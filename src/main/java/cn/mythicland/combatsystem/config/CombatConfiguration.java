package cn.mythicland.combatsystem.config;

import cn.mythicland.combatsystem.lore.CombatStat;
import cn.mythicland.lib.bootstrap.annotation.ConfigComponent;
import cn.mythicland.lib.config.ConfigValue;
import cn.mythicland.lib.config.ConfigView;
import cn.mythicland.lib.config.ConfigurableComponent;
import cn.mythicland.lib.text.LegacyText;

import java.util.EnumMap;
import java.util.Objects;

/**
 * Binds CombatSystem's flat configuration values and publishes its immutable domain settings.
 */
@ConfigComponent
public final class CombatConfiguration implements ConfigurableComponent {

    private volatile CombatSettings snapshot;

    private static String visibleLabel(String value, CombatStat stat) {
        String visible = LegacyText.stripColor(value).trim();
        return visible.isBlank() ? stat.defaultDisplayName() : visible;
    }

    /**
     * Binds the current main configuration and publishes a complete settings snapshot.
     *
     * @param configuration Lib-owned configuration view
     */
    @Override
    public void reload(ConfigView configuration) {
        RawSettings raw = Objects.requireNonNull(configuration, "configuration")
                .bind(RawSettings.class);
        EnumMap<CombatStat, String> labels = new EnumMap<>(CombatStat.class);
        labels.put(CombatStat.DAMAGE, visibleLabel(raw.damageLabel(), CombatStat.DAMAGE));
        labels.put(CombatStat.DEFENSE, visibleLabel(raw.defenseLabel(), CombatStat.DEFENSE));
        labels.put(CombatStat.HEALTH, visibleLabel(raw.healthLabel(), CombatStat.HEALTH));
        labels.put(CombatStat.HEALTH_REGEN, visibleLabel(raw.healthRegenLabel(), CombatStat.HEALTH_REGEN));
        labels.put(CombatStat.CRIT_CHANCE, visibleLabel(raw.critChanceLabel(), CombatStat.CRIT_CHANCE));
        labels.put(CombatStat.CRIT_DAMAGE, visibleLabel(raw.critDamageLabel(), CombatStat.CRIT_DAMAGE));
        snapshot = new CombatSettings(
                raw.feedbackEnabled(),
                labels,
                new CombatMessages(raw.criticalHitMessage(), raw.healthRegenMessage())
        );
    }

    /**
     * Returns the most recently bound settings.
     *
     * @return immutable CombatSystem settings
     */
    public CombatSettings snapshot() {
        CombatSettings value = snapshot;
        if (value == null) throw new IllegalStateException("CombatSystem settings are not loaded");
        return value;
    }

    private record RawSettings(
            @ConfigValue(
                    path = "combat-feedback",
                    defaultValue = "false"
            )
            boolean feedbackEnabled,
            @ConfigValue(
                    path = "messages.critical-hit",
                    defaultValue = "&c&l暴击! &f你造成了&c{damage}&f点伤害!",
                    nonBlank = true
            )
            String criticalHitMessage,
            @ConfigValue(
                    path = "messages.health-regen",
                    defaultValue = "&a&l生命回复! &f你恢复了&c{amount}&f点生命值!",
                    nonBlank = true
            )
            String healthRegenMessage,
            @ConfigValue(
                    path = "labels.damage",
                    defaultValue = "伤害",
                    nonBlank = true
            )
            String damageLabel,
            @ConfigValue(
                    path = "labels.defense",
                    defaultValue = "防御",
                    nonBlank = true
            )
            String defenseLabel,
            @ConfigValue(
                    path = "labels.health",
                    defaultValue = "生命值",
                    nonBlank = true
            )
            String healthLabel,
            @ConfigValue(
                    path = "labels.health-regen",
                    defaultValue = "生命回复",
                    nonBlank = true
            )
            String healthRegenLabel,
            @ConfigValue(
                    path = "labels.crit-chance",
                    defaultValue = "暴击几率",
                    nonBlank = true
            )
            String critChanceLabel,
            @ConfigValue(
                    path = "labels.crit-damage",
                    defaultValue = "暴击伤害",
                    nonBlank = true
            )
            String critDamageLabel
    ) {
    }
}
