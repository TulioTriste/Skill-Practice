package net.skillwars.practice.commands.duel;

import net.skillwars.practice.Practice;
import net.skillwars.practice.party.Party;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.nodebufflite.NoDebuffLiteEvent;
import net.skillwars.practice.events.teamfights.TeamFightEvent;
import net.skillwars.practice.events.sumo.SumoEvent;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.StringUtil;

import java.util.Collections;

public class SpectateCommand extends Command {
    private Practice plugin;

    public SpectateCommand() {
        super("spectate");
        this.plugin = Practice.getInstance();
        this.setDescription("Spectate a player's match.");
        this.setUsage(ChatColor.RED + "Usage: /spectate <player>");
        this.setAliases(Collections.singletonList("spec"));
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
        Party party = this.plugin.getPartyManager().getParty(playerData.getUniqueId());
        if (party != null || (playerData.getPlayerState() != PlayerState.SPAWN && playerData.getPlayerState() != PlayerState.SPECTATING)) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[0]));
            return true;
        }
        PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        if (targetData.getPlayerState() == PlayerState.EVENT) {
            PracticeEvent event = this.plugin.getEventManager().getEventPlaying(target);
            if (event == null) {
                player.sendMessage(ChatColor.RED + "That player is currently not in an event.");
                return true;
            }
            if (event instanceof SumoEvent) {
                player.performCommand("eventspectate Sumo");
            } else if(event instanceof NoDebuffLiteEvent) {
                player.performCommand("eventspectate MiniNoDebuff");
            } else if(event instanceof TeamFightEvent) {
                player.performCommand("eventspectate Redrover");
            }
            return true;
        } else {
            if (targetData.getPlayerState() != PlayerState.FIGHTING) {
                player.sendMessage(ChatColor.RED + "That player is currently not in a fight.");
                return true;
            }
            Match targetMatch = this.plugin.getMatchManager().getMatch(targetData);
            if (!targetMatch.isParty()) {

                if (!targetData.getOptions().isSpectators() && !player.hasPermission("practice.staff")) {
                    player.sendMessage(ChatColor.RED + "That player has ignored spectators.");
                    return true;
                }

                MatchTeam team = targetMatch.getTeams().get(0);
                MatchTeam team2 = targetMatch.getTeams().get(1);
                PlayerData otherPlayerData = this.plugin.getPlayerManager().getPlayerData(team.getPlayers().get(0) == target.getUniqueId() ? team2.getPlayers().get(0) : team.getPlayers().get(0));
                if (otherPlayerData != null && !otherPlayerData.getOptions().isSpectators() && !player.hasPermission("practice.staff")) {
                    player.sendMessage(ChatColor.RED + "That player has ignored spectators.");
                    return true;
                }
            }
            if (playerData.getPlayerState() == PlayerState.SPECTATING) {
                Match match = this.plugin.getMatchManager().getSpectatingMatch(player.getUniqueId());
                if (match.equals(targetMatch)) {
                    player.sendMessage(CC.RED + "You are already spectating this match.");
                    return true;
                }
                match.removeSpectator(player.getUniqueId());
            }
            player.sendMessage(CC.PRIMARY + "You are now spectating " + CC.SECONDARY + target.getName() + CC.PRIMARY + ".");
            this.plugin.getMatchManager().addSpectator(player, playerData, target, targetMatch);
            return true;
        }
    }
}
