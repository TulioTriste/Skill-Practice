package net.skillwars.practice.commands.event;

import net.skillwars.practice.Practice;
import net.skillwars.practice.events.EventState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.events.PracticeEvent;

public class EventManagerCommand extends Command {
    private Practice plugin;

    public EventManagerCommand() {
        super("eventmanager");
        this.plugin = Practice.getInstance();
        this.setDescription("Manage an event.");
        this.setUsage(ChatColor.RED + "Usage: /eventmanager <start/end/status/cooldown> <event>");
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if(args.length == 0) {
        	sender.sendMessage(Style.translate("&6&lEventManager Command"));
        	sender.sendMessage(Style.translate(""));
        	sender.sendMessage(Style.translate("&c/" + alias + " start {event}"));
        	sender.sendMessage(Style.translate("&c/" + alias + " end {event}"));
        	sender.sendMessage(Style.translate("&c/" + alias + " status {event}"));
        	sender.sendMessage(Style.translate("&c/" + alias + " cooldown {event}"));
        	sender.sendMessage("");
        	return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("practice.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        String action = args[0];
        String eventName = null;
        if (args.length == 2) {
            eventName = args[1];
        }
        if (this.plugin.getEventManager().getByName(eventName) == null) {
            player.sendMessage(ChatColor.RED + "That event doesn't exist.");
            return true;
        }
        PracticeEvent event = this.plugin.getEventManager().getByName(eventName);
        if (action.equalsIgnoreCase("START") && event.getState() == EventState.WAITING) {
            event.getCountdownTask().setTimeUntilStart(5);
            player.sendMessage(ChatColor.RED + "Event was force started.");
        } else if (action.equalsIgnoreCase("END") && event.getState() == EventState.STARTED) {
            event.end();
            player.sendMessage(ChatColor.RED + "Event was cancelled.");
        } else if (action.equalsIgnoreCase("STATUS")) {
            String[] message = {ChatColor.YELLOW + "Event: " + ChatColor.WHITE + event.getName(), ChatColor.YELLOW + "Host: " + ChatColor.WHITE + ((event.getHost() == null) ? "Player Left" : event.getHost().getName()), ChatColor.YELLOW + "Players: " + ChatColor.WHITE + event.getPlayers().size() + "/" + event.getLimit(), ChatColor.YELLOW + "State: " + ChatColor.WHITE + event.getState().name()};
            player.sendMessage(message);
        } else if (action.equalsIgnoreCase("COOLDOWN")) {
            this.plugin.getEventManager().setCooldown(0L);
            player.sendMessage(ChatColor.RED + "Event cooldown was cancelled.");
        } else {
            player.sendMessage(this.usageMessage);
        }
        return true;
    }
}
