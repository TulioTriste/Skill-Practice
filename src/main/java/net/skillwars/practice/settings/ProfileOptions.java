package net.skillwars.practice.settings;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.skillwars.practice.settings.item.ProfileOptionsItem;
import net.skillwars.practice.settings.item.ProfileOptionsItemState;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

@Accessors(chain = true)
public class ProfileOptions {

    @Getter @Setter private boolean duelRequests = true;
    @Getter @Setter private boolean partyInvites = true;
    @Getter @Setter private boolean spectators = true;
    @Getter @Setter private boolean scoreboard = true;
    @Getter @Setter private ProfileOptionsItemState time = ProfileOptionsItemState.DAY;

    public Inventory getInventory() {
        Inventory toReturn = Bukkit.createInventory(null, 9, "Settings");

        toReturn.setItem(0  , ProfileOptionsItem.DUEL_REQUESTS.getItem(duelRequests ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(2, ProfileOptionsItem.PARTY_INVITES.getItem(partyInvites ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(4, ProfileOptionsItem.ALLOW_SPECTATORS.getItem(spectators ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(6, ProfileOptionsItem.TOGGLE_SCOREBOARD.getItem(scoreboard ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(8, ProfileOptionsItem.TOGGLE_TIME.getItem(time));
        return toReturn;
    }

}
