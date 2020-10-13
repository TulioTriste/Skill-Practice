package net.skillwars.practice.events.ffa;

import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.events.EventPlayer;
import net.skillwars.practice.events.PracticeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

@Setter
@Getter
public class FFAPlayer extends EventPlayer {

    private FFAState state = FFAState.WAITING;
    private BukkitTask fightTask;
    private FFAPlayer fighting;

    public FFAPlayer(UUID uuid, PracticeEvent event) {
        super(uuid, event);
    }

    public enum FFAState {
        WAITING, PREPARING, FIGHTING, ELIMINATED
    }
}
