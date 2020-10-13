package net.skillwars.practice.events.nodebufflite;

import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.events.EventPlayer;
import net.skillwars.practice.events.PracticeEvent;

import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

@Setter
@Getter
public class NoDebuffLitePlayer extends EventPlayer {

    private MiniNoDebuffState state = MiniNoDebuffState.WAITING;
    private BukkitTask fightTask;
    private NoDebuffLitePlayer fighting;

    public NoDebuffLitePlayer(UUID uuid, PracticeEvent event) {
        super(uuid, event);
    }

    public enum MiniNoDebuffState{
        WAITING, PREPARING, FIGHTING, ELIMINATED
    }
}
