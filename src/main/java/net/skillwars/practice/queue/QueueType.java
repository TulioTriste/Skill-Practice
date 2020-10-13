package net.skillwars.practice.queue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueueType {

    UNRANKED("Unranked"),
    RANKED("Ranked");

    private final String name;

    public boolean isRanked() {
        return this == QueueType.RANKED;
    }

}
