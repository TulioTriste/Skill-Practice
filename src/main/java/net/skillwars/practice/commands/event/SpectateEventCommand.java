package net.skillwars.practice.commands.event;

import net.skillwars.practice.Practice;
import net.skillwars.practice.events.EventState;
import net.skillwars.practice.events.nodebufflite.NoDebuffLiteEvent;
import net.skillwars.practice.events.teamfights.TeamFightEvent;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.sumo.SumoEvent;
import net.skillwars.practice.player.PlayerState;

import java.util.Arrays;

public class SpectateEventCommand extends Command {
    private Practice plugin;

    public SpectateEventCommand() {
        super("eventspectate");
        this.plugin = Practice.getInstance();
        this.setDescription("Spectate an event.");
        this.setUsage(ChatColor.RED + "Usage: /eventspectate <event>");
        this.setAliases(Arrays.asList("eventspec", "specevent"));
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
        PracticeEvent event = this.plugin.getEventManager().getByName(args[0]);
        if (event == null) {
            player.sendMessage(ChatColor.RED + "That player is currently not in an event.");
            return true;
        }
        if (event.getState() != EventState.STARTED) {
            player.sendMessage(ChatColor.RED + "That event hasn't started, please wait.");
            return true;
        }
        if (playerData.getPlayerState() == PlayerState.SPECTATING) {
            if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You are already spectating this event.");
                return true;
            }
            this.plugin.getEventManager().removeSpectator(player);
        }
        player.sendMessage(ChatColor.GREEN + "You are now spectating " + ChatColor.GRAY + event.getName() + " Event" + ChatColor.GREEN + ".");
        if (event instanceof SumoEvent) {
            this.plugin.getEventManager().addSpectatorSumo(player, playerData, (SumoEvent) event);
        } else if (event instanceof NoDebuffLiteEvent) {
            this.plugin.getEventManager().addSpectatorNoDebuffLite(player, playerData, (NoDebuffLiteEvent) event);
        } else if(event instanceof TeamFightEvent) {
            this.plugin.getEventManager().addSpectatorTeamFights(player, playerData, (TeamFightEvent) event);
        }
        return true;
    }
}
