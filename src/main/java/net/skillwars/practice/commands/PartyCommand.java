package net.skillwars.practice.commands;

import net.skillwars.practice.Practice;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.Clickable;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.joeleoli.nucleus.Nucleus;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.Color;
import net.skillwars.practice.util.StringUtil;

import java.util.*;

public class PartyCommand extends Command {
    private static String NOT_LEADER = CC.RED + "You are not the leader of the party!";
    private static String[] HELP_MESSAGE = new String[] {
            CC.DARK_GRAY + CC.STRIKE_THROUGH + "----------------------------------------------------",
            CC.GOLD + CC.BOLD + "Party Commands:",
            CC.GRAY + " - " + CC.YELLOW + "/party help " + CC.GRAY + "- Displays the help menu",
            CC.GRAY + " - " + CC.YELLOW + "/party create " + CC.GRAY + "- Creates a party instance",
            CC.GRAY + " - " + CC.YELLOW + "/party leave " + CC.GRAY + "- Leave your current party",
            CC.GRAY + " - " + CC.YELLOW + "/party info " + CC.GRAY + "- Displays your party information",
            CC.GRAY + " - " + CC.YELLOW + "/party join (player) " + CC.GRAY + "- Join a party (invited or unlocked)",
            "",
            CC.GRAY + CC.BOLD + "Leader Commands:",
            CC.GRAY + " - " + CC.YELLOW + "/party open " + CC.GRAY + "- Open your party for others to join",
            CC.GRAY + " - " + CC.YELLOW + "/party lock " + CC.GRAY + "- Lock your party for others to join",
            CC.GRAY + " - " + CC.YELLOW + "/party setlimit (amount) " + CC.GRAY + "- Set a limit to your party",
            CC.GRAY + " - " + CC.YELLOW + "/party invite (player) " + CC.GRAY + "- Invites a player to your party",
            CC.GRAY + " - " + CC.YELLOW + "/party kick (player) " + CC.GRAY + "- Kicks a player from your party",
            CC.DARK_GRAY + CC.STRIKE_THROUGH + "----------------------------------------------------"
    };

    private Practice plugin;

    public PartyCommand() {
        super("party");
        this.plugin = Practice.getInstance();
        this.setDescription("Party Command.");
        this.setUsage(CC.RED + "Usage: /party <subcommand> [player]");
        this.setAliases(Collections.singletonList("p"));
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());

        String subCommand = args.length < 1 ? "help" : args[0];

        switch (subCommand.toLowerCase()) {
            case "create":
                if (party != null) {
                    player.sendMessage(CC.RED + "You are already in a party.");
                } else if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "Cannot execute this command in your current state.");
                } else {
                    this.plugin.getPartyManager().createParty(player);
                }
                break;
            case "leave":
                if (party == null) {
                    player.sendMessage(CC.RED + "You are not in a party.");
                } else if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "Cannot execute this command in your current state.");
                } else {
                    this.plugin.getPartyManager().leaveParty(player);
                }
                break;
            case "inv":
            case "invite":
                if (party == null) {
                    player.sendMessage(CC.RED + "You are not in a party.");
                } else if (!this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                    player.sendMessage(CC.RED + "You are not the leader of the party.");
                } else if (this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null) {
                    player.sendMessage(CC.RED + "You are currently in a tournament.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party invite (player)");
                } else if (party.isOpen()) {
                    player.sendMessage(CC.RED + "This party is open, so anyone can join.");
                } else if (party.getMembers().size() >= party.getLimit()) {
                    player.sendMessage(CC.RED + "Party size has reached it's limit");
                } else {
                    if (party.getLeader() != player.getUniqueId()) {
                        player.sendMessage(PartyCommand.NOT_LEADER);
                        return true;
                    }
                    Player target = this.plugin.getServer().getPlayer(args[1]);

                    if (target == null) {
                        player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());

                    if (!targetData.getOptions().isDuelRequests()) {
                        player.sendMessage(CC.RED + "That player has ignored party invite requests.");
                        return true;
                    }

                    if (target.getUniqueId() == player.getUniqueId()) {
                        player.sendMessage(CC.RED + "You can't invite yourself.");
                    } else if (this.plugin.getPartyManager().getParty(target.getUniqueId()) != null) {
                        player.sendMessage(CC.RED + "That player is already in a party.");
                    } else if (targetData.getPlayerState() != PlayerState.SPAWN) {
                        player.sendMessage(CC.RED + "That player is currently busy.");
                    } else if (this.plugin.getPartyManager().hasPartyInvite(target.getUniqueId(), player.getUniqueId())) {
                        player.sendMessage(CC.RED + "You have already sent a party invitation to this player, please wait.");
                    } else {
                        this.plugin.getPartyManager().createPartyInvite(player.getUniqueId(), target.getUniqueId());

                        Clickable partyInvite = new Clickable(Nucleus.getInstance().getChat().getPlayerPrefix((Player) sender) + sender.getName() + CC.YELLOW + " has invited you to their party! " + CC.GRAY + "[Click to Accept]",
                                CC.GRAY + "Click to accept",
                                "/party accept " + sender.getName());

                        partyInvite.sendToPlayer(target);

                        party.broadcast(Nucleus.getInstance().getChat().getPlayerPrefix(target) + target.getName() + CC.YELLOW + " has been invited to the party.");

                    }
                }
                break;
            case "accept":
                if (party != null) {
                    player.sendMessage(CC.RED + "You are already in a party.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party accept <player>.");
                } else if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "Cannot execute this command in your current state.");
                } else {
                    Player target = this.plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());

                    if (targetParty == null) {
                        player.sendMessage(CC.RED + "That player is not in a party.");
                    } else if (targetParty.getMembers().size() >= targetParty.getLimit()) {
                        player.sendMessage(CC.RED + "Party size has reached it's limit");
                    } else if (!this.plugin.getPartyManager().hasPartyInvite(player.getUniqueId(), targetParty.getLeader())) {
                        player.sendMessage(CC.RED + "You do not have any pending requests.");
                    } else {
                        this.plugin.getPartyManager().joinParty(targetParty.getLeader(), player);
                    }
                }
                break;
            case "join":
                if (party != null) {
                    player.sendMessage(CC.RED + "You are already in a party.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party join <player>.");
                } else if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "Cannot execute this command in your current state.");
                } else {
                    Player target = this.plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());

                    if (targetParty == null || !targetParty.isOpen() || targetParty.getMembers().size() >= targetParty.getLimit()) {
                        player.sendMessage(CC.RED + "You can't join this party.");
                    } else {
                        this.plugin.getPartyManager().joinParty(targetParty.getLeader(), player);
                    }
                }
                break;
            case "kick":
                if (party == null) {
                    player.sendMessage(CC.RED + "You are not in a party.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party kick <player>.");
                } else {
                    if (party.getLeader() != player.getUniqueId()) {
                        player.sendMessage(PartyCommand.NOT_LEADER);
                        return true;
                    }
                    Player target = this.plugin.getServer().getPlayer(args[1]);

                    if (target == null) {
                        player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());

                    if (targetParty == null || targetParty.getLeader() != party.getLeader()) {
                        player.sendMessage(CC.RED + "That player is not in your party.");
                    } else {
                        this.plugin.getPartyManager().leaveParty(target);
                    }
                }
                break;
            case "setlimit":
                if (party == null) {
                    player.sendMessage(CC.RED + "You are not in a party.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party setlimit <amount>.");
                } else {
                    if (party.getLeader() != player.getUniqueId()) {
                        player.sendMessage(PartyCommand.NOT_LEADER);
                        return true;
                    }
                    try {
                        int limit = Integer.parseInt(args[1]);

                        if (limit < 2 || limit > 100) {
                            player.sendMessage(CC.RED + "the maximum limit is 100.");
                        } else {
                            party.setLimit(limit);
                            player.sendMessage(CC.GREEN + "You have set the party player limit to " + CC.YELLOW + limit + " players.");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(CC.RED + "That is not a number.");
                    }
                }
                break;
            case "open":
            case "lock":
                if (party == null) {
                    player.sendMessage(CC.RED + "You are not in a party.");
                } else {
                    if (party.getLeader() != player.getUniqueId()) {
                        player.sendMessage(PartyCommand.NOT_LEADER);
                        return true;
                    }
                    party.setOpen(!party.isOpen());

                    if(party.isOpen()){
                        if(player.hasPermission("party.annunce")){
                            party.setBroadcastTask(new BukkitRunnable(){
                                @Override
                                public void run() {
                                    for (Player other : Bukkit.getOnlinePlayers()) {
                                        Party otherParty = plugin.getPartyManager().getParty(other.getUniqueId());
                                        Player leader = Bukkit.getPlayer(party.getLeader());

                                        if (otherParty == null) {
                                            Clickable partyPublic = new Clickable(Color.translate(Nucleus.getInstance().getChat().getPlayerPrefix(leader) + leader.getName() +
                                                    " &eis hosting a public party! &b" + "[Click to join]"),
                                                    CC.GRAY + "Click to join",
                                                    "/party join " + leader.getName());

                                            partyPublic.sendToPlayer(other);
                                        }
                                    }
                                }
                            }.runTaskTimerAsynchronously(Practice.getInstance(), 20L, 20L * 60L));
                        }
                    }else{
                        if(party.getBroadcastTask() != null){
                            party.getBroadcastTask().cancel();
                        }
                    }

                    party.broadcast(CC.YELLOW + "Your party is now " + CC.BOLD + (party.isOpen() ? "OPEN" : "LOCKED"));
                }
                break;
            case "info":
                if (party == null) {
                    player.sendMessage(CC.RED + "You are not in a party.");
                } else {

                    List<UUID> members = new ArrayList<>(party.getMembers());
                    members.remove(party.getLeader());

                    StringBuilder builder = new StringBuilder(CC.GOLD + "Members (" + (party.getMembers().size() + "): "));
                    members.stream().map(this.plugin.getServer()::getPlayer).filter(Objects::nonNull).forEach(member -> builder.append(CC.GRAY).append(member.getName()).append(","));

                    String[] information = new String[] {
                            CC.DARK_GRAY + CC.STRIKE_THROUGH + "----------------------------------------------------",
                            CC.GOLD + CC.BOLD + "Party Information:",
                            CC.YELLOW + "Leader: " + CC.GRAY + this.plugin.getServer().getPlayer(party.getLeader()).getName(),
                            CC.YELLOW + builder.toString(),
                            CC.YELLOW + "Party State: " + CC.GRAY + (party.isOpen() ?
                                    CC.GREEN + "Open" :
                                    CC.RED + "Locked"),
                            CC.DARK_GRAY + CC.STRIKE_THROUGH + "----------------------------------------------------"
                    };

                    player.sendMessage(information);
                }
                break;
            case "list":
                if (party == null) {
                    player.sendMessage(CC.RED + "You are not in a party.");
                } else {
                    StringBuilder builder = new StringBuilder(CC.PRIMARY + "Your party (" + party.getMembers().size() + "):\n");

                    List<UUID> members = new ArrayList<>(party.getMembers());

                    members.remove(party.getLeader());

                    builder.append(CC.GREEN).append("Leader: ").append(this.plugin.getServer().getPlayer(party.getLeader()).getName()).append("\n");

                    members.stream().map(this.plugin.getServer()::getPlayer).filter(Objects::nonNull).forEach(member -> builder.append(CC.AQUA).append(member.getName()).append("\n"));

                    player.sendMessage(builder.toString());
                }
                break;
            default:
                player.sendMessage(PartyCommand.HELP_MESSAGE);
                break;
        }
        return true;
    }
}
