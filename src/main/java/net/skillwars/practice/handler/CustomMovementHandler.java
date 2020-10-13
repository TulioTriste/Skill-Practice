package net.skillwars.practice.handler;

import net.skillwars.practice.Practice;
import net.skillwars.practice.util.CustomLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.teamfights.TeamFightEvent;
import net.skillwars.practice.events.teamfights.TeamFightPlayer;
import net.skillwars.practice.events.sumo.SumoEvent;
import net.skillwars.practice.events.sumo.SumoPlayer;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.util.BlockUtil;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import pt.foxspigot.jar.handler.MovementHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class CustomMovementHandler implements MovementHandler {
    private Practice plugin = Practice.getInstance();
    private static HashMap<Match, HashMap<UUID, CustomLocation>> parkourCheckpoints = new HashMap<>();


    @Override
    public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packetPlayInFlying) {
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        if (playerData == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            player.kickPlayer(ChatColor.RED + "Your data is null please reconnect");
            return;
        }

        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());

            if(match == null) {
                return;
            }

            if (match.getKit().isSpleef() || match.getKit().isSumo()) {

                if (BlockUtil.isOnLiquid(to, 0) || BlockUtil.isOnLiquid(to, 1)) {
                    this.plugin.getMatchManager().removeFighter(player, playerData, true);
                }


                if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
                    if (match.getMatchState() == MatchState.STARTING) {
                        player.teleport(from);
                        ((CraftPlayer) player).getHandle().playerConnection.checkMovement = false;
                    }
                }
            }

            if (match.getKit().isParkour()) {

                if(BlockUtil.isStandingOn(player, Material.GOLD_PLATE)) {

                    Iterator<UUID> uuidIterator = this.plugin.getMatchManager().getOpponents(match, player).iterator();

                    while (uuidIterator.hasNext()) {
                        UUID uuid = uuidIterator.next();

                        Player opponent = Bukkit.getPlayer(uuid);

                        if(opponent != null) {
                            this.plugin.getMatchManager().removeFighter(opponent, this.plugin.getPlayerManager().getPlayerData(opponent.getUniqueId()), true);
                        }
                    }

                    parkourCheckpoints.remove(match);

                } else if (BlockUtil.isStandingOn(player, Material.WATER) || BlockUtil.isStandingOn(player, Material.STATIONARY_WATER)) {
                    this.teleportToSpawnOrCheckpoint(match, player);
                } else if (BlockUtil.isStandingOn(player, Material.STONE_PLATE) || BlockUtil.isStandingOn(player, Material.IRON_PLATE) || BlockUtil.isStandingOn(player, Material.WOOD_PLATE)) {

                    boolean checkpoint = false;

                    if(!parkourCheckpoints.containsKey(match)) {
                        checkpoint = true;
                        parkourCheckpoints.put(match, new HashMap<>());
                    }

                    if(!parkourCheckpoints.get(match).containsKey(player.getUniqueId())) {
                        checkpoint = true;
                        parkourCheckpoints.get(match).put(player.getUniqueId(), CustomLocation.fromBukkitLocation(player.getLocation()));
                    }
                    else if(parkourCheckpoints.get(match).containsKey(player.getUniqueId()) && !BlockUtil.isSameLocation(player.getLocation(), parkourCheckpoints.get(match).get(player.getUniqueId()).toBukkitLocation())) {
                        checkpoint = true;
                        parkourCheckpoints.get(match).put(player.getUniqueId(), CustomLocation.fromBukkitLocation(player.getLocation()));
                    }

                    if(checkpoint) {
                        player.sendMessage(ChatColor.GRAY + "Checkpoint has been saved.");
                    }

                }

                if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
                    if (match.getMatchState() == MatchState.STARTING) {
                        player.teleport(from);
                        ((CraftPlayer) player).getHandle().playerConnection.checkMovement = false;
                    }
                }
            }
        }

        PracticeEvent event2 = this.plugin.getEventManager().getEventPlaying(player);

        if(event2 != null) {

            if(event2 instanceof SumoEvent) {
                SumoEvent sumoEvent = (SumoEvent) event2;

                if (sumoEvent.getPlayer(player).getFighting() != null && sumoEvent.getPlayer(player).getState() == SumoPlayer.SumoState.PREPARING) {
                    player.teleport(from);
                    ((CraftPlayer) player).getHandle().playerConnection.checkMovement = false;
                }
            } else if(event2 instanceof TeamFightEvent) {
                TeamFightEvent redroverEvent = (TeamFightEvent) event2;

                if (redroverEvent.getPlayer(player).getFightTask() != null && redroverEvent.getPlayer(player).getState() == TeamFightPlayer.TeamFightState.PREPARING) {
                    player.teleport(from);
                    ((CraftPlayer) player).getHandle().playerConnection.checkMovement = false;
                }

            }
        }
    }

    @Override
    public void handleUpdateRotation(Player player, Location location, Location location1, PacketPlayInFlying packetPlayInFlying) {

    }

    private void teleportToSpawnOrCheckpoint(Match match, Player player) {

        if(!parkourCheckpoints.containsKey(match)) {
            player.sendMessage(ChatColor.GRAY + "Teleporting back to the beginning.");
            player.teleport(match.getArena().getA().toBukkitLocation());
            return;
        }

        if(!parkourCheckpoints.get(match).containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.GRAY + "Teleporting back to the beginning.");
            player.teleport(match.getArena().getA().toBukkitLocation());
            return;
        }

        player.teleport(parkourCheckpoints.get(match).get(player.getUniqueId()).toBukkitLocation());
        player.sendMessage(ChatColor.GRAY + "Teleporting back to last checkpoint.");
    }

    public static HashMap<Match, HashMap<UUID, CustomLocation>> getParkourCheckpoints() {
        return parkourCheckpoints;
    }
}
