package net.skillwars.practice.board;

import com.google.common.collect.Lists;
import me.joeleoli.frame.FrameAdapter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.cache.StatusCache;
import net.skillwars.practice.events.EventState;
import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.sumo.SumoEvent;
import net.skillwars.practice.events.sumo.SumoPlayer;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.queue.QueueEntry;
import net.skillwars.practice.queue.QueueType;
import net.skillwars.practice.tournament.Tournament;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.Color;
import net.skillwars.practice.util.PlayerUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PracticeAdapter implements FrameAdapter {

	private Practice plugin = Practice.getInstance();
	Config config = new Config("scoreboard", this.plugin);

	@Override
	public String getTitle(Player player) {
		return CC.translate(config.getConfig().getString("title"));
	}

	@Override
	public List<String> getLines(Player player) {
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

		if (playerData == null) {
			this.plugin.getLogger().warning(player.getName() + "'s player data is null");
			return null;
		}

		if (!playerData.getOptions().isScoreboard()) {
			return null;
		}

		switch (playerData.getPlayerState()) {
		case LOADING:
		case EDITING:
		case FFA:
		case SPAWN:
		case EVENT:
		case SPECTATING:
			return this.getLobbyBoard(player, false);
		case QUEUE:
			return this.getLobbyBoard(player, true);
		case FIGHTING:
			return this.getGameBoard(player);
		}

		return null;
	}

	private List<String> getLobbyBoard(Player player, boolean queuing) {
		List<String> lines = Lists.newLinkedList();
		Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
		PracticeEvent event = this.plugin.getEventManager().getEventPlaying(player);

		if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
			event = this.plugin.getEventManager().getSpectators().get(player.getUniqueId());
		}

		PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

		for (String string : config.getConfig().getStringList("lobby.lines")) {

			if (string.contains("{lobby-event}")) {
				if (event == null) {
					for (String linessb : config.getConfig().getStringList("lobby.event-in-lobby")) {
						int eventTime = (int) ((this.plugin.getEventManager().getCooldown() - System.currentTimeMillis()) / 1000);
						if (eventTime >= 1L) {
							linessb = linessb.replace("{event}", String.valueOf(eventTime));
						} else {
							linessb = linessb.replace("{event}", this.plugin.getEventManager().getName());
						}
						lines.add(Color.translate(linessb));
					}
				}
				continue;
			}

			if (string.contains("{no-fight}")) {
				if (event == null) {
					for (String linessb : config.getConfig().getStringList("lobby.no-fight")) {

						if (linessb.contains("{online}")) {
							linessb = linessb.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
						}
						if (linessb.contains("{fights}")) {
							linessb = linessb.replace("{fights}", String.valueOf(StatusCache.getInstance().getFighting()));
						}
						lines.add(Color.translate(linessb));
					}
				}
				continue;
			}

			if (string.contains("{in-queue}")) {
				if (queuing) {
					for (String linessb : config.getConfig().getStringList("lobby.in-queue")) {
						QueueEntry queueEntry = party == null
								? this.plugin.getQueueManager().getQueueEntry(player.getUniqueId())
								: this.plugin.getQueueManager().getQueueEntry(party.getLeader());
						if (linessb.contains("{queue-name}")) {
							linessb = linessb.replace("{queue-name}", queueEntry.getQueueType().getName());
						}
						if (linessb.contains("{queue-kitname}")) {
							linessb = linessb.replace("{queue-kitname}", queueEntry.getKitName());
						}
						if (linessb.contains("{queue-ranked}")) {
							if (queueEntry.getQueueType().equals(QueueType.RANKED)) {
								for (String linessb2 : config.getConfig()
										.getStringList("lobby.in-queue-ranked-lines")) {
									long queueTime = System.currentTimeMillis() - (party == null
											? this.plugin.getQueueManager().getPlayerQueueTime(player.getUniqueId())
											: this.plugin.getQueueManager().getPlayerQueueTime(party.getLeader()));

									int eloRange = playerData.getEloRange();

									int seconds = Math.round(queueTime / 1000L);
									if (seconds > 5) {
										if (eloRange != -1) {
											eloRange += seconds * 50;
											if (eloRange >= 3000) {
												eloRange = 3000;
											}
										}
									}

									int elo = playerData.getElo(queueEntry.getKitName());
									String eloRangeString = "[" + Math.max(elo - eloRange / 2, 0) + " -> "
											+ Math.max(elo + eloRange / 2, 0) + "]";
									if (linessb2.contains("{range}")) {
										linessb2 = linessb2.replace("{range}", eloRangeString);
									}
									lines.add(Color.translate(linessb2));
								}
							}
							continue;
						}
						lines.add(Color.translate(linessb));
					}
				}
				continue;
			}

			if (string.contains("{party}")) {
				if (party != null) {
					for (String linessb : config.getConfig().getStringList("lobby.in-party")) {
						if (linessb.contains("{party-members}")) {
							linessb = linessb.replace("{party-members}", String.valueOf(party.getMembers().size()));
						}
						if (linessb.contains("{party-leader}")) {
							linessb = linessb.replace("{party-leader}", Bukkit.getPlayer(party.getLeader()).getName());
						}
						lines.add(Color.translate(linessb));
					}
				}
				continue;
			}

			if (string.contains("{events}")) {
				if (event != null) {
					for (String linessb : config.getConfig().getStringList("lobby.in-event")) {
						if (linessb.contains("{sumo}")) {
							if (event instanceof SumoEvent) {
                                SumoEvent sumoEvent = (SumoEvent) event;
								int playingSumo = sumoEvent.getByState(SumoPlayer.SumoState.WAITING).size() + sumoEvent.getByState(SumoPlayer.SumoState.FIGHTING).size() + sumoEvent.getByState(SumoPlayer.SumoState.PREPARING).size();
								int limitSumo = sumoEvent.getLimit();
								for (String linessb2 : config.getConfig().getStringList("lobby.in-event-sumo-lines")) {
									linessb2 = linessb2.replace("{players}", String.valueOf(playingSumo))
									.replace("{limit}", String.valueOf(limitSumo));
									if (linessb2.contains("{starting}")) {
										int countdown = sumoEvent.getCountdownTask().getTimeUntilStart();
										if (countdown > 0 && countdown <= 60) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-sumo-starting-lines")) {
												linessb3 = linessb3.replace("{time}", String.valueOf(countdown));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{state}")) {
										if (sumoEvent.getPlayer(player) != null) {
											SumoPlayer sumoPlayer = sumoEvent.getPlayer(player);
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-sumo-state-lines")) {
												linessb3 = linessb3.replace("{state}", StringUtils.capitalize(sumoPlayer.getState().name().toLowerCase()));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{fight}")) {
										if (sumoEvent.getState().equals(EventState.STARTED)) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-sumo-fighting-lines")) {
												List<String> players = new ArrayList<>(sumoEvent.getFighting());
												Player firstPlayer = Bukkit.getPlayer(players.get(0));
												Player secondPlayer = Bukkit.getPlayer(players.get(1));
												linessb3 = linessb3.replace("{firstplayer}", firstPlayer.getName())
												.replace("{firstping}", String.valueOf(PlayerUtil.getPing(firstPlayer)))
												.replace("{secondplayer}", secondPlayer.getName())
												.replace("{secondping}", String.valueOf(PlayerUtil.getPing(secondPlayer)));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									lines.add(Color.translate(linessb2));
								}
							}
							continue;
						}
						linessb = linessb.replace("{event}", event.getName());
						lines.add(Color.translate(linessb));
					}
				}
				continue;
			}

			if (string.contains("{tournament}")) {
				if (playerData.getPlayerState() != PlayerState.EVENT && this.plugin.getTournamentManager().getTournaments().size() >= 1) {
					for (Tournament tournament : this.plugin.getTournamentManager().getTournaments().values()) {
						for (String linessb : config.getConfig().getStringList("lobby.in-tournament")) {
							if (linessb.contains("{countdown}")) {
								int countdown = tournament.getCountdown();
								if (countdown > 0 && countdown <= 30) {
									for (String linessb2 : config.getConfig().getStringList("lobby.in-tournament-countdown-lines")) {
										linessb2 = linessb2.replace("{countdown}", String.valueOf(countdown));
										lines.add(Color.translate(linessb2));
									}
								}
								continue;
							}
							linessb = linessb.replace("{ladder}", tournament.getKitName())
							.replace("{size}", String.valueOf(tournament.getTeamSize()))
							.replace("{stage}", String.valueOf(tournament.getCurrentRound()))
							.replace("{players}", String.valueOf(tournament.getPlayers().size()))
							.replace("{maxplayers}", String.valueOf(tournament.getSize()));
							lines.add(Color.translate(linessb));
						}
					}
				}
				continue;
			}

			lines.add(Color.translate(string));
		}
		return lines;
	}

	private List<String> getGameBoard(Player player) {
		List<String> lines = Lists.newLinkedList();
		PlayerData playerData = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
		Match match = plugin.getMatchManager().getMatch(playerData);

		for (String string : config.getConfig().getStringList("fight.lines")) {

			if (string.contains("{normal-match}")) {
				if (!match.isPartyMatch() && !match.isFFA() && !match.isParty()) {
					for (String linessb : config.getConfig().getStringList("fight.normal-match")) {
						if (linessb.contains("{opponent}")) {
							Player opponentPlayer = match.getTeams().get(0).getPlayers().get(0) == player.getUniqueId()
									? this.plugin.getServer().getPlayer(match.getTeams().get(1).getPlayers().get(0))
									: this.plugin.getServer().getPlayer(match.getTeams().get(0).getPlayers().get(0));
							linessb = linessb.replace("{opponent}", opponentPlayer.getName());
						}
						lines.add(Color.translate(linessb));
					}
				}
				continue;
			}

			if (string.contains("{party-ffa-match}")) {
				if (match.isFFA()) {
					for (String linessb : config.getConfig().getStringList("fight.in-party-ffa-match")) {
						MatchTeam playerTeam = match.getTeams().get(playerData.getTeamID());
						if (linessb.contains("{ffa-left-players}")) {
							linessb = linessb.replace("{ffa-left-players}",
									String.valueOf(playerTeam.getAlivePlayers().size()));
						}
						if (linessb.contains("{ffa-max-players}")) {
							linessb = linessb.replace("{ffa-max-players}",
									String.valueOf(playerTeam.getPlayers().size()));
						}
						lines.add(Color.translate(linessb));
					}
				}
				continue;
			}

			if (string.contains("{party-teams-match}")) {
				if(match.isPartyMatch()) {
					for (String linessb : config.getConfig().getStringList("fight.in-party-teams-match")) {
			            MatchTeam opposingTeam = playerData.getTeamID() == 0 ? match.getTeams().get(1) : match.getTeams().get(0);
			            MatchTeam playerTeam = match.getTeams().get(playerData.getTeamID());
						if(opposingTeam.getPlayers().size() == 2 && playerTeam.getPlayers().size() == 2) {
							if(linessb.contains("{opponent-left-players}")) {
								linessb = linessb.replace("{opponent-left-players}", String.valueOf(opposingTeam.getAlivePlayers().size()));
							}
							if(linessb.contains("{opponent-max-players}")) {
								linessb = linessb.replace("{opponent-max-players}", String.valueOf(opposingTeam.getPlayers().size()));
							}
							if(linessb.contains("{party-left-players}")) {
								linessb = linessb.replace("{party-left-players}", String.valueOf(playerTeam.getAlivePlayers().size()));
							}
							if(linessb.contains("{party-max-players}")) {
								linessb = linessb.replace("{party-max-players}", String.valueOf(playerTeam.getPlayers().size()));
							}
						}
						lines.add(Color.translate(linessb));
					}
				}
				continue;
			}

			if (string.contains("{duration}")) {
				string = string.replace("{duration}", match.getDuration());
			}

			lines.add(Color.translate(string));
		}

		return lines;
	}
}