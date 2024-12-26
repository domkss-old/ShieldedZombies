package net.domkss.shieldedzombies.config;


import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ModConfig {


    @ConfigField(comment = "The chance of a zombie spawning with a shield in hand: 0.015 means 1.5%")
    private double normalSpawnChance = 0.015;
    @ConfigField(comment = "The chance of a zombie spawning with a shield in hand if game difficulty is HARD: 0.03 means 3%")
    private double hardSpawnChance = 0.03;
    @ConfigField(comment = "Determines how long the zombie can hold the blocking stance; the default is 3 seconds")
    private int blockDuration = 3;
    @ConfigField(comment="Specifies the minimum cooldown time between shield activations")
    private int shieldCooldown = 10;



    private final String CONFIG_FILE_NAME = ".\\config\\ShieldedZombies.yaml";
    private final Logger logger;

    public ModConfig(Logger logger) {
        this.logger=logger;
        loadConfig();
    }

    // Load configuration from the YAML file
    private void loadConfig() {
        File configFile = new File(CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            logger.info("Config file does not exist. Creating with default values.");
            saveDefaultConfig();
        } else {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(fis);

                if (data == null) {
                    logger.info("Config file is empty or corrupt. Creating with default values.");
                    saveDefaultConfig();
                } else {
                    // Process the configuration file
                    updateConfigFromFile(data);
                }
            } catch (IOException e) {
                logger.info("Error reading config file, creating a new one with default values.");
                saveDefaultConfig();
            }
        }
    }

    // Automatically update configuration variables from the YAML file data
    private void updateConfigFromFile(Map<String, Object> fileConfigMap) {
        Map<String, Object> defaultConfigMap = getDefaultValues();
        Map<String,Object> updatedConfigMap = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : defaultConfigMap.entrySet()) {
            String key = entry.getKey();
            Object defaultValue = entry.getValue();

            if (fileConfigMap.containsKey(key)) {
                // If the key exists in the YAML file, update the corresponding field
                setFieldValue(key, fileConfigMap.get(key));
                updatedConfigMap.put(key,fileConfigMap.get(key)); //Keep the original file value
            } else{
                updatedConfigMap.put(key,defaultValue);

                if(defaultValue!=null && !key.startsWith("#")){
                // If the key does not exist, add the default value to the file and update the field
                logger.log(Level.SEVERE,"Missing '" + key + "' in config, adding default value.");
                setFieldValue(key, defaultValue);
                }
            }

        }

        // Save the updated configuration back to the file
        saveConfig(updatedConfigMap);
    }

    // Set the field value based on the field name and value
    private void setFieldValue(String fieldName, Object value) {
        try {
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            logger.log(Level.SEVERE,"Error setting field value for " + fieldName);
        }
    }


    // Get default values for all fields annotated with @Config
    private Map<String, Object> getDefaultValues() {
        Map<String, Object> defaultValues = new LinkedHashMap<>();
        try {
            // Loop through all fields in the class
            for (Field field : this.getClass().getDeclaredFields()) {
                // Check if the field has the @Config annotation
                if (field.isAnnotationPresent(ConfigField.class)) {
                    field.setAccessible(true);  // Make private fields accessible
                    ConfigField configAnnotation = field.getAnnotation(ConfigField.class);

                    if(!configAnnotation.comment().isEmpty())
                        defaultValues.put("# "+configAnnotation.comment(), null);

                    defaultValues.put(field.getName(), field.get(this)); // Add the field to map
                }
            }
        } catch (IllegalAccessException e) {
           logger.log(Level.SEVERE,"Error accessing fields while getting default values.");
        }
        return defaultValues;
    }

    // Save the configuration (either updated or default) to the YAML file
    private void saveConfig(Map<String, Object> updatedConfig) {
        File configFile = new File(CONFIG_FILE_NAME);

        try (FileWriter writer = new FileWriter(configFile)) {
            // Write YAML with comments
            for (Map.Entry<String, Object> entry : updatedConfig.entrySet()) {
                if (entry.getKey().startsWith("#")&&entry.getValue()==null) {
                    writer.write(entry.getKey() + "\n");
                }else writer.write(entry.getKey() + ": " + entry.getValue() + "\n\n");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Error saving config file.");
        }
    }

    // Save the default configuration to the YAML file
    private void saveDefaultConfig() {
        Map<String, Object> defaultConfig = getDefaultValues();
        saveConfig(defaultConfig);
    }

    public double getNormalSpawnChance() {
        return Math.max(normalSpawnChance,0);
    }

    public double getHardSpawnChance() {
        return Math.max(hardSpawnChance,0);
    }

    public int getBlockDuration() {
        return Math.max(blockDuration,0);
    }

    public int getShieldCooldown() {
        return Math.max(shieldCooldown,0);
    }
}

