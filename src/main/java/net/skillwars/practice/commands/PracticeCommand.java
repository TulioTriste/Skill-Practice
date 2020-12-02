package net.skillwars.practice.commands;

import net.skillwars.practice.Practice;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class PracticeCommand extends Command {

    private Practice plugin = Practice.getInstance();
    private Config scoreboard = new Config("scoreboard", this.plugin);

    public PracticeCommand() {
        super("practice");
        this.setPermission("practice.admin");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!commandSender.hasPermission(getPermission())) {
            commandSender.sendMessage(CC.translate("&cNo tienes permisos para usar esto."));
            return true;
        }
        if (strings.length == 0) {
            commandSender.sendMessage(CC.translate("&cUsa: /" + s + " <scoreboard> reload"));
            return true;
        }
        if (strings[0].equalsIgnoreCase("scoreboard")) {
            if (strings.length == 1) {
                commandSender.sendMessage(CC.translate("&cUsa: /" + s + " <scoreboard> reload"));
                return true;
            }
            if (strings[1].equalsIgnoreCase("reload")) {
                scoreboard.reload();
                commandSender.sendMessage(CC.translate("&aScoreboard file reiniciado correctamente."));
                return true;
            }
            commandSender.sendMessage(CC.translate("&cUsa: /" + s + " <scoreboard> reload"));
        }
        return false;
    }
}
