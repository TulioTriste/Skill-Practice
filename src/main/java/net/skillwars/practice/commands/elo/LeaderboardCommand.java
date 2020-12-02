package net.skillwars.practice.commands.elo;

import net.skillwars.practice.leaderboards.LeaderBoardMenu;
import net.skillwars.practice.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class LeaderboardCommand extends Command {

    public LeaderboardCommand() {
        super("leaderboard");
        this.setAliases(Arrays.asList("topelo", "tops"));
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(CC.translate("&4No Console."));
            return true;
        }
        Player player = (Player) commandSender;
        new LeaderBoardMenu(player).openMenu(player);
        return false;
    }
}
