package net.skillwars.practice.commands.toggle;

import net.skillwars.practice.Practice;
import net.skillwars.practice.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class EventModeCommand extends Command {

    private Practice plugin = Practice.getInstance();

    public EventModeCommand() {
        super("eventmode");
        this.setPermission("practice.admin");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!commandSender.hasPermission(getPermission())) {
            commandSender.sendMessage(CC.translate("&cNo tienes los suficientes permisos."));
            return true;
        }

        if (strings.length == 0) {
            commandSender.sendMessage(CC.translate("&cUsa: /" + s + " enable|disable"));
            return true;
        }

        if (strings[0].equalsIgnoreCase("enable")) {
            if (this.plugin.getServerManager().isEventMode()) {
                commandSender.sendMessage(CC.translate("&aEl modo Evento ya está activado!"));
                return true;
            }
            commandSender.sendMessage(CC.translate("&aModo Evento activado correctamente!"));
            this.plugin.getServerManager().setEventMode(true);
        }
        else if (strings[0].equalsIgnoreCase("disable")) {
            if (!this.plugin.getServerManager().isEventMode()) {
                commandSender.sendMessage(CC.translate("&aEl modo Evento ya está desactivado!"));
                return true;
            }
            commandSender.sendMessage(CC.translate("&cModo Evento desactivado correctamente!"));
            this.plugin.getServerManager().setEventMode(false);
        }
        return false;
    }
}
