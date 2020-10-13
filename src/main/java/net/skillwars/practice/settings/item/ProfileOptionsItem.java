package net.skillwars.practice.settings.item;

import net.skillwars.practice.util.inventory.UtilItem;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.skillwars.practice.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public enum ProfileOptionsItem {

    DUEL_REQUESTS(UtilItem.createItem(Material.NAME_TAG, ChatColor.AQUA.toString() + ChatColor.BOLD + "Peticiones de Duelos", 1), "Deseas aceptar peticiones de Duelos?"),
    PARTY_INVITES(UtilItem.createItem(Material.EMPTY_MAP, ChatColor.AQUA.toString() + ChatColor.BOLD + "Invitaciones de Party's", 1), "Deseas aceptar invitaciones de Party's?"),
    TOGGLE_SCOREBOARD(UtilItem.createItem(Material.YELLOW_FLOWER, ChatColor.AQUA.toString() + ChatColor.BOLD + "Activar/Desactivar Scoreboard", 1), "Activar/Desactivar tu scoreboard"),
    ALLOW_SPECTATORS(UtilItem.createItem(Material.STRING, ChatColor.AQUA.toString() + ChatColor.BOLD + "Activar/Desactivar Spectators", 1), "Deseas activar que los jugadores te especteen?"),
    TOGGLE_TIME(UtilItem.createItem(Material.WATCH, ChatColor.AQUA.toString() + ChatColor.BOLD + "Setear Tiempo", 1), "Activar si quieres que el tiempo sea Dia, Soleado & Noche");

    private ItemStack item;
    private List<String> description;

    ProfileOptionsItem(ItemStack item, String description) {
        this.item = item;
        this.description = new ArrayList<>();

        this.description.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");

        StringBuilder parts = new StringBuilder();

        for (int i = 0; i < description.split(" ").length; i++) {
            String part = description.split(" ")[i];

            parts.append(part).append(" ");

            if (i == 4 || (i + 1) == description.split(" ").length) {
                this.description.add(ChatColor.WHITE + parts.toString().trim());
                parts = new StringBuilder();
            }
        }

        this.description.add(" ");
    }

    public ItemStack getItem(ProfileOptionsItemState state) {
        if (this == DUEL_REQUESTS || this == PARTY_INVITES || this == ALLOW_SPECTATORS) {
            List<String> lore = new ArrayList<>(description);

            lore.add("  " + (state == ProfileOptionsItemState.ENABLED ? ChatColor.GREEN + StringEscapeUtils.unescapeHtml4("\u2713") + " " : "  ") + ChatColor.WHITE + getOptionDescription(ProfileOptionsItemState.ENABLED));
            lore.add("  " + (state == ProfileOptionsItemState.DISABLED ? ChatColor.RED + StringEscapeUtils.unescapeHtml4("\u2718") + " "  : "  ") + ChatColor.WHITE + getOptionDescription(ProfileOptionsItemState.DISABLED));

            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");

            return new ItemBuilder(item).lore(lore).build();
        }

        else if(this == TOGGLE_TIME) {
            List<String> lore = new ArrayList<>(description);

            lore.add("  " + (state == ProfileOptionsItemState.DAY ? ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("\u2022") + " " : "  ") + ChatColor.WHITE + getOptionDescription(ProfileOptionsItemState.DAY));
            lore.add("  " + (state == ProfileOptionsItemState.SUNSET ? ChatColor.GOLD + StringEscapeUtils.unescapeHtml4("\u2022") + " "  : "  ") + ChatColor.WHITE + getOptionDescription(ProfileOptionsItemState.SUNSET));
            lore.add("  " + (state == ProfileOptionsItemState.NIGHT ? ChatColor.BLUE + StringEscapeUtils.unescapeHtml4("\u2022") + " "  : "  ") + ChatColor.WHITE + getOptionDescription(ProfileOptionsItemState.NIGHT));
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");

            return new ItemBuilder(item).lore(lore).build();
        }

        else if(this == TOGGLE_SCOREBOARD) {
            List<String> lore = new ArrayList<>(description);

            lore.add("  " + (state == ProfileOptionsItemState.ENABLED ? ChatColor.GREEN + StringEscapeUtils.unescapeHtml4("\u2713") + " " : "  ") + ChatColor.WHITE + getOptionDescription(ProfileOptionsItemState.ENABLED));
            lore.add("  " + (state == ProfileOptionsItemState.DISABLED ? ChatColor.RED + StringEscapeUtils.unescapeHtml4("\u2718") + " "  : "  ") + ChatColor.WHITE + getOptionDescription(ProfileOptionsItemState.DISABLED));
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");

            return new ItemBuilder(item).lore(lore).build();
        }

        return getItem(ProfileOptionsItemState.DISABLED);
    }

    public String getOptionDescription(ProfileOptionsItemState state) {
        if (this == DUEL_REQUESTS || this == PARTY_INVITES || this == ALLOW_SPECTATORS) {

            if (state == ProfileOptionsItemState.ENABLED) {
                return "Enable";
            } else if (state == ProfileOptionsItemState.DISABLED) {
                return "Disable";
            }
        }

        else if(this == TOGGLE_TIME) {
            if (state == ProfileOptionsItemState.DAY) {
                return "Day";
            } else if (state == ProfileOptionsItemState.SUNSET) {
                return "Sunset";
            } else if (state == ProfileOptionsItemState.NIGHT) {
                return "Night";
            }
        }

        else if(this == TOGGLE_SCOREBOARD) {
            if (state == ProfileOptionsItemState.ENABLED) {
                return "Enable";
            } else if (state == ProfileOptionsItemState.DISABLED) {
                return "Disable";
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


}
