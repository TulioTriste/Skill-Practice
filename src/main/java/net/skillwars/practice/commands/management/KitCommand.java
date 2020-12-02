package net.skillwars.practice.commands.management;

import net.skillwars.practice.Practice;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.skillwars.practice.arena.Arena;
import net.skillwars.practice.kit.Kit;

import java.util.Arrays;

public class KitCommand extends Command {
	
    private Practice plugin = Practice.getInstance();
    private static String NO_KIT;
    private static String NO_ARENA;
    private Config config = new Config("kits", this.plugin);

    static {
        NO_KIT = ChatColor.RED + "That kit doesn't exist!";
        NO_ARENA = ChatColor.RED + "That arena doesn't exist!";
    }

    public KitCommand() {
        super("kit");
        this.setDescription("Kit command.");
        this.setAliases(Arrays.asList("ladder", "ladders"));
        this.setUsage(ChatColor.RED + "Usage: /kit <subcommand> [args]");
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
        if(args.length == 0) {
            sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------");
            sender.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "Kit Help");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.AQUA + "Obligatorios");
            sender.sendMessage(ChatColor.WHITE + "/kit create <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit enable <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit icon <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit setinv <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit seteditable <kitname>");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.AQUA + "Opcionales");
            sender.sendMessage(ChatColor.WHITE + "/kit delete <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit disable <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit ranked <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit combo|build|sumo|spleef|parkour|tnttag|waterdrop|freefall <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit whitelistarena <kitname> <arenaname>");
            sender.sendMessage(ChatColor.WHITE + "/kit getinv <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit excludearena <kitname> <arenaname>");
            sender.sendMessage(ChatColor.WHITE + "/kit excludearenafromallkitsbut <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit seteditinv <kitname>");
            sender.sendMessage(ChatColor.WHITE + "/kit geteditinv <kitname>");
            sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(this.usageMessage);
            return true;
        }
        Kit kit = this.plugin.getKitManager().getKit(args[1]);
        String lowerCase = args[0].toLowerCase();
        switch (lowerCase) {
            case "create": {
                if (kit == null) {
                    this.plugin.getKitManager().createKit(args[1]);
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(ChatColor.GREEN + "Successfully created kit " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ChatColor.RED + "That kit already exists!");
                break;
            }
            case "delete": {
                if (kit != null) {
                    this.plugin.getKitManager().deleteKit(args[1]);
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(ChatColor.GREEN + "Successfully deleted kit " + args[1] + ".");
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "disable":
            case "enable": {
                if (kit != null) {
                    kit.setEnabled(!kit.isEnabled());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isEnabled() ? (ChatColor.GREEN + "Successfully enabled kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "combo": {
                if (kit != null) {
                    kit.setCombo(!kit.isCombo());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isCombo() ? (ChatColor.GREEN + "Successfully enabled combo mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled combo mode for kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "sumo": {
                if (kit != null) {
                    kit.setSumo(!kit.isSumo());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isSumo() ? (ChatColor.GREEN + "Successfully enabled sumo mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled sumo mode for kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "build": {
                if (kit != null) {
                    kit.setBuild(!kit.isBuild());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isBuild() ? (ChatColor.GREEN + "Successfully enabled build mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled build mode for kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "spleef": {
                if (kit != null) {
                    kit.setSpleef(!kit.isSpleef());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isSpleef() ? (ChatColor.GREEN + "Successfully enabled spleef mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled spleef mode for kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "parkour": {
                if (kit != null) {
                    kit.setParkour(!kit.isParkour());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isParkour() ? (ChatColor.GREEN + "Successfully enabled parkour mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled parkour mode for kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "tnttag": {
                if (kit != null) {
                    kit.setTnttag(!kit.isTnttag());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isTnttag() ? (ChatColor.GREEN + "Successfully enabled tnttag mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled tnttag mode for kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "waterdrop": {
                if (kit != null) {
                    kit.setWaterdrop(!kit.isWaterdrop());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isWaterdrop() ? (ChatColor.GREEN + "Successfully enabled waterdrop mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled waterdrop mode for kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "freefall": {
                if (kit != null) {
                    kit.setFreeFall(!kit.isFreeFall());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isFreeFall() ? (ChatColor.GREEN + "Successfully enabled freefall mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled freefall mode for kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "ranked": {
                if (kit != null) {
                    kit.setRanked(!kit.isRanked());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(kit.isRanked() ? (ChatColor.GREEN + "Successfully enabled ranked mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled ranked mode for kit " + args[1] + "."));
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "excludearenafromallkitsbut": {
                if (kit != null) {
                    Arena arena = this.plugin.getArenaManager().getArena(args[2]);
                    if (arena != null) {
                        for (Kit loopKit : this.plugin.getKitManager().getKits()) {
                            if (!loopKit.equals(kit)) {
                                player.performCommand("kit excludearena " + loopKit.getName() + " " + arena.getName());
                            }
                        }
                    }
                    else {
                        sender.sendMessage(KitCommand.NO_ARENA);
                    }
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "excludearena": {
                if (args.length < 3) {
                    sender.sendMessage(this.usageMessage);
                    return true;
                }
                if (kit != null) {
                    Arena arena = this.plugin.getArenaManager().getArena(args[2]);
                    if (arena != null) {
                        kit.excludeArena(arena.getName());
                        this.plugin.getKitManager().saveKits();
                        sender.sendMessage(kit.getExcludedArenas().contains(arena.getName()) ? (ChatColor.GREEN + "Arena " + arena.getName() + " is now excluded from kit " + args[1] + ".") : (ChatColor.GREEN + "Arena " + arena.getName() + " is no longer excluded from kit " + args[1] + "."));
                    }
                    else {
                        sender.sendMessage(KitCommand.NO_ARENA);
                    }
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "whitelistarena": {
                if (args.length < 3) {
                    sender.sendMessage(this.usageMessage);
                    return true;
                }
                if (kit != null) {
                    Arena arena = this.plugin.getArenaManager().getArena(args[2]);
                    if (arena != null) {
                        kit.whitelistArena(arena.getName());
                        this.plugin.getKitManager().saveKits();
                        sender.sendMessage(kit.getArenaWhiteList().contains(arena.getName()) ? (ChatColor.GREEN + "Arena " + arena.getName() + " is now whitelisted to kit " + args[1] + ".") : (ChatColor.GREEN + "Arena " + arena.getName() + " is no longer whitelisted to kit " + args[1] + "."));
                    }
                    else {
                        sender.sendMessage(KitCommand.NO_ARENA);
                    }
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "icon": {
                if (kit == null) {
                    sender.sendMessage(KitCommand.NO_KIT);
                    break;
                }
                if (player.getItemInHand().getType() != Material.AIR) {
                    ItemStack icon = ItemUtil.renameItem(player.getItemInHand().clone(), ChatColor.BLUE + kit.getName());
                    kit.setIcon(icon);
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(ChatColor.GREEN + "Successfully set icon for kit " + args[1] + ".");
                    break;
                }
                player.sendMessage(ChatColor.RED + "You must be holding an item to set the kit icon!");
                break;
            }
            case "setinv": {
                if (kit == null) {
                    sender.sendMessage(KitCommand.NO_KIT);
                    break;
                }
                if (player.getGameMode() == GameMode.CREATIVE) {
                    sender.sendMessage(ChatColor.RED + "You can't set item contents in creative mode!");
                    break;
                }
                player.updateInventory();
                kit.setContents(player.getInventory().getContents());
                kit.setArmor(player.getInventory().getArmorContents());
                this.plugin.getKitManager().saveKits();
                sender.sendMessage(ChatColor.GREEN + "Successfully set kit contents for " + args[1] + ".");
                break;
            }
            case "getinv": {
                if (kit != null) {
                    player.getInventory().setContents(kit.getContents());
                    player.getInventory().setArmorContents(kit.getArmor());
                    player.updateInventory();
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(ChatColor.GREEN + "Successfully retrieved kit contents from " + args[1] + ".");
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            case "seteditable":{
                if (kit == null) {
                    sender.sendMessage(KitCommand.NO_KIT);
                    break;
                }
                if(kit.isEditable()){
                    kit.setEditable(false);
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(ChatColor.YELLOW + "Successfully set edit kit not editable .");
                }else{
                    kit.setEditable(true);
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(ChatColor.GREEN + "Successfully set edit kit editable .");
                }
                break;
            }
            case "seteditinv": {
                if (kit == null) {
                    sender.sendMessage(KitCommand.NO_KIT);
                    break;
                }
                if (player.getGameMode() == GameMode.CREATIVE) {
                    sender.sendMessage(ChatColor.RED + "You can't set item contents in creative mode!");
                    break;
                }
                player.updateInventory();
                kit.setKitEditContents(player.getInventory().getContents());
                this.plugin.getKitManager().saveKits();
                sender.sendMessage(ChatColor.GREEN + "Successfully set edit kit contents for " + args[1] + ".");
                break;
            }
            case "geteditinv": {
                if (kit != null) {
                    player.getInventory().setContents(kit.getKitEditContents());
                    player.updateInventory();
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(ChatColor.GREEN + "Successfully retrieved edit kit contents from " + args[1] + ".");
                    break;
                }
                sender.sendMessage(KitCommand.NO_KIT);
                break;
            }
            default: {
                sender.sendMessage(this.usageMessage);
                break;
            }
        }
        return true;
    }
}
