package net.skillwars.practice.match;

import io.netty.util.internal.ConcurrentSet;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.arena.Arena;
import net.skillwars.practice.arena.StandaloneArena;
import net.skillwars.practice.inventory.InventorySnapshot;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.queue.QueueType;
import net.skillwars.practice.util.Clickable;
import net.skillwars.practice.util.TimeUtil;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Setter
@Getter
public class Match {

	private final Practice plugin = Practice.getInstance();

	private final Map<UUID, InventorySnapshot> snapshots = new HashMap<>();

	private final Set<Entity> entitiesToRemove = new HashSet<>();

	private final Set<BlockState> originalBlockChanges = Sets.newConcurrentHashSet();

	private final Set<Location> placedBlockLocations = Sets.newConcurrentHashSet();

	private final Map<Location, Block> originalBlocksMap = new HashMap<>();

	private final Set<UUID> spectators = new ConcurrentSet<>();

	private final Set<Integer> runnables = new HashSet<>();

	private final Set<UUID> haveSpectated = new HashSet<>();

	private final List<MatchTeam> teams;
	private final UUID matchId = UUID.randomUUID();
	private final QueueType type;
	private final Arena arena;
	private final Kit kit;

	private final boolean redrover;

	private StandaloneArena standaloneArena;
	private MatchState matchState = MatchState.STARTING;
	private int winningTeamId;
	private int countdown = 6;
	private long startTimestamp;

	public Match(Arena arena, Kit kit, QueueType type, MatchTeam... teams) {
		this(arena, kit, type, false, teams);
	}

	public Match(Arena arena, Kit kit, QueueType type, boolean redrover, MatchTeam... teams) {
		this.arena = arena;
		this.kit = kit;
		this.type = type;
		this.redrover = redrover;
		this.teams = Arrays.asList(teams);
	}

	public String getDuration() {
		if (this.isStarting()) {
			return "00:00";
		} else if (this.isEnding()) {
			return "Ending";
		} else {
			return TimeUtil.millisToTimer(this.getElapsedDuration());
		}
	}

	public boolean isStarting() {
		return this.matchState == MatchState.STARTING;
	}

	public boolean isFighting() {
		return this.matchState == MatchState.FIGHTING;
	}

	public boolean isEnding() {
		return this.matchState == MatchState.ENDING;
	}

	public long getElapsedDuration() {
		return System.currentTimeMillis() - this.startTimestamp;
	}

	public void addSpectator(UUID uuid) {
		this.spectators.add(uuid);
	}

	public void removeSpectator(UUID uuid) {
		this.spectators.remove(uuid);
	}

	public void addHaveSpectated(UUID uuid) {
		this.haveSpectated.add(uuid);
	}

	public boolean haveSpectated(UUID uuid) {
		return this.haveSpectated.contains(uuid);
	}

	public void addSnapshot(Player player) {
		this.snapshots.put(player.getUniqueId(), new InventorySnapshot(player, this));
	}

	public boolean hasSnapshot(UUID uuid) {
		return this.snapshots.containsKey(uuid);
	}

	public InventorySnapshot getSnapshot(UUID uuid) {
		return this.snapshots.get(uuid);
	}

	public void addEntityToRemove(Entity entity) {
		this.entitiesToRemove.add(entity);
	}

	public void removeEntityToRemove(Entity entity) {
		this.entitiesToRemove.remove(entity);
	}

	public void clearEntitiesToRemove() {
		this.entitiesToRemove.clear();
	}

	public void addRunnable(int id) {
		this.runnables.add(id);
	}

	public void addOriginalBlockChange(BlockState blockState) {
		this.originalBlockChanges.add(blockState);
	}

	public void removeOriginalBlockChange(BlockState blockState) {
		this.originalBlockChanges.remove(blockState);
	}

	public void addPlacedBlockLocation(Location location) {
		this.placedBlockLocations.add(location);
	}

	public void removePlacedBlockLocation(Location location) {
		this.placedBlockLocations.remove(location);
	}

	public void addOriginalBlocksMap(Location loc, Block block) {
		this.originalBlocksMap.put(loc, block);
	}

	public void broadcastWithSound(String message, Sound sound) {
		this.teams.forEach(team -> team.alivePlayers().forEach(player -> {
			player.sendMessage(message);
			player.playSound(player.getLocation(), sound, 10, 1);
		}));
		this.spectatorPlayers().forEach(spectator -> {
			spectator.sendMessage(message);
			spectator.playSound(spectator.getLocation(), sound, 10, 1);
		});
	}

	public void broadcast(String message) {
		this.teams.forEach(team -> team.alivePlayers().forEach(player -> player.sendMessage(message)));
		this.spectatorPlayers().forEach(spectator -> spectator.sendMessage(message));
	}

	public void broadcast(Clickable message) {
		this.teams.forEach(team -> team.alivePlayers().forEach(message::sendToPlayer));
		this.spectatorPlayers().forEach(message::sendToPlayer);
	}

	public Stream<Player> spectatorPlayers() {
		return this.spectators.stream().map(this.plugin.getServer()::getPlayer).filter(Objects::nonNull);
	}

	public int decrementCountdown() {
		return --this.countdown;
	}

	public boolean isParty() {
		return this.isFFA() || this.teams.get(0).getPlayers().size() != 1 && this.teams.get(1).getPlayers().size() != 1;
	}

	public boolean isPartyMatch() {
		return this.isFFA() || (this.teams.get(0).getPlayers().size() >= 2 || this.teams.get(1).getPlayers().size() >= 2);
	}

	public boolean isFFA() {
		return this.teams.size() == 1;
	}
}
