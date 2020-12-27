package net.islandearth.anvillogin.listeners;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.islandearth.anvillogin.AnvilLogin;
import net.islandearth.anvillogin.translation.Translations;
import net.wesjd.anvilgui.AnvilGUI;
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

public class PlayerListener implements Listener {

    private final AnvilLogin plugin;

    public PlayerListener(AnvilLogin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent pje) {
        Player myPlayer = pje.getPlayer();
        if (!myPlayer.hasPermission("AnvilLogin.bypass")
                && !plugin.getLoggedIn().contains(myPlayer.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getNotLoggedIn().add(myPlayer.getUniqueId());
                new AnvilGUI.Builder()
                        .onComplete((player, text) -> {
                            if (plugin.isAuthme() && plugin.getConfig().getBoolean("register") && !AuthMeApi.getInstance().isRegistered(player.getName())) {
                                AuthMeApi.getInstance().forceRegister(player, text, true);
                                Translations.LOGGED_IN.send(player);
                                return AnvilGUI.Response.close();
                            }

                            if (text.equalsIgnoreCase(plugin.getConfig().getString("Password"))
                                    || (plugin.isAuthme() && AuthMeApi.getInstance().checkPassword(player.getName(), text))) {
                                plugin.getLoggedIn().add(player.getUniqueId());
                                plugin.getNotLoggedIn().remove(player.getUniqueId());
                                Translations.LOGGED_IN.send(player);
                                if (plugin.isAuthme()) AuthMeApi.getInstance().forceLogin(player);
                                player.setLevel(player.getLevel());
                                return AnvilGUI.Response.close();
                            } else {
                                return AnvilGUI.Response.text(Translations.GUI_WRONG.get(myPlayer));
                            }
                        })
                        .preventClose()
                        .text(Translations.GUI_TEXT.get(myPlayer))
                        .itemLeft(new ItemStack(Material.ANVIL))
                        .title(Translations.GUI_TITLE.get(myPlayer))  //only works in 1.14+
                        .plugin(plugin)
                        .open(myPlayer);
            }, 40L);

            if (plugin.getConfig().getBoolean("Timeout")) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!plugin.getLoggedIn().contains(myPlayer.getUniqueId())) {
                        myPlayer.kickPlayer(Translations.KICKED.get(myPlayer));
                    }
                }, plugin.getConfig().getLong("Time"));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent pqe) {
        Player player = pqe.getPlayer();
        plugin.getLoggedIn().remove(player.getUniqueId());
        plugin.getNotLoggedIn().remove(player.getUniqueId());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent ice) {
        if (ice.getPlayer() instanceof Player) {
            Player player = (Player) ice.getPlayer();
            if (ice.getInventory().getType() == InventoryType.ANVIL
                    && plugin.getNotLoggedIn().contains(player.getUniqueId())
                    && !plugin.getLoggedIn().contains(player.getUniqueId())) {
                player.kickPlayer(Translations.CLOSED_INVENTORY.get(player));
            }
        }
    }
}
