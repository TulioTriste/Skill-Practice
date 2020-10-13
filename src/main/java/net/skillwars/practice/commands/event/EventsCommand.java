package net.skillwars.practice.commands.event;

import net.skillwars.practice.Practice;
import net.skillwars.practice.events.inventory.HostInvetory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class EventsCommand extends Command {

    private Practice plugin;

    public EventsCommand() {
        super("eventos");
        this.plugin = Practice.getInstance();
        this.setAliases(Arrays.asList("events", "evento"));
        this.setDescription("Open Inventory for host events");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        Player player = (Player) commandSender;
        new HostInvetory().openMenu(player);
        return true;
    }
}
