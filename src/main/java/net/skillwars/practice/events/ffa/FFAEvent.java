package net.skillwars.practice.events.ffa;

import lombok.Getter;
import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.nametag.NameTagHandler;
import net.skillwars.practice.Practice;
import net.skillwars.practice.events.EventCountdownTask;
import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.CustomLocation;
import net.skillwars.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FFAEvent extends PracticeEvent<FFAPlayer> {

    public Map<UUID, FFAPlayer> players = new HashMap<>();

    @Getter HashSet<String> fighting = new HashSet<>();
    private FFACountdownTask countdownTask = new FFACountdownTask(this);

    public FFAEvent() {
        super("FFA");
    }

    @Override
    public Map<UUID, FFAPlayer> getPlayers() {
        return players;
    }

    @Override
    public EventCountdownTask getCountdownTask() {
        return countdownTask;
    }

    @Override
    public List<CustomLocation> getSpawnLocations() {
        return Collections.singletonList(this.getPlugin().getSpawnManager().getTournamentLocation());
    }

    @Override
    public void onStart() {
        selectPlayers();
    }

    @Override
    public Consumer<Player> onJoin() {
        return player -> players.put(player.getUniqueId(), new FFAPlayer(player.getUniqueId(), this));
    }

    @Override
    public Consumer<Player> onDeath() {

        return player -> {

            FFAPlayer data = getPlayer(player);
            PlayerData playerData = this.getPlugin().getPlayerManager().getPlayerData(player.getUniqueId());

            if (data == null || data.getFighting() == null) {
                return;
            }

            if (data.getState() == FFAPlayer.FFAState.FIGHTING || data.getState() == FFAPlayer.FFAState.PREPARING) {

                data.setState(FFAPlayer.FFAState.ELIMINATED);
                this.fighting.remove(player.getName());
                getPlugin().getEventManager().addSpectatorFFA(player, playerData, this);

                player.spigot().respawn();

                sendMessage(ChatColor.YELLOW + "(Event) " + ChatColor.RED + player.getName() + ChatColor.GRAY + " has been eliminated.");

                if (this.getByState(FFAPlayer.FFAState.FIGHTING).size() == 1) {
                    Player winner = Bukkit.getPlayer(this.getByState(FFAPlayer.FFAState.FIGHTING).get(0));

                    PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                    winnerData.setSumoEventWins(winnerData.getSumoEventWins() + 1);

                    Bukkit.broadcastMessage(CC.translate("&e[Evento] &fGanador: &a" + winner.getName()));

                    this.fighting.clear();
                    end();
                }
            }
        };
    }

    private void selectPlayers() {

        sendMessage(ChatColor.YELLOW + "(Event) " + ChatColor.GRAY + "Teletransportando jugadores...");

        this.fighting.clear();

        Kit kit = Practice.getInstance().getKitManager().getKit("FFA");

        for (Player online : this.getBukkitPlayers()) {

            if (online != null) {

                FFAPlayer data = getPlayer(online);

                PlayerUtil.clearPlayer(online);

                data.setFighting(data);
                data.setState(FFAPlayer.FFAState.FIGHTING);
                this.fighting.add(online.getName());

                for (String other : fighting) {
                    Player otherPlayer = Bukkit.getPlayer(other);
                    NameTagHandler.removeFromTeams(online, otherPlayer);
                    NameTagHandler.addToTeam(online, otherPlayer, ChatColor.RED, false);
                }

                online.teleport(Practice.getInstance().getSpawnManager().getFFALocation().toBukkitLocation());
                online.getInventory().setContents(kit.getContents());
                online.getInventory().setArmorContents(kit.getArmor());
                online.updateInventory();
            }
        }
    }

    public List<UUID> getByState(FFAPlayer.FFAState state) {
        return players.values().stream().filter(player -> player.getState() == state).map(FFAPlayer::getUuid).collect(Collectors.toList());
    }
}
