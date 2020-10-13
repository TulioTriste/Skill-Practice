package net.skillwars.practice.commands.time;

import net.skillwars.practice.Practice;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.settings.item.ProfileOptionsItemState;

public class DayCommand extends Command {

	public DayCommand() {
		super("day");
		this.setDescription("Set player time to day.");
		this.setUsage(ChatColor.RED + "Usage: /day");
	}

	@Override
	public boolean execute(CommandSender sender, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		((Player) sender).setPlayerTime(6000L, true);

		PlayerData playerData = Practice.getInstance().getPlayerManager().getPlayerData(((Player) sender).getUniqueId());
		playerData.getOptions().setTime(ProfileOptionsItemState.DAY);

		return true;
	}
}
