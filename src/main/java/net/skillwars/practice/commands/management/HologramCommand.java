package net.skillwars.practice.commands.management;

import com.google.common.collect.Lists;
import com.sainttx.holograms.HologramPlugin;
import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.line.HologramLine;
import com.sainttx.holograms.api.line.TextLine;
import net.skillwars.practice.Practice;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class HologramCommand extends Command implements Listener {

    private Practice plugin = Practice.getInstance();
    private Config config = new Config("holograms", plugin);
    private HologramManager hologramManager = HologramPlugin.getPlugin(HologramPlugin.class).getHologramManager();

    public HologramCommand() {
        super("practicehologram");
        this.setPermission("practice.command.hologram");
        this.setAliases(Arrays.asList("prachologram", "hologrampractice", "holoprac", "pracholo"));
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(CC.translate("&4No Console."));
            return true;
        }

        if (!commandSender.hasPermission(getPermission())) {
            commandSender.sendMessage(CC.translate("&cNo tienes los suficientes permisos para usar esto."));
            return true;
        }

        Player player = (Player) commandSender;
        if (strings.length == 0) {
            player.sendMessage(CC.translate("&cUsa: /" + s + " create/delete"));
            return true;
        }
        Location loc = player.getLocation();
        Hologram hologram = new Hologram("STATS", loc);
        List<PlayerData> playerDataList2 = Practice.getInstance().getLeaderboardManager().getGlobalplayerDataList();
        List<String> stats = Lists.newArrayList();
        stats.add(CC.translate("STATS GLOBAL TEST"));
        for (int i = 1; i <= 3; i++) {
            PlayerData data = playerDataList2.get(i);
            OfflinePlayer target = Bukkit.getOfflinePlayer(data.getUniqueId());
            stats.add(CC.translate("&e" + i + ") &f" + target.getName() + " " + data.getGlobalStats("ELO")));
        }
        stats.forEach(lines -> {
            HologramLine line = new TextLine(hologram, lines);
            hologram.addLine(line);
        });
        hologram.setPersistent(true);
        hologram.spawn();
        hologramManager.addActiveHologram(hologram);
        hologramManager.saveHologram(hologram);
        hologramManager.reload();

        new BukkitRunnable() {

            @Override
            public void run() {
                hologramManager.reload();
            }
        }.runTaskTimer(this.plugin, 20L, 20L);

        return false;
    }
}
