package net.skillwars.practice.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.skillwars.practice.player.PlayerData;

public class PlayerDataRetrieveEvent extends Event {
    private static HandlerList HANDLERS;

    static {
        HANDLERS = new HandlerList();
    }

    private PlayerData playerData;

    public PlayerDataRetrieveEvent(PlayerData playerData) {
        this.playerData = playerData;
    }

    public static HandlerList getHandlerList() {
        return PlayerDataRetrieveEvent.HANDLERS;
    }

    public HandlerList getHandlers() {
        return PlayerDataRetrieveEvent.HANDLERS;
    }

    public PlayerData getPlayerData() {
        return this.playerData;
    }
}
