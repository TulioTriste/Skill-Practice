package net.skillwars.practice.listeners;

import com.google.common.collect.Lists;
import me.joeleoli.nucleus.nametag.NameTagHandler;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.skillwars.practice.Practice;
import net.skillwars.practice.event.match.MatchCancelEvent;
import net.skillwars.practice.event.match.MatchEndEvent;
import net.skillwars.practice.event.match.MatchResetEvent;
import net.skillwars.practice.event.match.MatchStartEvent;
import net.skillwars.practice.inventory.InventorySnapshot;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.queue.QueueType;
import net.skillwars.practice.runnable.MatchResetRunnable;
import net.skillwars.practice.runnable.MatchRunnable;
import net.skillwars.practice.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import pt.foxspigot.jar.knockback.KnockbackModule;
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

        if (kit.isBuild() || kit.isWaterdrop() || kit.isSpleef()) {
            if (match.getArena().getAvailableArenas().size() <= 0) {
                match.broadcast(ChatColor.RED + "No hay arenas disponibles.");
                this.plugin.getMatchManager().removeMatch(match);
                match.getTeams().forEach(team -> team.getPlayers().forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    this.plugin.getPlayerManager().sendToSpawnAndResetNoTP(player);
                }));
                return;
            }
            match.setStandaloneArena(match.getArena().getAvailableArena());
            this.plugin.getArenaManager().setArenaMatchUUID(match.getArena(), match.getMatchId());
        }

        Set<Player> matchPlayers = new HashSet<>();

        CustomLocation locationA = match.getArena().getA();
        CustomLocation locationB = match.getArena().getB();
        CustomLocation locationCenter = match.getArena().getCenter();
        List<Location> locs = Circle.getCircle(MathUtil.getMiddle(locationA.toBukkitLocation(), locationB.toBukkitLocation()), kit.isSumo() ? 2 : 10,
                match.getTeams().get(0).getAlivePlayers().size());

        /*match.getTeams().forEach(team -> team.getAlivePlayers().forEach(player1 -> {
            Player p1 = Bukkit.getPlayer(player1);
            team.getAlivePlayers().forEach(player2 -> {
                Player p2 = Bukkit.getPlayer(player2);
                //if (player1 == player2) return;
                if (team.getPlayers().contains(player1) && team.getPlayers().contains(player2)) {
                    NameTagHandler.addToTeam(p1, p2, ChatColor.GREEN, kit.isBuild());
                    NameTagHandler.addToTeam(p2, p1, ChatColor.GREEN, kit.isBuild());
                } else {
                    NameTagHandler.addToTeam(p1, p2, ChatColor.RED, kit.isBuild());
                    NameTagHandler.addToTeam(p2, p1, ChatColor.RED, kit.isBuild());
                }
            });
        }));*/

        match.getTeams().forEach(team -> team.alivePlayers().forEach(player -> {
            matchPlayers.add(player);

            this.plugin.getMatchManager().removeMatchRequests(player.getUniqueId());

            PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

            player.setAllowFlight(false);
            player.setFlying(false);

            playerData.setCurrentMatchID(match.getMatchId());
            playerData.setTeamID(team.getTeamID());
            playerData.setPlayerState(PlayerState.FIGHTING);

            playerData.setMissedPots(0);
            playerData.setLongestCombo(0);
            playerData.setCombo(0);
            playerData.setHits(0);

            PlayerUtil.clearPlayer(player);

            if(match.isFFA()){
                Location loc = locs.get(0);
                Location target = kit.isTnttag() ? loc.setDirection(locationCenter.toBukkitLocation().toVector()) : loc.setDirection(MathUtil.getMiddle(locationA.toBukkitLocation(), locationB.toBukkitLocation()).subtract(loc).toVector());
                player.teleport(target);
                locs.remove(0);
            } else {
                Location locA = locationA.toBukkitLocation();
                Location locB = locationB.toBukkitLocation();
                if (kit.isWaterdrop()) {
                    locA.subtract(0.0, 1.0, 0.0);
                    locB.subtract(0.0, 1.0, 0.0);
                }
                player.teleport(team.getTeamID() == 1 ? locA : locB);
            }
            if (kit.isCombo()) {
                player.setMaximumNoDamageTicks(3);
                CraftPlayer playerCp = (CraftPlayer) player;
                EntityPlayer playerEp = playerCp.getHandle();
                KnockbackProfile profile4 = KnockbackModule.getByName("combo");
                playerEp.setKnockback(profile4);
            } else if (kit.isSumo()) {
                player.setMaximumNoDamageTicks(20);
                CraftPlayer playerCp = (CraftPlayer) player;
                EntityPlayer playerEp = playerCp.getHandle();
                KnockbackProfile profile4 = KnockbackModule.getByName("sumo");
                playerEp.setKnockback(profile4);
            } else {
                player.setMaximumNoDamageTicks(20);
                CraftPlayer playerCp = (CraftPlayer) player;
                EntityPlayer playerEp = playerCp.getHandle();
                KnockbackProfile profile4 = KnockbackModule.getByName("default");
                playerEp.setKnockback(profile4);
            }
            if (!match.isRedrover()) {
                if (!kit.isSumo() && !kit.isTnttag()) {
                    this.plugin.getMatchManager().giveKits(player, kit);
                }

                playerData.setPlayerState(PlayerState.FIGHTING);
            }

            if (match.isFFA()) {
                for (UUID uuid : team.getAlivePlayers()){
                    Player teamplayer = this.plugin.getServer().getPlayer(uuid);
                    NameTagHandler.removeFromTeams(player, teamplayer);
                    NameTagHandler.addToTeam(player, teamplayer, ChatColor.RED, kit.isBuild());
                }
            } else {
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

            if(kit.isSumo() || kit.isSpleef() || kit.isWaterdrop()){
                PlayerUtil.denyMovement(player);
            }
        }));

        for (Player player : matchPlayers) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.hidePlayer(player);
                player.hidePlayer(online);
            }
        }

        for (Player player : matchPlayers) {
            for (Player other : matchPlayers) {
                player.showPlayer(other);
            }
        }

        if (!match.isParty() && !match.isPartyMatch()) {
            Player player1 = Bukkit.getPlayer(match.getTeams().get(0).getAlivePlayers().get(0));
            PlayerData data1 = this.plugin.getPlayerManager().getPlayerData(player1.getUniqueId());
            Player player2 = Bukkit.getPlayer(match.getTeams().get(1).getAlivePlayers().get(0));
            PlayerData data2 = this.plugin.getPlayerManager().getPlayerData(player2.getUniqueId());

            player1.sendMessage(CC.translate("&7&m----------------------------------"));
            player1.sendMessage(CC.translate("&eOponente: &c" + player2.getName()));
            player1.sendMessage(CC.translate("&ePais: &c" + data2.getCountry()));
            if (match.getType() == QueueType.RANKED) {
                player1.sendMessage(CC.translate("&eElo: &c" + data2.getElo(kit.getName())));
            }
            player1.sendMessage(CC.translate("&7&m----------------------------------"));

            player2.sendMessage(CC.translate("&7&m----------------------------------"));
            player2.sendMessage(CC.translate("&eOponente: &c" + player1.getName()));
            player2.sendMessage(CC.translate("&ePais: &c" + data1.getCountry()));
            if (match.getType() == QueueType.RANKED) {
                player2.sendMessage(CC.translate("&eElo: &c" + data1.getElo(kit.getName())));
            }
            player2.sendMessage(CC.translate("&7&m----------------------------------"));
        }

        new MatchRunnable(match).runTaskTimer(this.plugin, 20L, 20L);
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        Match match = event.getMatch();

        match.setMatchState(MatchState.ENDING);
        match.setWinningTeamId(event.getWinningTeam().getTeamID());
        match.setCountdown(4);

        event.getWinningTeam().getPlayers().forEach(uuidWin -> {
            Player pWin1 = Bukkit.getPlayer(uuidWin);
            if (match.getKit().isBuild()) {
                NameTagHandler.removeHealthDisplay(pWin1);
            }
            event.getWinningTeam().getPlayers().forEach(uuidWin2 -> {
                Player pWin2 = Bukkit.getPlayer(uuidWin2);
                if (pWin1 == pWin2) return;
                NameTagHandler.removeFromTeams(pWin1, pWin2);
                NameTagHandler.removeFromTeams(pWin2, pWin1);
            });
            event.getLosingTeam().getPlayers().forEach(uuidLos -> {
                Player pLos1 = Bukkit.getPlayer(uuidLos);
                NameTagHandler.removeFromTeams(pLos1, pWin1);
                NameTagHandler.removeFromTeams(pWin1, pLos1);
            });
            //NametagEdit.updatePrefix(pWin1);
        });
        event.getLosingTeam().getPlayers().forEach(uuidLos -> {
            Player pLos1 = Bukkit.getPlayer(uuidLos);
            if (match.getKit().isBuild()) {
                NameTagHandler.removeHealthDisplay(pLos1);
            }
            event.getLosingTeam().getPlayers().forEach(uuidLos2 -> {
                Player pLos2 = Bukkit.getPlayer(uuidLos2);
                if (pLos1 == pLos2) return;
                NameTagHandler.removeFromTeams(pLos1, pLos2);
                NameTagHandler.removeFromTeams(pLos2, pLos1);
            });
            //NametagEdit.updatePrefix(pLos1);
        });

        if (match.isFFA()) {
            Player winner = this.plugin.getServer().getPlayer(event.getWinningTeam().getAlivePlayers().get(0));
            String winnerMessage = CC.GREEN + "Ganador: " + CC.WHITE;
            String losserMessage = CC.RED + "Perdedor: " + CC.WHITE;
            List<String> winnerPlayers = Lists.newArrayList();
            List<String> losserPlayers = Lists.newArrayList();
            /*JSONMessage winnerInventories = JSONMessage.create();
            JSONMessage losserInventories = JSONMessage.create();*/

            match.spectatorPlayers().forEach(player -> this.plugin.getPlayerManager().sendToSpawnAndReset(player));

            event.getWinningTeam().players().forEach(player -> {
                PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
                data.setPlayerState(PlayerState.SPAWN);
                if (!match.hasSnapshot(player.getUniqueId())) {
                    match.addSnapshot(player);
                }
                if (player.getUniqueId() == winner.getUniqueId()) {
                    winnerPlayers.add(player.getName());
                } else {
                    losserPlayers.add(player.getName());
                }


                /*NameTagHandler.removeHealthDisplay(player);

                event.getLosingTeam().players().forEach(other -> {
                    NameTagHandler.removeFromTeams(other, player);
                    NameTagHandler.removeFromTeams(player, other);
                });*/
                if (match.getKit().isCombo()) {
                    player.setMaximumNoDamageTicks(20);
                    CraftPlayer playerCp = (CraftPlayer) player;
                    EntityPlayer playerEp = playerCp.getHandle();
                    KnockbackProfile profile4 = KnockbackModule.getByName("default");
                    playerEp.setKnockback(profile4);
                }
            });
            /*event.getLosingTeam().players().forEach(player -> {
                PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
                data.setPlayerState(PlayerState.SPAWN);
            });*/
            for (InventorySnapshot snapshot : match.getSnapshots().values()) {
                this.plugin.getInventoryManager().addSnapshot(snapshot);
            }

            match.getTeams().forEach(team -> team.players().forEach(players -> {
                players.sendMessage(CC.translate("&b" + winner.getName() + " &fha &aGanado &fla Pelea."));
                players.sendMessage(CC.translate("&7&m---------------------------------------"));
                players.sendMessage(CC.translate("&eInventarios &7(Clic en el nick)"));
                players.sendMessage(CC.translate(winnerMessage + winnerPlayers.toString().replace("[", "").replace("]", "")));
                players.sendMessage(CC.translate(losserMessage + losserPlayers.toString().replace("[", "").replace("]", "")));
                players.sendMessage(CC.translate("&7&m---------------------------------------"));
            }));
        } else if (match.isRedrover()) {
            match.broadcast(CC.SECONDARY + event.getWinningTeam().getLeaderName() + CC.PRIMARY + " ha ganado el Redrover!");
        } else {
            Player winner = Bukkit.getPlayer(event.getWinningTeam().getLeader());
            Player losser = Bukkit.getPlayer(event.getLosingTeam().getLeader());
            PlayerData winnerData = this.plugin.getPlayerManager().getPlayerData(winner.getUniqueId());
            String winnerMessage = CC.GREEN + "Ganador: " + CC.WHITE;
            String losserMessage = CC.RED + "Perdedor: " + CC.WHITE;
            JSONMessage winnerInventories = JSONMessage.create();
            JSONMessage losserInventories = JSONMessage.create();
            match.getTeams().forEach(team -> team.players().forEach(player -> {
                if (!match.hasSnapshot(player.getUniqueId())) {
                    match.addSnapshot(player);
                }

                boolean onWinningTeam = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getTeamID() == event.getWinningTeam().getTeamID();
                if (onWinningTeam) {
                    winnerInventories.then(winnerMessage + player.getName())
                            .tooltip("§bVer inventario")
                            .runCommand("/inv " + match.getSnapshot(player.getUniqueId()).getSnapshotId());
                } else {
                    losserInventories.then(losserMessage + player.getName())
                            .tooltip("§bVer inventario")
                            .runCommand("/inv " + match.getSnapshot(player.getUniqueId()).getSnapshotId());
                }

                MatchTeam otherTeam = team == match.getTeams().get(0) ? match.getTeams().get(1) : match.getTeams().get(0);

                /*NameTagHandler.removeHealthDisplay(player);
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
                }*/

                if (match.getKit().isCombo()) {
                    player.setMaximumNoDamageTicks(20);
                    CraftPlayer playerCp = (CraftPlayer) player;
                    EntityPlayer playerEp = playerCp.getHandle();
                    KnockbackProfile profile4 = KnockbackModule.getByName("default");
                    playerEp.setKnockback(profile4);
                }
            }));
            for (InventorySnapshot snapshot : match.getSnapshots().values()) {
                this.plugin.getInventoryManager().addSnapshot(snapshot);
            }

            if (match.getKit().getName().equalsIgnoreCase("NoDebuff") || match.getKit().getName().equalsIgnoreCase("NoDebuffLite") ||
                    match.getKit().getName().equalsIgnoreCase("AxePvP") || match.getKit().getName().equalsIgnoreCase("HCF") ||
                    match.getKit().getName().equalsIgnoreCase("Tanqueado") || match.getKit().getName().equalsIgnoreCase("Debuff")) {
                winnerData.setPotsLeft((int) Arrays.stream(winner.getInventory().getContents())
                        .filter(Objects::nonNull).map(ItemStack::getDurability).filter(d -> d == 16421).count());
            }

            match.getTeams().forEach(team -> team.players().forEach(players -> {
                players.sendMessage(CC.translate("&b" + winner.getName() + " &fha &aGanado &fla Pelea."));
                players.sendMessage(CC.translate("&7&m---------------------------------------"));
                players.sendMessage(CC.translate("&eInventarios &7(Clic en el nick)"));
                winnerInventories.send(players);
                losserInventories.send(players);
                players.sendMessage(CC.translate("&7&m---------------------------------------"));
            }));

            match.getSpectators().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                match.getSpectators().remove(uuid);
            });

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

                        int matchWins = winnerLeaderData.getRankedWins(kitName);
                        winnerData.setRankedWins(kitName, matchWins + 1);

                        int matchLosses = loserLeaderData.getRankedLosses(kitName);
                        loserLeaderData.setRankedLosses(kitName, matchLosses + 1);

                        /*winnerLeaderData.setRankedWins(kitName, winnerLeaderData.getRankedWins(kitName) + 1);
                        loserLeaderData.setRankedLosses(kitName, loserLeaderData.getRankedLosses(kitName) + 1);*/
                    }
                }

                match.broadcast(eloMessage);
            }
            this.plugin.getMatchManager().saveRematches(match);
        }
    }

    @EventHandler
    public void onMatchCancel(MatchCancelEvent event) {
        Match match = event.getMatch();
        match.setMatchState(MatchState.ENDING);
        match.getTeams().forEach(teams -> teams.getPlayers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            PlayerData data = this.plugin.getPlayerManager().getPlayerData(uuid);
            player.sendMessage(CC.translate("&eEsta partida ha sido cancelada por un Staff debido a problemas tecnicos."));
            data.setPlayerState(PlayerState.SPAWN);
            this.plugin.getPlayerManager().sendToSpawnAndReset(player);
        }));
        this.plugin.getMatchManager().removeMatch(match);
        new MatchResetRunnable(match).runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onMatchReset(MatchResetEvent event) {
        Match match = event.getMatch();
        Kit kit = match.getKit();
        match.setCountdown(6);
        match.setMatchState(MatchState.STARTING);

        Set<Player> matchPlayers = new HashSet<>();

        CustomLocation locationA = match.getArena().getA();
        CustomLocation locationB = match.getArena().getB();
        CustomLocation locationCenter = match.getArena().getCenter();
        List<Location> locs = Circle.getCircle(MathUtil.getMiddle(locationA.toBukkitLocation(), locationB.toBukkitLocation()), kit.isSumo() ? 2 : 10,
                match.getTeams().get(0).getAlivePlayers().size());

        match.getTeams().forEach(team -> team.alivePlayers().forEach(player -> {
            PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            matchPlayers.add(player);
            PlayerUtil.clearPlayer(player);

            if(match.isFFA()){
                Location loc = locs.get(0);
                Location target = kit.isTnttag() ? loc.setDirection(locationCenter.toBukkitLocation().toVector()) : loc.setDirection(MathUtil.getMiddle(locationA.toBukkitLocation(), locationB.toBukkitLocation()).subtract(loc).toVector());
                player.teleport(target);
                locs.remove(0);
            } else {
                Location locA = locationA.toBukkitLocation();
                Location locB = locationB.toBukkitLocation();
                if (kit.isWaterdrop()) {
                    locA.subtract(0.0, 1.0, 0.0);
                    locB.subtract(0.0, 1.0, 0.0);
                }
                player.teleport(team.getTeamID() == 1 ? locA : locB);
            }

            if (!match.isRedrover()) {
                if (!kit.isSumo() && !kit.isTnttag()) {
                    this.plugin.getMatchManager().giveKits(player, kit);
                }
                playerData.setPlayerState(PlayerState.FIGHTING);
            }

            if(kit.isSumo() || kit.isSpleef() || kit.isWaterdrop()){
                PlayerUtil.denyMovement(player);
            }
        }));

        for (Player player : matchPlayers) {
            for (Player online : Bukkit.getOnlinePlayers()) {
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
}