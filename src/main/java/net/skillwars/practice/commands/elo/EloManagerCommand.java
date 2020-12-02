package net.skillwars.practice.commands.elo;

import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.Ints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EloManagerCommand extends Command {

    private final Practice plugin;

    public EloManagerCommand() {
        super("elomanager");
        this.plugin = Practice.getInstance();
        this.setDescription("EloManager command.");
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!sender.hasPermission("practice.admin")) {
            sender.sendMessage(CC.translate("&cNo tienes permisos para ejecutar este comando."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(CC.translate("&3&lELO Manager"));
            sender.sendMessage(CC.translate(""));
            sender.sendMessage(CC.translate("&b/elomanager set <player> <elo> <kitName|all>"));
            sender.sendMessage(CC.translate("&b/elomanager add <player> <elo> <kitName|all>"));
            sender.sendMessage(CC.translate("&b/elomanager reset <player> <kitName|all>"));
            sender.sendMessage(CC.translate(""));
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 4) {
                sender.sendMessage(CC.translate("&cUsage: /elomanager set <player> <elo> <kitName|all>"));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            if (target == null) {
                sender.sendMessage(CC.translate("&cJugador '&f" + args[1] + "&c' no se ha encontrado."));
                return true;
            }

            Integer elo = Ints.tryParse(args[2]);

            if (elo == null) {
                sender.sendMessage(CC.translate("&c'" + args[2] + "' no es un numero valido."));
                return true;
            }

            if (elo <= 0) {
                sender.sendMessage(CC.translate("&cEl elo debe ser un numero positivo."));
                return true;
            }

            if (args[3].equalsIgnoreCase("all")) {
                for (Kit kit : this.plugin.getKitManager().getKits()) {
                    this.plugin.getPlayerManager().getPlayerData(target.getUniqueId()).setElo(kit.getName(), elo);
                }
                sender.sendMessage(CC.translate("&aLe has seteado " + elo + " de ELO ha " + target.getName() + " en todos los kits."));
                return true;
            }

            Kit kit = this.plugin.getKitManager().getKit(args[3]);

            if (kit != null) {
                this.plugin.getPlayerManager().getPlayerData(target.getUniqueId()).setElo(kit.getName(), elo);
                sender.sendMessage(CC.translate("&aLe has seteado " + elo + " de ELO ha " + target.getName() + " en " + kit.getName() + "."));
                return true;
            }
            sender.sendMessage(CC.translate("&cEl kit '" + args[3] + "' no existe."));
        }
        else if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 4) {
                sender.sendMessage(CC.translate("&cUsage: /elomanager add <player> <elo> <kitName|all>"));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            if (target == null) {
                sender.sendMessage(CC.translate("&cJugador '&f" + args[1] + "&c' no se ha encontrado."));
                return true;
            }

            PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());
            Kit kit = this.plugin.getKitManager().getKit(args[3]);

            if (kit == null) {
                sender.sendMessage(CC.translate("&cEl kit '" + args[3] + "' no existe."));
                return true;
            }

            Integer elo = targetData.getElo(kit.getName());
            Integer eloAdd = Integer.getInteger(args[2]);

            if (eloAdd == null) {
                sender.sendMessage(CC.translate("&c'" + eloAdd + "' no es un numero valido."));
                return true;
            }

            if (eloAdd <= 0) {
                sender.sendMessage(CC.translate("&cEl elo debe ser un numero positivo."));
                return true;
            }

            if (args[3].equalsIgnoreCase("all")) {
                for (Kit kits : this.plugin.getKitManager().getKits()) {
                    targetData.setElo(kits.getName(), elo + eloAdd);
                }
                sender.sendMessage(CC.translate("&aLe has seteado " + elo + " de ELO ha " + target.getName() + " en todos los kits."));
                return true;
            }

            targetData.setElo(kit.getName(), elo + eloAdd);
            sender.sendMessage(CC.translate("&aLe has añadido " + elo + " de ELO ha " + target.getName() + " en " + kit.getName() + "."));
        }
        else if (args[0].equalsIgnoreCase("reset")) {

            if (args.length < 3) {
                sender.sendMessage(CC.translate("&cUsage: /elomanager reset <player> <kitName|all>"));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            if (target == null) {
                sender.sendMessage(CC.translate("&cJugador '&f" + args[1] + "&c' no se ha encontrado."));
                return true;
            }

            if (args[2].equalsIgnoreCase("all")) {
                for (Kit kit : this.plugin.getKitManager().getKits()) {
                    this.plugin.getPlayerManager().getPlayerData(target.getUniqueId()).setElo(kit.getName(), PlayerData.DEFAULT_ELO);
                }
                sender.sendMessage(CC.translate("&aLe has reiniciado el ELO ha " + target.getName() + " de todos los kits."));
                return true;
            }

            Kit kit = this.plugin.getKitManager().getKit(args[2]);

            if (kit != null) {
                this.plugin.getPlayerManager().getPlayerData(target.getUniqueId()).setElo(kit.getName(), PlayerData.DEFAULT_ELO);
                sender.sendMessage(CC.translate("&aLe has reiniciado el ELO ha " + target.getName() + " de " + kit.getName() + "."));
                return true;
            }
            sender.sendMessage(CC.translate("&cEl kit '" + args[2] + "' no existe."));
        }
        else {
            sender.sendMessage(CC.translate("&3&lEloManager"));
            sender.sendMessage(CC.translate(""));
            sender.sendMessage(CC.translate("&b/elomanager set <player> <elo> <kitName|all>"));
            sender.sendMessage(CC.translate("&b/elomanager reset <player> <kitName|all>"));
            sender.sendMessage(CC.translate(""));
        }
        return true;
    }
}
