package net.skillwars.practice.events.teamfights;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.joeleoli.nucleus.Nucleus;
import net.skillwars.practice.Practice;
import net.skillwars.practice.events.EventCountdownTask;
import net.skillwars.practice.events.EventState;
import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.nodebufflite.NoDebuffLitePlayer;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
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
		return Collections.singletonList(this.getPlugin().getSpawnManager().getTeamFightsLocation());
	}

	@Override
	public void onStart() {
		this.gameTask = new TeamFightGameTask();
		this.gameTask.runTaskTimerAsynchronously(getPlugin(), 0, 20L);
		this.fighting.clear();
		this.redTeam.clear();
		this.blueTeam.clear();
		this.generateTeams();
		this.prepareNextMatch();
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

				sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " ha sido eliminado.");
				this.getPlayers().remove(player.getUniqueId());

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

			if (this.blueTeam.size() == 0 || this.redTeam.size() == 0) {

				if (this.getState().equals(EventState.STARTED)) {
					gameTask.cancel();
				}

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
						winnerJoiner.add(player.getName());
						this.fighting.remove(player.getUniqueId());
					}
				}

				Bukkit.broadcastMessage("");
				Bukkit.broadcastMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "TeamFights Event " + ChatColor.AQUA.toString() + "Ganador: " + winnerTeamName);
				Bukkit.broadcastMessage("");

				end();
			}
		};
	}

	private void prepareNextMatch() {
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

					for (UUID uuid : redTeam) {

						Player player = Bukkit.getPlayer(uuid);

						if (streakPlayer != null && streakPlayer == player.getUniqueId()) {
							continue;
						}

						PlayerUtil.clearPlayer(player);
						getPlugin().getKitManager().getKit("TeamFights").applyToPlayer(player);
						player.updateInventory();

						player.teleport(Practice.getInstance().getSpawnManager().getTeamFightsFirst().toBukkitLocation());
					}

					for (UUID uuid : blueTeam) {

						Player player = Bukkit.getPlayer(uuid);

						if (streakPlayer != null && streakPlayer == player.getUniqueId()) {
							continue;
						}

						PlayerUtil.clearPlayer(player);
						getPlugin().getKitManager().getKit("TeamFights").applyToPlayer(player);
						player.updateInventory();

						player.teleport(Practice.getInstance().getSpawnManager().getTeamFightsSecond().toBukkitLocation());
					}
				}
			});
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
				player.sendMessage( CC.PRIMARY + "TeamFights " + CC.SECONDARY + "Has sido añadido al Team " + ChatColor.BLUE.toString() + ChatColor.BOLD + "BLUE" + CC.SECONDARY + ".");
			}
		}

		for(UUID uuid : this.redTeam) {
			Player player = Bukkit.getPlayer(uuid);
			if(player != null) {
				player.sendMessage(CC.PRIMARY + "TeamFights " + CC.SECONDARY + "Has sido añadido al Team " + ChatColor.RED.toString() + ChatColor.BOLD + "RED" + CC.SECONDARY + ".");
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
				PlayerUtil.sendMessage(ChatColor.AQUA + "La pelea comienza en " + ChatColor.GREEN + 3 + ChatColor.AQUA + "...", player);
			} else if (time == 1799) {
				PlayerUtil.sendMessage(ChatColor.AQUA + "La pelea comienza en " + ChatColor.GREEN + 2 + ChatColor.AQUA + "...", player);
			} else if (time == 1798) {
				PlayerUtil.sendMessage(ChatColor.AQUA + "La pelea comienza en " + ChatColor.GREEN + 1 + ChatColor.AQUA + "...", player);
			} else if (time == 1797) {
				PlayerUtil.sendMessage(ChatColor.GREEN + "La pelea ha iniciado, buena suerte!", player);
				this.teamPlayer.setState(TeamFightPlayer.TeamFightState.FIGHTING);
			}

			if (Arrays.asList(30, 25, 20, 15, 10).contains(time)) {
				PlayerUtil.sendMessage(ChatColor.AQUA + "La pelea se termina en " + ChatColor.GREEN + time + ChatColor.AQUA + "...", player);
			} else if (Arrays.asList(5, 4, 3, 2, 1).contains(time)) {
				PlayerUtil.sendMessage(ChatColor.AQUA + "La pelea terminara en " + ChatColor.GREEN + time + ChatColor.AQUA + "...", player);
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
				PlayerUtil.sendMessage(ChatColor.AQUA + "El juego se terminara en " + ChatColor.GREEN + time + ChatColor.AQUA + "...", getBukkitPlayers());
			} else if (Arrays.asList(5, 4, 3, 2, 1).contains(time)) {
				PlayerUtil.sendMessage(ChatColor.AQUA + "El juego terminara en " + ChatColor.GREEN + time + ChatColor.AQUA + "...", getBukkitPlayers());
			}

			time--;
		}
	}

}
