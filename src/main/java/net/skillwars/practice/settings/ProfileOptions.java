package net.skillwars.practice.settings;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.skillwars.practice.settings.item.ProfileOptionsItem;
import net.skillwars.practice.settings.item.ProfileOptionsItemState;

import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

@Accessors(chain = true)
public class ProfileOptions {

    @Getter @Setter private boolean duelRequests = true;
    @Getter @Setter private boolean partyInvites = true;
    @Getter @Setter private boolean spectators = true;
    @Getter @Setter private boolean scoreboard = true;
    @Getter @Setter private ProfileOptionsItemState time = ProfileOptionsItemState.DAY;

    public Inventory getInventory() {
        Inventory toReturn = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Preferencias del servidor.");

        toReturn.setItem(11, ProfileOptionsItem.DUEL_REQUESTS.getItem(duelRequests ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(12, ProfileOptionsItem.PARTY_INVITES.getItem(partyInvites ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(13, ProfileOptionsItem.ALLOW_SPECTATORS.getItem(spectators ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(14, ProfileOptionsItem.TOGGLE_SCOREBOARD.getItem(scoreboard ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(15, ProfileOptionsItem.TOGGLE_TIME.getItem(time));

        for (int i = 0; i < toReturn.getSize(); i++) {
            if (toReturn.getItem(i) == null || toReturn.getItem(i).getType().equals(Material.AIR)) {
                toReturn.setItem(i, ProfileOptionsItem.getFill());
            }
        }

        return toReturn;
    }
}
