package net.skillwars.practice.events.inventory;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.Color;
import net.skillwars.practice.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public class HostInvetory extends Menu {

    @Override
    public String getTitle(Player player) {
        return Color.translate("&9&lEventos");
    }

    @Override
    public int getSize() {
        return 9 * 3;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        Button fond = new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
            }
        };
        Button coming_soon = new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                ItemStack a = new ItemStack(Material.GOLDEN_APPLE);
                ItemMeta aMeta = a.getItemMeta();
                aMeta.setDisplayName(CC.translate("&cProximamente."));
                a.setItemMeta(aMeta);
                return a;
            }
        };
        Button sign = new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                ItemStack a = new ItemStack(Material.SIGN);
                ItemMeta aMeta = a.getItemMeta();
                aMeta.setDisplayName(CC.translate("&3&lEventos"));
                aMeta.setLore(CC.translate(Arrays.asList("&e¿Quieres hostear un evento?",
                        "",
                        "&7Para hostear un evento debes de tener &bVIP",
                        "&atienda.skillwars.us")));
                a.setItemMeta(aMeta);
                return a;
            }
        };

        for (int i = 0; i < 27; i++) {
            buttons.put(i, fond);
        }

        buttons.put(10, new EventButton(Material.FISHING_ROD, "&bFFA", 0));
        buttons.put(11, new EventButton(Material.POTION, "&bNoDebuffLite", 0));
        buttons.put(12, new EventButton(Material.CLAY_BRICK, "&bSumo", 0));
        buttons.put(13, sign);
        buttons.put(14, new EventButton(Material.TNT, "&bTNTTag", 0));
        buttons.put(15, new EventButton(Material.DIAMOND_SWORD, "&bTeamFights", 0));
        buttons.put(16, coming_soon);

        return buttons;
    }

    @AllArgsConstructor
    public class EventButton extends Button{

        private Material type;
        private String name;
        private int data;

        @Override
        public ItemStack getButtonItem(Player player) {

            if(player.hasPermission("host." + Style.strip(name))){
                return new ItemBuilder(type).name(Style.translate(name)).durability(data).build();
            }else{
                return new ItemBuilder(type)
                        .name(Style.translate(name))
                        .lore(Style.translateLines(Arrays.asList(
                                "&ePara hostear este evento",
                                "&enecesitas comprar rango en",
                                "&astore.skillwars.us"
                        )))
                        .durability(data)
                        .build();
            }
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.performCommand("host " + Style.strip(name));
        }
    }
}
