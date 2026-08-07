package cn.mythicland.combatsystem.command;

import cn.mythicland.combatsystem.CombatSystemPlugin;
import cn.mythicland.combatsystem.api.CombatStatsSnapshot;
import cn.mythicland.combatsystem.stats.CombatStatsService;
import cn.mythicland.lib.command.CommandRouter;
import cn.mythicland.lib.command.CommandUsageException;
import cn.mythicland.lib.command.Subcommand;
import cn.mythicland.lib.command.VanillaCommandMessages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class CombatCommand {

    private static final String STATS_PERMISSION = "combatsystem.stats";
    private static final String ADMIN_PERMISSION = "combatsystem.admin";

    private CombatCommand() {
    }

    public static void register(
            CommandRouter router,
            CombatSystemPlugin plugin,
            CombatStatsService statsService
    ) {
        router.register(new StatsCommand(statsService));
        router.register(new ReloadCommand(plugin));
        router.register(new DebugCommand(statsService));
    }

    private record StatsCommand(CombatStatsService statsService) implements Subcommand {

        @Override
        public String name() {
            return "stats";
        }

        @Override
        public String usage() {
            return "/combatsystem stats";
        }

        @Override
        public String permission() {
            return STATS_PERMISSION;
        }

        @Override
        public void execute(CommandSender sender, List<String> arguments) {
            if (!arguments.isEmpty()) throw new CommandUsageException(usage());
            if (!(sender instanceof Player player)) {
                sender.sendMessage(VanillaCommandMessages.red("该命令只能由玩家执行。"));
                return;
            }

            CombatStatsSnapshot stats = statsService.getStats(player);
            for (String line : CombatStatsDisplay.render(stats)) sender.sendMessage(line);
        }
    }

    private record ReloadCommand(CombatSystemPlugin plugin) implements Subcommand {

        @Override
        public String name() {
            return "reload";
        }

        @Override
        public String usage() {
            return "/combatsystem reload";
        }

        @Override
        public String permission() {
            return ADMIN_PERMISSION;
        }

        @Override
        public void execute(CommandSender sender, List<String> arguments) {
            if (!arguments.isEmpty()) throw new CommandUsageException(usage());
            plugin.reloadCombatConfig();
            sender.sendMessage(VanillaCommandMessages.green("CombatSystem 配置已重载。"));
        }
    }

    private record DebugCommand(CombatStatsService statsService) implements Subcommand {

        @Override
        public String name() {
            return "debug";
        }

        @Override
        public String usage() {
            return "/combatsystem debug";
        }

        @Override
        public String permission() {
            return ADMIN_PERMISSION;
        }

        @Override
        public void execute(CommandSender sender, List<String> arguments) {
            if (!arguments.isEmpty()) throw new CommandUsageException(usage());
            if (!(sender instanceof Player player)) {
                sender.sendMessage(VanillaCommandMessages.red("该命令只能由玩家执行。"));
                return;
            }
            sender.sendMessage(ChatColor.GOLD + "CombatSystem Lore 解析调试");
            for (String line : statsService.debug(player)) {
                sender.sendMessage(ChatColor.GRAY + line);
            }
        }
    }

}
