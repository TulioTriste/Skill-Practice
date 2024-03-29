package net.skillwars.practice.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.runnable.TournamentRunnable;
import net.skillwars.practice.tournament.Tournament;
import net.skillwars.practice.tournament.TournamentState;
import net.skillwars.practice.tournament.TournamentTeam;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.TeamUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TournamentManager {

    private final Practice plugin = Practice.getInstance();

    private final Map<UUID, Integer> players = new HashMap<>();
    private final Map<UUID, Integer> matches = new HashMap<>();
    @Getter
    private final Map<Integer, Tournament> tournaments = new HashMap<>();

    public boolean isInTournament(UUID uuid) {
        return this.players.containsKey(uuid);
    }

    public Tournament getTournament(UUID uuid) {
        Integer id = this.players.get(uuid);

        if (id == null) {
            return null;
        }

        return this.tournaments.get(id);
    }

    public Tournament getTournamentFromMatch(UUID uuid) {
        Integer id = this.matches.get(uuid);

        if (id == null) {
            return null;
        }

        return this.tournaments.get(id);
    }

    public void createTournament(CommandSender commandSender, int id, int teamSize, int size, String kitName) {
        Tournament tournament = new Tournament(id, teamSize, size, kitName);

        this.tournaments.put(id, tournament);

        new TournamentRunnable(tournament).runTaskTimer(this.plugin, 20L, 20L);

        commandSender.sendMessage(CC.PRIMARY + "Se ha creado el Tournament correctamente con la id " + CC.SECONDARY + id + CC.PRIMARY
                + " con un tamaño de team " + CC.SECONDARY + teamSize + CC.PRIMARY + ", kit " + CC.SECONDARY + kitName + CC.PRIMARY
                + ", y tamaño de tournament " + CC.SECONDARY + size + CC.PRIMARY + ".");
    }

    private void playerLeft(Tournament tournament, Player player) {
        TournamentTeam team = tournament.getPlayerTeam(player.getUniqueId());

        tournament.removePlayer(player.getUniqueId());

        player.sendMessage(CC.PRIMARY + "Te has salido del Tournament.");

        this.players.remove(player.getUniqueId());

        this.plugin.getPlayerManager().sendToSpawnAndReset(player);

        tournament.broadcast(CC.SECONDARY + player.getName() + CC.PRIMARY + " se ha salido del Tournament. ("
                + CC.SECONDARY + tournament.getPlayers().size() + CC.PRIMARY + "/" + CC.SECONDARY + tournament.getSize() + CC.PRIMARY + ")");

        if (team != null) {
            team.killPlayer(player.getUniqueId());

            if (team.getAlivePlayers().size() == 0) {
                tournament.killTeam(team);

                if (tournament.getAliveTeams().size() == 1) {
                    TournamentTeam tournamentTeam = tournament.getAliveTeams().get(0);

                    String names = TeamUtil.getNames(tournamentTeam);

                    this.plugin.getServer().broadcastMessage(names + " ha ganado el Tournament " + CC.SECONDARY + tournament.getId() + CC.PRIMARY + "!");

                    for (UUID playerUUID : tournamentTeam.getAlivePlayers()) {
                        this.players.remove(playerUUID);
                        Player tournamentPlayer = this.plugin.getServer().getPlayer(playerUUID);
                        this.plugin.getPlayerManager().sendToSpawnAndReset(tournamentPlayer);
                    }

                    this.plugin.getTournamentManager().removeTournament(tournament.getId());
                }
            } else {
                if (team.getLeader().equals(player.getUniqueId())) {
                    team.setLeader(team.getAlivePlayers().get(0));
                }
            }
        }
    }

    private void teamEliminated(Tournament tournament, TournamentTeam winnerTeam, TournamentTeam losingTeam) {
        for (UUID playerUUID : losingTeam.getAlivePlayers()) {
            Player player = this.plugin.getServer().getPlayer(playerUUID);

            tournament.removePlayer(player.getUniqueId());

            player.sendMessage(CC.RED + "Has sido eliminado.");
            player.sendMessage(CC.RED + "Usa /tournament status " + tournament.getId() + " para ver la informacion del Tournament.");

            this.players.remove(player.getUniqueId());
        }

        String word = losingTeam.getAlivePlayers().size() > 1 ? "tienes" : "tiene";

        tournament.broadcast(TeamUtil.getNames(losingTeam) + CC.PRIMARY + " " + word + " ha sido eliminado por " +
                TeamUtil.getNames(winnerTeam) + CC.PRIMARY + ". ("
                + CC.SECONDARY + tournament.getPlayers().size() + CC.PRIMARY + "/" + CC.SECONDARY + tournament.getSize() + CC.PRIMARY + ")");
    }

    public void leaveTournament(Player player) {
        Tournament tournament = this.getTournament(player.getUniqueId());

        if (tournament == null) {
            return;
        }

        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party != null && tournament.getTournamentState() != TournamentState.FIGHTING) {
            if (this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                for (UUID memberUUID : party.getMembers()) {
                    Player member = this.plugin.getServer().getPlayer(memberUUID);

                    this.playerLeft(tournament, member);
                }
            } else {
                player.sendMessage(CC.RED + "No eres el Leader de esta Party!");
            }
        } else {
            this.playerLeft(tournament, player);
        }
    }

    private void playerJoined(Tournament tournament, Player player) {
        tournament.addPlayer(player.getUniqueId());

        this.players.put(player.getUniqueId(), tournament.getId());

        this.plugin.getPlayerManager().sendToSpawnAndReset(player);

        tournament.broadcast(CC.SECONDARY + player.getName() + CC.PRIMARY + " ha entrado al Tournament. ("
                + CC.SECONDARY + tournament.getPlayers().size() + CC.PRIMARY + "/" + CC.SECONDARY + tournament.getSize() + CC.PRIMARY + ")");
    }

    public void joinTournament(Integer id, Player player) {
        Tournament tournament = this.tournaments.get(id);

        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party != null) {
            if (this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                if ((party.getMembers().size() + tournament.getPlayers().size()) <= tournament.getSize()) {
                    if (party.getMembers().size() != tournament.getTeamSize() || party.getMembers().size() == 1) {
                        player.sendMessage(CC.RED + "Estas en una Party que no coincide con este Tournament!");
                    } else {
                        for (UUID memberUUID : party.getMembers()) {
                            Player member = this.plugin.getServer().getPlayer(memberUUID);

                            this.playerJoined(tournament, member);
                        }
                    }
                } else {
                    player.sendMessage(CC.RED + "Este Tournament se encuentra lleno!");
                }
            } else {
                player.sendMessage(CC.RED + "No eres el leader de la Party!");
            }
        } else {
            this.playerJoined(tournament, player);
        }

        if (tournament.getPlayers().size() == tournament.getSize()) {
            tournament.setTournamentState(TournamentState.STARTING);
        }
    }

    public Tournament getTournament(Integer id) {
        return this.tournaments.get(id);
    }

    public void removeTournament(Integer id) {
        Tournament tournament = this.tournaments.get(id);

        if (tournament == null) {
            return;
        }

        this.tournaments.remove(id);
    }

    public void addTournamentMatch(UUID matchId, Integer tournamentId) {
        this.matches.put(matchId, tournamentId);
    }

    public void removeTournamentMatch(Match match) {
        Tournament tournament = this.getTournamentFromMatch(match.getMatchId());

        if (tournament == null) {
            return;
        }

        tournament.removeMatch(match.getMatchId());

        this.matches.remove(match.getMatchId());

        MatchTeam losingTeam = match.getWinningTeamId() == 0 ? match.getTeams().get(1) : match.getTeams().get(0);

        TournamentTeam losingTournamentTeam = tournament.getPlayerTeam(losingTeam.getPlayers().get(0));

        tournament.killTeam(losingTournamentTeam);

        MatchTeam winningTeam = match.getTeams().get(match.getWinningTeamId());

        TournamentTeam winningTournamentTeam = tournament.getPlayerTeam(winningTeam.getAlivePlayers().get(0));

        this.teamEliminated(tournament, winningTournamentTeam, losingTournamentTeam);

        winningTournamentTeam.broadcast(CC.PRIMARY + "Tip: Si te encuentras aburrido, usa " + CC.SECONDARY + "/tournament status " + tournament.getId() + CC.PRIMARY + " para ver " +
                "lo restante de las rondas del Tournament!");

        if (tournament.getMatches().size() == 0) {
            if (tournament.getAliveTeams().size() > 1) {
                tournament.setTournamentState(TournamentState.STARTING);
                tournament.setCurrentRound(tournament.getCurrentRound() + 1);
                tournament.setCountdown(16);
            } else {
                String names = TeamUtil.getNames(winningTournamentTeam);

                this.plugin.getServer().broadcastMessage(names + " ha ganado el Tournament " + CC.SECONDARY + tournament.getId() + CC.PRIMARY + "!");

                for (UUID playerUUID : winningTournamentTeam.getAlivePlayers()) {
                    this.players.remove(playerUUID);
                    Player tournamentPlayer = this.plugin.getServer().getPlayer(playerUUID);
                    this.plugin.getPlayerManager().sendToSpawnAndReset(tournamentPlayer);
                }

                this.plugin.getTournamentManager().removeTournament(tournament.getId());
            }
        }
    }
}