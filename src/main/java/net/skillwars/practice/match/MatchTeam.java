package net.skillwars.practice.match;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import net.skillwars.practice.team.KillableTeam;

@Getter
public class MatchTeam extends KillableTeam {

	private final int teamID;

	public MatchTeam(UUID leader, List<UUID> players, int teamID) {
		super(leader, players);
		this.teamID = teamID;
	}
}
