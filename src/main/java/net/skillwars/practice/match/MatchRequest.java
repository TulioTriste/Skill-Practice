package net.skillwars.practice.match;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.skillwars.practice.arena.Arena;

@Getter
@RequiredArgsConstructor
public class MatchRequest {
	private final UUID requester;
	private final UUID requested;

	private final Arena arena;
	private final String kitName;
	private final boolean party;
}
