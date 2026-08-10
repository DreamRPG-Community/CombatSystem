package cn.mythicland.combatsystem.command;

import cn.mythicland.combatsystem.CombatSystemLifecycle;
import cn.mythicland.combatsystem.CombatSystemPlugin;
import cn.mythicland.combatsystem.api.CombatStatsSnapshot;
import cn.mythicland.combatsystem.stats.CombatStatsService;
import cn.mythicland.lib.bootstrap.annotation.CommandComponent;
import cn.mythicland.lib.bootstrap.annotation.CommandHandler;
import cn.mythicland.lib.command.CommandContext;
import cn.mythicland.lib.command.VanillaCommandMessages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Handles CombatSystem commands.
 */
@CommandComponent("combatsystem")
public final class CombatCommand {

    private static final String STATS_PERMISSION = "combatsystem.stats";
    private static final String ADMIN_PERMISSION = "combatsystem.admin";

    private final CombatSystemPlugin plugin;
    private final CombatSystemLifecycle lifecycle;

    public CombatCommand(CombatSystemPlugin plugin, CombatSystemLifecycle lifecycle) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
    }

    @CommandHandler(value = "stats", permission = STATS_PERMISSION)
    void stats(CommandContext context) {
        context.requireArguments(0);
        if (!(context.sender() instanceof Player player)) {
            context.sender().sendMessage(VanillaCommandMessages.red("该命令只能由玩家执行。"));
            return;
        }

        CombatStatsService statsService = lifecycle.statsService();
        CombatStatsSnapshot stats = statsService.getStats(player);
        for (String line : CombatStatsDisplay.render(stats)) context.sender().sendMessage(line);
    }

    @CommandHandler(value = "reload", permission = ADMIN_PERMISSION)
    void reload(CommandContext context) {
        context.requireArguments(0);
        plugin.reloadCombatConfig();
        context.sender().sendMessage(VanillaCommandMessages.green("CombatSystem 配置已重载。"));
    }

    @CommandHandler(value = "debug", permission = ADMIN_PERMISSION)
    void debug(CommandContext context) {
        context.requireArguments(0);
        if (!(context.sender() instanceof Player player)) {
            context.sender().sendMessage(VanillaCommandMessages.red("该命令只能由玩家执行。"));
            return;
        }
        context.sender().sendMessage(ChatColor.GOLD + "CombatSystem Lore 解析调试");
        for (String line : lifecycle.statsService().debug(player)) {
            context.sender().sendMessage(ChatColor.GRAY + line);
        }
    }
}
