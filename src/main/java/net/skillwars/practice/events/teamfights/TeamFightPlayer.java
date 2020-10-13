package net.skillwars.practice.events.teamfights;

import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.events.EventPlayer;
import net.skillwars.practice.events.PracticeEvent;

import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

@Setter
@Getter
public class TeamFightPlayer extends EventPlayer {

	private TeamFightState state = TeamFightState.WAITING;
	private TeamFightPlayer fightPlayer;
	private BukkitTask fightTask;

	public TeamFightPlayer(UUID uuid, PracticeEvent event) {
		super(uuid, event);
	}

	public enum TeamFightState {
		WAITING, PREPARING, FIGHTING

	}
}
