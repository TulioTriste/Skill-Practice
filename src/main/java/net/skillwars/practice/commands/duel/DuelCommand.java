package net.skillwars.practice.commands.duel;

import net.skillwars.practice.Practice;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class DuelCommand extends Command {
    private Practice plugin;

    public DuelCommand() {
        super("duel");
        this.plugin = Practice.getInstance();
        this.setDescription("Duel a player.");
        this.setUsage(ChatColor.RED + "Usage: /duel <player>");
        this.setAliases(Collections.singletonList("duel"));
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
        if (this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Actualmente estas en un torneo.");
            return true;
        }
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Para usar este comando tienes que estar en el Spawn.");
            return true;
        }
        Player target = this.plugin.getServer().getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[0]));
            return true;
        }
        if (this.plugin.getTournamentManager().getTournament(target.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Este jugador esta actualmente en un torneo.");
            return true;
        }
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());
        if (player.getName().equals(target.getName())) {
            player.sendMessage(ChatColor.RED + "No puedes luchar contra ti mismo.");
            return true;
        }
        if (party != null && targetParty != null && party == targetParty) {
            player.sendMessage(ChatColor.RED + "No puedes luchar contra ti mismo.");
            return true;
        }
        if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No eres el líder de la party.");
            return true;
        }
        PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());

        if (targetData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Este jugador ya está en un duelo.");
            return true;
        }

        if (!targetData.getOptions().isDuelRequests()) {
            player.sendMessage(ChatColor.RED + "Este jugador tiene los duelos desactivados.");
            return true;
        }

        if (party == null && targetParty != null) {
            player.sendMessage(ChatColor.RED + "Este jugador está actualmente en una party.");
            return true;
        }
        if (party != null && targetParty == null) {
            player.sendMessage(ChatColor.RED + "Actualmente estas en una party.");
            return true;
        }
        playerData.setDuelSelecting(target.getUniqueId());
        player.openInventory(this.plugin.getInventoryManager().getDuelInventory().getCurrentPage());
        return true;
    }
}
