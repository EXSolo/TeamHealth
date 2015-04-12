package com.exsoloscript.teamhealth.event;

import com.publicuhc.ultrahardcore.features.FeatureManager;
import com.publicuhc.ultrahardcore.features.IFeature;
import com.publicuhc.ultrahardcore.features.events.FeatureEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FeatureListener implements Listener {

    private FeatureManager manager;

    public FeatureListener(FeatureManager fm) {
        this.manager = fm;
    }

    @EventHandler
    public void onFeatureEnable(FeatureEnableEvent event) {
        IFeature feature = event.getFeature();

        if (feature.getFeatureID().equals("PlayerList")) {
            this.manager.getFeatureByID("TeamHealth").disableFeature();
        } else if (feature.getFeatureID().equals("TeamHealth")) {
            this.manager.getFeatureByID("PlayerList").disableFeature();
        }
    }
}
