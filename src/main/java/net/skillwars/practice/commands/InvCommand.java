package net.skillwars.practice.commands;

import net.skillwars.practice.Practice;
import net.skillwars.practice.inventory.InventorySnapshot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

public class InvCommand extends Command {
    private static Pattern UUID_PATTERN;
    private static String INVENTORY_NOT_FOUND;

    static {
        UUID_PATTERN = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
        INVENTORY_NOT_FOUND = ChatColor.RED + "Cannot find the requested inventory. Maybe it expired?";
    }

    private Practice plugin;

    public InvCommand() {
        super("inventory");
        this.setAliases(Collections.singletonList("inv"));
        this.plugin = Practice.getInstance();
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (args.length == 0) {
            return true;
        }
        if (!args[0].matches(InvCommand.UUID_PATTERN.pattern())) {
            sender.sendMessage(InvCommand.INVENTORY_NOT_FOUND);
            return true;
        }
        InventorySnapshot snapshot = this.plugin.getInventoryManager().getSnapshot(UUID.fromString(args[0]));
        if (snapshot == null) {
            sender.sendMessage(InvCommand.INVENTORY_NOT_FOUND);
        } else {
            ((Player) sender).openInventory(snapshot.getInventoryUI().getCurrentPage());
        }
        return true;
    }
}
