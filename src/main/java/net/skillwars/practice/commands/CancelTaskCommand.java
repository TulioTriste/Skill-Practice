package net.skillwars.practice.commands;

import net.skillwars.practice.Practice;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitWorker;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class CancelTaskCommand extends Command {

    private Practice plugin = Practice.getInstance();

    public CancelTaskCommand() {
        super("canceltask");
        this.setPermission("practice.canceltask");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!commandSender.hasPermission(getPermission())) {
            commandSender.sendMessage(CC.translate("&cNo tienes suficientes permisos para usar este comando."));
            return true;
        }
        if (strings.length == 0) {
            commandSender.sendMessage(CC.translate("&cUsa: /" + s + " <taskId>"));
            return true;
        }
        int id = Integer.parseInt(strings[0]);
        if (!this.plugin.getServer().getScheduler().isCurrentlyRunning(id) && !this.plugin.getServer().getScheduler().isQueued(id)) {
            commandSender.sendMessage(CC.translate("&cEste Scheduler Task no existe."));
            return true;
        }
        this.plugin.getServer().getScheduler().cancelTask(id);
        commandSender.sendMessage(CC.translate("&aScheduler " + id + " cancelado correctamente."));
        return false;
    }
}
