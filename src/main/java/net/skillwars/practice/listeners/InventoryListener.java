package net.skillwars.practice.listeners;

import lombok.Getter;
import me.joeleoli.nucleus.menu.Menu;
import net.skillwars.practice.Practice;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;

import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.ItemBuilder;
import net.skillwars.practice.util.ItemUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InventoryListener implements Listener {

    private final Practice plugin;
    public static Map<String, Boolean> setPlayersLimit = new HashMap<>();

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

    @EventHandler
    public void onInventoryClickPartySettings(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().getTitle().equalsIgnoreCase(CC.PRIMARY + "Settings Party")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
            switch (event.getCurrentItem().getType()) {
                case DOUBLE_PLANT:
                    if (party.getLeader() == player.getUniqueId()) {
                        if (party.isOpen()) {
                            party.setOpen(false);
                            player.sendMessage(CC.translate("&cSe ha cerrado la Party."));
                        } else {
                            party.setOpen(true);
                            player.sendMessage(CC.translate("&aSe ha abierto la Party."));
                        }

                        List<String> membersItemLores = new ArrayList<>();
                        membersItemLores.addAll(CC.translate(Arrays.asList("&7&m------------------------",
                                "&fAbrir la Party para todo el publico",
                                "&fpara que cualquiera pueda unirse",
                                "",
                                (party.isOpen() ? "&a\u2713 " : "  ") + "&fHabilitado",
                                (party.isOpen() ? "  " : "&c\u2718 ") + "&fDesabilitado",
                                "&7&m------------------------")));
                        ItemStack membersLimit = new ItemBuilder(ItemUtil.createItem(Material.PAPER, CC.translate("&b&lEditar limite de Miembros"))).lore(membersItemLores).build();

                        event.getClickedInventory().setItem(2, membersLimit);
                    } else {
                        player.sendMessage(CC.translate("&cSolo el Leader puede hacer esta Accion"));
                    }
                    break;
                case PAPER:
                    if (party.getLeader().equals(player.getUniqueId()) && player.hasPermission("practice.party.editlimit")) {
                        setPlayersLimit.put(player.getName(), true);
                        player.sendMessage(CC.translate("&aInserte un numero!"));
                        player.closeInventory();
                    } else if (!player.hasPermission("practice.party.editlimit")) {
                        player.sendMessage(CC.translate("&cNo tienes el permiso para editar el limite de miembros."));
                    } else if (!party.getLeader().equals(player.getUniqueId())) {
                        player.sendMessage(CC.translate("&cNo eres el Leader para hacer esta accion."));
                    }
                    break;
            }
        }
    }
}
