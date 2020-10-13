package net.skillwars.practice.event.match;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.skillwars.practice.match.Match;

public class MatchEvent extends Event {
    private static HandlerList HANDLERS;

    static {
        HANDLERS = new HandlerList();
    }

    private Match match;

    public MatchEvent(Match match) {
        this.match = match;
    }

    public static HandlerList getHandlerList() {
        return MatchEvent.HANDLERS;
    }

    public HandlerList getHandlers() {
        return MatchEvent.HANDLERS;
    }

    public Match getMatch() {
        return this.match;
    }
}
