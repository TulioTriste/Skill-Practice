package net.skillwars.practice.listeners;

import me.joeleoli.nucleus.menu.Menu;
import net.skillwars.practice.Practice;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {

    private final Practice plugin;

    public InventoryListener() {
        this.plugin = Practice.getInstance();
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        Menu openMenu = Menu.currentlyOpenedMenus.get(player.getName());
        if (openMenu != null) {
            return;
        }
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (playerData.getPlayerState() == PlayerState.SPAWN || (playerData.getPlayerState() == PlayerState.EVENT && player.getItemInHand() != null && player.getItemInHand().getType() == Material.COMPASS)) {
                event.setCancelled(true);
            }
        }
    }
}
