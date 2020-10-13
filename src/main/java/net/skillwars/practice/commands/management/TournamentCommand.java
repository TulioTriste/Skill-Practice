package net.skillwars.practice.commands.management;

import net.skillwars.practice.Practice;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.tournament.Tournament;
import net.skillwars.practice.util.Clickable;
import net.skillwars.practice.util.TeamUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.util.CC;

import java.util.Collections;
import java.util.UUID;

public class TournamentCommand extends Command {
    private static String[] HELP_ADMIN_MESSAGE;
    private static String[] HELP_REGULAR_MESSAGE;

    static {
        HELP_ADMIN_MESSAGE = new String[]{ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------", ChatColor.RED + "Tournament Commands:", ChatColor.GOLD + "(*) /tournament start " + ChatColor.GRAY + "- Start a Tournament", ChatColor.GOLD + "(*) /tournament stop " + ChatColor.GRAY + "- Stop a Tournament", ChatColor.GOLD + "(*) /tournament alert " + ChatColor.GRAY + "- Alert a Tournament", ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"};
        HELP_REGULAR_MESSAGE = new String[]{ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------", ChatColor.RED + "Tournament Commands:", ChatColor.GOLD + "(*) /join <id> " + ChatColor.GRAY + "- Join a Tournament", ChatColor.GOLD + "(*) /leave " + ChatColor.GRAY + "- Leave a Tournament", ChatColor.GOLD + "(*) /status " + ChatColor.GRAY + "- Status of a Tournament", ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"};
    }

    private Practice plugin;

    public TournamentCommand() {
        super("tournament");
        this.plugin = Practice.getInstance();
        this.setAliases(Collections.singletonList("torneo"));
        this.setUsage(ChatColor.RED + "Usage: /tournament [args]");
    }

    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        Player player = (Player) commandSender;
        if (args.length == 0) {
            commandSender.sendMessage(player.hasPermission("practice.admin") ? TournamentCommand.HELP_ADMIN_MESSAGE : TournamentCommand.HELP_REGULAR_MESSAGE);
            return true;
        }
        String lowerCase = args[0].toLowerCase();
        switch (lowerCase) {
            case "start": {
				if (!player.hasPermission("practice.admin")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
					return true;
				}
                if (args.length == 5) {
                    try {
                        int id = Integer.parseInt(args[1]);
                        int teamSize = Integer.parseInt(args[3]);
                        int size = Integer.parseInt(args[4]);
                        String kitName = args[2];
                        if (size % teamSize != 0) {
                            commandSender.sendMessage(ChatColor.RED + "Tournament size & team sizes are invalid. Please try again.");
                            return true;
                        }
                        if (this.plugin.getTournamentManager().getTournament(id) != null) {
                            commandSender.sendMessage(ChatColor.RED + "This tournament already exists.");
                            return true;
                        }
                        Kit kit = this.plugin.getKitManager().getKit(kitName);
                        if (kit == null) {
                            commandSender.sendMessage(ChatColor.RED + "That kit does not exist.");
                            return true;
                        }
                        this.plugin.getTournamentManager().createTournament(commandSender, id, teamSize, size, kitName);
                    }
                    catch (NumberFormatException e) {
                        commandSender.sendMessage(ChatColor.RED + "Usage: /tournament start <id> <kit> <team size> <tournament size>");
                    }
                    break;
                }
                commandSender.sendMessage(ChatColor.RED + "Usage: /tournament start <id> <kit> <team size> <tournament size>");
                break;
            }
            case "stop": {
				if (!player.hasPermission("practice.admin")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
					return true;
				}
                if (args.length == 2) {
                    int id = Integer.parseInt(args[1]);
                    Tournament tournament = this.plugin.getTournamentManager().getTournament(id);
                    if (tournament != null) {
                        this.plugin.getTournamentManager().removeTournament(id);
                        commandSender.sendMessage(ChatColor.RED + "Successfully removed tournament " + id + ".");
                    }
                    else {
                        commandSender.sendMessage(ChatColor.RED + "This tournament does not exist.");
                    }
                    break;
                }
                commandSender.sendMessage(ChatColor.RED + "Usage: /tournament stop <id>");
                break;
            }
            case "alert": {
				if (!player.hasPermission("practice.admin")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
					return true;
				}
                if (args.length == 2) {
                    int id = Integer.parseInt(args[1]);
                    Tournament tournament = this.plugin.getTournamentManager().getTournament(id);
                    if (tournament != null) {
                        String toSend = "\n" + ChatColor.YELLOW + "(Tournament) " + ChatColor.GREEN + " " +
                                tournament.getKitName() + " (" + tournament.getTeamSize() + "v" + tournament.getTeamSize() + ")" +
                                " is starting soon. " + ChatColor.GRAY + "[Click to Join]" + "\n ";
                        Clickable message = new Clickable(toSend, ChatColor.GRAY + "Click to join this tournament.",
                                "/join " + id);
                        Bukkit.getServer().getOnlinePlayers().forEach(online -> {
                            online.sendMessage(" ");
                            message.sendToPlayer(online);
                            online.sendMessage(" ");
                        });
                    }
                    break;
                }
                commandSender.sendMessage(ChatColor.RED + "Usage: /tournament alert <id>");
                break;
            }
            case "status":
                if (args.length == 2) {
                    try {
                        int id = Integer.parseInt(args[1]);
                        Tournament tournament = this.plugin.getTournamentManager().getTournament(id);

                        if (tournament != null) {
                            StringBuilder builder = new StringBuilder();
                            builder.append(CC.RED).append(" ").append(CC.RED).append("\n");
                            builder.append(CC.SECONDARY).append("Tournament ").append(tournament.getId()).append(CC.PRIMARY).append("'s matches:");
                            builder.append(CC.RED).append(" ").append(CC.RED).append("\n");
                            for (UUID matchUUID : tournament.getMatches()) {
                                Match match = this.plugin.getMatchManager().getMatchFromUUID(matchUUID);

                                MatchTeam teamA = match.getTeams().get(0);
                                MatchTeam teamB = match.getTeams().get(1);

                                String teamANames = TeamUtil.getNames(teamA);
                                String teamBNames = TeamUtil.getNames(teamB);

                                builder.append(teamANames).append(" vs. ").append(teamBNames).append("\n");
                            }
                            builder.append(CC.RED).append(" ").append(CC.RED).append("\n");
                            builder.append(CC.PRIMARY).append("Round: ").append(CC.SECONDARY).append(tournament.getCurrentRound()).append("\n");
                            builder.append(CC.PRIMARY).append("Players: ").append(CC.SECONDARY).append(tournament.getPlayers().size()).append("\n");
                            builder.append(CC.RED).append(" ").append(CC.RED).append("\n");
                            commandSender.sendMessage(builder.toString());
                        } else {
                            commandSender.sendMessage(CC.RED + "This tournament does not exist!");
                        }
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage(CC.RED + "This is not a number!");
                    }
                }
                break;
            default: {
                commandSender.sendMessage(player.hasPermission("practice.admin") ? TournamentCommand.HELP_ADMIN_MESSAGE : TournamentCommand.HELP_REGULAR_MESSAGE);
                break;
            }
        }
        return false;
    }
}
