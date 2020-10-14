package net.skillwars.practice.commands.management;

import net.skillwars.practice.Practice;
import net.skillwars.practice.util.CustomLocation;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.joeleoli.nucleus.util.Style;

import java.util.Arrays;

public class SpawnsCommand extends Command {
	
    public SpawnsCommand() {
        super("setspawn");
        this.setDescription("Spawn command.");
        this.setAliases(Arrays.asList("spawns"));
        this.setUsage(ChatColor.RED + "Usage: /setspawn <subcommand>");
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("practice.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Style.translate("&7&m--------------------------------"));
            sender.sendMessage(Style.translate("&3&lSet Spawn Locations Help"));
            sender.sendMessage(Style.translate(""));
            sender.sendMessage(Style.translate("&c/" + alias + " spawnlocation/spawnmin/spawnmax"));
            sender.sendMessage(Style.translate("&c/" + alias + " editorlocation/editormin/editormax"));
            sender.sendMessage(Style.translate("&c/" + alias + " sumolocation/sumofirst/sumosecond/sumomin/sumomax"));
            sender.sendMessage(Style.translate("&c/" + alias + " tournamentlocation/tournamentfirst/tournamentsecond/tournamentmin/tournamentmax"));
            sender.sendMessage(Style.translate("&c/" + alias + " ffalocation/ffamin/ffamax"));
            sender.sendMessage(Style.translate("&c/" + alias + " teamfightslocation/teamfightsfirst/teamfightssecond/teamfightsmin/teamfightsmax"));
            sender.sendMessage(Style.translate("&7&m--------------------------------"));
            return true;
        }
        String lowerCase = args[0].toLowerCase();
        switch (lowerCase) {
            case "spawnlocation": {
                Practice.getInstance().getSpawnManager().setSpawnLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the spawn location.");
                break;
            }
            case "spawnmin": {
                Practice.getInstance().getSpawnManager().setSpawnMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the spawn min.");
                break;
            }
            case "spawnmax": {
                Practice.getInstance().getSpawnManager().setSpawnMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the spawn max.");
                break;
            }
            case "editorlocation": {
                Practice.getInstance().getSpawnManager().setEditorLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the editor location.");
                break;
            }
            case "editormin": {
                Practice.getInstance().getSpawnManager().setEditorMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the editor min.");
                break;
            }
            case "editormax": {
                Practice.getInstance().getSpawnManager().setEditorMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the editor max.");
                break;
            }
            case "sumolocation": {
                Practice.getInstance().getSpawnManager().setSumoLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo location.");
                break;
            }
            case "sumofirst": {
                Practice.getInstance().getSpawnManager().setSumoFirst(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo location A.");
                break;
            }
            case "sumosecond": {
                Practice.getInstance().getSpawnManager().setSumoSecond(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo location B.");
                break;
            }
            case "sumomin": {
                Practice.getInstance().getSpawnManager().setSumoMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo min.");
                break;
            }
            case "sumomax": {
                Practice.getInstance().getSpawnManager().setSumoMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the sumo max.");
                break;
            }
            case "tournamentlocation": {
                Practice.getInstance().getSpawnManager().setTournamentLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the tournament location.");
                break;
            }
            case "tournamentfirst": {
                Practice.getInstance().getSpawnManager().setTournamentFirst(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the tournament location A.");
                break;
            }
            case "tournamentsecond": {
                Practice.getInstance().getSpawnManager().setTournamentSecond(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the tournament location B.");
                break;
            }
            case "tournamentmin": {
                Practice.getInstance().getSpawnManager().setTournamentMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the tournament min.");
                break;
            }
            case "tournamentmax": {
                Practice.getInstance().getSpawnManager().setTournamentMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the tournament max.");
                break;
            }
            case "ffalocation": {
                Practice.getInstance().getSpawnManager().setFFALocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the ffa location.");
                break;
            }
            case "ffamin": {
                Practice.getInstance().getSpawnManager().setFFAMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the ffa min.");
                break;
            }
            case "ffamax": {
                Practice.getInstance().getSpawnManager().setFFAMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the ffa max.");
                break;
            }
            case "teamfightslocation": {
                Practice.getInstance().getSpawnManager().setTeamFightsLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the teamfights location.");
                break;
            }
            case "teamfightsfirst": {
                Practice.getInstance().getSpawnManager().setTeamFightsFirst(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the teamfights first.");
                break;
            }
            case "teamfightssecond": {
                Practice.getInstance().getSpawnManager().setTeamFightsSecond(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the teamfights second.");
                break;
            }
            case "teamfightsmin": {
                Practice.getInstance().getSpawnManager().setTeamFightsMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the teamfights min.");
                break;
            }
            case "teamfightsmax": {
                Practice.getInstance().getSpawnManager().setTeamFightsMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                Practice.getInstance().getSpawnManager().saveConfig();
                player.sendMessage(ChatColor.GREEN + "Successfully set the teamfights max.");
                break;
            }
        }
        return false;
    }
}
