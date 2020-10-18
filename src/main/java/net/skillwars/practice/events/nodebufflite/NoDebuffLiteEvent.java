package net.skillwars.practice.events.nodebufflite;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.joeleoli.nucleus.Nucleus;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NoDebuffLiteEvent extends PracticeEvent<NoDebuffLitePlayer> {

    private Map<UUID, NoDebuffLitePlayer> players = new HashMap<>();

    @Getter HashSet<String> fighting = new HashSet<>();
    private NoDebuffLiteCountdownTask countdownTask = new NoDebuffLiteCountdownTask(this);

    public NoDebuffLiteEvent() {
        super("NoDebuffLite");
    }

    @Override
    public Map<UUID, NoDebuffLitePlayer> getPlayers() {
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
        return player -> players.put(player.getUniqueId(), new NoDebuffLitePlayer(player.getUniqueId(), this));
    }

    @Override
    public Consumer<Player> onDeath() {

        return player -> {

            NoDebuffLitePlayer data = getPlayer(player);

            if (data == null || data.getFighting() == null) {
                return;
            }

            if(data.getState() == NoDebuffLitePlayer.MiniNoDebuffState.FIGHTING || data.getState() == NoDebuffLitePlayer.MiniNoDebuffState.PREPARING) {

                NoDebuffLitePlayer killerData = data.getFighting();
                Player killer = getPlugin().getServer().getPlayer(killerData.getUuid());

                data.getFightTask().cancel();
                killerData.getFightTask().cancel();


                PlayerData playerData = this.getPlugin().getPlayerManager().getPlayerData(player.getUniqueId());

                if (playerData != null) {
                    playerData.setSumoEventLosses(playerData.getSumoEventLosses() + 1);
                }

                data.setState(NoDebuffLitePlayer.MiniNoDebuffState.ELIMINATED);
                killerData.setState(NoDebuffLitePlayer.MiniNoDebuffState.WAITING);

                PlayerUtil.clearPlayer(player);
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        getPlugin().getPlayerManager().giveLobbyItems(player);
                    }
                }.runTaskLater(Practice.getInstance(), 5L);

                PlayerUtil.clearPlayer(killer);
                this.getPlugin().getPlayerManager().giveLobbyItems(killer);


                if (getSpawnLocations().size() == 1) {
                    player.teleport(getSpawnLocations().get(0).toBukkitLocation());
                    killer.teleport(getSpawnLocations().get(0).toBukkitLocation());
                }

                /*((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);
                ((CraftPlayer) player).getHandle().playerConnection.player.setFakingDeath(false);*/
                player.spigot().respawn();

                sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " ha sido eliminado" + (killer == null ? "." : " por " + ChatColor.GREEN + killer.getName()));

                if (this.getByState(NoDebuffLitePlayer.MiniNoDebuffState.WAITING).size() == 1) {
                    Player winner = Bukkit.getPlayer(this.getByState(NoDebuffLitePlayer.MiniNoDebuffState.WAITING).get(0));

                    PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                    winnerData.setSumoEventWins(winnerData.getSumoEventWins() + 1);
                    Bukkit.broadcastMessage(CC.translate("&e[Evento] &fGanador: &a" + winner.getName()));

                    this.fighting.clear();
                    end();
                } else {
                    getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), this::selectPlayers, 3 * 20);
                }
            }
        };
    }

    private CustomLocation[] getTournamentLocation() {
        CustomLocation[] array = new CustomLocation[2];
        array[0] = this.getPlugin().getSpawnManager().getTournamentFirst();
        array[1] = this.getPlugin().getSpawnManager().getTournamentSecond();
        return array;
    }

    private void selectPlayers() {

        if (this.getByState(NoDebuffLitePlayer.MiniNoDebuffState.WAITING).size() == 1) {
            Player winner = Bukkit.getPlayer(this.getByState(NoDebuffLitePlayer.MiniNoDebuffState.WAITING).get(0));

            PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
            //winnerData.setSumoEventWins(winnerData.getSumoEventWins() + 1);

            Bukkit.broadcastMessage(CC.translate("&e[Evento] &fGanador: &a" + winner.getName()));

            this.fighting.clear();
            end();
            return;
        }

        Player picked1 = getRandomPlayer();
        Player picked2 = getRandomPlayer();

        if(picked1 == null || picked2 == null) {
            selectPlayers();
            return;
        }

        sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "NoDebuffLite Event " + ChatColor.GRAY + "Seleccionando jugadores randoms...");

        this.fighting.clear();

        NoDebuffLitePlayer picked1Data = getPlayer(picked1);
        NoDebuffLitePlayer picked2Data = getPlayer(picked2);

        picked1Data.setFighting(picked2Data);
        picked2Data.setFighting(picked1Data);

        this.fighting.add(picked1.getName());
        this.fighting.add(picked2.getName());

        PlayerUtil.clearPlayer(picked1);
        PlayerUtil.clearPlayer(picked2);

        Kit kit = Practice.getInstance().getKitManager().getKit("NoDebuffLite");

        picked1.getInventory().setContents(kit.getContents());
        picked1.getInventory().setArmorContents(kit.getArmor());

        picked1.updateInventory();

        picked2.getInventory().setContents(kit.getContents());
        picked2.getInventory().setArmorContents(kit.getArmor());

        picked2.updateInventory();

        picked1.teleport(Practice.getInstance().getSpawnManager().getTournamentFirst().toBukkitLocation());
        picked2.teleport(Practice.getInstance().getSpawnManager().getTournamentSecond().toBukkitLocation());

        for(Player other : this.getBukkitPlayers()) {
            if(other != null) {
                other.showPlayer(picked1);
                other.showPlayer(picked2);
            }
        }

        for(UUID spectatorUUID : this.getPlugin().getEventManager().getSpectators().keySet()) {
            Player spectator = Bukkit.getPlayer(spectatorUUID);
            if(spectatorUUID != null) {
                spectator.showPlayer(picked1);
                spectator.showPlayer(picked2);
            }
        }

        picked1.showPlayer(picked2);
        picked2.showPlayer(picked1);

        sendMessage(ChatColor.YELLOW + "Empezando la Pelea. " + ChatColor.GREEN + "(" + picked1.getName() + " vs " + picked2.getName() + ")");

        BukkitTask task = new MiniNoDebuffFightTask(picked1, picked2, picked1Data, picked2Data).runTaskTimer(getPlugin(), 0, 20);

        picked1Data.setFightTask(task);
        picked2Data.setFightTask(task);
    }

    private Player getRandomPlayer() {

        if(getByState(NoDebuffLitePlayer.MiniNoDebuffState.WAITING).isEmpty()) {
            return null;
        }

        List<UUID> waiting = getByState(NoDebuffLitePlayer.MiniNoDebuffState.WAITING);

        Collections.shuffle(waiting);

        UUID uuid = waiting.get(ThreadLocalRandom.current().nextInt(waiting.size()));

        getPlayer(uuid).setState(NoDebuffLitePlayer.MiniNoDebuffState.PREPARING);

        return getPlugin().getServer().getPlayer(uuid);
    }

    public List<UUID> getByState(NoDebuffLitePlayer.MiniNoDebuffState state) {
        return players.values().stream().filter(player -> player.getState() == state).map(NoDebuffLitePlayer::getUuid).collect(Collectors.toList());
    }

    /**
     * To ensure that the fight doesn't go on forever and to
     * let the players know how much time they have left.
     */
    @Getter
    @RequiredArgsConstructor
    public class MiniNoDebuffFightTask extends BukkitRunnable {
        private final Player player;
        private final Player other;

        private final NoDebuffLitePlayer playerSumo;
        private final NoDebuffLitePlayer otherSumo;

        private int time = 90;

        @Override
        public void run() {

            if (player == null || other == null || !player.isOnline() || !other.isOnline()) {
                cancel();
                return;
            }

            if (time == 90) {
                PlayerUtil.sendMessage(ChatColor.AQUA + "La pelea iniciara en " + ChatColor.GREEN + 3 + ChatColor.AQUA + "...", player, other);
            } else if (time == 89) {
                PlayerUtil.sendMessage(ChatColor.AQUA + "La pelea iniciara en " + ChatColor.GREEN + 2 + ChatColor.AQUA + "...", player, other);
            } else if (time == 88) {
                PlayerUtil.sendMessage(ChatColor.AQUA + "La pelea iniciara en " + ChatColor.GREEN + 1 + ChatColor.AQUA + "...", player, other);
            } else if (time == 87) {
                PlayerUtil.sendMessage(ChatColor.GREEN + "La pelea ha iniciado, buena suerte!", player, other);
                this.otherSumo.setState(NoDebuffLitePlayer.MiniNoDebuffState.FIGHTING);
                this.playerSumo.setState(NoDebuffLitePlayer.MiniNoDebuffState.FIGHTING);
            } else if (time <= 0) {
                List<Player> players = Arrays.asList(player, other);
                Player winner = players.get(ThreadLocalRandom.current().nextInt(players.size()));
                players.stream().filter(pl -> !pl.equals(winner)).forEach(pl -> onDeath().accept(pl));

                cancel();
                return;
            }

            if (Arrays.asList(30, 25, 20, 15, 10).contains(time)) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "La pelea se terminara en " + ChatColor.GREEN + time + ChatColor.YELLOW + "...", player, other);
            } else if (Arrays.asList(5, 4, 3, 2, 1).contains(time)) {
                PlayerUtil.sendMessage(ChatColor.YELLOW + "La pelea terminara en " + ChatColor.GREEN + time + ChatColor.YELLOW + "...", player, other);
            }

            time--;
        }
    }
}
