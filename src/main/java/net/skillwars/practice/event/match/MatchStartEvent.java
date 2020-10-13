package net.skillwars.practice.event.match;

import net.skillwars.practice.match.Match;

public class MatchStartEvent extends MatchEvent {
    public MatchStartEvent(Match match) {
        super(match);
    }
}
