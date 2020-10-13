package net.skillwars.practice.commands.event;

import net.skillwars.practice.Practice;
import net.skillwars.practice.events.EventState;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.tournament.Tournament;
import net.skillwars.practice.tournament.TournamentState;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.player.PlayerState;

public class JoinEventCommand extends Command {
    private Practice plugin;

    public JoinEventCommand() {
        super("join");
        this.plugin = Practice.getInstance();
        this.setDescription("Join an event or tournament.");
        this.setUsage(ChatColor.RED + "Usage: /join <id>");
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(this.usageMessage);
            return true;
        }
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (this.plugin.getPartyManager().getParty(playerData.getUniqueId()) != null || playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        boolean inTournament = this.plugin.getTournamentManager().isInTournament(player.getUniqueId());
        boolean inEvent = this.plugin.getEventManager().getEventPlaying(player) != null;
        String eventId = args[0].toLowerCase();
        if (!NumberUtils.isNumber(eventId)) {
            PracticeEvent event = this.plugin.getEventManager().getByName(eventId);
            if (inTournament) {
                player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
                return true;
            }
            if (event == null) {
                player.sendMessage(ChatColor.RED + "That event doesn't exist.");
                return true;
            }
            if (event.getState() != EventState.WAITING) {
                player.sendMessage(ChatColor.RED + "That event is currently not available.");
                return true;
            }
            if (event.getPlayers().containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You are already in this event.");
                return true;
            }
            if (event.getPlayers().size() >= event.getLimit() && !player.hasPermission("practice.joinevent.bypass")) {
                player.sendMessage(ChatColor.RED + "Sorry! The event is already full.");
            }
            event.join(player);
            return true;
        } else {
            if (inEvent) {
                player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
                return true;
            }
            if (this.plugin.getTournamentManager().isInTournament(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You are currently in a tournament.");
                return true;
            }
            int id = Integer.parseInt(eventId);
            Tournament tournament = this.plugin.getTournamentManager().getTournament(id);
            if (tournament != null) {
                if (tournament.getTeamSize() > 1) {
                    Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
                    if (party != null && party.getMembers().size() != tournament.getTeamSize()) {
                        player.sendMessage(ChatColor.RED + "The party size must be of " + tournament.getTeamSize() + " players.");
                        return true;
                    }
                }
                if (tournament.getSize() > tournament.getPlayers().size()) {
                    if ((tournament.getTournamentState() == TournamentState.WAITING || tournament.getTournamentState() == TournamentState.STARTING) && tournament.getCurrentRound() == 1) {
                        this.plugin.getTournamentManager().joinTournament(id, player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Sorry! The tournament already started.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Sorry! The tournament is already full.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "That tournament doesn't exist.");
            }
            return true;
        }
    }
}
