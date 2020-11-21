package net.skillwars.practice.runnable;

import net.skillwars.practice.Practice;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class MatchTntTagRunnable extends BukkitRunnable {

    private final Practice plugin;
    private final Match match;
    private final Player bomb;
    private int i = 30;

    public MatchTntTagRunnable(Match match) {
        this.plugin = Practice.getInstance();
        this.match = match;
        bomb = this.plugin.getMatchManager().selectBomb(match);
    }

    @Override
    public void run() {
        match.setMatchState(MatchState.STARTING);
        this.match.getTeams().forEach(team -> team.getAlivePlayers().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (i == 30) {
                    player.sendMessage(CC.translate("&fLa pelea comienza en &b3 &fsegundo(s)"));
                }
                else if (i == 29) {
                    player.sendMessage(CC.translate("&fLa pelea comienza en &b2 &fsegundo(s)"));
                }
                else if (i == 28) {
                    player.sendMessage(CC.translate("&fLa pelea comienza en &b1 &fsegundo(s)"));
                }
                else if (i == 27) {
                    player.sendMessage(CC.translate("&aLa pelea ha comenzado"));
                    player.sendMessage(CC.translate("&b" + bomb.getName() + " es la Bomba!"));
                    bomb.getInventory().setHelmet(new ItemStack(Material.TNT));
                    bomb.getInventory().setItem(0, new ItemStack(Material.TNT));
                    match.setMatchState(MatchState.FIGHTING);
                }
                else if (Arrays.asList(10, 5, 4, 3, 2, 1).contains(i)) {
                    player.sendMessage(CC.translate("&bQuedan " + i + " para que termina la Pelea."));
                }
                else if (i == 0) {
                    plugin.getPlayerManager().sendToSpawnAndReset(player);
                }
            }));
        if (i <= 0) {
            this.cancel();
            match.getTeams().forEach(matchTeam -> matchTeam.getAlivePlayers().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player.getInventory().getHelmet().getType().equals(Material.TNT) && player.getInventory().getItem(0).getType().equals(Material.TNT)) {
                    player.setHealth(0.0D);
                }
            }));
            match.setMatchState(MatchState.ENDING);
        }
        i--;
    }
}
