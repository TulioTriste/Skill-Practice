package net.skillwars.practice.event;

import net.skillwars.practice.events.PracticeEvent;

public class EventStartEvent extends BaseEvent {
    private PracticeEvent event;

    public EventStartEvent(PracticeEvent event) {
        this.event = event;
    }

    public PracticeEvent getEvent() {
        return this.event;
    }
}
