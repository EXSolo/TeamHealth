package com.exsoloscript.teamhealth.util;

import com.google.common.base.Optional;
import com.publicuhc.uhc.framework.configuration.Configurator;
import com.publicuhc.uhc.framework.configuration.events.ConfigFileReloadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UHCConfigurator implements Configurator {

    private final Map<String, FileConfiguration> m_configs = new HashMap<String, FileConfiguration>();
    private final File dataFolder;
    private final ClassLoader classLoader;

    public UHCConfigurator(File dataFolder, ClassLoader classLoader) {
        this.dataFolder = dataFolder;
        this.classLoader = classLoader;
    }

    @Override
    public FileConfiguration getConfig(String id) {
        FileConfiguration config = m_configs.get(id);
        if (null == config) {
            return reloadConfig(id);
        }
        return config;
    }

    @Override
    public void saveConfig(String id) {
        FileConfiguration configuration = m_configs.get(id);
        if (configuration != null) {
            YamlUtil.saveConfiguration(configuration, dataFolder, id + ".yml");
        }
    }

    @Override
    public FileConfiguration reloadConfig(String id) {
        try {
            Optional<FileConfiguration> config = YamlUtil.loadConfigWithDefaults(id + ".yml", classLoader, dataFolder);

            if (!config.isPresent()) {
                return null;
            }

            ConfigFileReloadedEvent event = new ConfigFileReloadedEvent(id, config.get());
            Bukkit.getPluginManager().callEvent(event);

            FileConfiguration configuration = config.get();
            m_configs.put(id, configuration);

            return configuration;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
