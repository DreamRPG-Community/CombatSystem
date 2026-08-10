package cn.mythicland.combatsystem;

import cn.mythicland.combatsystem.config.CombatSettings;
import cn.mythicland.lib.api.LibApi;
import cn.mythicland.lib.bootstrap.PluginBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Minimal Bukkit entry point for the Lib-managed Lore combat system.
 */
public final class CombatSystemPlugin extends JavaPlugin {

    public static final String COMMAND_NAME = "combatsystem";
    public static final String COMMAND_ALIAS = "cs";

    private static final String COMPONENT_PACKAGE = "cn.mythicland.combatsystem";

    private PluginBootstrap bootstrap;

    /**
     * Starts the Lib-managed CombatSystem component graph.
     */
    @Override
    @SuppressWarnings("resource")
    public void onEnable() {
        try {
            LibApi lib = LibApi.require(this);
            bootstrap = lib.createPluginBootstrap(this, COMPONENT_PACKAGE);
            bootstrap.enable();
        } catch (RuntimeException exception) {
            getLogger().log(
                    Level.SEVERE,
                    "CombatSystem failed to enable: " + LibApi.rootCauseMessage(exception),
                    exception
            );
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Closes the Lib-managed CombatSystem component graph.
     */
    @Override
    public void onDisable() {
        if (bootstrap != null) bootstrap.disable();
        bootstrap = null;
    }

    /**
     * Returns the current combat settings for existing combat components.
     *
     * @return current combat settings
     */
    public CombatSettings settings() {
        return lifecycle().settings();
    }

    /**
     * Reloads the mutable CombatSystem configuration.
     */
    public void reloadCombatConfig() {
        lifecycle().reloadConfiguration();
    }

    private CombatSystemLifecycle lifecycle() {
        return Objects.requireNonNull(bootstrap, "CombatSystem bootstrap is unavailable")
                .resolve(CombatSystemLifecycle.class);
    }
}
