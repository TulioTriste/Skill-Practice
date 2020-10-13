package net.skillwars.practice.managers;

import lombok.Setter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.events.EventState;
import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.nodebufflite.NoDebuffLiteEvent;
import net.skillwars.practice.events.teamfights.TeamFightEvent;
import net.skillwars.practice.events.sumo.SumoEvent;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.util.CustomLocation;

import org.bukkit.GameMode;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Getter
public class EventManager {

    private final Map<Class<? extends PracticeEvent>, PracticeEvent> events = new HashMap<>();

    private final Practice plugin = Practice.getInstance();

    private HashMap<UUID, PracticeEvent> spectators;

    @Setter private boolean enabled;
    @Setter private String name;
    @Setter private long cooldown;

    private final World eventWorld;

    public EventManager() {
        Arrays.asList(
                SumoEvent.class,
                NoDebuffLiteEvent.class,
                TeamFightEvent.class
        ).forEach(this::addEvent);

        boolean newWorld;
        eventWorld = plugin.getServer().getWorld("event");
        newWorld = false;

        enabled = false;
        name = "Ninguno";

        this.spectators = new HashMap<>();

        this.cooldown = 0L;

        if (eventWorld != null) {

            if(newWorld) {
                plugin.getServer().getWorlds().add(eventWorld);
            }

            eventWorld.setTime(2000L);
            eventWorld.setGameRuleValue("doDaylightCycle", "false");
            eventWorld.setGameRuleValue("doMobSpawning", "false");
            eventWorld.setStorm(false);
            eventWorld.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
        }
    }

    public PracticeEvent getByName(String name) {
        return events.values().stream().filter(event -> event.getName().toLowerCase().equalsIgnoreCase(name.toLowerCase())).findFirst().orElse(null);
    }

    public void hostEvent(PracticeEvent event, Player host) {
        event.setState(EventState.WAITING);
        event.setHost(host);
        event.startCountdown();
        this.enabled = true;
    }

    private void addEvent(Class<? extends PracticeEvent> clazz) {
        PracticeEvent event = null;

        try {
            event = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        events.put(clazz, event);
    }

    public void addSpectatorTeamFights(Player player, PlayerData playerData, TeamFightEvent event) {

        this.addSpectator(player, playerData, event);

        if (event.getSpawnLocations().size() == 1) {
            player.teleport(event.getSpawnLocations().get(0).toBukkitLocation());
        } else {
            List<CustomLocation> spawnLocations = new ArrayList<>(event.getSpawnLocations());
            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())).toBukkitLocation());
        }


        for(Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }

        player.setGameMode(GameMode.SPECTATOR);

        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void addSpectatorSumo(Player player, PlayerData playerData, SumoEvent event) {

        this.addSpectator(player, playerData, event);

        if (event.getSpawnLocations().size() == 1) {
            player.teleport(event.getSpawnLocations().get(0).toBukkitLocation());
        } else {
            List<CustomLocation> spawnLocations = new ArrayList<>(event.getSpawnLocations());
            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())).toBukkitLocation());
        }


        for(Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }

        player.setGameMode(GameMode.SPECTATOR);

        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void addSpectatorNoDebuffLite(Player player, PlayerData playerData, NoDebuffLiteEvent event) {

        this.addSpectator(player, playerData, event);

        if (event.getSpawnLocations().size() == 1) {
            player.teleport(event.getSpawnLocations().get(0).toBukkitLocation());
        } else {
            List<CustomLocation> spawnLocations = new ArrayList<>(event.getSpawnLocations());
            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())).toBukkitLocation());
        }


        for(Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }

        player.setGameMode(GameMode.SPECTATOR);

        player.setAllowFlight(true);
        player.setFlying(true);
    }

    private void addSpectator(Player player, PlayerData playerData, PracticeEvent event) {

        playerData.setPlayerState(PlayerState.SPECTATING);
        this.spectators.put(player.getUniqueId(), event);

        player.getInventory().setContents(this.plugin.getItemManager().getSpecItems());
        player.updateInventory();

        this.plugin.getServer().getOnlinePlayers().forEach(online -> {
            online.hidePlayer(player);
            player.hidePlayer(online);
        });

    }

    public void removeSpectator(Player player) {
        this.getSpectators().remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }


    public boolean isPlaying(Player player, PracticeEvent event) {
        return event.getPlayers().containsKey(player.getUniqueId());
    }

    public PracticeEvent getEventPlaying(Player player) {
        return this.events.values().stream().filter(event -> this.isPlaying(player, event)).findFirst().orElse(null);
    }
}