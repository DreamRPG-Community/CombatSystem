package cn.mythicland.combatsystem.config;

import cn.mythicland.combatsystem.lore.CombatStat;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable CombatSystem configuration used by the runtime modules.
 */
public final class CombatSettings {

    private final boolean feedbackEnabled;
    private final Map<CombatStat, String> labels;
    private final CombatMessages messages;

    /**
     * Creates settings with the default triggered-effect messages.
     *
     * @param feedbackEnabled whether triggered-effect messages are enabled
     * @param labels          configured visible labels without color codes
     */
    public CombatSettings(boolean feedbackEnabled, Map<CombatStat, String> labels) {
        this(feedbackEnabled, labels, CombatMessages.defaults());
    }

    CombatSettings(
            boolean feedbackEnabled,
            Map<CombatStat, String> labels,
            CombatMessages messages
    ) {
        this.feedbackEnabled = feedbackEnabled;
        Objects.requireNonNull(labels, "labels");
        EnumMap<CombatStat, String> copy = new EnumMap<>(CombatStat.class);
        copy.putAll(labels);
        this.labels = Collections.unmodifiableMap(copy);
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    /**
     * Returns whether triggered-effect messages are enabled.
     *
     * @return true when feedback is enabled
     */
    public boolean feedbackEnabled() {
        return feedbackEnabled;
    }

    /**
     * Returns the configured visible label.
     *
     * @param stat the requested stat
     * @return the visible configured label
     */
    public String label(CombatStat stat) {
        return labels.get(Objects.requireNonNull(stat, "stat"));
    }

    /**
     * Returns the visible configured label.
     *
     * @param stat the requested stat
     * @return the visible label
     */
    public String labelText(CombatStat stat) {
        return label(stat);
    }

    /**
     * Renders the configured critical-hit feedback message.
     *
     * @param damage the final critical-hit damage
     * @return a colorized message
     */
    public String criticalHitMessage(double damage) {
        return messages.criticalHit(damage);
    }

    /**
     * Renders the configured health-regeneration feedback message.
     *
     * @param amount the restored health amount
     * @return a colorized message
     */
    public String healthRegenMessage(double amount) {
        return messages.healthRegen(amount);
    }

}
