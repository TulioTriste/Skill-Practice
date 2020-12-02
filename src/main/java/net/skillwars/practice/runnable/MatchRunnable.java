package net.skillwars.practice.runnable;

import net.skillwars.practice.Practice;
import net.skillwars.practice.event.match.MatchResetEvent;
import net.skillwars.practice.listeners.MatchListener;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.util.CC;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class MatchRunnable extends BukkitRunnable {

    private final Practice plugin;
    private final Match match;

    public MatchRunnable(final Match match) {
        this.plugin = Practice.getInstance();
        this.match = match;
    }

    public void run() {
        switch (this.match.getMatchState()) {
            case STARTING:
                if (this.match.decrementCountdown() == 0) {
                    this.match.setMatchState(MatchState.FIGHTING);
                    this.match.broadcastWithSound(CC.GREEN + "La pelea ha comenzado!", Sound.FIREWORK_BLAST);
                    if (this.match.isRedrover()) {
                        this.plugin.getMatchManager().pickPlayer(this.match);
                    }
                    this.match.setStartTimestamp(System.currentTimeMillis());
                    Kit kit = match.getKit();

                    if (kit.isTnttag()) {
                        MatchTeam team = this.match.getTeams().get(ThreadLocalRandom.current().nextInt(this.match.getTeams().size()));
                        Player bomb = Bukkit.getPlayer(team.getAlivePlayers().get(ThreadLocalRandom.current().nextInt(team.getAlivePlayers().size())));
                        this.match.setBomb(bomb);
                        this.match.getBomb().getInventory().setHelmet(new ItemStack(Material.TNT));
                        this.match.getBomb().getInventory().setItem(0, new ItemStack(Material.TNT));
                        this.match.setMatchTime(30);
                        this.match.broadcast("&b" + this.match.getBomb().getName() + " &fes la Bomba.");
                    }

                    if (kit.isWaterdrop()) {
                        Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
                            @Override
                            public void run() {
                                if (match.getMatchState() == MatchState.FIGHTING) {
                                    plugin.getServer().getScheduler().cancelTask(MatchRunnable.this.getTaskId());
                                    match.getEntitiesToRemove().forEach(Entity::remove);
                                    match.getPlacedBlockLocations().forEach(location -> location.getBlock().setType(Material.AIR));
                                    match.getOriginalBlockChanges().forEach((blockState) -> blockState.getLocation().getBlock().setType(blockState.getType()));
                                    match.getWaterDropSpawn().forEach((location, block1) -> location.getBlock().setType(Material.OBSIDIAN));
                                    Bukkit.getPluginManager().callEvent(new MatchResetEvent(match));
                                }
                            }
                        }, 120L);
                    }

                    match.getTeams().forEach(team -> team.alivePlayers().forEach(player -> {
                        if (kit.isSumo() || kit.isSpleef() || kit.isWaterdrop()){
                            PlayerUtil.allowMovement(player);
                        }
                        if (kit.isWaterdrop()) {
                            player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().setType(Material.AIR);
                            this.match.getWaterDropSpawn().put(player.getLocation().subtract(0.0, 1.0, 0.0), player.getLocation().subtract(0.0, 1.0, 0.0).getBlock());
                        }
                        if (kit.getName().contains("Gapple")){
                            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                        } else if (kit.getName().contains("Combo")){
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                        } else if (kit.getName().contains("OnePunch")) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 99));
                        } else if (kit.getName().contains("Archer")) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                        } else if (kit.getName().contains("TNTTag")) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                        }
                    }));
                } else {
                    this.match.broadcastWithSound(CC.WHITE + "La pelea empieza en " + CC.SECONDARY
                            + this.match.getCountdown() + CC.WHITE + " segundo(s)...", Sound.CLICK);
                }
                break;
            case FIGHTING:
                if (match.getKit().isTnttag()) {
                    this.match.decrementMatchTime();
                    if (Arrays.asList(15, 10, 5, 4, 3, 2, 1).contains(this.match.getMatchTime())) {
                        this.match.broadcastWithSound(CC.WHITE + "La pelea termina en " + CC.SECONDARY
                        + this.match.getMatchTime() + CC.WHITE + " segundo(s)...", Sound.CLICK);
                    }
                    if (this.match.getMatchTime() == 0) {
                        this.match.getBomb().setHealth(0.0F);
                    }
                }
                break;
            case SWITCHING:
                if (this.match.decrementCountdown() == 0) {
                    this.match.getEntitiesToRemove().forEach(Entity::remove);
                    this.match.clearEntitiesToRemove();
                    this.match.setMatchState(MatchState.FIGHTING);
                    this.plugin.getMatchManager().pickPlayer(this.match);
                }
                break;
            case ENDING:
                if (this.match.decrementCountdown() == 0) {
                    plugin.getTournamentManager().removeTournamentMatch(match);
                    match.getRunnables().forEach(id -> plugin.getServer().getScheduler().cancelTask(id));
                    match.getEntitiesToRemove().forEach(Entity::remove);
                    match.getTeams().forEach(team ->
                            team.alivePlayers().forEach(plugin.getPlayerManager()::sendToSpawnAndReset));
                    match.spectatorPlayers().forEach(plugin.getMatchManager()::removeSpectator);
                    match.getPlacedBlockLocations().forEach(location -> location.getBlock().setType(Material.AIR));
                    match.getOriginalBlockChanges().forEach((blockState) -> blockState.getLocation().getBlock().setType(blockState.getType()));
                    if (match.getKit().isWaterdrop()) {
                        this.match.getWaterDropSpawn().forEach((location, block1) -> location.getBlock().setType(Material.OBSIDIAN));
                    }
                    plugin.getMatchManager().removeMatch(match);
                    new MatchResetRunnable(match).runTaskTimer(plugin, 20L, 20L);
                    this.cancel();
                }
                break;
        }
    }
}