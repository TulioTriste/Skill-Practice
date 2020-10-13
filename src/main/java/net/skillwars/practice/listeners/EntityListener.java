package net.skillwars.practice.listeners;

import net.skillwars.practice.Practice;
import net.skillwars.practice.events.nodebufflite.NoDebuffLitePlayer;
import net.skillwars.practice.events.teamfights.TeamFightEvent;
import net.skillwars.practice.events.sumo.SumoPlayer;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.nodebufflite.NoDebuffLiteEvent;
import net.skillwars.practice.events.teamfights.TeamFightPlayer;
import net.skillwars.practice.events.sumo.SumoEvent;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.player.PlayerState;

public class EntityListener implements Listener {
    private final Practice plugin = Practice.getInstance();

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

            switch (playerData.getPlayerState()) {
                case FIGHTING:
                    Match match = this.plugin.getMatchManager().getMatch(playerData);
                    if (match.getMatchState() != MatchState.FIGHTING) {
                        e.setCancelled(true);
                    }
                    if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                        this.plugin.getMatchManager().removeFighter(player, playerData, true);
                    }

                    if(match.getKit().isParkour()) {
                        e.setCancelled(true);
                    }

                    break;
                case EVENT:
                    PracticeEvent event = this.plugin.getEventManager().getEventPlaying(player);

                    if(event != null) {

                        if(event instanceof SumoEvent) {
                            SumoEvent sumoEvent = (SumoEvent) event;
                            SumoPlayer sumoPlayer = sumoEvent.getPlayer(player);

                            if (sumoPlayer != null && sumoPlayer.getState() == SumoPlayer.SumoState.FIGHTING) {
                                e.setCancelled(false);
                            }
                        }
                    }
                    break;
                default:
                    if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                        e.getEntity().teleport(this.plugin.getSpawnManager().getSpawnLocation().toBukkitLocation());
                    }
                    e.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {

        if(!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
            return;
        }

        Player entity = (Player) e.getEntity();

        PlayerData entityData = this.plugin.getPlayerManager().getPlayerData(entity.getUniqueId());
        PracticeEvent eventEntity = this.plugin.getEventManager().getEventPlaying(entity);

        Player damager;

        damager = (Player) e.getDamager();

        PlayerData damagerData = this.plugin.getPlayerManager().getPlayerData(damager.getUniqueId());

        if(entityData == null || damagerData == null) {
            e.setCancelled(true);
            return;
        }

        boolean isEventEntity = this.plugin.getEventManager().getEventPlaying(entity) != null;
        boolean isEventDamager = this.plugin.getEventManager().getEventPlaying(damager) != null;

        PracticeEvent eventDamager = this.plugin.getEventManager().getEventPlaying(damager);

        if(damagerData.getPlayerState() == PlayerState.SPECTATING || this.plugin.getEventManager().getSpectators().containsKey(damager.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        if((!entity.canSee(damager) && damager.canSee(entity)) || damager.getGameMode() == GameMode.SPECTATOR) {
            e.setCancelled(true);
            return;
        }

        if (isEventDamager && eventDamager instanceof TeamFightEvent && ((TeamFightEvent) eventDamager).getPlayer(damager).getState() != TeamFightPlayer.TeamFightState.FIGHTING || isEventEntity &&  eventDamager instanceof TeamFightEvent && ((TeamFightEvent) eventEntity).getPlayer(entity).getState() != TeamFightPlayer.TeamFightState.FIGHTING  || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
            e.setCancelled(true);
            return;
        }

        if (isEventDamager && eventDamager instanceof SumoEvent && ((SumoEvent) eventDamager).getPlayer(damager).getState() != SumoPlayer.SumoState.FIGHTING || isEventEntity &&  eventDamager instanceof SumoEvent && ((SumoEvent) eventEntity).getPlayer(entity).getState() != SumoPlayer.SumoState.FIGHTING  || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
            e.setCancelled(true);
            return;
        }

        if (isEventDamager && eventDamager instanceof NoDebuffLiteEvent
                && ((NoDebuffLiteEvent) eventDamager).getPlayer(damager).getState() != NoDebuffLitePlayer.MiniNoDebuffState.FIGHTING
                || isEventEntity &&  eventDamager instanceof NoDebuffLiteEvent
                && ((NoDebuffLiteEvent) eventEntity).getPlayer(entity).getState() != NoDebuffLitePlayer.MiniNoDebuffState.FIGHTING) {
            e.setCancelled(true);
            return;
        }

        if(entityData.getPlayerState() == PlayerState.EVENT
                && eventEntity instanceof SumoEvent
                || damagerData.getPlayerState() == PlayerState.EVENT
                && eventDamager instanceof SumoEvent) {
            e.setDamage(0.0D);
            return;
        }

        if(entityData.getPlayerState() == PlayerState.EVENT &&
                eventEntity instanceof TeamFightEvent
                || damagerData.getPlayerState() == PlayerState.EVENT
                && eventDamager instanceof TeamFightEvent) {
            return;
        }

        if(entityData.getPlayerState() == PlayerState.EVENT &&
                eventEntity instanceof NoDebuffLiteEvent
                || damagerData.getPlayerState() == PlayerState.EVENT
                && eventDamager instanceof NoDebuffLiteEvent) {
            return;
        }

        Match match = this.plugin.getMatchManager().getMatch(entityData);

        if(match == null) {
            e.setDamage(0.0D);
            return;
        }

        if (damagerData.getTeamID() == entityData.getTeamID() && !match.isFFA()) {
            e.setCancelled(true);
            return;
        }

        if(match.getKit().isParkour()) {
            e.setCancelled(true);
            return;
        }

        if (match.getKit().isSpleef() || match.getKit().isSumo()) {
            e.setDamage(0.0D);
        }

        if (e.getDamager() instanceof Player) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (match.getKit().isCombo()) {
                        entity.setNoDamageTicks(3);
                    }else{
                        entity.setNoDamageTicks(20);
                    }
                }
            }.runTaskLater(Practice.getInstance(), 1L);
            damagerData.setCombo(damagerData.getCombo() + 1);
            damagerData.setHits(damagerData.getHits() + 1);

            if (damagerData.getCombo() > damagerData.getLongestCombo()) {
                damagerData.setLongestCombo(damagerData.getCombo());
            }

            entityData.setCombo(0);

            if (match.getKit().isSpleef()) {
                e.setCancelled(true);
            }
        } else if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();

            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();

                if (!entity.getName().equals(shooter.getName())) {
                    double health = Math.ceil(entity.getHealth() - e.getFinalDamage()) / 2.0D;

                    if (health > 0.0D) {
                        shooter.sendMessage(ChatColor.YELLOW + "[*] " + ChatColor.GREEN + entity.getName() + " has been shot." + ChatColor.DARK_GRAY + " (" + ChatColor.RED + health + "❤" + ChatColor.DARK_GRAY  + ")");
                    }
                }
            }
        }

    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }
        for (PotionEffect effect : e.getEntity().getEffects()) {
            if (effect.getType().equals(PotionEffectType.HEAL)) {
                Player shooter = (Player) e.getEntity().getShooter();

                if (e.getIntensity(shooter) <= 0.5D) {
                    PlayerData shooterData = this.plugin.getPlayerManager().getPlayerData(shooter.getUniqueId());

                    if (shooterData != null) {
                        shooterData.setMissedPots(shooterData.getMissedPots() + 1);
                    }
                }
                break;
            }
        }
    }
}