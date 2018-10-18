package net.islandearth.anvillogin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.xephi.authme.events.LoginEvent;
import lombok.AllArgsConstructor;
import net.islandearth.anvillogin.AnvilLogin;
import net.wesjd.anvilgui.AnvilGUI;

@AllArgsConstructor
public class AuthMeListener implements Listener {
	
	private AnvilLogin plugin;
	
	@EventHandler
	public void onLogin(LoginEvent le) {
		Player player = le.getPlayer();
		plugin.getNotLoggedIn().add(player.getUniqueId());
		
		if (!player.hasPermission("AnvilLogin.bypass") && !plugin.getLoggedIn().contains(player.getUniqueId())) {
			
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				new AnvilGUI(plugin, player, "Please enter server password:", (oPlayer, reply) -> {
					if (reply.equalsIgnoreCase(plugin.getConfig().getString("Password"))) {
				    	plugin.getLoggedIn().add(player.getUniqueId());
				        plugin.getNotLoggedIn().remove(player.getUniqueId());
				        player.sendMessage(plugin.getTranslator().getTranslationFor(player, "loggedin"));
				        return null;
				    }
				    return "Incorrect.";
				});
			}, 20L);
			
			if (plugin.getConfig().getBoolean("Timeout")) {
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					if (!plugin.getLoggedIn().contains(player.getUniqueId())) {
	    				player.kickPlayer(plugin.getTranslator().getTranslationFor(player, "kicked"));
					}
				}, 600L);
			}
		}
	}
}
