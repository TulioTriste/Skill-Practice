package net.skillwars.practice.commands.time;

import net.skillwars.practice.Practice;
import net.skillwars.practice.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.settings.item.ProfileOptionsItemState;

public class SunsetCommand extends Command {

	public SunsetCommand() {
		super("sunset");
		this.setDescription("Set player time to sunset.");
		this.setUsage(ChatColor.RED + "Usage: /sunset");
	}

	@Override
	public boolean execute(CommandSender sender, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		((Player) sender).setPlayerTime(12000L, true);
		PlayerData playerData = Practice.getInstance().getPlayerManager().getPlayerData(((Player) sender).getUniqueId());
		playerData.getOptions().setTime(ProfileOptionsItemState.SUNSET);
		return true;
	}
}
