package me.koenn.serverchat.bungee;

import me.koenn.serverchat.api.util.IConfigManager;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import static net.md_5.bungee.api.ChatColor.translateAlternateColorCodes;

public class ConfigManager implements IConfigManager {

    private final Configuration config;

    public ConfigManager(Configuration config) {
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
        return translateAlternateColorCodes('&', getSection(path).get(key).toString());
    }

    /**
     * Get a ConfigurationSection at a certain path.
     *
     * @param path Path to get the section from
     * @return ConfigurationSection object instance
     */
    private @NotNull Configuration getSection(@NotNull String... path) {
        Configuration section = null;
        for (String p : path) {
            if (section != null) {
                section = section.getSection(p);
            } else {
                section = config.getSection(p);
            }
        }
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null");
        }
        return section;
    }
}
