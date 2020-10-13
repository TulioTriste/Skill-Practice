package net.skillwars.practice.commands.event;

import net.skillwars.practice.Practice;
import net.skillwars.practice.tournament.Tournament;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.events.PracticeEvent;

public class LeaveEventCommand extends Command {
    private Practice plugin;

    public LeaveEventCommand() {
        super("leave");
        this.plugin = Practice.getInstance();
        this.setDescription("Leave an event or tournament.");
        this.setUsage(ChatColor.RED + "Usage: /leave");
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        boolean inTournament = this.plugin.getTournamentManager().isInTournament(player.getUniqueId());
        boolean inEvent = this.plugin.getEventManager().getEventPlaying(player) != null;
        if (inEvent) {
            this.leaveEvent(player);
        } else if (inTournament) {
            this.leaveTournament(player);
        } else {
            player.sendMessage(ChatColor.RED + "There is nothing to leave.");
        }
        return true;
    }

    private void leaveTournament(Player player) {
        Tournament tournament = this.plugin.getTournamentManager().getTournament(player.getUniqueId());
        if (tournament != null) {
            this.plugin.getTournamentManager().leaveTournament(player);
        }
    }

    private void leaveEvent(Player player) {
        PracticeEvent event = this.plugin.getEventManager().getEventPlaying(player);
        if (event == null) {
            player.sendMessage(ChatColor.RED + "That event doesn't exist.");
            return;
        }
        if (!this.plugin.getEventManager().isPlaying(player, event)) {
            player.sendMessage(ChatColor.RED + "You are not in an event.");
            return;
        }
        event.leave(player);
    }
}
