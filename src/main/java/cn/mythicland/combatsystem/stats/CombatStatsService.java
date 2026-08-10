package cn.mythicland.combatsystem.stats;

import cn.mythicland.combatsystem.api.CombatApi;
import cn.mythicland.combatsystem.api.CombatStatsSnapshot;
import cn.mythicland.combatsystem.config.CombatSettings;
import cn.mythicland.combatsystem.lore.CombatItemStats;
import cn.mythicland.combatsystem.lore.CombatLoreParser;
import cn.mythicland.combatsystem.lore.CombatStat;
import cn.mythicland.lib.item.ItemLoreReader;
import cn.mythicland.lib.item.NumericRange;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class CombatStatsService implements CombatApi {

    private static final long INVALID_WARNING_COOLDOWN_MILLIS = 60_000L;

    private final JavaPlugin plugin;
    private final Map<String, Long> warningTimes = new HashMap<>();
    private volatile CombatLoreParser parser;
    private volatile CombatSettings settings;

    public CombatStatsService(JavaPlugin plugin, CombatSettings settings) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.parser = new CombatLoreParser(settings);
    }

    static CombatStatsSnapshot aggregate(List<CombatItemStats> itemStats) {
        Objects.requireNonNull(itemStats, "itemStats");
        double damageMinimum = 0.0D;
        double damageMaximum = 0.0D;
        double defense = 0.0D;
        double health = 0.0D;
        double healthRegen = 0.0D;
        double critChance = 0.0D;
        double critDamage = 0.0D;
        boolean hasDamage = false;
        boolean hasDefense = false;

        for (CombatItemStats item : itemStats) {
            NumericRange damage = item.value(CombatStat.DAMAGE).orElse(null);
            if (damage != null) {
                hasDamage = true;
                damageMinimum += damage.minimum();
                damageMaximum += damage.maximum();
            }
            NumericRange defenseValue = item.value(CombatStat.DEFENSE).orElse(null);
            if (defenseValue != null) {
                hasDefense = true;
                defense += defenseValue.average();
            }
            health += average(item, CombatStat.HEALTH);
            healthRegen += average(item, CombatStat.HEALTH_REGEN);
            critChance += average(item, CombatStat.CRIT_CHANCE);
            critDamage += average(item, CombatStat.CRIT_DAMAGE);
        }

        return CombatStatsSnapshot.builder()
                .damageMinimum(nonNegative(damageMinimum))
                .damageMaximum(nonNegative(damageMaximum))
                .hasDamage(hasDamage)
                .defensePercent(Math.clamp(defense, 0.0D, 100.0D))
                .hasDefense(hasDefense)
                .health(nonNegative(health))
                .healthRegenPercent(Math.clamp(healthRegen, 0.0D, 100.0D))
                .critChancePercent(Math.clamp(critChance, 0.0D, 100.0D))
                .critDamagePercent(nonNegative(critDamage))
                .build();
    }

    private static List<ItemStack> supportedEquipment(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return List.of();
        List<ItemStack> items = new ArrayList<>(5);
        items.add(equipment.getItemInMainHand());
        ItemStack[] armorContents = equipment.getArmorContents();
        if (armorContents != null) {
            Collections.addAll(items, armorContents);
        }
        return items;
    }

    private static double average(CombatItemStats item, CombatStat stat) {
        NumericRange value = item.value(stat).orElse(null);
        return value == null ? 0.0D : value.average();
    }

    private static double nonNegative(double value) {
        return Math.max(0.0D, value);
    }

    private static String describe(NumericRange value) {
        String range = value.isFixed()
                ? format(value.minimum())
                : format(value.minimum()) + "-" + format(value.maximum());
        return value.percent() ? range + "%" : range;
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value)
                .replaceAll("\\.?0+$", "");
    }

    public void reload(CombatSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
        parser = new CombatLoreParser(settings);
        warningTimes.clear();
    }

    @Override
    public CombatStatsSnapshot getStats(Player player) {
        return getStats((LivingEntity) Objects.requireNonNull(player, "player"));
    }

    @Override
    public CombatStatsSnapshot getStats(LivingEntity entity) {
        Objects.requireNonNull(entity, "entity");
        List<CombatItemStats> itemStats = new ArrayList<>();
        for (ItemStack item : supportedEquipment(entity)) {
            CombatItemStats stats = parseItem(item);
            itemStats.add(stats);
        }
        return aggregate(itemStats);
    }

    @Override
    public CombatItemStats parseItem(ItemStack itemStack) {
        CombatItemStats stats = parser.parse(ItemLoreReader.read(itemStack));
        for (String invalidLine : stats.invalidLines()) warnInvalidLine(invalidLine);
        return stats;
    }

    public List<String> debug(Player player) {
        List<String> lines = new ArrayList<>();
        EntityEquipment equipment = Objects.requireNonNull(player, "player").getEquipment();
        List<EquipmentEntry> entries = List.of(
                new EquipmentEntry("主手", equipment.getItemInMainHand()),
                new EquipmentEntry("头盔", equipment.getHelmet()),
                new EquipmentEntry("胸甲", equipment.getChestplate()),
                new EquipmentEntry("护腿", equipment.getLeggings()),
                new EquipmentEntry("靴子", equipment.getBoots())
        );
        for (EquipmentEntry entry : entries) {
            CombatItemStats stats = parseItem(entry.item());
            if (stats.values().isEmpty() && stats.invalidLines().isEmpty()) {
                lines.add(entry.slot() + ": 无支持属性");
                continue;
            }
            lines.add(entry.slot() + ":");
            for (CombatStat stat : CombatStat.values()) {
                stats.value(stat).ifPresent(value -> lines.add("  " + settings.label(stat) + " = " + describe(value)));
            }
            for (String invalidLine : stats.invalidLines()) lines.add("  跳过: " + invalidLine);
        }
        return lines;
    }

    private void warnInvalidLine(String invalidLine) {
        long now = System.currentTimeMillis();
        Long previous = warningTimes.get(invalidLine);
        if (previous != null && now - previous < INVALID_WARNING_COOLDOWN_MILLIS) return;
        warningTimes.put(invalidLine, now);
        plugin.getLogger().warning("Ignored invalid Combat lore: " + invalidLine);
    }

    private record EquipmentEntry(String slot, ItemStack item) {
    }
}
