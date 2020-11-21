package net.skillwars.practice.commands;

import net.skillwars.practice.Practice;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class PlayerStatusCommand extends Command {

    private Practice plugin;

    public PlayerStatusCommand() {
        super("playerstatus");
        this.setPermission("practice.playerstatus");
        this.plugin = Practice.getInstance();
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (strings.length == 0) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(CC.translate("&4No Console."));
                return true;
            }
            Player player = (Player) commandSender;
            PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            player.sendMessage(CC.translate("&7&m--------------------------------"));
            player.sendMessage(CC.translate("&b&l" + player.getName() + " &bState&7: &f" + data.getPlayerState().name()));
            player.sendMessage(CC.translate("&7&m--------------------------------"));
        }
        else if (strings.length == 1) {
            Player target = Bukkit.getPlayer(strings[0]);
            PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());
            if (!target.isOnline()) {
                commandSender.sendMessage(CC.translate("&cEste Usuario no se encuentra online."));
                return true;
            }
            commandSender.sendMessage(CC.translate("&7&m--------------------------------"));
            commandSender.sendMessage(CC.translate("&b&l" + target.getName() + " &bState&7: &f" + targetData.getPlayerState().name()));
            commandSender.sendMessage(CC.translate("&7&m--------------------------------"));
        }
        return false;
    }
}
