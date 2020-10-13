package net.skillwars.practice.events.sumo;

import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.events.EventPlayer;
import net.skillwars.practice.events.PracticeEvent;

import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

@Setter
@Getter
public class SumoPlayer extends EventPlayer {

    private SumoState state = SumoState.WAITING;
    private BukkitTask fightTask;
    private SumoPlayer fighting;

    public SumoPlayer(UUID uuid, PracticeEvent event) {
        super(uuid, event);
    }

    public enum SumoState {
        WAITING, PREPARING, FIGHTING, ELIMINATED
    }
}
