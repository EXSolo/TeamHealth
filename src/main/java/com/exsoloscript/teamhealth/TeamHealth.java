package com.exsoloscript.teamhealth;

import com.exsoloscript.teamhealth.event.FeatureListener;
import com.exsoloscript.teamhealth.util.UHCConfigurator;
import com.publicuhc.ultrahardcore.UltraHardcore;
import com.publicuhc.ultrahardcore.features.exceptions.FeatureIDConflictException;
import com.publicuhc.ultrahardcore.features.exceptions.InvalidFeatureIDException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TeamHealth extends JavaPlugin {

    public void onEnable() {
        UltraHardcore uhc = (UltraHardcore) Bukkit.getPluginManager().getPlugin("UltraHardcore");

        // Register the event
        Bukkit.getPluginManager().registerEvents(new FeatureListener(uhc.getFeatureManager()), this);

        // Register the feature
        TeamHealthFeature f = new TeamHealthFeature(this, new UHCConfigurator(uhc.getDataFolder(), uhc.getClass().getClassLoader()), null);

        try {
            uhc.getFeatureManager().addFeature(f);
        } catch (FeatureIDConflictException ignored) {
            getLogger().severe("TeamHealth Feature ID is conflicting, did you install the latest version?");
        } catch (InvalidFeatureIDException ignored) {
            getLogger().severe("TeamHealth feature ID is invalid, this should never happen!");
        }
    }
}
