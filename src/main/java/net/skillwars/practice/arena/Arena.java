package net.skillwars.practice.arena;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.skillwars.practice.util.CustomLocation;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Arena {

    private final String name;

    private List<StandaloneArena> standaloneArenas;
    private List<StandaloneArena> availableArenas;

    private CustomLocation a;
    private CustomLocation b;

    private CustomLocation min;
    private CustomLocation max;

    private boolean enabled;

    public StandaloneArena getAvailableArena() {
        return this.availableArenas.remove(0);
    }

    public void addStandaloneArena(StandaloneArena arena) {
        this.standaloneArenas.add(arena);
    }

    public void addAvailableArena(StandaloneArena arena) {
        this.availableArenas.add(arena);
    }
}