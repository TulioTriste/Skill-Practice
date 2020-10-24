package net.skillwars.practice.commands;

import net.skillwars.practice.util.CC;
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
            player.sendMessage(ChatColor.RED + "No tienes permisos para ejecutar este comando.");
            return true;
        }
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Solo puedes ejecutar este comando en el Spawn.");
            return true;
        }
        player.setAllowFlight(!player.getAllowFlight());
        if (player.getAllowFlight()) {
            player.sendMessage(CC.SECONDARY + "Tu modo de vuelo ha sido " + CC.GREEN + "activado.");
        } else {
            player.sendMessage(CC.SECONDARY + "Tu modo de vuelo ha sido " + CC.RED + "desactivado.");
        }
        return true;
    }
}
