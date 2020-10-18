package net.skillwars.practice.listeners;

import me.joeleoli.nucleus.nametag.NameTagHandler;
import net.skillwars.practice.Practice;
import net.skillwars.practice.event.match.MatchEndEvent;
import net.skillwars.practice.event.match.MatchStartEvent;
import net.skillwars.practice.inventory.InventorySnapshot;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.queue.QueueType;
import net.skillwars.practice.runnable.MatchRunnable;
import net.skillwars.practice.util.*;
import net.minecraft.server.v1_8_R3.EntityPlayer;

import net.skillwars.practice.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pt.foxspigot.jar.FoxSpigot;
import pt.foxspigot.jar.knockback.KnockbackProfile;

import java.util.*;

public class MatchListener implements Listener {
    private final Practice plugin = Practice.getInstance();

    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        Match match = event.getMatch();
        Kit kit = match.getKit();

        if (!kit.isEnabled()) {
            match.broadcast(CC.RED + "Este kit se encuentra desactivado, prueba con otro kit.");
            this.plugin.getMatchManager().removeMatch(match);
            return;
        }

        if (kit.isSpleef()) {
            if (match.getArena().getAvailableArenas().size() <= 0) {
                match.broadcast(ChatColor.RED + "No hay arenas disponibles.");
                this.plugin.getMatchManager().removeMatch(match);
                return;
            }
            match.setStandaloneArena(match.getArena().getAvailableArena());
            this.plugin.getArenaManager().setArenaMatchUUID(match.getStandaloneArena(), match.getMatchId());
        }

        Set<Player> matchPlayers = new HashSet<>();

        CustomLocation locationA = match.getArena().getA();
        CustomLocation locationB = match.getArena().getB();
        List<Location> locs = Circle.getCircle(MathUtil.getMiddle(locationA.toBukkitLocation(), locationB.toBukkitLocation()), kit.isSumo() ? 2 : 10,
                match.getTeams().get(0).getAlivePlayers().size());

        match.getTeams().forEach(team -> team.alivePlayers().forEach(player -> {
            matchPlayers.add(player);

            this.plugin.getMatchManager().removeMatchRequests(player.getUniqueId());

            PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

            player.setAllowFlight(false);
            player.setFlying(false);

            playerData.setCurrentMatchID(match.getMatchId());
            playerData.setTeamID(team.getTeamID());

            playerData.setMissedPots(0);
            playerData.setLongestCombo(0);
            playerData.setCombo(0);
            playerData.setHits(0);

            PlayerUtil.clearPlayer(player);

            if(match.isFFA()){
                Location loc = locs.get(0);
                Location target = loc.setDirection(MathUtil.getMiddle(locationA.toBukkitLocation(), locationB.toBukkitLocation()).subtract(loc).toVector());
                player.teleport(target);
                locs.remove(0);
            } else {
                player.teleport(team.getTeamID() == 1 ? locationA.toBukkitLocation() : locationB.toBukkitLocation());
            }
            if (kit.isCombo()) {
                player.setMaximumNoDamageTicks(3);
                CraftPlayer playerCp = (CraftPlayer) player;
                EntityPlayer playerEp = playerCp.getHandle();
                KnockbackProfile profile4 = new KnockbackProfile("combo");
                playerEp.setKnockback(profile4);
//                KnockbackModule profile4 = KnockbackModule.get();
//                playerEp.setKnockback(profile4.getKnockbackProfile("Combo"));
            } else if (kit.isSumo()) {
                player.setMaximumNoDamageTicks(20);
                CraftPlayer playerCp = (CraftPlayer) player;
                EntityPlayer playerEp = playerCp.getHandle();
                KnockbackProfile profile4 = new KnockbackProfile("sumo");
                playerEp.setKnockback(profile4);
            } else {
                player.setMaximumNoDamageTicks(20);
                CraftPlayer playerCp = (CraftPlayer) player;
                EntityPlayer playerEp = playerCp.getHandle();
                KnockbackProfile profile4 = new KnockbackProfile("default");
                playerEp.setKnockback(profile4);
//                KnockbackModule profile4 = KnockbackModule.get();
//                playerEp.setKnockback(profile4.getKnockbackProfile("default"));
            }
            if (!match.isRedrover()) {
                if (!kit.isSumo()) {
                    this.plugin.getMatchManager().giveKits(player, kit);
                }

                playerData.setPlayerState(PlayerState.FIGHTING);
            } else {
                this.plugin.getMatchManager().addRedroverSpectator(player, match);
            }

            if(match.isFFA()){
                for (UUID uuid : team.getAlivePlayers()){
                    Player teamplayer = this.plugin.getServer().getPlayer(uuid);
                    NameTagHandler.removeFromTeams(player, teamplayer);
                    NameTagHandler.addToTeam(player, teamplayer, ChatColor.RED, kit.isBuild());
                }
            }else{
                MatchTeam otherteam = team == match.getTeams().get(0) ? match.getTeams().get(1) : match.getTeams().get(0);
                for (UUID memberUUID : team.getAlivePlayers()){
                    Player member = this.plugin.getServer().getPlayer(memberUUID);
                    NameTagHandler.removeFromTeams(player, member);
                    NameTagHandler.addToTeam(player, member, ChatColor.GREEN, kit.isBuild());

                }
                for (UUID enemyUUID : otherteam.getAlivePlayers()){
                    Player enemy = this.plugin.getServer().getPlayer(enemyUUID);
                    NameTagHandler.removeFromTeams(player, enemy);
                    NameTagHandler.addToTeam(player, enemy, ChatColor.RED, kit.isBuild());
                }
            }

            if(kit.isSumo() || match.getKit().isSpleef()){
                PlayerUtil.denyMovement(player);
            }
        }));

        for (Player player : matchPlayers) {
            for (Player online : this.plugin.getServer().getOnlinePlayers()) {
                online.hidePlayer(player);
                player.hidePlayer(online);
            }
        }

        for (Player player : matchPlayers) {
            for (Player other : matchPlayers) {
                player.showPlayer(other);
            }
        }


        new MatchRunnable(match).runTaskTimer(this.plugin, 20L, 20L);
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        Match match = event.getMatch();
        Clickable inventories = new Clickable(CC.WHITE + "Inventarios: ");

        match.setMatchState(MatchState.ENDING);
        match.setWinningTeamId(event.getWinningTeam().getTeamID());
        match.setCountdown(4);

        if (match.isFFA()) {
            Player winner = this.plugin.getServer().getPlayer(event.getWinningTeam().getAlivePlayers().get(0));
            String winnerMessage = CC.WHITE + "Ganador: " + CC.SECONDARY + winner.getName();

            event.getWinningTeam().players().forEach(player -> {
                if (!match.hasSnapshot(player.getUniqueId())) {
                    match.addSnapshot(player);
                }
                inventories.add((player.getUniqueId() == winner.getUniqueId() ? CC.GREEN : CC.RED)
                                + player.getName() + " ",
                        CC.WHITE + "Ver inventario",
                        "/inv " + match.getSnapshot(player.getUniqueId()).getSnapshotId());


                NameTagHandler.removeHealthDisplay(player);

                event.getLosingTeam().players().forEach(other -> {
                    NameTagHandler.removeFromTeams(other, player);
                    NameTagHandler.removeFromTeams(player, other);
                });
//                PlayerUtil.refreshDisplayName(player);
                if (match.getKit().isCombo()) {
                    player.setMaximumNoDamageTicks(20);
                    CraftPlayer playerCp = (CraftPlayer) player;
                    EntityPlayer playerEp = playerCp.getHandle();
                    KnockbackProfile profile4 = new KnockbackProfile("default");
                    playerEp.setKnockback(profile4);
//                    KnockbackModule profile4 = KnockbackModule.get();
//                    playerEp.setKnockback(profile4.getKnockbackProfile("default"));
                }
            });
            for (InventorySnapshot snapshot : match.getSnapshots().values()) {
                this.plugin.getInventoryManager().addSnapshot(snapshot);
            }

            match.broadcast(winnerMessage);
            match.broadcast(inventories);
        } else if (match.isRedrover()) {
            match.broadcast(CC.SECONDARY + event.getWinningTeam().getLeaderName() + CC.PRIMARY + " ha ganado el Redrover!");
        } else {
            match.getTeams().forEach(team -> team.players().forEach(player -> {
                if (!match.hasSnapshot(player.getUniqueId())) {
                    match.addSnapshot(player);
                }

                boolean onWinningTeam =
                        this.plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getTeamID() ==
                                event.getWinningTeam().getTeamID();
                inventories.add((onWinningTeam ? CC.GREEN : CC.RED)
                                + player.getName() + " ",
                        CC.WHITE + "Ver inventario",
                        "/inv " + match.getSnapshot(player.getUniqueId()).getSnapshotId());

                MatchTeam otherTeam = team == match.getTeams().get(0) ? match.getTeams().get(1) : match.getTeams().get(0);

                NameTagHandler.removeHealthDisplay(player);
                for (UUID uuid : otherTeam.getAlivePlayers()){
                    Player teamplayer = this.plugin.getServer().getPlayer(uuid);

                    NameTagHandler.removeHealthDisplay(teamplayer);
                    NameTagHandler.removeFromTeams(player, teamplayer);
                    NameTagHandler.removeFromTeams(teamplayer, player);
                }
                for (UUID uuid : team.getAlivePlayers()){
                    Player teamplayer = this.plugin.getServer().getPlayer(uuid);

                    NameTagHandler.removeFromTeams(player, teamplayer);
                    NameTagHandler.removeFromTeams(teamplayer, player);
                    NameTagHandler.removeHealthDisplay(teamplayer);
                }

//                PlayerUtil.refreshDisplayName(player);
                if (match.getKit().isCombo()) {
                    player.setMaximumNoDamageTicks(20);
                    CraftPlayer playerCp = (CraftPlayer) player;
                    EntityPlayer playerEp = playerCp.getHandle();
                    KnockbackProfile profile4 = new KnockbackProfile("default");
                    playerEp.setKnockback(profile4);
//                    KnockbackModule profile4 = KnockbackModule.get();
//                    playerEp.setKnockback(profile4.getKnockbackProfile("default"));
                }
            }));
            for (InventorySnapshot snapshot : match.getSnapshots().values()) {
                this.plugin.getInventoryManager().addSnapshot(snapshot);
            }

            String winnerMessage = CC.WHITE + (match.isParty() ? "Ha ganado el team: " : "Ganador: ")
                    + CC.SECONDARY + event.getWinningTeam().getLeaderName();

            match.broadcast(winnerMessage);
            match.broadcast(inventories);

            if (match.getType().isRanked()) {
                String kitName = match.getKit().getName();

                Player winnerLeader = this.plugin.getServer().getPlayer(event.getWinningTeam().getPlayers().get(0));
                PlayerData winnerLeaderData = this.plugin.getPlayerManager()
                        .getPlayerData(winnerLeader.getUniqueId());
                Player loserLeader = this.plugin.getServer().getPlayer(event.getLosingTeam().getPlayers().get(0));
                PlayerData loserLeaderData = this.plugin.getPlayerManager()
                        .getPlayerData(loserLeader.getUniqueId());

                String eloMessage;

                int[] preElo = new int[2];
                int[] newElo = new int[2];
                int winnerElo = 0;
                int loserElo = 0;
                int newWinnerElo;
                int newLoserElo;

                if (event.getWinningTeam().getPlayers().size() == 2) {
                    Player winnerMember = this.plugin.getServer().getPlayer(event.getWinningTeam().getPlayers().get(1));
                    PlayerData winnerMemberData = this.plugin.getPlayerManager().getPlayerData(winnerMember.getUniqueId());

                    Player loserMember = this.plugin.getServer().getPlayer(event.getLosingTeam().getPlayers().get(1));
                    PlayerData loserMemberData = this.plugin.getPlayerManager().getPlayerData(loserMember.getUniqueId());

                    winnerElo = winnerLeaderData.getPartyElo(kitName);
                    loserElo = loserLeaderData.getPartyElo(kitName);

                    preElo[0] = winnerElo;
                    preElo[1] = loserElo;

                    newWinnerElo = EloUtil.getNewRating(winnerElo, loserElo, true);
                    newLoserElo = EloUtil.getNewRating(loserElo, winnerElo, false);

                    newElo[0] = newWinnerElo;
                    newElo[1] = newLoserElo;

                    winnerMemberData.setPartyElo(kitName, newWinnerElo);
                    loserMemberData.setPartyElo(kitName, newLoserElo);

                    eloMessage = CC.WHITE + "Cambios de elo: " + CC.GREEN + winnerLeader.getName() + ", " +
                            winnerMember.getName() + " " + newWinnerElo +
                            " (+" + (newWinnerElo - winnerElo) + ") " + CC.RED + loserLeader.getName() + "," +
                            " " +
                            loserMember.getName() + " " +
                            newLoserElo + " (" + (newLoserElo - loserElo) + ")";
                } else {
                    if (match.getType() == QueueType.RANKED) {
                        winnerElo = winnerLeaderData.getElo(kitName);
                        loserElo = loserLeaderData.getElo(kitName);

                    }

                    preElo[0] = winnerElo;
                    preElo[1] = loserElo;

                    newWinnerElo = EloUtil.getNewRating(winnerElo, loserElo, true);
                    newLoserElo = EloUtil.getNewRating(loserElo, winnerElo, false);

                    newElo[0] = newWinnerElo;
                    newElo[1] = newLoserElo;

                    eloMessage = CC.WHITE + "Cambios de elo: " + CC.GREEN + winnerLeader.getName() + " " + newWinnerElo +
                            " (+" + (newWinnerElo - winnerElo) + ") " +
                            CC.RED + loserLeader.getName() + " " + newLoserElo + " (" +
                            (newLoserElo - loserElo) + ")";

                    if (match.getType() == QueueType.RANKED) {
                        winnerLeaderData.setElo(kitName, newWinnerElo);
                        loserLeaderData.setElo(kitName, newLoserElo);

                        winnerLeaderData.setWins(kitName, winnerLeaderData.getWins(kitName) + 1);
                        loserLeaderData.setLosses(kitName, loserLeaderData.getLosses(kitName) + 1);
                    }
                }

                match.broadcast(eloMessage);
            }
            this.plugin.getMatchManager().saveRematches(match);
        }
    }
}