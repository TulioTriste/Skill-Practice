package net.skillwars.practice.adapters;

import com.google.common.collect.Lists;
import me.joansiitoh.datas.GlobalBridge;
import me.joeleoli.frame.FrameAdapter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.cache.StatusCache;
import net.skillwars.practice.events.EventState;
import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.ffa.FFAEvent;
import net.skillwars.practice.events.ffa.FFAPlayer;
import net.skillwars.practice.events.nodebufflite.NoDebuffLiteEvent;
import net.skillwars.practice.events.nodebufflite.NoDebuffLitePlayer;
import net.skillwars.practice.events.sumo.SumoEvent;
import net.skillwars.practice.events.sumo.SumoPlayer;
import net.skillwars.practice.events.teamfights.TeamFightEvent;
import net.skillwars.practice.events.teamfights.TeamFightPlayer;
import net.skillwars.practice.events.tnttag.TNTTagEvent;
import net.skillwars.practice.events.tnttag.TNTTagPlayer;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchState;
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
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
			player.kickPlayer("Porfavor reinicia.");
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
				if (this.plugin.getMatchManager().getSpectatorPlayer().get(player.getUniqueId()) != null) {
					return getSpectateBoard(player);
				}
				return getLobbyBoard(player, false);
			case QUEUE:
				return getLobbyBoard(player, true);
			case FIGHTING:
				return getGameBoard(player);
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

						if (eventTime >= 1L) linessb = linessb.replace("{event}", String.valueOf(eventTime));

						else linessb = linessb.replace("{event}", this.plugin.getEventManager().getName());

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
						if (linessb.contains("{queue}")) {
							linessb = linessb.replace("{queue}", String.valueOf(StatusCache.getInstance().getQueueing()));
						}
						lines.add(Color.translate(linessb));
					}
				}
				if (player.getName().equalsIgnoreCase("TulioTriste")) {
					double tps = Bukkit.spigot().getTPS()[1];
					lines.add(Color.translate("&fTPS: &r" + formatTps(tps)));
				}
				continue;
			}

			if (string.contains("{in-queue}")) {
				if (queuing) {
					for (String linessb : config.getConfig().getStringList("lobby.in-queue")) {
						QueueEntry queueEntry = party == null
								? this.plugin.getQueueManager().getQueueEntry(player.getUniqueId())
								: this.plugin.getQueueManager().getQueueEntry(party.getLeader());
						if (queueEntry != null) {
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
						else if (linessb.contains("{ffa}")) {
							if (event instanceof FFAEvent) {
								FFAEvent ffaEvent = (FFAEvent) event;
								int playingFFA = ffaEvent.getByState(FFAPlayer.FFAState.WAITING).size() + ffaEvent.getByState(FFAPlayer.FFAState.FIGHTING).size() + ffaEvent.getByState(FFAPlayer.FFAState.PREPARING).size();
								int limitFFA = ffaEvent.getLimit();
								for (String linessb2 : config.getConfig().getStringList("lobby.in-event-ffa-lines")) {
									linessb2 = linessb2.replace("{players}", String.valueOf(playingFFA))
											.replace("{limit}", String.valueOf(limitFFA));
									if (linessb2.contains("{starting}")) {
										int countdown = ffaEvent.getCountdownTask().getTimeUntilStart();
										if (countdown > 0 && countdown <= 60) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-ffa-starting-lines")) {
												linessb3 = linessb3.replace("{time}", String.valueOf(countdown));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{state}")) {
										if (ffaEvent.getPlayer(player) != null) {
											FFAPlayer ffaPlayer = ffaEvent.getPlayer(player);
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-ffa-state-lines")) {
												linessb3 = linessb3.replace("{state}", StringUtils.capitalize(ffaPlayer.getState().name().toLowerCase()));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{fight}")) {
										if (ffaEvent.getState().equals(EventState.STARTED)) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-ffa-fighting-lines")) {
												List<String> players = new ArrayList<>(ffaEvent.getFighting());
												linessb3 = linessb3.replace("{leftPlayers}", String.valueOf(players.size()))
												.replace("{maxPlayers}", String.valueOf(ffaEvent.getPlayers().size()));
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
						else if (linessb.contains("{nodebufflite}")) {
							if (event instanceof NoDebuffLiteEvent) {
								NoDebuffLiteEvent ndlEvent = (NoDebuffLiteEvent) event;
								int playingNDL = ndlEvent.getByState(NoDebuffLitePlayer.NoDebuffLiteState.WAITING).size() + ndlEvent.getByState(NoDebuffLitePlayer.NoDebuffLiteState.FIGHTING).size() + ndlEvent.getByState(NoDebuffLitePlayer.NoDebuffLiteState.PREPARING).size();
								int limitNDL = ndlEvent.getLimit();
								for (String linessb2 : config.getConfig().getStringList("lobby.in-event-nodebufflite-lines")) {
									linessb2 = linessb2.replace("{players}", String.valueOf(playingNDL))
											.replace("{limit}", String.valueOf(limitNDL));
									if (linessb2.contains("{starting}")) {
										int countdown = ndlEvent.getCountdownTask().getTimeUntilStart();
										if (countdown > 0 && countdown <= 60) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-nodebufflite-starting-lines")) {
												linessb3 = linessb3.replace("{time}", String.valueOf(countdown));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{state}")) {
										if (ndlEvent.getPlayer(player) != null) {
											NoDebuffLitePlayer ndlPlayer = ndlEvent.getPlayer(player);
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-nodebufflite-state-lines")) {
												linessb3 = linessb3.replace("{state}", StringUtils.capitalize(ndlPlayer.getState().name().toLowerCase()));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{fight}")) {
										if (ndlEvent.getState().equals(EventState.STARTED)) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-nodebufflite-fighting-lines")) {
												List<String> players = new ArrayList<>(ndlEvent.getFighting());
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
						else if (linessb.contains("{teamfights}")) {
							if (event instanceof TeamFightEvent) {
								TeamFightEvent teamfightEvent = (TeamFightEvent) event;
								int playingTeamFight = teamfightEvent.getByState(TeamFightPlayer.TeamFightState.WAITING).size() + teamfightEvent.getByState(TeamFightPlayer.TeamFightState.FIGHTING).size() + teamfightEvent.getByState(TeamFightPlayer.TeamFightState.PREPARING).size();
								int limitTeamFight = teamfightEvent.getLimit();
								for (String linessb2 : config.getConfig().getStringList("lobby.in-event-teamfights-lines")) {
									linessb2 = linessb2.replace("{players}", String.valueOf(playingTeamFight))
											.replace("{limit}", String.valueOf(limitTeamFight));
									if (linessb2.contains("{starting}")) {
										int countdown = teamfightEvent.getCountdownTask().getTimeUntilStart();
										if (countdown > 0 && countdown <= 60) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-teamfights-starting-lines")) {
												linessb3 = linessb3.replace("{time}", String.valueOf(countdown));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{state}")) {
										if (teamfightEvent.getPlayer(player) != null) {
											TeamFightPlayer teamfightPlayer = teamfightEvent.getPlayer(player);
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-teamfights-state-lines")) {
												linessb3 = linessb3.replace("{state}", StringUtils.capitalize(teamfightPlayer.getState().name().toLowerCase()));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{fight}")) {
										if (teamfightEvent.getState().equals(EventState.STARTED)) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-teamfights-fighting-lines")) {
												List<UUID> bluePlayers = new ArrayList<>(teamfightEvent.getBlueTeam());
												List<UUID> redPlayers = new ArrayList<>(teamfightEvent.getRedTeam());
												linessb3 = linessb3.replace("{blueLeft}", String.valueOf(bluePlayers.size()))
												.replace("{blueMax}", String.valueOf(teamfightEvent.getBlueFighting().size()))
												.replace("{redLeft}", String.valueOf(redPlayers.size()))
												.replace("{redMax}", String.valueOf(teamfightEvent.getRedFighting().size()));
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
						else if (linessb.contains("{tnttag}")) {
							if (event instanceof TNTTagEvent) {
								TNTTagEvent tntTagEvent = (TNTTagEvent) event;
								int playingtnttag = tntTagEvent.getByState(TNTTagPlayer.TNTTagState.WAITING).size() + tntTagEvent.getByState(TNTTagPlayer.TNTTagState.FIGHTING).size() + tntTagEvent.getByState(TNTTagPlayer.TNTTagState.PREPARING).size();
								int limittnttag = tntTagEvent.getLimit();
								for (String linessb2 : config.getConfig().getStringList("lobby.in-event-tnttag-lines")) {
									linessb2 = linessb2.replace("{players}", String.valueOf(playingtnttag))
											.replace("{limit}", String.valueOf(limittnttag));
									if (linessb2.contains("{starting}")) {
										int countdown = tntTagEvent.getCountdownTask().getTimeUntilStart();
										if (countdown > 0 && countdown <= 60) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-tnttag-starting-lines")) {
												linessb3 = linessb3.replace("{time}", String.valueOf(countdown));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{state}")) {
										if (tntTagEvent.getPlayer(player) != null) {
											TNTTagPlayer teamfightPlayer = tntTagEvent.getPlayer(player);
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-tnttag-state-lines")) {
												linessb3 = linessb3.replace("{state}", StringUtils.capitalize(teamfightPlayer.getState().name().toLowerCase()));
												lines.add(Color.translate(linessb3));
											}
										}
										continue;
									}
									if (linessb2.contains("{fight}")) {
										if (tntTagEvent.getState().equals(EventState.STARTED)) {
											for (String linessb3 : config.getConfig().getStringList("lobby.in-event-tnttag-fighting-lines")) {
												Player bomb = tntTagEvent.getBomb();
												linessb3 = linessb3.replace("{playerBomb}", bomb.getName())
													.replace("{countdown}", String.valueOf(tntTagEvent.getCountdownTest().time))
												.replace("{leftPlayers}", String.valueOf(tntTagEvent.getFighting().size()));
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
				if (playerData.getPlayerState() != PlayerState.EVENT && this.plugin.getTournamentManager().getTournaments().size() >= 1
						&& !queuing && party == null) {
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

	private List<String> getSpectateBoard(Player player) {
		List<String> lines = Lists.newLinkedList();
		Match match = this.plugin.getMatchManager().getMatchFromUUID(this.plugin.getMatchManager().getSpectatorPlayer().get(player.getUniqueId()));

		if (match.getMatchState() == MatchState.ENDING) {
			return getLobbyBoard(player, false);
		}

		if (match.isFFA()) {
			MatchTeam playerTeam = match.getTeams().get(match.getTeams().get(0).getTeamID());
			for (String string : config.getConfig().getStringList("spectate.ffa")) {

				if (string.contains("{kit}")) {
					string = string.replace("{kit}", match.getKit().getName());
				}

				if (string.contains("{leftPlayers}")) {
					string = string.replace("{leftPlayers}", String.valueOf(playerTeam.getAlivePlayers().size()));
				}

				if (string.contains("{maxPlayers}")) {
					string = string.replace("{maxPlayers}", String.valueOf(playerTeam.getPlayers().size()));
				}

				lines.add(CC.translate(string));
			}
		} else {
			if (match.getTeams().size() < 2) return getGameBoard(player);

			OfflinePlayer firstOffLeader = Bukkit.getOfflinePlayer(match.getTeams().get(0).getLeaderName());
			OfflinePlayer secondOffLeader = Bukkit.getOfflinePlayer(match.getTeams().get(1).getLeaderName());
			if (!firstOffLeader.isOnline() || !secondOffLeader.isOnline()) {
				return getGameBoard(player);
			}
			Player firstLeader = Bukkit.getPlayer(match.getTeams().get(0).getLeaderName());
			Player secondLeader = Bukkit.getPlayer(match.getTeams().get(1).getLeaderName());
			PlayerData firstData = this.plugin.getPlayerManager().getPlayerData(firstLeader.getUniqueId());
			PlayerData secondData = this.plugin.getPlayerManager().getPlayerData(secondLeader.getUniqueId());

			if (player.hasPermission("practice.staff")) {
				for (String string : config.getConfig().getStringList("spectate.teams.staff")) {

					if (string.contains("{firstLeader}")) {
						string = string.replace("{firstLeader}", firstLeader.getName());
					}

					if (string.contains("{secondLeader}")) {
						string = string.replace("{secondLeader}", secondLeader.getName());
					}

					if (string.contains("{firstLeaderCountry}")) {
						string = string.replace("{firstLeaderCountry}", firstData.getCountry());
					}

					if (string.contains("{secondLeaderCountry}")) {
						string = string.replace("{secondLeaderCountry}", secondData.getCountry());
					}

					if (string.contains("{ranked}")) {
						string = string.replace("{ranked}", match.getType().isRanked() ? CC.translate("&aSí") : CC.translate("&cNo"));
					}

					lines.add(Color.translate(string));
				}
			} else {
				for (String string : config.getConfig().getStringList("spectate.teams.staff")) {

					if (string.contains("{firstLeader}")) {
						string = string.replace("{firstLeader}", firstLeader.getName());
					}

					if (string.contains("{secondLeader}")) {
						string = string.replace("{secondLeader}", secondLeader.getName());
					}

					if (string.contains("{firstCPS}")) {
						string = string.replace("{firstCPS}", String.valueOf(GlobalBridge.getInstance().getDetectorListener().getClicks(firstLeader)));
					}

					if (string.contains("{secondCPS}")) {
						string = string.replace("{secondCPS}", String.valueOf(GlobalBridge.getInstance().getDetectorListener().getClicks(secondLeader)));
					}

					if (string.contains("{firstPing}")) {
						string = string.replace("{firstPing}", String.valueOf(PlayerUtil.getPing(firstLeader)));
					}

					if (string.contains("{secondPing}")) {
						string = string.replace("{secondPing}", String.valueOf(PlayerUtil.getPing(secondLeader)));
					}

					if (string.contains("{kit}")) {
						string = string.replace("{kit}", match.getKit().getName());
					}

					lines.add(Color.translate(string));
				}
			}
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
						Player opponentPlayer = match.getTeams().get(0).getPlayers().get(0) == player.getUniqueId()
								? this.plugin.getServer().getPlayer(match.getTeams().get(1).getPlayers().get(0))
								: this.plugin.getServer().getPlayer(match.getTeams().get(0).getPlayers().get(0));
						if (opponentPlayer != null) {
							linessb = linessb.replace("{opponent}", opponentPlayer.getName())
									.replace("{player}", player.getName())
									.replace("{opponentPing}", String.valueOf(PlayerUtil.getPing(opponentPlayer)))
									.replace("{playerPing}", String.valueOf(PlayerUtil.getPing(player)))
									.replace("{duration}", match.getDuration());
							lines.add(Color.translate(linessb));
						}
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
						if (linessb.contains("{duration}")) {
							linessb = linessb.replace("{duration}", match.getDuration());
						}
						lines.add(Color.translate(linessb));
					}
				}
				continue;
			}

			if (string.contains("{party-teams-match}")) {
				if(match.isPartyMatch() && !match.isFFA()) {
					for (String linessb : config.getConfig().getStringList("fight.in-party-teams-match")) {
			            MatchTeam opposingTeam = playerData.getTeamID() == 0 ? match.getTeams().get(1) : match.getTeams().get(0);
			            MatchTeam playerTeam = match.getTeams().get(playerData.getTeamID());
						if(linessb.contains("{opleftplayers}")) {
							linessb = linessb.replace("{opleftplayers}", String.valueOf(opposingTeam.getAlivePlayers().size()));
						}
						if(linessb.contains("{opmaxplayers}")) {
							linessb = linessb.replace("{opmaxplayers}", String.valueOf(opposingTeam.getPlayers().size()));
						}
						if(linessb.contains("{leftplayers}")) {
							linessb = linessb.replace("{leftplayers}", String.valueOf(playerTeam.getAlivePlayers().size()));
						}
						if(linessb.contains("{maxplayers}")) {
							linessb = linessb.replace("{maxplayers}", String.valueOf(playerTeam.getPlayers().size()));
						}
						if (linessb.contains("{duration}")) {
							linessb = linessb.replace("{duration}", match.getDuration());
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

	private String formatTps(double tps) {
		return (tps > 18.0 ? ChatColor.GREEN : tps > 16.0 ? ChatColor.YELLOW : ChatColor.RED).toString() + Math.min(Math.round(tps * 100.0D) / 100.0D, 20.0D);
	}
}