package net.skillwars.practice.listeners;

import me.joansiitoh.datas.events.NickUpdateEvent;
import me.joansiitoh.skillcore.apis.NametagEdit;
import me.joeleoli.nucleus.nametag.NameTagHandler;
import net.skillwars.practice.Practice;
import net.skillwars.practice.commands.management.PlayersCommand;
import net.skillwars.practice.event.match.MatchCancelEvent;
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
import net.skillwars.practice.runnable.MatchResetRunnable;
import net.skillwars.practice.runnable.MatchRunnable;
import net.skillwars.practice.runnable.MatchTntTagRunnable;
import net.skillwars.practice.util.*;
import net.minecraft.server.v1_8_R3.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pt.foxspigot.jar.knockback.KnockbackModule;
import pt.foxspigot.jar.knockback.KnockbackProfile;

import javax.swing.*;
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
            this.plugin.getArenaManager().setArenaMatchUUID(match.getArena(), match.getMatchId());
        }

        if (kit.isBuild()) {
            if (match.getArena().getAvailableArenas().size() <= 0) {
                match.broadcast(ChatColor.RED + "No hay arenas disponibles.");
                this.plugin.getMatchManager().removeMatch(match);
                match.getTeams().forEach(team -> team.getAlivePlayers().forEach(uuid -> {
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

        match.getTeams().forEach(team -> team.getAlivePlayers().forEach(player1 -> {
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
        }));

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
                player.teleport(team.getTeamID() == 1 ? locationA.toBukkitLocation() : locationB.toBukkitLocation());
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

            /*if (match.isFFA()) {
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
            }*/

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

        if (kit.isTnttag()) {
            new MatchTntTagRunnable(match).runTaskTimer(this.plugin, 20L, 20L);
            return;
        }
        new MatchRunnable(match).runTaskTimer(this.plugin, 20L, 20L);
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        Match match = event.getMatch();

        match.setMatchState(MatchState.ENDING);
        match.setWinningTeamId(event.getWinningTeam().getTeamID());
        match.setCountdown(4);

        /*event.getWinningTeam().getPlayers().forEach(uuidWin -> {
            Player pWin1 = Bukkit.getPlayer(uuidWin);
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
        });
        event.getLosingTeam().getPlayers().forEach(uuidLos -> {
            Player pLos1 = Bukkit.getPlayer(uuidLos);
            event.getLosingTeam().getPlayers().forEach(uuidLos2 -> {
                Player pLos2 = Bukkit.getPlayer(uuidLos2);
                if (pLos1 == pLos2) return;
                NameTagHandler.removeFromTeams(pLos1, pLos2);
                NameTagHandler.removeFromTeams(pLos2, pLos1);
            });
        });*/

        if (match.isFFA()) {
            Player winner = this.plugin.getServer().getPlayer(event.getWinningTeam().getAlivePlayers().get(0));
            PlayerData winnerData = this.plugin.getPlayerManager().getPlayerData(winner.getUniqueId());
            Player losser = this.plugin.getServer().getPlayer(event.getLosingTeam().getAlivePlayers().get(0));
            PlayerData losserData = this.plugin.getPlayerManager().getPlayerData(losser.getUniqueId());
            Clickable inventories = new Clickable("            " + CC.DARK_AQUA);

            event.getWinningTeam().players().forEach(player -> {
                PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
                data.setPlayerState(PlayerState.SPAWN);
                if (!match.hasSnapshot(player.getUniqueId())) {
                    match.addSnapshot(player);
                }
                if (player.getUniqueId() == winner.getUniqueId()) {
                    inventories.add(CC.DARK_AQUA + player.getName() + CC.GRAY + " vs ",
                            CC.AQUA + "Ver inventario",
                            "/inv " + match.getSnapshot(player.getUniqueId()).getSnapshotId());
                } else {
                    inventories.add(CC.DARK_AQUA + player.getName(),
                            CC.AQUA + "Ver inventario",
                            "/inv " + match.getSnapshot(player.getUniqueId()).getSnapshotId());
                }


                NameTagHandler.removeHealthDisplay(player);

                /*event.getLosingTeam().players().forEach(other -> {
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
            event.getLosingTeam().players().forEach(player -> {
                PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
                data.setPlayerState(PlayerState.SPAWN);
            });
            for (InventorySnapshot snapshot : match.getSnapshots().values()) {
                this.plugin.getInventoryManager().addSnapshot(snapshot);
            }

            match.broadcast("&b" + winner.getName() + " &fha &aGanado &fla Pelea.");
            match.broadcast("&7&m---------------------------------------");
            match.broadcast("                  &b" + match.getKit().getName() + " &7- &b" + TimeUtil.millisToTimer(match.getElapsedDuration()));
            match.broadcast("");
            match.broadcast(inventories);
            match.broadcast("");
            match.broadcast("               &f" + winnerData.getHits() + " &7- &bGolpes dados &7- &f" + losserData.getHits());
            match.broadcast("            &f" + winnerData.getLongestCombo() + " &7- &bCombo mas largo &7- &f" + losserData.getLongestCombo());
            if (match.getKit().getName().equalsIgnoreCase("NoDebuff") || match.getKit().getName().equalsIgnoreCase("NoDebuffLite") ||
                    match.getKit().getName().equalsIgnoreCase("AxePvP") || match.getKit().getName().equalsIgnoreCase("HCF") ||
                    match.getKit().getName().equalsIgnoreCase("Tanqueado") || match.getKit().getName().equalsIgnoreCase("Debuff")) {
                match.broadcast("         &f" + winnerData.getPotsLeft() + " &7- &bPociones restantes &7- &f" + losserData.getPotsLeft());
                match.broadcast("         &f" + winnerData.getMissedPots() + " &7- &bPociones gastadas &7- &f" + losserData.getMissedPots());
            }
            match.broadcast("");
            match.broadcast("                        &f" + MathUtil.roundToHalves(winner.getHealth() / 2.0D) + " / 10 &c❤");
            match.broadcast(CC.translate("&7&m---------------------------------------"));
        } else if (match.isRedrover()) {
            match.broadcast(CC.SECONDARY + event.getWinningTeam().getLeaderName() + CC.PRIMARY + " ha ganado el Redrover!");
        } else {
            Player winner = Bukkit.getPlayer(event.getWinningTeam().getLeader());
            Player losser = Bukkit.getPlayer(event.getLosingTeam().getLeader());
            PlayerData winnerData = this.plugin.getPlayerManager().getPlayerData(winner.getUniqueId());
            PlayerData losserData = this.plugin.getPlayerManager().getPlayerData(losser.getUniqueId());
            Clickable inventories = new Clickable("            " + CC.DARK_AQUA);
            match.getTeams().forEach(team -> team.players().forEach(player -> {
                PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
                data.setPlayerState(PlayerState.SPAWN);
                if (!match.hasSnapshot(player.getUniqueId())) {
                    match.addSnapshot(player);
                }

                boolean onWinningTeam = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getTeamID() == event.getWinningTeam().getTeamID();
                if (onWinningTeam) {
                    inventories.add(CC.DARK_AQUA + player.getName() + CC.GRAY + " vs ",
                            CC.AQUA + "Ver inventario",
                            "/inv " + match.getSnapshot(player.getUniqueId()).getSnapshotId());
                } else {
                    inventories.add(CC.DARK_AQUA + player.getName(),
                            CC.AQUA + "Ver inventario",
                            "/inv " + match.getSnapshot(player.getUniqueId()).getSnapshotId());
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

            match.broadcast("&b" + winner.getName() + " &fha &aGanado &fla Pelea.");
            match.broadcast("&7&m---------------------------------------");
            match.broadcast("                  &b" + match.getKit().getName() + " &7- &b" + TimeUtil.millisToTimer(match.getElapsedDuration()));
            match.broadcast("");
            match.broadcast(inventories);
            match.broadcast("");
            match.broadcast("               &f" + winnerData.getHits() + " &7- &bGolpes dados &7- &f" + losserData.getHits());
            match.broadcast("            &f" + winnerData.getLongestCombo() + " &7- &bCombo mas largo &7- &f" + losserData.getLongestCombo());
            if (match.getKit().getName().equalsIgnoreCase("NoDebuff") || match.getKit().getName().equalsIgnoreCase("NoDebuffLite") ||
                    match.getKit().getName().equalsIgnoreCase("AxePvP") || match.getKit().getName().equalsIgnoreCase("HCF") ||
                    match.getKit().getName().equalsIgnoreCase("Tanqueado") || match.getKit().getName().equalsIgnoreCase("Debuff")) {
                match.broadcast("         &f" + winnerData.getPotsLeft() + " &7- &bPociones restantes &7- &f" + losserData.getPotsLeft());
                match.broadcast("         &f" + winnerData.getMissedPots() + " &7- &bPociones gastadas &7- &f" + losserData.getMissedPots());
            }
            match.broadcast("");
            match.broadcast("                        &f" + MathUtil.roundToHalves(winner.getHealth() / 2.0D) + " / 10 &c❤");
            match.broadcast(CC.translate("&7&m---------------------------------------"));

            if (match.getType().isRanked()) {
                String kitName = match.getKit().getName();
                event.getWinningTeam().getPlayers().forEach(player -> {
                    PlayerData data = this.plugin.getPlayerManager().getPlayerData(player);
                    int matchWins = data.getMatchWins(kitName);
                    data.setMatchWins(kitName, matchWins + 1);
                });

                event.getLosingTeam().getPlayers().forEach(player -> {
                    PlayerData data = this.plugin.getPlayerManager().getPlayerData(player);
                    int matchLosses = data.getMatchLosses(kitName);
                    data.setMatchLosses(kitName, matchLosses + 1);
                });

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
}