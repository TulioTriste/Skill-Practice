package net.skillwars.practice.events.inventory;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.util.Color;
import net.skillwars.practice.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

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

        buttons.put(10, new EventButton(Material.FISHING_ROD, "&3FFA", 0));
        buttons.put(12, new EventButton(Material.CLAY_BRICK, "&3Sumo", 0));
        buttons.put(14, new EventButton(Material.POTION, "&3NoDebuffLite", 0));
        buttons.put(16, new EventButton(Material.DIAMOND_SWORD, "&3TeamFights", 0));

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
