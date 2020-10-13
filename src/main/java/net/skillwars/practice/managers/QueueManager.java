package net.skillwars.practice.managers;

import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.arena.Arena;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.queue.QueueEntry;
import net.skillwars.practice.queue.QueueType;
import net.skillwars.practice.util.CC;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {
    private final Map<UUID, QueueEntry> queued;
    private final Map<UUID, Long> playerQueueTime;
    private final Practice plugin;
    @Getter
    @Setter
    private boolean rankedEnabled = true;
    public QueueManager() {
        this.queued = new ConcurrentHashMap<>();
        this.playerQueueTime = new HashMap<>();
        this.plugin = Practice.getInstance();
        this.rankedEnabled = true;
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin,
                () -> this.queued.forEach((key, value) -> {
                    if (value.isParty()) {
                        this.findMatch(this.plugin.getPartyManager().getParty(key), value.getKitName(),
                                value.getElo(), value.getQueueType());
                    } else {
                        this.findMatch(this.plugin.getServer().getPlayer(key), value.getKitName(),
                                value.getElo(), value.getQueueType());
                    }
                }), 20L, 20L);
    }

    public void addPlayerToQueue(final Player player, final PlayerData playerData, final String kitName, final QueueType type) {
        if (type == QueueType.RANKED && !this.rankedEnabled) {
            player.closeInventory();
            return;
        }
        playerData.setPlayerState(PlayerState.QUEUE);
        final int elo = (type == QueueType.RANKED) ? playerData.getElo(kitName) : 0;
        final QueueEntry entry = new QueueEntry(type, kitName, elo, false);
        this.queued.put(playerData.getUniqueId(), entry);
        this.giveQueueItems(player);
        player.sendMessage(type == QueueType.RANKED ?
                CC.PRIMARY + "Has sido añadido a " + CC.SECONDARY + type.getName() + " " + kitName + CC.PRIMARY +
                        " " +
                        "queue" +
                        " con " + CC.SECONDARY + elo + CC.PRIMARY + " de elo." :
                CC.PRIMARY + "Has sido añadido a " + CC.SECONDARY + "UnRanked " + kitName + CC.PRIMARY + " queue.");

        this.playerQueueTime.put(player.getUniqueId(), System.currentTimeMillis());

        if (!this.findMatch(player, kitName, elo, type) && type.isRanked()) {
            player.sendMessage(CC.SECONDARY + "Buscando en el rango de ELO " + CC.PRIMARY
                    + (playerData.getEloRange() == -1
                    ? "Restringido"
                    : "[" + Math.max(elo - playerData.getEloRange() / 2, 0)
                    + " -> " + Math.max(elo + playerData.getEloRange() / 2, 0) + "]"));
        }
    }

    private void giveQueueItems(final Player player) {
        player.closeInventory();
        player.getInventory().setContents(this.plugin.getItemManager().getQueueItems());
        player.updateInventory();
    }

    public QueueEntry getQueueEntry(final UUID uuid) {
        return this.queued.get(uuid);
    }

    public long getPlayerQueueTime(final UUID uuid) {
        return this.playerQueueTime.get(uuid);
    }

    public int getQueueSize(final String ladder, final QueueType type) {
        return (int) this.queued.entrySet().stream().filter(entry -> entry.getValue().getQueueType() == type).filter(entry -> entry.getValue().getKitName().equals(ladder)).count();
    }

    private boolean findMatch(final Player player, final String kitName, final int elo, final QueueType type) {
        long queueTime = System.currentTimeMillis() - this.playerQueueTime.get(player.getUniqueId());

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        if (playerData == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            player.kickPlayer("Porfavor vuelve a ingresar.");
            return false;
        }
        // Increase elo range by 50 every second after 5 seconds
        int eloRange = playerData.getEloRange();
        int pingRange = -1;
        int seconds = Math.round(queueTime / 1000L);
        if (seconds > 5 && type == QueueType.RANKED) {
            if (pingRange != -1) {
                pingRange += (seconds - 5) * 25;
            }

            if (eloRange != -1) {
                eloRange += seconds * 50;
                if (eloRange >= 3000) {
                    eloRange = 3000;
                } else {
                    player.sendMessage(
                            CC.SECONDARY + "Buscando en el rango de ELO "
                                    + CC.PRIMARY + (eloRange == -1 ? "Restringido"
                                    : "[" + Math.max(elo - eloRange / 2, 0) + " -> " +
                                    Math.max(elo + eloRange / 2, 0) + "]"));
                }
            }
        }

        if (eloRange == -1) {
            eloRange = Integer.MAX_VALUE;
        }
        if (pingRange == -1) {
            pingRange = Integer.MAX_VALUE;
        }

        int ping = 0;
        for (UUID opponent : this.queued.keySet()) {
            if (opponent == player.getUniqueId()) {
                continue;
            }

            QueueEntry queueEntry = this.queued.get(opponent);

            if (!queueEntry.getKitName().equals(kitName)) {
                continue;
            }

            if (queueEntry.getQueueType() != type) {
                continue;
            }

            if (queueEntry.isParty()) {
                continue;
            }

            Player opponentPlayer = this.plugin.getServer().getPlayer(opponent);

            int eloDiff = Math.abs(queueEntry.getElo() - elo);
            PlayerData opponentData = this.plugin.getPlayerManager().getPlayerData(opponent);

            if (type.isRanked()) {
                if (eloDiff > eloRange) {
                    continue;
                }

                long opponentQueueTime = System.currentTimeMillis() - this.playerQueueTime.get(opponentPlayer.getUniqueId());

                int opponentEloRange = opponentData.getEloRange();
                int opponentPingRange = -1;
                int opponentSeconds = Math.round(opponentQueueTime / 1000L);

                if (opponentSeconds > 5) {
                    if (opponentPingRange != -1) {
                        opponentPingRange += (opponentSeconds - 5) * 25;
                    }

                    if (opponentEloRange != -1) {
                        opponentEloRange += opponentSeconds * 50;
                        if (opponentEloRange >= 3000) {
                            opponentEloRange = 3000;
                        }
                    }
                }
                if (opponentEloRange == -1) {
                    opponentEloRange = Integer.MAX_VALUE;
                }
                if (opponentPingRange == -1) {
                    opponentPingRange = Integer.MAX_VALUE;
                }

                if (eloDiff > opponentEloRange) {
                    continue;
                }

                int pingDiff = Math.abs(0 - ping);

                if (type == QueueType.RANKED) {
                    if (pingDiff > opponentPingRange) {
                        continue;
                    }
                    if (pingDiff > pingRange) {
                        continue;
                    }
                }
            }

            Kit kit = this.plugin.getKitManager().getKit(kitName);

            Arena arena = this.plugin.getArenaManager().getRandomArena(kit);

            String playerFoundMatchMessage;
            String matchedFoundMatchMessage;

            if (type.isRanked()) {
                playerFoundMatchMessage = CC.PRIMARY + "Se ha encontrado una pelea " + type.getName().toLowerCase() + ": " + CC
                        .GREEN +
                        player.getName() + " (" + elo + " elo)" + CC.PRIMARY
                        + " vs. " + CC.RED + opponentPlayer.getName() + " (" +
                        this.queued.get(opponentPlayer.getUniqueId()).getElo() + " elo)";
                matchedFoundMatchMessage = CC.PRIMARY + "Se ha encontrado una pelea " + type.getName().toLowerCase() + ": " +
                        CC.GREEN +
                        opponentPlayer.getName() + " (" +
                        this.queued.get(opponentPlayer.getUniqueId()).getElo()
                        + " elo)" + CC.PRIMARY + " vs. " + CC.RED + player.getName() + " (" + elo
                        + " elo)";
            } else {
                playerFoundMatchMessage = CC.PRIMARY + "Se ha encontrado una pelea UnRanked: " + CC.GREEN + player.getName() +
                        CC.PRIMARY
                        + " vs. " + CC.RED + opponentPlayer.getName();
                matchedFoundMatchMessage = CC.PRIMARY + "Se ha encontrado una pelea UnRanked: " + CC.GREEN + opponentPlayer.getName
                        () +
                        CC.PRIMARY + " vs. " + CC.RED + player.getName();
            }

            player.sendMessage(playerFoundMatchMessage);
            opponentPlayer.sendMessage(matchedFoundMatchMessage);


            MatchTeam teamA = new MatchTeam(player.getUniqueId(), Collections.singletonList(player.getUniqueId()), 0);
            MatchTeam teamB = new MatchTeam(opponentPlayer.getUniqueId(), Collections.singletonList(opponentPlayer.getUniqueId()), 1);

            Match match = new Match(arena, kit, type, teamA, teamB);

            this.plugin.getMatchManager().createMatch(match);

            this.queued.remove(player.getUniqueId());
            this.queued.remove(opponentPlayer.getUniqueId());

            this.playerQueueTime.remove(player.getUniqueId());

            return true;
        }

        return false;
    }

    public void removePlayerFromQueue(final Player player) {
        final QueueEntry entry = this.queued.get(player.getUniqueId());
        this.queued.remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);

		if(entry == null){
			return;
		}
        player.sendMessage(CC.PRIMARY + "has sido removido de la queue " + CC.SECONDARY + entry.getQueueType().getName()
                + " " + entry.getKitName() + CC.PRIMARY + ".");
    }

    public void addPartyToQueue(final Player leader, final Party party, final String kitName, final QueueType type) {
        if (type.isRanked() && !this.rankedEnabled) {
            leader.sendMessage(CC.RED + "Ranked esta actualmente desabilitado.");
            leader.closeInventory();
        } else if (party.getMembers().size() != 2) {
            leader.sendMessage(CC.RED + "Solo puedes entrar a la queue si hay 2 miembros en la party.");
            leader.closeInventory();
        } else {
            party.getMembers().stream().map(this.plugin.getPlayerManager()::getPlayerData).forEach(member -> member.setPlayerState(PlayerState.QUEUE));
            final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(leader.getUniqueId());
            final int elo = type.isRanked() ? playerData.getPartyElo(kitName) : -1;
            this.queued.put(playerData.getUniqueId(), new QueueEntry(type, kitName, elo, true));
            this.giveQueueItems(leader);
            party.broadcast(type.isRanked() ?
                    CC.PRIMARY + "Tu party ha sido añadida a la queue " + type.getName().toLowerCase() + " " + CC.SECONDARY
                            + kitName + CC.PRIMARY + " con " + CC.SECONDARY + elo + CC.PRIMARY + " elo." :
                    CC.PRIMARY + "Tu party ha sido añadida a la queue Unranked " + CC.SECONDARY + kitName + CC.PRIMARY +
                            ".");
            this.playerQueueTime.put(party.getLeader(), System.currentTimeMillis());
            this.findMatch(party, kitName, elo, type);
        }
    }

    private void findMatch(final Party partyA, final String kitName, final int elo, final QueueType type) {
        long queueTime = System.currentTimeMillis() - this.playerQueueTime.get(partyA.getLeader());

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(partyA.getLeader());

        // Increase elo range by 50 every second after 5 seconds
        int eloRange = playerData.getEloRange();
        int seconds = Math.round(queueTime / 1000L);
        if (seconds > 5 && type.isRanked()) {
            eloRange += seconds * 50;
            if (eloRange >= 1000) {
                eloRange = 1000;
            }
            partyA.broadcast(
                    CC.SECONDARY + "Buscando en el rango de ELO " + CC.PRIMARY + "[" + (elo - eloRange / 2) + " -> " +
                            (elo + eloRange / 2) + "]");
        }

        int finalEloRange = eloRange;
        UUID opponent = this.queued.entrySet().stream()
                .filter(entry -> entry.getKey() != partyA.getLeader())
                .filter(entry -> entry.getValue().isParty())
                .filter(entry -> entry.getValue().getQueueType() == type)
                .filter(entry -> !type.isRanked() || Math.abs(entry.getValue().getElo() - elo) < finalEloRange)
                .filter(entry -> entry.getValue().getKitName().equals(kitName))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);

        if (opponent == null) {
            return;
        }

        Player leaderA = this.plugin.getServer().getPlayer(partyA.getLeader());
        Player leaderB = this.plugin.getServer().getPlayer(opponent);

        Party partyB = this.plugin.getPartyManager().getParty(opponent);

        Kit kit = this.plugin.getKitManager().getKit(kitName);

        Arena arena = this.plugin.getArenaManager().getRandomArena(kit);

        String partyAFoundMatchMessage;
        String partyBFoundMatchMessage;

        if (type.isRanked()) {
            partyAFoundMatchMessage = CC.PRIMARY + "Se ha encontrado una pelea Ranked: " + CC.GREEN + leaderA.getName() +
                    "'s party (" +
                    elo + " elo)" + CC.PRIMARY + " vs. "
                    + CC.RED + leaderB.getName() + "'s Party (" +
                    this.queued.get(leaderB.getUniqueId()).getElo() + " elo)";
            partyBFoundMatchMessage = CC.PRIMARY + "Se ha encontrado una pelea Ranked: " + CC.GREEN + leaderB.getName() + "'s party ("
                    + this.queued.get(leaderB.getUniqueId()).getElo() + " elo)" + CC.PRIMARY +
                    " vs. " +
                    CC.RED + leaderA.getName() + "'s Party (" + elo + " elo)";
        } else {
            partyAFoundMatchMessage = CC.PRIMARY + "Se ha encontrado una pelea UnRanked: " + CC.GREEN + leaderA.getName() +
                    "'s party" +
                    CC.PRIMARY + " vs. "
                    + CC.RED + leaderB.getName() + "'s party";
            partyBFoundMatchMessage = CC.PRIMARY + "Se ha encontrado una pelea UnRanked: " + CC.GREEN + leaderB.getName() +
                    "'s party" +
                    CC.PRIMARY + " vs. "
                    + CC.RED + leaderA.getName() + "'s party";
        }

        partyA.broadcast(partyAFoundMatchMessage);
        partyB.broadcast(partyBFoundMatchMessage);

        List<UUID> playersA = new ArrayList<>(partyA.getMembers());
        List<UUID> playersB = new ArrayList<>(partyB.getMembers());

        MatchTeam teamA = new MatchTeam(leaderA.getUniqueId(), playersA, 0);
        MatchTeam teamB = new MatchTeam(leaderB.getUniqueId(), playersB, 1);

        Match match = new Match(arena, kit, type, teamA, teamB);

        this.plugin.getMatchManager().createMatch(match);

        this.queued.remove(partyA.getLeader());
        this.queued.remove(partyB.getLeader());
    }


    public void removePartyFromQueue(Party party) {
        QueueEntry entry = this.queued.get(party.getLeader());

        this.queued.remove(party.getLeader());

        party.members().forEach(this.plugin.getPlayerManager()::sendToSpawnAndReset);

        String type = entry.getQueueType().isRanked() ? "Ranked" : "UnRanked";

        party.broadcast(ChatColor.YELLOW + "Tu party se ha salido de la queue " + type + " " + entry.getKitName() + ".");
    }
}
