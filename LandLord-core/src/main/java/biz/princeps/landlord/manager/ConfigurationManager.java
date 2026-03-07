package biz.princeps.landlord.manager;

import biz.princeps.landlord.api.IConfigurationManager;
import biz.princeps.landlord.api.ILandLord;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigurationManager implements IConfigurationManager {

    private static final String WORLDS_SECTION = "worlds";

    private final ILandLord plugin;

    public ConfigurationManager(ILandLord plugin) {
        this.plugin = plugin;
    }

    /**
     * In case you upgraded the config version (adding a field...) you have to increment the variable "version" in the
     * config.yml. This will cause the old config to be backuped and the new (changed) config to be copied in the
     * right place.
     */
    public void handleConfigUpdate(String pathToExisting, String pathInJar) {
        if (pathInJar == null || pathToExisting == null)
            return;

        try {
            File existing = new File(pathToExisting);
            if (!existing.exists()) {
                return;
            }

            YamlConfiguration config = new YamlConfiguration();
            config.load(existing);

            YamlConfiguration jarConfig = loadBundledConfig(pathInJar);
            if (jarConfig == null) {
                plugin.getLogger().warning("You are using an unknown translation. " +
                        "Please be aware, that LandLord will not add any new strings to your translation. " +
                        "If you would like to see your translation inside the plugin, please contact the author!");
                return;
            }

            int version = config.getInt("version");
            int i = jarConfig.getInt("version");
            if (i > version) {
                copyBundledConfig(pathInJar, pathToExisting + ".v" + i);
                plugin.getLogger().warning(pathToExisting + " config file is not up-to-date! " +
                        "You are on version " + version + " and LandLord expects version " + i + "! " +
                        "Please be aware, LandLord may not work as expected, take a look at generated file.");
            }

            int insertedKeys = copyMissingKeys(config, jarConfig);
            if (insertedKeys > 0) {
                config.save(existing);
                plugin.getLogger().info("Added " + insertedKeys + " missing entries to " + pathToExisting + ".");
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private YamlConfiguration loadBundledConfig(String pathInJar) throws IOException, InvalidConfigurationException {
        try (InputStream resourceAsStream = plugin.getClass().getResourceAsStream(pathInJar)) {
            if (resourceAsStream == null) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
            YamlConfiguration jarConfig = new YamlConfiguration();
            jarConfig.load(reader);
            return jarConfig;
        }
    }

    private void copyBundledConfig(String pathInJar, String destination) throws IOException {
        try (InputStream resourceAsStream = plugin.getClass().getResourceAsStream(pathInJar)) {
            if (resourceAsStream == null) {
                return;
            }
            Files.copy(resourceAsStream, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private int copyMissingKeys(YamlConfiguration targetConfig, YamlConfiguration sourceConfig) {
        int insertedKeys = 0;

        for (String key : sourceConfig.getKeys(true)) {
            if (sourceConfig.isConfigurationSection(key) || targetConfig.contains(key)) {
                continue;
            }

            targetConfig.set(key, sourceConfig.get(key));
            insertedKeys++;
        }

        return insertedKeys;
    }

    @Override
    public String getCustomizableString(World world, String defaultPath, String defaultValue) {
        FileConfiguration configuration = plugin.getConfig();
        return configuration.getString(WORLDS_SECTION + "." + world.getName() + "." + defaultPath,
                configuration.getString(defaultPath, defaultValue));
    }

    @Override
    public int getCustomizableInt(World world, String defaultPath, int defaultValue) {
        FileConfiguration configuration = plugin.getConfig();
        return configuration.getInt(WORLDS_SECTION + "." + world.getName() + "." + defaultPath,
                configuration.getInt(defaultPath, defaultValue));
    }

    @Override
    public boolean getCustomizableBoolean(World world, String defaultPath, boolean defaultValue) {
        FileConfiguration configuration = plugin.getConfig();
        return configuration.getBoolean(WORLDS_SECTION + "." + world.getName() + "." + defaultPath,
                configuration.getBoolean(defaultPath, defaultValue));
    }
}
