package net.islandearth.anvillogin.listeners;

import com.github.games647.fastlogin.bukkit.FastLoginBukkit;
import com.github.games647.fastlogin.core.PremiumStatus;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.islandearth.anvillogin.AnvilLogin;
import net.islandearth.anvillogin.translation.Translations;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
            if (plugin.isAuthme()
                    && (AuthMeApi.getInstance().isAuthenticated(myPlayer) || AuthMeApi.getInstance().isUnrestricted(myPlayer))) {
                return;
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getConfig().getBoolean("fastlogin")) {
                    if (Bukkit.getPluginManager().getPlugin("FastLogin") != null) {
                        FastLoginBukkit fastLogin = (FastLoginBukkit) Bukkit.getPluginManager().getPlugin("FastLogin");
                        if (fastLogin != null) {
                            PremiumStatus premiumStatus = fastLogin.getStatus(myPlayer.getUniqueId());
                            if (premiumStatus == PremiumStatus.PREMIUM) {
                                if (plugin.debug()) {
                                    plugin.getLogger().info("Skipping player " + myPlayer.getName() + " because they are premium.");
                                }
                                return;
                            }
                        }
                    }
                }

                plugin.getNotLoggedIn().add(myPlayer.getUniqueId());
                new AnvilGUI.Builder()
                        .onComplete((player, text) -> {
                            if (plugin.isAuthme() && plugin.getConfig().getBoolean("register") && !AuthMeApi.getInstance().isRegistered(player.getName())) {
                                AuthMeApi.getInstance().forceRegister(player, text, true);
                                plugin.getLoggedIn().add(player.getUniqueId());
                                plugin.getNotLoggedIn().remove(player.getUniqueId());
                                if (plugin.getConfig().getBoolean("login_messages")) {
                                    Translations.LOGGED_IN.send(player);
                                }
                                return AnvilGUI.Response.close();
                            }

                            if (text.equalsIgnoreCase(plugin.getConfig().getString("Password"))
                                    || (plugin.isAuthme() && AuthMeApi.getInstance().checkPassword(player.getName(), text))) {
                                plugin.getLoggedIn().add(player.getUniqueId());
                                plugin.getNotLoggedIn().remove(player.getUniqueId());
                                if (plugin.getConfig().getBoolean("login_messages")) {
                                    Translations.LOGGED_IN.send(player);
                                }
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


                if (plugin.getConfig().getBoolean("Timeout")) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (!plugin.getLoggedIn().contains(myPlayer.getUniqueId())) {
                            myPlayer.kickPlayer(Translations.KICKED.get(myPlayer));
                        }
                    }, plugin.getConfig().getLong("Time"));
                }
            }, 40L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent pqe) {
        Player player = pqe.getPlayer();
        plugin.getLoggedIn().remove(player.getUniqueId());
        plugin.getNotLoggedIn().remove(player.getUniqueId());
    }
}
