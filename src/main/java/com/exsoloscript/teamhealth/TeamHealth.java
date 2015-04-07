package com.exsoloscript.teamhealth;

import org.bukkit.plugin.java.JavaPlugin;
import uk.co.eluinhost.UltraHardcore.config.ConfigHandler;
import uk.co.eluinhost.UltraHardcore.exceptions.FeatureIDConflictException;
import uk.co.eluinhost.UltraHardcore.exceptions.InvalidFeatureIDException;
import uk.co.eluinhost.UltraHardcore.features.FeatureManager;
import uk.co.eluinhost.UltraHardcore.features.UHCFeature;
import uk.co.eluinhost.UltraHardcore.features.UHCFeatureList;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class TeamHealth extends JavaPlugin {

    public void onEnable() {
        loadConfig();

        UHCFeature f = new TeamHealthFeature(ConfigHandler.getConfig(0).getBoolean("features.teamHealth.enabled"));

        try {
            FeatureManager.addFeature(f);
        } catch (FeatureIDConflictException e) {
            getLogger().log(Level.SEVERE, "A different plugin is using the feature name TeamHealth already, disabling");
        } catch (InvalidFeatureIDException e) {
            getLogger().log(Level.SEVERE, "The feature ID is invalid. Is the plugin up-to-date?");
        }
    }

    private void loadConfig() {
        ConfigHandler.getConfig(0).addDefault("features.teamHealth.enabled", false);
        ConfigHandler.saveConfig(0);
    }
}
