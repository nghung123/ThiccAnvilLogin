package net.islandearth.anvillogin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import net.islandearth.anvillogin.listeners.AuthMeListener;
import net.islandearth.anvillogin.listeners.PlayerListener;
import net.islandearth.languagy.language.Language;
import net.islandearth.languagy.language.Translator;
import net.wesjd.anvilgui.version.VersionMatcher;

public class AnvilLogin extends JavaPlugin {
    
    private Logger log = Bukkit.getLogger();
    
    @Getter private List<UUID> loggedIn = new ArrayList<>();
    @Getter private List<UUID> notLoggedIn = new ArrayList<>();
    @Getter private Translator translator;
    @Getter private boolean authme;
    
    public void onEnable() {
        try {
            new VersionMatcher().match();
        } catch (RuntimeException e) {
            log.severe("Your server version is not supported! Please update to the latest version!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        if (Bukkit.getPluginManager().getPlugin("AuthMe") != null) {
            log.info("Found authme!");
            this.authme = true;
        } else this.authme = false;
        createFiles();
        registerListeners();
        log.info("[AnvilLogin] Enabled & registered events!");
    }
    
    public void onDisable() {
        log.info("[AnvilLogin] Disabled plugin!");
    }
    
    private void createFiles() {
        saveDefaultConfig();
        File lang = new File(getDataFolder() + "/lang/");
        if (!lang.exists()) lang.mkdirs();
        File fallback = new File(getDataFolder() + "/lang/" + "en_gb.yml");
        for (Language language : Language.values()) {
            File file = new File(getDataFolder() + "/lang/" + language.getCode() + ".yml");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.options().copyDefaults(true);
                config.addDefault("kicked", "&7[&9AnvilLogin&7] &cYou have been kicked for not entering the password within 30 seconds.");
                config.addDefault("loggedin", "&7[&9AnvilLogin&7] &aYou logged in to the server!");
                config.addDefault("closedinventory", "&7[&9AnvilLogin&7] &cYou need to enter a password!");
                config.addDefault("guititle", "Enter Password");
                config.addDefault("guitext", "Enter Password");
                config.addDefault("guiwrong", "Incorrect.");
                try {
                    config.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }    
            }
        }
        
        this.translator = new Translator(this, fallback, Language.ENGLISH);
        translator.setDisplay(Material.ANVIL);
    }
    
    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        if (!authme) pm.registerEvents(new PlayerListener(this), this);
        else pm.registerEvents(new AuthMeListener(this), this);
    }
}
