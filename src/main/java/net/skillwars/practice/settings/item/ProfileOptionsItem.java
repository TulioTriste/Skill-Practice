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

    DUEL_REQUESTS(UtilItem.createItem(Material.LEASH, ChatColor.WHITE.toString() + ChatColor.BOLD + "Duel Requests", 1), "Do you want to accept duel requests?"),
    PARTY_INVITES(UtilItem.createItem(Material.PAPER, ChatColor.WHITE.toString() + ChatColor.BOLD + "Party Invites", 1), "Do you want to accept party invitations?"),
    TOGGLE_SCOREBOARD(UtilItem.createItem(Material.EMPTY_MAP, ChatColor.WHITE.toString() + ChatColor.BOLD + "Toggle Scoreboard", 1), "Toggle your scoreboard"),
    ALLOW_SPECTATORS(UtilItem.createItem(Material.COMPASS, ChatColor.WHITE.toString() + ChatColor.BOLD + "Allow Spectators", 1), "Allow players to spectate your matches?" + ChatColor.RED  + " Only donators"),
    TOGGLE_TIME(UtilItem.createItem(Material.SLIME_BALL, ChatColor.WHITE.toString() + ChatColor.BOLD + "Toggle Time", 1), "Toggle between day, sunset & night");

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
                this.description.add(ChatColor.GRAY + parts.toString().trim());
                parts = new StringBuilder();
            }
        }

        this.description.add(" ");
    }

    public ItemStack getItem(ProfileOptionsItemState state) {
        if (this == DUEL_REQUESTS || this == PARTY_INVITES || this == ALLOW_SPECTATORS) {
            List<String> lore = new ArrayList<>(description);

            lore.add("  " + (state == ProfileOptionsItemState.ENABLED ? ChatColor.GREEN + StringEscapeUtils.unescapeHtml4("&#9658;") + " " : "  ") + ChatColor.GRAY + getOptionDescription(ProfileOptionsItemState.ENABLED));
            lore.add("  " + (state == ProfileOptionsItemState.DISABLED ? ChatColor.RED + StringEscapeUtils.unescapeHtml4("&#9658;") + " "  : "  ") + ChatColor.GRAY + getOptionDescription(ProfileOptionsItemState.DISABLED));

            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");

            return new ItemBuilder(item).lore(lore).build();
        }

        else if(this == TOGGLE_TIME) {
            List<String> lore = new ArrayList<>(description);

            lore.add("  " + (state == ProfileOptionsItemState.DAY ? ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("&#9658;") + " " : "  ") + ChatColor.GRAY + getOptionDescription(ProfileOptionsItemState.DAY));
            lore.add("  " + (state == ProfileOptionsItemState.SUNSET ? ChatColor.GOLD + StringEscapeUtils.unescapeHtml4("&#9658;") + " "  : "  ") + ChatColor.GRAY + getOptionDescription(ProfileOptionsItemState.SUNSET));
            lore.add("  " + (state == ProfileOptionsItemState.NIGHT ? ChatColor.BLUE + StringEscapeUtils.unescapeHtml4("&#9658;") + " "  : "  ") + ChatColor.GRAY + getOptionDescription(ProfileOptionsItemState.NIGHT));
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");

            return new ItemBuilder(item).lore(lore).build();
        }

        else if(this == TOGGLE_SCOREBOARD) {
            List<String> lore = new ArrayList<>(description);

            lore.add("  " + (state == ProfileOptionsItemState.ENABLED ? ChatColor.GREEN + StringEscapeUtils.unescapeHtml4("&#9658;") + " " : "  ") + ChatColor.GRAY + getOptionDescription(ProfileOptionsItemState.ENABLED));
            lore.add("  " + (state == ProfileOptionsItemState.DISABLED ? ChatColor.RED + StringEscapeUtils.unescapeHtml4("&#9658;") + " "  : "  ") + ChatColor.GRAY + getOptionDescription(ProfileOptionsItemState.DISABLED));
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
