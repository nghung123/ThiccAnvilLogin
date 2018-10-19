package net.islandearth.anvillogin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.xephi.authme.api.v3.AuthMeApi;
import lombok.AllArgsConstructor;
import net.islandearth.anvillogin.AnvilLogin;
import net.wesjd.anvilgui.AnvilGUI;

@AllArgsConstructor
public class AuthMeListener implements Listener {
	
	private AnvilLogin plugin;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent pje) {
		Player player = pje.getPlayer();
		if (!player.hasPermission("AnvilLogin.bypass") 
				&& !plugin.getLoggedIn().contains(player.getUniqueId())
				&& AuthMeApi.getInstance().isRegistered(player.getName())) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				plugin.getNotLoggedIn().add(player.getUniqueId());
				new AnvilGUI(plugin, player, "Enter Password", (oPlayer, reply) -> {
					if (AuthMeApi.getInstance().checkPassword(player.getName(), reply)) {
				    	plugin.getLoggedIn().add(player.getUniqueId());
				        plugin.getNotLoggedIn().remove(player.getUniqueId());
				        player.sendMessage(plugin.getTranslator().getTranslationFor(player, "loggedin"));
				        AuthMeApi.getInstance().forceLogin(player);
				        
				        return null;
				    }
				    return "Incorrect.";
				});
			}, 40L);
			
			if (plugin.getConfig().getBoolean("Timeout")) {
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					if (!plugin.getLoggedIn().contains(player.getUniqueId())) {
	    				player.kickPlayer(plugin.getTranslator().getTranslationFor(player, "kicked"));
					}
				}, plugin.getConfig().getLong("Time"));
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent pqe) {
		Player player = pqe.getPlayer();
		if (plugin.getLoggedIn().contains(player.getUniqueId())) plugin.getLoggedIn().remove(player.getUniqueId());
		if (plugin.getNotLoggedIn().contains(player.getUniqueId())) plugin.getNotLoggedIn().remove(player.getUniqueId());
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent ice) {
		if (ice.getPlayer() instanceof Player) {
			Player player = (Player) ice.getPlayer();
			if (ice.getInventory().getType() == InventoryType.ANVIL && plugin.getNotLoggedIn().contains(player.getUniqueId()) && !plugin.getLoggedIn().contains(player.getUniqueId())) {
				player.kickPlayer(plugin.getTranslator().getTranslationFor(player, "closedinventory"));
			}
		}
	}
}
