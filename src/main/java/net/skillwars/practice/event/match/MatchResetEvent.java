package net.skillwars.practice.event.match;

import net.skillwars.practice.match.Match;

public class MatchResetEvent extends MatchEvent {
    public MatchResetEvent(Match match) {
        super(match);
    }
}
