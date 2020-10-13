package net.skillwars.practice.events.teamfights;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.skillwars.practice.events.EventCountdownTask;
import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.util.CustomLocation;
import net.skillwars.practice.util.PlayerUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TeamFightEvent extends PracticeEvent<TeamFightPlayer> {

	private Map<UUID, TeamFightPlayer> players = new HashMap<>();

	@Getter private List<UUID> blueTeam = new ArrayList<>();
	@Getter private List<UUID> redTeam = new ArrayList<>();

	@Getter UUID streakPlayer = null;
	@Getter List<UUID> fighting = new ArrayList<>();

	@Getter private TeamFightGameTask gameTask = null;
	private TeamFightCountdownTask countdownTask = new TeamFightCountdownTask(this);

	public TeamFightEvent() {
		super("TeamFights");
	}

	@Override
	public Map<UUID, TeamFightPlayer> getPlayers() {
		return players;
	}

	@Override
	public EventCountdownTask getCountdownTask() {
		return countdownTask;
	}

	@Override
	public List<CustomLocation> getSpawnLocations() {
		return Collections.singletonList(this.getPlugin().getSpawnManager().getRedroverLocation());
	}

	@Override
	public void onStart() {
		this.gameTask = new TeamFightGameTask();
		this.gameTask.runTaskTimerAsynchronously(getPlugin(), 0, 20L);
		this.fighting.clear();
		this.redTeam.clear();
		this.blueTeam.clear();
		this.generateTeams();
	}

	@Override
	public Consumer<Player> onJoin() {
		return player -> players.put(player.getUniqueId(), new TeamFightPlayer(player.getUniqueId(), this));
	}

	@Override
	public Consumer<Player> onDeath() {

		return player -> {

			TeamFightPlayer data = getPlayer(player);

			if (data == null) {
				return;
			}

			if(data.getState() == TeamFightPlayer.TeamFightState.FIGHTING || data.getState() == TeamFightPlayer.TeamFightState.PREPARING) {

				if(data.getFightTask() != null) {
					data.getFightTask().cancel();
				}

				if(data.getFightPlayer() != null && data.getFightPlayer().getFightTask() != null) {
					data.getFightPlayer().getFightTask().cancel();
				}


				this.getPlayers().remove(player.getUniqueId());
				sendMessage(ChatColor.YELLOW + "(Event) " + ChatColor.RED + player.getName() + ChatColor.GRAY + " has been eliminated" + (Bukkit.getPlayer(data.getFightPlayer().getUuid()) == null ? "." : " by " + ChatColor.GREEN + Bukkit.getPlayer(data.getFightPlayer().getUuid()).getName()));

				getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
					getPlugin().getPlayerManager().sendToSpawnAndReset(player);
					if(getPlayers().size() >= 2) {
						getPlugin().getEventManager().addSpectatorTeamFights(player, getPlugin().getPlayerManager().getPlayerData(player.getUniqueId()), this);
					}
				});

				this.fighting.remove(player.getUniqueId());
				this.redTeam.remove(player.getUniqueId());
				this.blueTeam.remove(player.getUniqueId());
			}
		};
	}

	private CustomLocation[] getGameLocations() {
		CustomLocation[] array = new CustomLocation[2];
		array[0] = this.getPlugin().getSpawnManager().getRedroverFirst();
		array[1] = this.getPlugin().getSpawnManager().getRedroverSecond();
		return array;
	}

	private void prepareNextMatch() {

		if(this.blueTeam.size() == 0 || this.redTeam.size() == 0) {

			List<UUID> winnerTeam = getWinningTeam();
			String winnerTeamName = ChatColor.WHITE.toString() + ChatColor.BOLD + "Tie";

			if(this.redTeam.size() > this.blueTeam.size()) {
				winnerTeamName = ChatColor.RED.toString() + ChatColor.BOLD + "RED";
			} else if(this.blueTeam.size() > redTeam.size()) {
				winnerTeamName = ChatColor.BLUE.toString() + ChatColor.BOLD + "BLUE";
			}

			StringJoiner winnerJoiner = new StringJoiner(", ");

			if(winnerTeam != null && winnerTeam.size() > 0) {

				for (UUID winner : winnerTeam) {
					Player player = this.getPlugin().getServer().getPlayer(winner);

					if (player != null) {
						winnerJoiner.add(player.getName());
						this.fighting.remove(player.getUniqueId());
					}
				}
			}

			for (int i = 0; i <= 2; ++i) {
				String announce = ChatColor.YELLOW + "(Event) " + ChatColor.GREEN.toString() + "Winner: " + winnerTeamName + (winnerJoiner.length() == 0 ? "" : "\n" + ChatColor.YELLOW + "(Event) " + ChatColor.GRAY + winnerJoiner.toString());
				Bukkit.broadcastMessage(announce);
			}

			gameTask.cancel();
			end();
			return;
		}

		this.fighting.clear();

		List<TeamFightPlayer> redPlayers = new ArrayList<>();
		for (UUID reds : redTeam) {
			redPlayers.add(getPlayer(reds));
		}
		List<TeamFightPlayer> bluePlayers = new ArrayList<>();
		for (UUID blues : blueTeam) {
			bluePlayers.add(getPlayer(blues));
		}

		/*if(this.fighting.size() == 1 && this.redTeam.contains(this.fighting.get(0))) {
			redPlayer = getPlayer(this.fighting.get(0));
			this.streakPlayer = redPlayer.getUuid();
		} else if(this.fighting.size() == 1 && this.blueTeam.contains(this.fighting.get(0))) {
			bluePlayer = getPlayer(this.fighting.get(0));
			this.streakPlayer = bluePlayer.getUuid();
		}*/

		this.fighting.addAll(redTeam);
		this.fighting.addAll(blueTeam);

		for (UUID uuidPicked : fighting) {
			Player picked = Bukkit.getPlayer(uuidPicked);
			TeamFightPlayer teamFightPlayer = getPlayer(uuidPicked);

			teamFightPlayer.setState(TeamFightPlayer.TeamFightState.PREPARING);

			BukkitTask task = new TeamFightFightTask(picked, teamFightPlayer).runTaskTimer(getPlugin(), 0, 20);

			teamFightPlayer.setFightTask(task);

			this.getPlugin().getServer().getScheduler().runTask(this.getPlugin(), new Runnable() {
				@Override
				public void run() {

					Player[] players = new Player[] {picked};

					for(Player player : players) {

						if (streakPlayer != null && streakPlayer == player.getUniqueId()) {
							continue;
						}

						PlayerUtil.clearPlayer(player);
						getPlugin().getKitManager().getKit("NoDebuff").applyToPlayer(player);
						player.updateInventory();
					}

					picked.teleport(TeamFightEvent.this.getGameLocations()[0].toBukkitLocation());
				}
			});

			this.sendMessage(ChatColor.YELLOW + "(Event) " + ChatColor.GRAY.toString() + "Upcoming Match: " + ChatColor.RED + picked.getName() + ChatColor.GRAY + " vs. " + ChatColor.BLUE + picked.getName() + ChatColor.GRAY + ".");

		}

		/*Player picked1 = getPlugin().getServer().getPlayer(redPlayer.getUuid());
		Player picked2 = getPlugin().getServer().getPlayer(bluePlayer.getUuid());

		redPlayer.setState(TeamFightPlayer.TeamFightState.PREPARING);
		bluePlayer.setState(TeamFightPlayer.TeamFightState.PREPARING);

		redPlayer.setFightPlayer(bluePlayer);
		bluePlayer.setFightPlayer(redPlayer);

		redPlayer.setFightTask(task);
		bluePlayer.setFightTask(task);*/
	}

	private void generateTeams() {
		ArrayList<UUID> players = Lists.newArrayList(this.players.keySet());
		redTeam.addAll(players.subList(0, players.size() / 2 + players.size() % 2));
		blueTeam.addAll(players.subList(players.size() / 2 + players.size() % 2, players.size()));

		for(UUID uuid : this.blueTeam) {
			Player player = Bukkit.getPlayer(uuid);
			if(player != null) {
				player.sendMessage( ChatColor.YELLOW + "(Event) " + ChatColor.GRAY.toString() + "You have been added to the " + ChatColor.BLUE.toString() + ChatColor.BOLD + "BLUE" + ChatColor.GRAY + " Team.");
			}
		}

		for(UUID uuid : this.redTeam) {
			Player player = Bukkit.getPlayer(uuid);
			if(player != null) {
				player.sendMessage( ChatColor.YELLOW + "(Event) " + ChatColor.GRAY.toString() + "You have been added to the " + ChatColor.RED.toString() + ChatColor.BOLD + "RED" + ChatColor.GRAY + " Team.");
			}
		}
	}

	private List<UUID> getWinningTeam() {

		if(this.redTeam.size() > this.blueTeam.size()) {
			return this.redTeam;
		} else if(this.blueTeam.size() > redTeam.size()) {
			return this.blueTeam;
		}

		return null;
	}


	public List<UUID> getByState(TeamFightPlayer.TeamFightState state) {
		return players.values().stream().filter(player -> player.getState() == state).map(TeamFightPlayer::getUuid).collect(Collectors.toList());
	}


	/**
	 * To ensure that the fight doesn't go on forever and to
	 * let the players know how much time they have left.
	 */
	@Getter
	@RequiredArgsConstructor
	public class TeamFightFightTask extends BukkitRunnable {
		private final Player player;

		private final TeamFightPlayer teamPlayer;

		private int time = 1800;

		@Override
		public void run() {

			if (player == null || !player.isOnline()) {
				cancel();
				return;
			}

			if (time == 1800) {
				PlayerUtil.sendMessage(ChatColor.YELLOW + "The match starts in " + ChatColor.GREEN + 3 + ChatColor.YELLOW + "...", player);
			} else if (time == 1799) {
				PlayerUtil.sendMessage(ChatColor.YELLOW + "The match starts in " + ChatColor.GREEN + 2 + ChatColor.YELLOW + "...", player);
			} else if (time == 1798) {
				PlayerUtil.sendMessage(ChatColor.YELLOW + "The match starts in " + ChatColor.GREEN + 1 + ChatColor.YELLOW + "...", player);
			} else if (time == 1797) {
				PlayerUtil.sendMessage(ChatColor.GREEN + "The match has started, good luck!", player);
				this.teamPlayer.setState(TeamFightPlayer.TeamFightState.FIGHTING);
			}

			if (Arrays.asList(30, 25, 20, 15, 10).contains(time)) {
				PlayerUtil.sendMessage(ChatColor.YELLOW + "The match ends in " + ChatColor.GREEN + time + ChatColor.YELLOW + "...", player);
			} else if (Arrays.asList(5, 4, 3, 2, 1).contains(time)) {
				PlayerUtil.sendMessage(ChatColor.YELLOW + "The match is ending in " + ChatColor.GREEN + time + ChatColor.YELLOW + "...", player);
			}

			/*if (time <= 0) {
				Player winner = players.get(ThreadLocalRandom.current().nextInt(players.size()));
				players.stream().filter(pl -> !pl.equals(winner)).forEach(pl -> onDeath().accept(pl));

				cancel();
				return;
			}*/
			time--;
		}
	}
	
	/**
	 * To ensure that the fight doesn't go on forever and to
	 * let the players know how much time they have left.
	 */
	@Getter
	@Setter
	@RequiredArgsConstructor
	public class TeamFightGameTask extends BukkitRunnable {

		private int time = 1200;

		@Override
		public void run() {

			if(time == 1800) {
				prepareNextMatch();
			}

			if (Arrays.asList(60, 50, 40, 30, 25, 20, 15, 10).contains(time)) {
				PlayerUtil.sendMessage(ChatColor.YELLOW + "The game ends in " + ChatColor.GREEN + time + ChatColor.YELLOW + "...", getBukkitPlayers());
			} else if (Arrays.asList(5, 4, 3, 2, 1).contains(time)) {
				PlayerUtil.sendMessage(ChatColor.YELLOW + "The game is ending in " + ChatColor.GREEN + time + ChatColor.YELLOW + "...", getBukkitPlayers());
			}

			time--;
		}
	}

}
