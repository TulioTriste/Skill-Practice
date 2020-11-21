package net.skillwars.practice.runnable;

import net.skillwars.practice.Practice;
import net.skillwars.practice.listeners.MatchListener;
import net.skillwars.practice.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.util.CC;

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

                    match.getTeams().forEach(team -> team.alivePlayers().forEach(player -> {
                        if (kit.isSumo() || match.getKit().isSpleef()){
                            PlayerUtil.allowMovement(player);
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
                        }
                    }));
                } else {
                    this.match.broadcastWithSound(CC.WHITE + "La pelea empieza en " + CC.SECONDARY
                            + this.match.getCountdown() + CC.WHITE + " segundo(s)...", Sound.CLICK);
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
                    plugin.getMatchManager().removeMatch(match);
                    new MatchResetRunnable(match).runTaskTimer(plugin, 20L, 20L);
                    this.cancel();
                }
                break;
        }
    }
}
