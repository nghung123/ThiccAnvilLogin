package net.islandearth.anvillogin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import lombok.AllArgsConstructor;
import net.islandearth.anvillogin.AnvilLogin;
import net.wesjd.anvilgui.AnvilGUI;

@AllArgsConstructor
public class PlayerListener implements Listener {

	private AnvilLogin plugin;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent pje) {
		Player player = pje.getPlayer();
		if (plugin.getAuthme() == null) {
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
			if (plugin.getNotLoggedIn().contains(player.getUniqueId()) && !plugin.getLoggedIn().contains(player.getUniqueId())) {
				player.kickPlayer(plugin.getTranslator().getTranslationFor(player, "closedinventory"));
			}
		}
	}
}
