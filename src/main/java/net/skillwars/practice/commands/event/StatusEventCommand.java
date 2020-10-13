package net.skillwars.practice.commands.event;

import net.skillwars.practice.Practice;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.util.Clickable;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.tournament.Tournament;

import java.util.UUID;

public class StatusEventCommand extends Command {
    private Practice plugin;

    public StatusEventCommand() {
        super("status");
        this.plugin = Practice.getInstance();
        this.setDescription("Show an event or tournament status.");
        this.setUsage(ChatColor.RED + "Usage: /status");
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        if (this.plugin.getTournamentManager().getTournaments().size() == 0) {
            player.sendMessage(ChatColor.RED + "There is no available tournaments.");
            return true;
        }
        for (Tournament tournament : this.plugin.getTournamentManager().getTournaments().values()) {
            if (tournament == null) {
                player.sendMessage(ChatColor.RED + "This tournament doesn't exist.");
                return true;
            }
            player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
            player.sendMessage(" ");
            player.sendMessage(ChatColor.YELLOW.toString() + "Tournament (" + tournament.getTeamSize() + "v" + tournament.getTeamSize() + ") " + ChatColor.GOLD.toString() + tournament.getKitName());
            if (tournament.getMatches().size() == 0) {
                player.sendMessage(ChatColor.RED + "There is no available matches.");
                player.sendMessage(" ");
                player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
                return true;
            }
            for (UUID matchUUID : tournament.getMatches()) {
                Match match = this.plugin.getMatchManager().getMatchFromUUID(matchUUID);
                MatchTeam teamA = match.getTeams().get(0);
                MatchTeam teamB = match.getTeams().get(1);
                String teamANames = (tournament.getTeamSize() > 1) ? (teamA.getLeaderName() + "'s Party") : teamA.getLeaderName();
                String teamBNames = (tournament.getTeamSize() > 1) ? (teamB.getLeaderName() + "'s Party") : teamB.getLeaderName();
                Clickable clickable = new Clickable(ChatColor.WHITE.toString() + ChatColor.BOLD + "* " + ChatColor.GOLD.toString() + teamANames + " vs " + teamBNames + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "[Click to Spectate]", ChatColor.GRAY + "Click to spectate", "/spectate " + teamA.getLeaderName());
                clickable.sendToPlayer(player);
            }
            player.sendMessage(" ");
            player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
        }
        return true;
    }
}
