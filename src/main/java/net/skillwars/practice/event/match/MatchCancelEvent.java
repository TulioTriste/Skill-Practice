package net.skillwars.practice.event.match;

import net.skillwars.practice.match.Match;

public class MatchCancelEvent extends MatchEvent {
    public MatchCancelEvent(Match match) {
        super(match);
    }
}
