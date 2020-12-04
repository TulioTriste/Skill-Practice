package net.skillwars.practice.commands;

import net.skillwars.practice.Practice;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class PlayerResetCommand extends Command {

    private Practice plugin;

    public PlayerResetCommand() {
        super("playerreset");
        this.setPermission("practice.staff");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!commandSender.hasPermission(getPermission())) {
            commandSender.sendMessage(CC.translate("&cNo tienes los suficientes permisos."));
            return true;
        }
        if (strings.length == 0) {
            commandSender.sendMessage(CC.translate("&cUsa: /" + s + " <player>"));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(strings[0]);

        if (!target.hasPlayedBefore()) {
            commandSender.sendMessage(CC.translate("&cEste jugador no existe."));
            return true;
        }

        if (target.isOnline()) {
            commandSender.sendMessage(CC.translate("&cEste usuario no se encuentra online"));
            return true;
        }
        this.plugin.getPlayerManager().sendToSpawnAndReset(target.getPlayer());
        commandSender.sendMessage(CC.translate("&aPlayer reseteado correctamente."));
        return false;
    }
}
