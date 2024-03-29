package net.skillwars.practice.listeners;

import net.skillwars.practice.Practice;
import net.skillwars.practice.events.EventState;
import net.skillwars.practice.events.ffa.FFAEvent;
import net.skillwars.practice.events.ffa.FFAPlayer;
import net.skillwars.practice.events.nodebufflite.NoDebuffLitePlayer;
import net.skillwars.practice.events.teamfights.TeamFightEvent;
import net.skillwars.practice.events.sumo.SumoPlayer;
import net.skillwars.practice.events.tnttag.TNTTagEvent;
import net.skillwars.practice.events.tnttag.TNTTagPlayer;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
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

                    if (match.getMatchState() != MatchState.FIGHTING) e.setCancelled(true);

                    //if (e.getCause() == EntityDamageEvent.DamageCause.VOID) this.plugin.getMatchManager().removeFighter(player, playerData, true);

                    if(match.getKit().isParkour()) e.setCancelled(true);

                    else if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && match.getKit().isTnttag()) e.setCancelled(true);

                    break;
                case EVENT:
                    PracticeEvent event = this.plugin.getEventManager().getEventPlaying(player);

                    if(event != null) {
                        if (event instanceof SumoEvent) {
                            SumoEvent sumoEvent = (SumoEvent) event;
                            SumoPlayer sumoPlayer = sumoEvent.getPlayer(player);

                            if (sumoPlayer != null && sumoPlayer.getState() == SumoPlayer.SumoState.FIGHTING && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                                e.setCancelled(false);
                            }
                        }
                        else if (event instanceof TeamFightEvent) {
                            TeamFightEvent tfEvent = (TeamFightEvent) event;
                            TeamFightPlayer tfPlayer = tfEvent.getPlayer(player);

                            if (tfEvent.getState().equals(EventState.WAITING) && !tfPlayer.getState().equals(TeamFightPlayer.TeamFightState.FIGHTING)) {
                                e.setCancelled(true);
                            }
                        }
                        else if (event instanceof TNTTagEvent) {
                            TNTTagEvent tntEvent = (TNTTagEvent) event;
                            TNTTagPlayer tntTagPlayer = tntEvent.getPlayer(player);

                            if (tntEvent.getState().equals(EventState.STARTED) && tntTagPlayer.getState().equals(TNTTagPlayer.TNTTagState.FIGHTING) && !e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                                e.setCancelled(false);
                            } else {
                                e.setCancelled(true);
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

        if (e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            PlayerData damagerData = this.plugin.getPlayerManager().getPlayerData(damager.getUniqueId());

            if(entityData == null || damagerData == null) {
                e.setCancelled(true);
                return;
            }

            boolean isEventEntity = this.plugin.getEventManager().getEventPlaying(entity) != null;
            boolean isEventDamager = this.plugin.getEventManager().getEventPlaying(damager) != null;

            PracticeEvent eventDamager = this.plugin.getEventManager().getEventPlaying(damager);
            PracticeEvent event = this.plugin.getEventManager().getEventPlaying(entity);

            if (isEventEntity && isEventDamager && eventDamager instanceof TeamFightEvent && event instanceof TeamFightEvent) {
                TeamFightEvent eventt = (TeamFightEvent) event;
                if (eventt.getBlueTeam().contains(entity.getUniqueId())) {
                    if (eventt.getBlueTeam().contains(damager.getUniqueId())) {
                        e.setCancelled(true);
                    }
                }
                else if (eventt.getRedTeam().contains(entity.getUniqueId())) {
                    if (eventt.getRedTeam().contains(damager.getUniqueId())) {
                        e.setCancelled(true);
                    }
                }
            }

            if (isEventDamager && isEventEntity && eventDamager instanceof TNTTagEvent && event instanceof TNTTagEvent) {
                TNTTagEvent eventt = (TNTTagEvent) eventDamager;
                TNTTagPlayer test = ((TNTTagEvent) eventDamager).getPlayer(damager.getUniqueId());
                if (test.getState().equals(TNTTagPlayer.TNTTagState.FIGHTING)) {
                    if (eventt.getBomb().equals(damager)) {
                        eventt.setBomb(entity);
                        eventt.getFighting().forEach(name -> {
                            Player player = Bukkit.getPlayer(name);
                            player.sendMessage(ChatColor.AQUA + eventt.getBomb().getName() + " tiene la Bomba!");
                        });
                    }
                }
            }

            if(damagerData.getPlayerState() == PlayerState.SPECTATING
                    || this.plugin.getEventManager().getSpectators().containsKey(damager.getUniqueId())) {
                e.setCancelled(true);
                return;
            }

            if((!entity.canSee(damager) && damager.canSee(entity)) || damager.getGameMode() == GameMode.SPECTATOR) {
                e.setCancelled(true);
                return;
            }

            if (isEventDamager && eventDamager instanceof TeamFightEvent
                    && ((TeamFightEvent) eventDamager).getPlayer(damager).getState() != TeamFightPlayer.TeamFightState.FIGHTING
                    || isEventEntity &&  eventDamager instanceof TeamFightEvent
                    && ((TeamFightEvent) eventEntity).getPlayer(entity).getState() != TeamFightPlayer.TeamFightState.FIGHTING
                    || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING
                    || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
                e.setCancelled(true);
                return;
            }

            if (isEventDamager && eventDamager instanceof SumoEvent
                    && ((SumoEvent) eventDamager).getPlayer(damager).getState() != SumoPlayer.SumoState.FIGHTING
                    || isEventEntity &&  eventDamager instanceof SumoEvent
                    && ((SumoEvent) eventEntity).getPlayer(entity).getState() != SumoPlayer.SumoState.FIGHTING
                    || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING
                    || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
                e.setCancelled(true);
                return;
            }

            if (isEventDamager && eventDamager instanceof FFAEvent
                    && ((FFAEvent) eventDamager).getPlayer(damager).getState() != FFAPlayer.FFAState.FIGHTING
                    || isEventEntity &&  eventDamager instanceof FFAEvent
                    && ((FFAEvent) eventEntity).getPlayer(entity).getState() != FFAPlayer.FFAState.FIGHTING
                    || !isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING
                    || !isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING) {
                e.setCancelled(true);
                return;
            }

            if (isEventDamager && eventDamager instanceof NoDebuffLiteEvent
                    && ((NoDebuffLiteEvent) eventDamager).getPlayer(damager).getState() != NoDebuffLitePlayer.NoDebuffLiteState.FIGHTING
                    || isEventEntity &&  eventDamager instanceof NoDebuffLiteEvent
                    && ((NoDebuffLiteEvent) eventEntity).getPlayer(entity).getState() != NoDebuffLitePlayer.NoDebuffLiteState.FIGHTING) {
                e.setCancelled(true);
                return;
            }

            if (isEventDamager && eventDamager instanceof TNTTagEvent
                    && ((TNTTagEvent) eventDamager).getPlayer(damager).getState() != TNTTagPlayer.TNTTagState.FIGHTING
                    || isEventEntity &&  eventDamager instanceof TNTTagEvent
                    && ((TNTTagEvent) eventEntity).getPlayer(entity).getState() != TNTTagPlayer.TNTTagState.FIGHTING) {
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

            if(entityData.getPlayerState() == PlayerState.EVENT &&
                    eventEntity instanceof FFAEvent
                    || damagerData.getPlayerState() == PlayerState.EVENT
                    && eventDamager instanceof FFAEvent) {
                return;
            }

            if(entityData.getPlayerState() == PlayerState.EVENT &&
                    eventEntity instanceof TNTTagEvent
                    || damagerData.getPlayerState() == PlayerState.EVENT
                    && eventDamager instanceof TNTTagEvent) {
                e.setDamage(0.0D);
                return;
            }

            Match match = this.plugin.getMatchManager().getMatch(entityData);

            if(match == null) {
                e.setDamage(0.0D);
                return;
            }

            if (match.getKit().isTnttag()) {
                e.setCancelled(false);
                e.setDamage(0.0D);
                if (damager.getInventory().getHelmet() == null && damager.getInventory().getItem(0) == null) return;

                if (damager.getInventory().getHelmet().getType() == Material.TNT && damager.getInventory().getItem(0).getType() == Material.TNT) {
                    damager.getInventory().setHelmet(null);
                    damager.getInventory().setItem(0, null);
                    entity.getInventory().setHelmet(new ItemStack(Material.TNT));
                    entity.getInventory().setItem(0, new ItemStack(Material.TNT));
                    match.broadcast("&b" + entity.getName() + " &fes actualmente la Bomba.");
                }
                return;
            }

            if (match.getKit().isWaterdrop()) {
                e.setCancelled(true);
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

            if (match.getKit().isSpleef() || match.getKit().isSumo() || match.getKit().isTnttag()) {
                e.setDamage(0.0D);
            }

            if (e.getDamager() instanceof Player) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (match.getKit().isCombo()) {
                            entity.setNoDamageTicks(3);
                        } else {
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
                            shooter.sendMessage(ChatColor.GREEN + entity.getName() + " ha sido disparado." + ChatColor.DARK_GRAY + " (" + ChatColor.RED + health + "❤" + ChatColor.DARK_GRAY  + ")");
                        }
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