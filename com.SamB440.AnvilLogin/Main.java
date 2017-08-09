package com.SamB440.AnvilLogin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	int task;
    private final ArrayList<UUID> Loggedin = new ArrayList<UUID>();
    private final ArrayList<UUID> NotLoggedin = new ArrayList<UUID>();
	public static File configf;
	public static Main instance;
    FileConfiguration config = getConfig();
	private static final Logger log = Logger.getLogger("Minecraft");
	public void onEnable() {
		createFiles();
		Bukkit.getPluginManager().registerEvents(this, this);
		log.info("[AnvilLogin] Enabled & registered events!");
		instance = this;
	}
	public void onDisable() {
		log.info("[AnvilLogin] Disabled plugin!");
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent l) {
	final Player p = l.getPlayer();
	NotLoggedin.add(p.getUniqueId());
	if(!p.hasPermission("AnvilLogin.bypass") && !Loggedin.contains(p.getUniqueId())) 
	{
        final AnvilGUI gui = new AnvilGUI(p, new AnvilGUI.AnvilClickEventHandler() 
        {
            @Override
            public void onAnvilClick(AnvilGUI.AnvilClickEvent event) 
            {
            	event.setWillClose(false);
            	event.setWillDestroy(false);
                if (event.getSlot() == AnvilGUI.AnvilSlot.OUTPUT) 
                {
                    if(event.getName().equalsIgnoreCase(getConfig().getString("Password"))) 
                    {
                        event.setWillClose(true);
                        event.setWillDestroy(true);
                        //event.getName() == string in anvil
                        Loggedin.add(p.getUniqueId());
                        NotLoggedin.remove(p.getUniqueId());
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Loggedinmsg")));
                	} else {
                		event.setWillClose(false);
                		event.setWillDestroy(false);
                	}
                }
            }
        });
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta papermeta = paper.getItemMeta();
        papermeta.setDisplayName("Enter password");
        paper.setItemMeta(papermeta);
        gui.setSlot(AnvilGUI.AnvilSlot.INPUT_LEFT, paper);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
            @Override
            public void run() {
                try {
                    gui.open();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
		}, 20L);
//		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            //public void run() {
            	//if(!Loggedin.contains(p.getUniqueId()) && NotLoggedin.contains(p.getUniqueId())) {
            		//try {
            			//gui.getPlayer().setLevel(p.getLevel() - 1);
            			//p.sendMessage("test");
            			//gui.open();
                		//} catch (IllegalAccessException e) {
                			//e.printStackTrace();
                		//} catch (InvocationTargetException e) {
                			//e.printStackTrace();
                		//} catch (InstantiationException e) {
                			//e.printStackTrace();
                		//}
            		//}
            	//else if(Loggedin.contains(p.getUniqueId())) {
            		//Bukkit.getScheduler().cancelTask(task);
            		//}
                //}
            //}, 0L, 200L);
        if(getConfig().getBoolean("Timeout")) {
    		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
                @Override
                public void run() {
                	if(!Loggedin.contains(p.getUniqueId())) {
                	p.kickPlayer(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Kickmsg")));
                	}
                }
    		}, 600L);
        }
	}
}
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent c) {
        Player p = (Player)c.getPlayer();
        if (c.getPlayer() instanceof Player) {
            if (NotLoggedin.contains(p.getUniqueId()) && !Loggedin.contains(p.getUniqueId())) {
            	p.kickPlayer(ChatColor.translateAlternateColorCodes('&', getConfig().getString("ClosedInventorymsg")));
            }
        }
    }
    @EventHandler
	public void onQuit(PlayerQuitEvent q) {
		Player p = q.getPlayer();
		Loggedin.remove(p.getUniqueId());
		NotLoggedin.remove(p.getUniqueId());
	}
	private void createFiles() {
		File dir = new File("plugins/AnvilLogin");
		log.info("[AnvilLogin] Importing files");
		if (!dir.exists()){
			dir.mkdir();
			configf = new File(getDataFolder(), "config.yml");  				    	
	        saveDefaultConfig();
			log.info("[AnvilLogin] Config file created!");
		}
	}
}
