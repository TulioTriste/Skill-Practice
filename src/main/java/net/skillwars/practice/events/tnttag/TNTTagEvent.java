package net.skillwars.practice.events.tnttag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.joansiitoh.skillcore.apis.NametagEdit;
import me.joeleoli.nucleus.nametag.NameTagHandler;
import net.skillwars.practice.Practice;
import net.skillwars.practice.events.EventCountdownTask;
import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.sumo.SumoPlayer;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.CustomLocation;
import net.skillwars.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TNTTagEvent extends PracticeEvent<TNTTagPlayer> {

    public Map<UUID, TNTTagPlayer> players = new HashMap<>();
    @Getter HashSet<String> fighting = new HashSet<>();
    private final TNTTagCountdownTask countdownTask = new TNTTagCountdownTask(this);
    @Getter public TNTTagTimeTask countdownTest = new TNTTagTimeTask();
    @Getter Player bomb;

    public TNTTagEvent() {
        super("TNTTag");
    }

    @Override
    public Map<UUID, TNTTagPlayer> getPlayers() {
        return players;
    }

    @Override
    public EventCountdownTask getCountdownTask() {
        return countdownTask;
    }

    @Override
    public List<CustomLocation> getSpawnLocations() {
        return Collections.singletonList(getPlugin().getSpawnManager().getTntTagLocation());
    }

    @Override
    public void onStart() {
        selectPlayers();
    }

    @Override
    public Consumer<Player> onJoin() {
        return player -> players.put(player.getUniqueId(), new TNTTagPlayer(player.getUniqueId(), this));
    }

    @Override
    public Consumer<Player> onDeath() {
        return player -> {

            TNTTagPlayer data = getPlayer(player);
            PlayerData playerData = this.getPlugin().getPlayerManager().getPlayerData(player.getUniqueId());

            if (data == null || data.getFighting() == null) {
                return;
            }

            if (data.getState() == TNTTagPlayer.TNTTagState.FIGHTING) {

                data.setState(TNTTagPlayer.TNTTagState.ELIMINATED);
                this.fighting.remove(player.getName());
                player.spigot().respawn();
                getPlugin().getEventManager().addSpectatorTNTTag(player, playerData, this);

                sendMessage(ChatColor.RED + player.getName() + ChatColor.GRAY + " ha sido eliminado");

                if (this.getByState(TNTTagPlayer.TNTTagState.FIGHTING).size() == 1) {
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            Player winner = Bukkit.getPlayer(getByState(TNTTagPlayer.TNTTagState.FIGHTING).get(0));

                            PlayerData winnerData = Practice.getInstance().getPlayerManager().getPlayerData(winner.getUniqueId());
                            winnerData.setSumoEventWins(winnerData.getSumoEventWins() + 1);

                            Bukkit.broadcastMessage(CC.translate("&e[Evento] &fGanador: &a" + winner.getName()));

                            fighting.clear();
                            end();
                        }
                    }.runTaskLater(getPlugin(), 60L);
                }
                else if (this.getByState(TNTTagPlayer.TNTTagState.FIGHTING).size() == 0) {
                    fighting.clear();
                    end();
                }
            }
        };
    }

    public void setBomb(Player target) {
        if (this.bomb != null) {
            this.bomb.getInventory().setHelmet(new ItemStack(Material.AIR));
            this.bomb.getInventory().setItem(0, new ItemStack(Material.AIR));
            this.bomb.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        }
        this.bomb = target;
        target.getInventory().setHelmet(new ItemStack(Material.TNT));
        this.bomb.getInventory().setItem(0, new ItemStack(Material.TNT));
        this.bomb.playSound(this.bomb.getLocation(), Sound.LEVEL_UP, 5.0F, 5.0F);
        this.bomb.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
    }

    private void selectPlayers() {
        sendMessage("&e[Evento] &fTeletransportando jugadores...");

        this.fighting.clear();

        for (Player online : this.getBukkitPlayers()) {
            if (online != null) {
                TNTTagPlayer data = getPlayer(online);
                PlayerUtil.clearPlayer(online);
                data.setFighting(data);
                data.setState(TNTTagPlayer.TNTTagState.PREPARING);
                this.fighting.add(online.getName());
                online.teleport(Practice.getInstance().getSpawnManager().getTntTagLocation().toBukkitLocation());
            }
        }

        countdownTest.runTaskTimer(getPlugin(), 0L, 20L);
    }

    public List<UUID> getByState(TNTTagPlayer.TNTTagState state) {
        return players.values().stream().filter(player -> player.getState() == state).map(TNTTagPlayer::getUuid).collect(Collectors.toList());
    }

    @Getter
    @RequiredArgsConstructor
    public class TNTTagTimeTask extends BukkitRunnable {
        @Getter public int time = 61;

        @Override
        public void run() {

            if (getPlayers().size() <= 0) {
                cancel();
                return;
            }

            if (time == 58) {
                List<Player> players = new ArrayList<>();
                getFighting().forEach(uuid -> players.add(Bukkit.getPlayer(uuid)));
                setBomb(players.get(ThreadLocalRandom.current().nextInt(players.size())));
            }

            getFighting().forEach(name -> {
                Player player = Bukkit.getPlayer(name);
                TNTTagPlayer tntTagPlayer = getPlayer(player.getUniqueId());

                if (time == 61) {
                    player.teleport(getPlugin().getSpawnManager().getTntTagLocation().toBukkitLocation());
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                } else if (time == 60) {
                    player.sendMessage(CC.translate("&aLa Ronda 1 comienza en 3"));
                } else if (time == 59) {
                    player.sendMessage(CC.translate("&aLa Ronda 1 comienza en 2"));
                } else if (time == 58) {
                    player.sendMessage(CC.translate("&aLa Ronda 1 comienza en 1"));
                } else if (time == 57) {
                    player.sendMessage(CC.translate("&aLa Ronda 1 ha comenzado"));
                    tntTagPlayer.setState(TNTTagPlayer.TNTTagState.FIGHTING);
                    bomb.getInventory().setHelmet(new ItemStack(Material.TNT));
                    player.sendMessage(CC.translate("&b" + bomb.getName() + " is it the Bomb!"));
                } else if (time <= 0) {
                    cancel();
                    return;
                }

                if (Arrays.asList(10, 5, 4, 3, 2, 1).contains(time)) {
                    player.sendMessage(CC.translate("&bLa ronda terminará en " + time));
                }
            });

            if (time <= 0) {
                bomb.playSound(bomb.getLocation(), Sound.EXPLODE, 5.0F, 5.0F);

                getFighting().forEach(name -> {
                    Player target = Bukkit.getPlayer(name);
                    target.sendMessage(CC.translate("&b" + bomb.getName() + " ha sido explotado"));
                    target.sendMessage(CC.translate("&cLa ronda ha terminado."));
                });

                bomb.setHealth(0.0);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new TNTTagTimeTask().runTaskTimer(getPlugin(), 0L, 20L);
                    }
                }.runTaskLater(getPlugin(), 40L);
            }

            time--;
        }
    }
}
