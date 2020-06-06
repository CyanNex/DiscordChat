package me.koenn.serverchat.bukkit;

import me.koenn.serverchat.api.util.IConfigManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class ConfigManager implements IConfigManager {

    private final FileConfiguration config;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Get a String from a path using a key.
     *
     * @param key  Key to get the String
     * @param path Path to get the String from
     * @return String value
     */
    public @NotNull String getString(@NotNull String key, @NotNull String @NotNull ... path) {
        return translateAlternateColorCodes('&', Objects.requireNonNull(getSection(path).get(key)).toString());
    }

    /**
     * Get a ConfigurationSection at a certain path.
     *
     * @param path Path to get the section from
     * @return ConfigurationSection object instance
     */
    private @NotNull ConfigurationSection getSection(@NotNull String... path) {
        ConfigurationSection section = null;
        for (String p : path) {
            if (section != null) {
                section = section.getConfigurationSection(p);
            } else {
                section = config.getConfigurationSection(p);
            }
        }
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null");
        }
        return section;
    }
}
