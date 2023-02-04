package me.dish.souppotions.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

    private static File file;
    private static FileConfiguration config;

    // generate config
    public static void setup() {

        file = new File(Bukkit.getServer().getPluginManager().getPlugin("SoupPotions").getDataFolder(), "config.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

    }

    public static FileConfiguration get() {
        return config;
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            System.out.println("Couldn't save file!");
        }
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

}
