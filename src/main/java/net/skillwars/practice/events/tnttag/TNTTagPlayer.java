package net.skillwars.practice.events.tnttag;

import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.events.EventPlayer;
import net.skillwars.practice.events.PracticeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

@Setter
@Getter
public class TNTTagPlayer extends EventPlayer {

    private TNTTagState state = TNTTagState.WAITING;
    private BukkitTask fightTask;
    private TNTTagPlayer fighting;

    public TNTTagPlayer(UUID uuid, PracticeEvent event) {
        super(uuid, event);
    }

    public enum TNTTagState {
        WAITING, PREPARING, FIGHTING, ELIMINATED
    }
}
