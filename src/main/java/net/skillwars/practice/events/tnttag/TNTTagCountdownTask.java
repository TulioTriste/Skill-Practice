package net.skillwars.practice.events.tnttag;

import net.skillwars.practice.events.EventCountdownTask;
import net.skillwars.practice.events.PracticeEvent;
import org.bukkit.ChatColor;

import java.util.Arrays;

public class TNTTagCountdownTask extends EventCountdownTask {

    public TNTTagCountdownTask(PracticeEvent event) {
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
        getEvent().sendMessage(ChatColor.RED + "No hay suficiente players. El Evento ha sido cancelado");
        getEvent().end();
        this.getEvent().getPlugin().getEventManager().setCooldown(0L);
    }
}
