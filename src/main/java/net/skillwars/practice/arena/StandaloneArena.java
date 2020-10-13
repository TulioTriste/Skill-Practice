package net.skillwars.practice.arena;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.skillwars.practice.util.CustomLocation;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class StandaloneArena {

    private CustomLocation a;
    private CustomLocation b;

    private CustomLocation min;
    private CustomLocation max;

}