package net.islandearth.anvillogin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import lombok.AllArgsConstructor;
import net.islandearth.anvillogin.AnvilLogin;
import net.wesjd.anvilgui.AnvilGUI;

@AllArgsConstructor
public class PlayerListener implements Listener {

    private AnvilLogin plugin;
    
    @EventHandler
    public void onJoin(PlayerJoinEvent pje) {
        Player myPlayer = pje.getPlayer();
        if (!plugin.isAuthme()) {
            
            if (!myPlayer.hasPermission("AnvilLogin.bypass") && !plugin.getLoggedIn().contains(myPlayer.getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getNotLoggedIn().add(myPlayer.getUniqueId());
                    new AnvilGUI.Builder()
                    .onClose(player -> {
                        player.sendMessage("You closed the inventory.");
                    })
                    .onComplete((player, text) -> {
                        if (text.equalsIgnoreCase(plugin.getConfig().getString("Password"))) {
                            plugin.getLoggedIn().add(player.getUniqueId());
                            plugin.getNotLoggedIn().remove(player.getUniqueId());
                            player.sendMessage(plugin.getTranslator().getTranslationFor(player, "loggedin"));
                            return AnvilGUI.Response.close();
                        } else {
                            return AnvilGUI.Response.text(plugin.getTranslator().getTranslationFor(myPlayer, "guiwrong"));
                        }
                    })
                    .preventClose()
                    .text(plugin.getTranslator().getTranslationFor(myPlayer, "guitext"))
                    .item(new ItemStack(Material.ANVIL))
                    .title(plugin.getTranslator().getTranslationFor(myPlayer, "guititle"))  //only works in 1.14+
                    .plugin(plugin)
                    .open(myPlayer);
                }, 200L);
                
                if (plugin.getConfig().getBoolean("Timeout")) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (!plugin.getLoggedIn().contains(myPlayer.getUniqueId())) {
                            myPlayer.kickPlayer(plugin.getTranslator().getTranslationFor(myPlayer, "kicked"));
                        }
                    }, plugin.getConfig().getLong("Time"));
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
            if (ice.getInventory().getType() == InventoryType.ANVIL && plugin.getNotLoggedIn().contains(player.getUniqueId()) && !plugin.getLoggedIn().contains(player.getUniqueId())) {
                player.kickPlayer(plugin.getTranslator().getTranslationFor(player, "closedinventory"));
            }
        }
    }
}
