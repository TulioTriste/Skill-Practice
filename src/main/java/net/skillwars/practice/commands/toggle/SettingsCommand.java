package net.skillwars.practice.commands.toggle;

import net.skillwars.practice.Practice;
import net.skillwars.practice.player.PlayerData;
import org.bukkit.ChatColor;
import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand extends Command {
	private Practice plugin = Practice.getInstance();

	public SettingsCommand() {
		super("settings");
		this.setDescription("Toggles multiple settings.");
		this.setUsage(ChatColor.RED + "Usage: /settings");
		this.setAliases(Arrays.asList("options", "toggle"));
	}

	@Override
	public boolean execute(CommandSender sender, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
		player.openInventory(playerData.getOptions().getInventory());
		return true;
	}
}
