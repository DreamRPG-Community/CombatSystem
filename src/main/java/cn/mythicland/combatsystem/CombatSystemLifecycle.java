package cn.mythicland.combatsystem;

import cn.mythicland.combatsystem.actionbar.CombatHealthBarService;
import cn.mythicland.combatsystem.api.CombatApi;
import cn.mythicland.combatsystem.command.CombatCommand;
import cn.mythicland.combatsystem.config.CombatSettings;
import cn.mythicland.combatsystem.integration.mythicmobs.MythicMobsAdapter;
import cn.mythicland.combatsystem.listener.CombatListener;
import cn.mythicland.combatsystem.stats.CombatStatsService;
import cn.mythicland.lib.api.LibApi;
import cn.mythicland.lib.bootstrap.LibPluginLifecycle;
import cn.mythicland.lib.bootstrap.annotation.InjectComponent;
import cn.mythicland.lib.command.CommandRouter;
import cn.mythicland.lib.config.ConfigSupport;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

/**
 * Owns CombatSystem construction, Bukkit registration, tasks, and reload state.
 */
@InjectComponent
public final class CombatSystemLifecycle implements LibPluginLifecycle {

    private final CombatSystemPlugin plugin;
    private final LibApi lib;
    private CombatSettings settings;
    private CombatStatsService statsService;
    private CombatHealthBarService healthBarService;
    private CombatListener listener;
    private BukkitTask regenerationTask;
    private BukkitTask healthBarTask;

    /**
     * Creates the lifecycle module from Lib-provided dependencies.
     *
     * @param plugin plugin entry point
     * @param lib shared Lib service
     */
    public CombatSystemLifecycle(CombatSystemPlugin plugin, LibApi lib) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.lib = Objects.requireNonNull(lib, "lib");
    }

    /**
     * Builds and registers the combat system.
     */
    @Override
    public void enable() {
        settings = CombatSettings.load(plugin, ConfigSupport.loadDefault(plugin));
        statsService = new CombatStatsService(plugin, settings);
        MythicMobsAdapter mythicMobsAdapter = MythicMobsAdapter.detect();
        healthBarService = new CombatHealthBarService(mythicMobsAdapter);
        if (mythicMobsAdapter.isAvailable()) plugin.getLogger().info("MythicMobs compatibility enabled.");
        listener = new CombatListener(plugin, lib, statsService, healthBarService);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        plugin.getServer().getServicesManager().register(
                CombatApi.class,
                statsService,
                plugin,
                ServicePriority.Normal
        );
        registerCommand();
        regenerationTask = lib.runTimer(20L, 20L, listener::regenerateOnlinePlayers);
        healthBarTask = lib.runTimer(10L, 10L, healthBarService::refreshOnlinePlayers);
        listener.refreshOnlinePlayers();
        plugin.getLogger().info("CombatSystem enabled.");
    }

    /**
     * Reloads settings and refreshes online player attributes.
     */
    @Override
    public void reload() {
        reloadConfiguration();
    }

    /**
     * Cancels tasks, unregisters the service, and removes health modifiers.
     */
    @Override
    public void disable() {
        if (regenerationTask != null) regenerationTask.cancel();
        if (healthBarTask != null) healthBarTask.cancel();
        regenerationTask = null;
        healthBarTask = null;
        if (healthBarService != null) healthBarService.clearAll();
        if (listener != null) {
            for (Player player : plugin.getServer().getOnlinePlayers()) listener.removeHealthModifier(player);
        }
        if (statsService != null) {
            plugin.getServer().getServicesManager().unregister(CombatApi.class, statsService);
        }
        listener = null;
        healthBarService = null;
        statsService = null;
        settings = null;
    }

    /**
     * Returns the current settings.
     *
     * @return combat settings
     */
    public CombatSettings settings() {
        return Objects.requireNonNull(settings, "Combat settings are unavailable");
    }

    /**
     * Reloads the plugin configuration.
     */
    public void reloadConfiguration() {
        FileConfiguration configuration = ConfigSupport.loadDefault(plugin);
        CombatSettings refreshedSettings = CombatSettings.load(plugin, configuration);
        settings = refreshedSettings;
        Objects.requireNonNull(statsService, "Combat stats service is unavailable")
                .reload(refreshedSettings);
        Objects.requireNonNull(listener, "Combat listener is unavailable").refreshOnlinePlayers();
    }

    private void registerCommand() {
        PluginCommand command = Objects.requireNonNull(
                plugin.getCommand(CombatSystemPlugin.COMMAND_NAME),
                CombatSystemPlugin.COMMAND_NAME + " command is missing from plugin.yml"
        );
        CommandRouter router = lib.createCommandRouter(plugin, CombatSystemPlugin.COMMAND_NAME);
        CombatCommand.register(router, plugin, statsService);
        command.setExecutor(router);
        command.setTabCompleter(router);
    }
}
