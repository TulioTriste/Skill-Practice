package net.skillwars.practice.commands.time;

import net.skillwars.practice.Practice;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.settings.item.ProfileOptionsItemState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NightCommand extends Command {
	public NightCommand() {
		super("night");
		this.setDescription("Set player time to night.");
		this.setUsage(ChatColor.RED + "Usage: /night");
	}

	@Override
	public boolean execute(CommandSender sender, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		((Player) sender).setPlayerTime(18000L, true);
		PlayerData playerData = Practice.getInstance().getPlayerManager().getPlayerData(((Player) sender).getUniqueId());
		playerData.getOptions().setTime(ProfileOptionsItemState.NIGHT);
		return true;
	}
}
