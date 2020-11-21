package net.skillwars.practice.commands.elo;

import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EloCommand extends Command {

    private final Practice plugin;

    public EloCommand() {
        super("elo");
        this.plugin = Practice.getInstance();
        this.setDescription("Elo command.");
        this.setUsage(ChatColor.RED + "Usage: /elo <player>");
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {

        if (!sender.hasPermission("practice.elo")) {
            sender.sendMessage(CC.translate("&cNo tienes permisos para ejecutar este comando."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(this.getUsage());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(CC.translate("&cJugador '&f" + args[0] + "&c' no encontrado."));
            return true;
        }

        sender.sendMessage(CC.translate("&3&l" + target.getName() + "'s ELO"));
        sender.sendMessage(CC.translate(""));
        for (Kit kit : this.plugin.getKitManager().getKits()) {
            if (kit.isRanked()) {
                int targetElo = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId()).getElo(kit.getName());
                sender.sendMessage(CC.translate("&b" + kit.getName() + "&7: &f" + targetElo));
            }
        }
        sender.sendMessage(CC.translate(""));
        return true;
    }
}
