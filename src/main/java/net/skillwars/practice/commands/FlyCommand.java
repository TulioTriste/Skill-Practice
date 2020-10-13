package net.skillwars.practice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.Practice;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;

import java.util.Collections;

public class FlyCommand extends Command {
    private Practice plugin;

    public FlyCommand() {
        super("fly");
        this.plugin = Practice.getInstance();
        this.setDescription("Toggles flight.");
        this.setUsage(ChatColor.RED + "Usage: /fly");
        this.setAliases(Collections.singletonList("flight"));
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("practice.fly")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        player.setAllowFlight(!player.getAllowFlight());
        if (player.getAllowFlight()) {
            player.sendMessage(ChatColor.YELLOW + "Your flight has been enabled.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Your flight has been disabled.");
        }
        return true;
    }
}
