package net.skillwars.practice.events.teamfights;

import org.bukkit.ChatColor;

import net.skillwars.practice.events.EventCountdownTask;
import net.skillwars.practice.events.PracticeEvent;

import java.util.Arrays;

public class TeamFightCountdownTask extends EventCountdownTask {

	public TeamFightCountdownTask(PracticeEvent event) {
		super(event, 60);
	}

	@Override
	public boolean shouldAnnounce(int timeUntilStart) {
		return Arrays.asList(45, 30, 15, 10, 5).contains(timeUntilStart);
	}

	@Override
	public boolean canStart() {
		return getEvent().getPlayers().size() >= 2;
	}

	@Override
	public void onCancel() {
		getEvent().sendMessage(ChatColor.RED + "Not enough players. Event has been cancelled");
		getEvent().end();
		this.getEvent().getPlugin().getEventManager().setCooldown(0L);
	}
}
