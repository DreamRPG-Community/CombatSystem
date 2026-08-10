package cn.mythicland.combatsystem.actionbar;

/**
 * Renders the temporary target health bar in legacy text format.
 */
final class CombatHealthBarFormatter {

    private static final double HEALTH_PER_HEART = 2.0D;
    private static final int MAX_HEARTS = 40;
    private static final String HEART = "❤";

    private CombatHealthBarFormatter() {
    }

    static String render(CombatHealthBarStatus status) {
        StringBuilder builder = new StringBuilder(trimTrailingWhitespace(status.targetDisplayName()));
        builder.append(" ");

        int rawMaxHearts = Math.max(1, hearts(status.maxHealth()));
        int maxHearts = Math.min(MAX_HEARTS, rawMaxHearts);
        int remainingHearts = rawMaxHearts <= MAX_HEARTS
                ? Math.min(hearts(status.remainingHealth()), maxHearts)
                : scaledHearts(status.remainingHealth(), status.maxHealth(), maxHearts, false);
        int absorptionHearts = rawMaxHearts <= MAX_HEARTS
                ? hearts(status.remainingAbsorption())
                : scaledHearts(status.remainingAbsorption(), status.maxHealth(), maxHearts, true);
        int damageHearts = rawMaxHearts <= MAX_HEARTS
                ? hearts(status.healthDamage())
                : scaledHearts(status.healthDamage(), status.maxHealth(), maxHearts, true);
        int visibleAbsorptionHearts = Math.clamp(absorptionHearts, 0, maxHearts - remainingHearts);
        int visibleDamageHearts = Math.clamp(
                damageHearts,
                0,
                maxHearts - remainingHearts - visibleAbsorptionHearts
        );
        int emptyHearts = Math.max(
                0,
                maxHearts - remainingHearts - visibleAbsorptionHearts - visibleDamageHearts
        );

        appendHearts(builder, "&4", remainingHearts);
        appendHearts(builder, "&e", visibleAbsorptionHearts);
        appendHearts(builder, "&c", visibleDamageHearts);
        appendHearts(builder, "&7", emptyHearts);
        return builder.toString();
    }

    private static void appendHearts(StringBuilder builder, String color, int hearts) {
        if (hearts <= 0) return;
        builder.append(color).repeat(HEART, hearts);
    }

    private static int hearts(double health) {
        return (int) Math.ceil(Math.max(0.0D, health) / HEALTH_PER_HEART);
    }

    private static int scaledHearts(
            double value,
            double maxValue,
            int maxHearts,
            boolean roundUp
    ) {
        double ratio = Math.clamp(value / maxValue, 0.0D, 1.0D);
        double scaled = ratio * maxHearts;
        return (int) (roundUp ? Math.ceil(scaled) : Math.floor(scaled));
    }

    private static String trimTrailingWhitespace(String text) {
        int end = text.length();
        while (end > 0 && Character.isWhitespace(text.charAt(end - 1))) end--;
        return text.substring(0, end);
    }
}
