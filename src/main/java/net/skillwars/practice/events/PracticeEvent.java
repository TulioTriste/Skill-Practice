package net.skillwars.practice.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.joeleoli.nucleus.Nucleus;
import net.skillwars.practice.Practice;
import net.skillwars.practice.event.EventStartEvent;
import net.skillwars.practice.events.ffa.FFAEvent;
import net.skillwars.practice.events.ffa.FFAPlayer;
import net.skillwars.practice.events.nodebufflite.NoDebuffLiteEvent;
import net.skillwars.practice.events.nodebufflite.NoDebuffLitePlayer;
import net.skillwars.practice.events.sumo.SumoEvent;
import net.skillwars.practice.events.sumo.SumoPlayer;
import net.skillwars.practice.events.teamfights.TeamFightEvent;
import net.skillwars.practice.events.teamfights.TeamFightPlayer;
import net.skillwars.practice.events.tnttag.TNTTagEvent;
import net.skillwars.practice.events.tnttag.TNTTagPlayer;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.util.CustomLocation;
import net.skillwars.practice.util.PlayerUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class PracticeEvent<K extends EventPlayer> {
    private final Practice plugin = Practice.getInstance();

    private final String name;

    private int limit = 30;
    private Player host;
    private EventState state = EventState.UNANNOUNCED;

    public void startCountdown() {
        if (this.getCountdownTask().isEnded()) {
            this.getCountdownTask().setTimeUntilStart(this.getCountdownTask().getCountdownTime());
            this.getCountdownTask().setEnded(false);
        } else {
            this.getCountdownTask().runTaskTimerAsynchronously(this.plugin, 20L, 20L);
        }
    }

    public void sendMessage(final String message) {
        this.getBukkitPlayers().forEach(player -> player.sendMessage(message));
    }

    public Set<Player> getBukkitPlayers() {
        return this.getPlayers().keySet().stream().filter(uuid -> this.plugin.getServer().getPlayer(uuid) != null).map(this.plugin.getServer()::getPlayer).collect(Collectors.toSet());
    }

    public void join(final Player player) {
        if (this.getPlayers().size() >= this.limit) {
            return;
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        playerData.setPlayerState(PlayerState.EVENT);
        PlayerUtil.clearPlayer(player);
        if (this.onJoin() != null) {
            this.onJoin().accept(player);
        }
        if (this.getSpawnLocations().size() == 1) {
            player.teleport(this.getSpawnLocations().get(0).toBukkitLocation());
        } else {
            final List<CustomLocation> spawnLocations = new ArrayList<>(this.getSpawnLocations());
            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())).toBukkitLocation());
        }
        this.plugin.getPlayerManager().giveLobbyItems(player);
        for (final Player other : this.getBukkitPlayers()) {
            other.showPlayer(player);
            player.showPlayer(other);
        }
        this.sendMessage(ChatColor.translateAlternateColorCodes('&', Practice.getInstance().getChat().getPlayerPrefix(player) + player.getName() + " &eha entrado al evento." +
                " &7(" + this.getPlayers().size() + " player" + ((this.getPlayers().size() == 1) ? "" : "s") + ")"));
    }

    public void leave(final Player player) {
        if (this.onDeath() != null) {
            this.onDeath().accept(player);
        }
        this.getPlayers().remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }

    public void start() {
        new EventStartEvent(this).call();
        this.setState(EventState.STARTED);
        this.onStart();
        this.plugin.getEventManager().setCooldown(0L);
    }

    public void end() {
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                Bukkit.getWorld(this.getSpawnLocations().get(0).getWorld()).getPlayers().forEach(player ->
                        this.plugin.getPlayerManager().sendToSpawnAndReset(player)), 2L);
        this.plugin.getEventManager().setEnabled(false);
        this.plugin.getEventManager().setName("Ninguno");
        this.plugin.getEventManager().setCooldown(System.currentTimeMillis() + 30 * 1000);

        if (this instanceof SumoEvent) {
            final SumoEvent sumoEvent = (SumoEvent) this;
            for (final SumoPlayer sumoPlayer : sumoEvent.getPlayers().values()) {
                if (sumoPlayer.getFightTask() != null) {
                    sumoPlayer.getFightTask().cancel();
                }
            }
            if (sumoEvent.getWaterCheckTask() != null) {
                sumoEvent.getWaterCheckTask().cancel();
            }
        }

        if (this instanceof NoDebuffLiteEvent) {
            final NoDebuffLiteEvent sumoEvent = (NoDebuffLiteEvent) this;
            for (final NoDebuffLitePlayer sumoPlayer : sumoEvent.getPlayers().values()) {
                if (sumoPlayer.getFightTask() != null) {
                    sumoPlayer.getFightTask().cancel();
                }
            }
        }

        if (this instanceof FFAEvent) {
            final FFAEvent ffaEvent = (FFAEvent) this;
            for (final FFAPlayer ffaPlayer : ffaEvent.getPlayers().values()) {
                if (ffaPlayer.getFightTask() != null) {
                    ffaPlayer.getFightTask().cancel();
                }
            }
        }

        if (this instanceof TeamFightEvent) {
            final TeamFightEvent tfEvent = (TeamFightEvent) this;
            for (final TeamFightPlayer tfPlayer : tfEvent.getPlayers().values()) {
                if (tfPlayer.getFightTask() != null) {
                    tfPlayer.getFightTask().cancel();
                }
            }
        }

        if (this instanceof TNTTagEvent) {
            final TNTTagEvent tnttagEvent = (TNTTagEvent) this;
            for (final TNTTagPlayer tnttagPlayer : tnttagEvent.getPlayers().values()) {
                if (tnttagPlayer.getFightTask() != null) {
                    tnttagPlayer.getFightTask().cancel();
                }
            }
        }
        this.getPlayers().clear();
        this.setState(EventState.UNANNOUNCED);
        final Iterator<UUID> iterator = this.plugin.getEventManager().getSpectators().keySet().iterator();
        while (iterator.hasNext()) {
            final UUID spectatorUUID = iterator.next();
            final Player spectator = Bukkit.getPlayer(spectatorUUID);
            if (spectator != null) {
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.getPlayerManager().sendToSpawnAndReset(spectator));
                iterator.remove();
            }
        }
        this.getBukkitPlayers().forEach(player -> this.plugin.getPlayerManager().sendToSpawnAndReset(player));
        this.plugin.getEventManager().getSpectators().clear();
        this.getPlayers().clear();
        this.getCountdownTask().setEnded(true);
    }

    public K getPlayer(final Player player) {
        return this.getPlayer(player.getUniqueId());
    }

    public K getPlayer(final UUID uuid) {
        return this.getPlayers().get(uuid);
    }

    public abstract Map<UUID, K> getPlayers();

    public abstract EventCountdownTask getCountdownTask();

    public abstract List<CustomLocation> getSpawnLocations();

    public abstract void onStart();

    public abstract Consumer<Player> onJoin();

    public abstract Consumer<Player> onDeath();
}
