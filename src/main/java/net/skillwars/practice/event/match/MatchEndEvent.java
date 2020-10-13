package net.skillwars.practice.event.match;

import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchTeam;

public class MatchEndEvent extends MatchEvent {
    private MatchTeam winningTeam;
    private MatchTeam losingTeam;

    public MatchEndEvent(Match match, MatchTeam winningTeam, MatchTeam losingTeam) {
        super(match);
        this.winningTeam = winningTeam;
        this.losingTeam = losingTeam;
    }

    public MatchTeam getWinningTeam() {
        return this.winningTeam;
    }

    public MatchTeam getLosingTeam() {
        return this.losingTeam;
    }
}
