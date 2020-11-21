package net.skillwars.practice.commands.duel;

import net.skillwars.practice.Practice;
import net.skillwars.practice.event.match.MatchCancelEvent;
import net.skillwars.practice.event.match.MatchEndEvent;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class CancelRankedCommand extends Command {

    private Practice plugin = Practice.getInstance();

    public CancelRankedCommand() {
        super("cancelranked");
        this.setPermission("practice.admin");
        this.setAliases(Arrays.asList("stopranked", "rankedcancel", "cancelmatch", "stopmatch", "matchstop", "matchcancel"));
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(CC.translate("&4No Console."));
            return true;
        }
        Player player = (Player) commandSender;
        if (strings.length == 0) {
            player.sendMessage(CC.translate("&cUsa: /" + s + " <player>"));
            return true;
        }
        Player target = Bukkit.getPlayer(strings[0]);
        PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        if (this.plugin.getMatchManager().getMatch(targetData) == null) {
            player.sendMessage(CC.translate("&cEste usuario no se encuentra en una partida."));
            return true;
        }
        Match match = this.plugin.getMatchManager().getMatch(targetData);
        Bukkit.getPluginManager().callEvent(new MatchCancelEvent(match));
        player.sendMessage(CC.translate("&aPartida cancelada Correctamente."));
        return false;
    }
}
