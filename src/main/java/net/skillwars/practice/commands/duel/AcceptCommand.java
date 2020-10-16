package net.skillwars.practice.commands.duel;

import net.skillwars.practice.Practice;
import net.skillwars.practice.party.Party;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.managers.PartyManager;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchRequest;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.queue.QueueType;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AcceptCommand extends Command {
    private Practice plugin;

    public AcceptCommand() {
        super("accept");
        this.plugin = Practice.getInstance();
        this.setDescription("Accept a duel.");
        this.setUsage(ChatColor.RED + "Usage: /accept <player>");
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
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "No puede aceptar un duelo mientras estas en uno.");
            return true;
        }
        Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[0]));
            return true;
        }
        if (player.getName().equals(target.getName())) {
            player.sendMessage(ChatColor.RED + "No puedes luchar contra ti mismo.");
            return true;
        }
        PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        if (targetData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Este jugador ya está en un duelo.");
            return true;
        }
        MatchRequest request = this.plugin.getMatchManager().getMatchRequest(target.getUniqueId(), player.getUniqueId());
        if (args.length > 1) {
            Kit kit = this.plugin.getKitManager().getKit(args[1]);
            if (kit != null) {
                request = this.plugin.getMatchManager().getMatchRequest(target.getUniqueId(), player.getUniqueId(), kit.getName());
            }
        }
        if (request == null) {
            player.sendMessage(ChatColor.RED + "No tiene ninguna solicitud pendiente.");
            return true;
        }
        if (request.getRequester().equals(target.getUniqueId())) {
            List<UUID> playersA = new ArrayList<>();
            List<UUID> playersB = new ArrayList<>();
            PartyManager partyManager = this.plugin.getPartyManager();
            Party party = partyManager.getParty(player.getUniqueId());
            Party targetParty = partyManager.getParty(target.getUniqueId());
            if (request.isParty()) {
                if (party == null || targetParty == null || !partyManager.isLeader(target.getUniqueId()) || !partyManager.isLeader(target.getUniqueId())) {
                    player.sendMessage(CC.RED + "Tú o ese jugador no es líder del grupo.");
                    return true;
                }
                playersA.addAll(party.getMembers());
                playersB.addAll(targetParty.getMembers());
            } else {
                if (party != null || targetParty != null) {
                    player.sendMessage(CC.RED + "Uno de ustedes está en una party.");
                    return true;
                }
                playersA.add(player.getUniqueId());
                playersB.add(target.getUniqueId());
            }
            Kit kit2 = this.plugin.getKitManager().getKit(request.getKitName());
            MatchTeam teamA = new MatchTeam(target.getUniqueId(), playersB, 0);
            MatchTeam teamB = new MatchTeam(player.getUniqueId(), playersA, 1);
            Match match = new Match(request.getArena(), kit2, QueueType.UNRANKED, teamA, teamB);
            Player leaderA = this.plugin.getServer().getPlayer(teamA.getLeader());
            Player leaderB = this.plugin.getServer().getPlayer(teamB.getLeader());
            match.broadcast(CC.PRIMARY + "Comenzando el duelo con el kit " + CC.SECONDARY + request.getKitName() +
                    CC.PRIMARY + " entre " + CC.SECONDARY + leaderA.getName() + CC.PRIMARY + " y " + CC.SECONDARY + leaderB.getName() + CC.PRIMARY + ".");
            this.plugin.getMatchManager().createMatch(match);
        }
        return true;
    }
}
