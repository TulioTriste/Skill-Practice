package net.skillwars.practice.commands;

import net.skillwars.practice.Practice;
import net.skillwars.practice.leaderboards.LeaderBoardMenu;
import net.skillwars.practice.stats.StatisticsMenu;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class StatsCommand extends Command {

	private Practice plugin = Practice.getInstance();

	public StatsCommand() {
		super("stats");
		this.setAliases(Arrays.asList("elo", "statistics"));
		this.setUsage(ChatColor.RED + "Usa: /stats [player]");
	}

	@Override
	public boolean execute(CommandSender sender, String s, String[] args) {
		if(!(sender instanceof Player)){
			sender.sendMessage(CC.translate("&4No Console."));
			return true;
		}

		Player player = (Player)sender;
		if (args.length == 0) {
			new StatisticsMenu(player).openMenu(player);
			return true;
		}

		OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args[0]);
		if (target == null) {
			sender.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[0]));
			return true;
		}

		new StatisticsMenu(target).openMenu(player);
		return true;
	}

}
