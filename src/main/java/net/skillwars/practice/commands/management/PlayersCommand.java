package net.skillwars.practice.commands.management;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.management.PlatformLoggingMXBean;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class PlayersCommand extends Command {

    private Practice plugin;
    public static PlayersCommand INSTANCE;
    @Getter Map<UUID, Boolean> mode = Maps.newConcurrentMap();

    public PlayersCommand() {
        super("players");
        this.setAliases(Arrays.asList("showplayer", "showplayers"));
        this.setPermission("practice.commands.players");
        this.plugin = Practice.getInstance();
        INSTANCE = this;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!commandSender.hasPermission(this.getPermission())) {
            commandSender.sendMessage(CC.translate("&cNo tienes los suficientes permisos para ejecutar ese comando."));
            return true;
        }
        Player player = (Player) commandSender;
        Bukkit.getOnlinePlayers().forEach(player1 -> Bukkit.getOnlinePlayers().forEach(player2 -> {
            player1.showPlayer(player2);
            player2.showPlayer(player1);
        }));
        getMode().put(player.getUniqueId(), true);
        player.sendMessage(CC.translate("&aAhora puedes ver a todos los Usuarios"));
        return false;
    }
}
