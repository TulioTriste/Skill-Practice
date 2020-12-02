package net.skillwars.practice.commands.management;

import net.skillwars.practice.Practice;
import net.skillwars.practice.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.util.StringUtil;

public class ResetStatsCommand extends Command {
    private Practice plugin;

    public ResetStatsCommand() {
        super("reset");
        this.plugin = Practice.getInstance();
        this.setUsage(ChatColor.RED + "Usage: /reset [player]");
    }

    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (!player.hasPermission("practice.admin")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
                return true;
            }
        }
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /reset <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            commandSender.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[0]));
            return true;
        }
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        for (Kit kit : this.plugin.getKitManager().getKits()) {
            playerData.setElo(kit.getName(), 1000);
            playerData.setRankedWins(kit.getName(), 0);
            playerData.setRankedLosses(kit.getName(), 0);
        }
        commandSender.sendMessage(ChatColor.GREEN + target.getName() + "'s stats have been wiped.");
        return true;
    }
}
