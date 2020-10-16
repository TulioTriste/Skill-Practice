package net.skillwars.practice.settings.item;

import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.inventory.UtilItem;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.skillwars.practice.util.ItemBuilder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public enum ProfileOptionsItem {

    DUEL_REQUESTS(UtilItem.createItem(Material.NAME_TAG, ChatColor.GOLD + "Peticiones de duelo", 1)),
    PARTY_INVITES(UtilItem.createItem(Material.EMPTY_MAP, ChatColor.GOLD + "Invitaciones de party", 1)),
    TOGGLE_SCOREBOARD(UtilItem.createItem(Material.ITEM_FRAME, ChatColor. GOLD + "Scoreboard", 1)),
    ALLOW_SPECTATORS(UtilItem.createItem(Material.SKULL_ITEM, ChatColor.GOLD + "Espectadores", 1, (short)3)),
    TOGGLE_TIME(UtilItem.createItem(Material.WATCH, ChatColor.GOLD + "Tiempo", 1));

    private final ItemStack item;

    ProfileOptionsItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem(ProfileOptionsItemState state) {
        if (this == DUEL_REQUESTS || this == PARTY_INVITES || this == ALLOW_SPECTATORS || this == TOGGLE_SCOREBOARD) {
            List<String> lore = new ArrayList<>();

            lore.add(CC.translate("&c\u00BB " + "&eEstado: " + (state == ProfileOptionsItemState.ENABLED ? "&a" + getOptionDescription(ProfileOptionsItemState.ENABLED) : "&c" + getOptionDescription(ProfileOptionsItemState.DISABLED))));

            return new ItemBuilder(item).lore(lore).build();
        }

        else if(this == TOGGLE_TIME) {
            List<String> lore = new ArrayList<>();

            lore.add(CC.translate("&c\u00BB &eTiempo: " + (state == ProfileOptionsItemState.DAY ? "&e" + getOptionDescription(ProfileOptionsItemState.DAY) :
                    (state == ProfileOptionsItemState.SUNSET ? "&6" + getOptionDescription(ProfileOptionsItemState.SUNSET) : "&9" + getOptionDescription(ProfileOptionsItemState.NIGHT)))));

            return new ItemBuilder(item).lore(lore).build();
        }

        return getItem(ProfileOptionsItemState.DISABLED);
    }

    public String getOptionDescription(ProfileOptionsItemState state) {
        if (this == DUEL_REQUESTS || this == PARTY_INVITES || this == ALLOW_SPECTATORS || this == TOGGLE_SCOREBOARD) {

            if (state == ProfileOptionsItemState.ENABLED) {
                return "Habilitdado";
            } else if (state == ProfileOptionsItemState.DISABLED) {
                return "Deshabilitado";
            }
        }

        else if(this == TOGGLE_TIME) {
            if (state == ProfileOptionsItemState.DAY) {
                return "Día";
            } else if (state == ProfileOptionsItemState.SUNSET) {
                return "Atardecer";
            } else if (state == ProfileOptionsItemState.NIGHT) {
                return "Noche";
            }
        }

        return getOptionDescription(ProfileOptionsItemState.DISABLED);
    }

    public static ProfileOptionsItem fromItem(ItemStack itemStack) {
        for (ProfileOptionsItem item : values()) {
            for (ProfileOptionsItemState state : ProfileOptionsItemState.values()) {
                if (item.getItem(state).isSimilar(itemStack)) {
                    return item;
                }
            }
        }
        return null;
    }

    public static ItemStack getFill() {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)3);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(CC.translate("&7 "));

        item.setItemMeta(meta);
        return item;
    }
}
