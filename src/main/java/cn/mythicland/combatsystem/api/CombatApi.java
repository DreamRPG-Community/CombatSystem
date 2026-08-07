package cn.mythicland.combatsystem.api;

import cn.mythicland.combatsystem.lore.CombatItemStats;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Read-only public service exposed by CombatSystem.
 */
public interface CombatApi {

    /**
     * Returns the current effective attributes of a player.
     *
     * @param player the player to inspect
     * @return an immutable snapshot
     */
    CombatStatsSnapshot getStats(Player player);

    /**
     * Returns the current effective attributes of any living entity's supported equipment.
     *
     * @param entity the entity to inspect
     * @return an immutable snapshot
     */
    CombatStatsSnapshot getStats(LivingEntity entity);

    /**
     * Parses supported attributes from one ItemStack.
     *
     * @param itemStack the item to inspect
     * @return immutable parsed values and skipped supported lines
     */
    CombatItemStats parseItem(ItemStack itemStack);
}
