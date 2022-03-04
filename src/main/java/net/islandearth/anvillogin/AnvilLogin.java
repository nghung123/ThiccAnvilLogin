package net.islandearth.anvillogin;

import net.islandearth.anvillogin.api.AnvilLoginAPI;
import net.islandearth.anvillogin.listeners.PlayerListener;
import net.islandearth.anvillogin.translation.Translations;
import net.islandearth.languagy.api.language.Language;
import net.islandearth.languagy.api.language.LanguagyImplementation;
import net.islandearth.languagy.api.language.LanguagyPluginHook;
import net.islandearth.languagy.api.language.Translator;
import net.wesjd.anvilgui.version.VersionMatcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnvilLogin extends JavaPlugin implements AnvilLoginAPI, LanguagyPluginHook {
    
    private List<UUID> loggedIn = new ArrayList<>();

    public List<UUID> getLoggedIn() {
        return loggedIn;
    }

    public List<UUID> getNotLoggedIn() {
        return notLoggedIn;
    }

    @Override
    public Translator getTranslator() {
        return translator;
    }

    public boolean isAuthme() {
        return authme;
    }

    private List<UUID> notLoggedIn = new ArrayList<>();

    @LanguagyImplementation(Language.ENGLISH)
    private Translator translator;

    private boolean authme;
    private static AnvilLogin plugin;

    @Override
    public void onEnable() {
        try {
            new VersionMatcher().match();
        } catch (RuntimeException e) {
            this.getLogger().severe("Oops, your server version is not supported! Please update to the latest version!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        if (Bukkit.getPluginManager().getPlugin("AuthMe") != null) {
            this.getLogger().info("Found authme!");
            this.authme = true;
        } else this.authme = false;

        plugin = this;
        createFiles();
        registerListeners();
        this.hook(this);
        this.getLogger().info("[ThiccAnvilLogin] Enabled & registered events!");
    }
    
    private void createFiles() {
        saveDefaultConfig();
        Translations.generateLang(this);
    }
    
    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
    }

    @Override
    public boolean debug() {
        return this.getConfig().getBoolean("debug");
    }

    @Override
    public void onLanguagyHook() {
        translator.setDisplay(Material.ANVIL);
    }

    public static AnvilLoginAPI getAPI() {
        return plugin;
    }
}
