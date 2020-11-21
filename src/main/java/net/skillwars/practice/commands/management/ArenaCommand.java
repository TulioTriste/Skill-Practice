package net.skillwars.practice.commands.management;

import net.skillwars.practice.Practice;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.CustomLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.skillwars.practice.arena.Arena;
import net.skillwars.practice.runnable.ArenaCommandRunnable;

public class ArenaCommand extends Command {
	
    private static String NO_ARENA;

    static {
        NO_ARENA = ChatColor.RED + "That arena doesn't exist!";
    }

    private Practice plugin = Practice.getInstance();
    private Config config = new Config("arenas", this.plugin);

    public ArenaCommand() {
        super("arena");
        this.setDescription("Arenas command.");
        this.setUsage(ChatColor.RED + "Usage: /arena <subcommand> [args]");
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
            sender.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "Arena Help");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.WHITE + "/arena list - List Arenas");
            sender.sendMessage(ChatColor.WHITE + "/arena create <name> - Create Arena");
            sender.sendMessage(ChatColor.WHITE + "/arena delete <name> - Delete Arena");
            sender.sendMessage(ChatColor.WHITE + "/arena a <name> - Set a first Arena Location");
            sender.sendMessage(ChatColor.WHITE + "/arena b <name> - Set a second Arena Location");
            sender.sendMessage(ChatColor.WHITE + "/arena center <name> - Set a center Arena Location");
            sender.sendMessage(ChatColor.WHITE + "/arena min <name> - Set a min Cuboid");
            sender.sendMessage(ChatColor.WHITE + "/arena max <name> - Set a max Cuboid");
            sender.sendMessage(ChatColor.WHITE + "/arena enable <name> - Enable Arena");
            sender.sendMessage(ChatColor.WHITE + "/arena disable <name> - Disable Arena");
            sender.sendMessage(ChatColor.WHITE + "/arena generate <name> - Generate a Arenas");
            sender.sendMessage(ChatColor.WHITE + "/arena save <name> - Save Arena");
            sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------------------------");
        	return true;
        }
        if (args.length < 2) {
            sender.sendMessage(this.usageMessage);
            return true;
        }
        Arena arena = this.plugin.getArenaManager().getArena(args[1]);
        String lowerCase = args[0].toLowerCase();
        Location location = player.getLocation();
        switch (lowerCase) {
            case "list": {
                sender.sendMessage(CC.translate("&7&m--------------------------------"));
                sender.sendMessage(CC.translate("&bLista de Arenas"));
                sender.sendMessage(CC.translate(""));
                config.getConfig().getConfigurationSection("arenas").getKeys(false).forEach(arenaName -> {
                    sender.sendMessage(CC.translate(" &7- &f" + arenaName));
                });
                sender.sendMessage(CC.translate("&7&m--------------------------------"));
                break;
            }
            case "create": {
                if (arena == null) {
                    this.plugin.getArenaManager().createArena(args[1]);
                    sender.sendMessage(ChatColor.GREEN + "Successfully created arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ChatColor.RED + "That arena already exists!");
                break;
            }
            case "delete": {
                if (arena != null) {
                    this.plugin.getArenaManager().deleteArena(args[1]);
                    sender.sendMessage(ChatColor.GREEN + "Successfully deleted arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "a": {
                if (arena != null) {
                    if (args.length < 3 || !args[2].equalsIgnoreCase("-e")) {
                        location.setX(location.getBlockX() + 0.5);
                        location.setY(location.getBlockY() + 3.0);
                        location.setZ(location.getBlockZ() + 0.5);
                    }
                    arena.setA(CustomLocation.fromBukkitLocation(location));
                    sender.sendMessage(ChatColor.GREEN + "Successfully set position Center for arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "b": {
                if (arena != null) {
                    if (args.length < 3 || !args[2].equalsIgnoreCase("-e")) {
                        location.setX(location.getBlockX() + 0.5);
                        location.setY(location.getBlockY() + 3.0);
                        location.setZ(location.getBlockZ() + 0.5);
                    }
                    arena.setB(CustomLocation.fromBukkitLocation(location));
                    sender.sendMessage(ChatColor.GREEN + "Successfully set position B for arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "center": {
                if (arena != null) {
                    if (args.length < 3 || !args[2].equalsIgnoreCase("-e")) {
                        location.setX(location.getBlockX() + 0.5);
                        location.setY(location.getBlockY() + 3.0);
                        location.setZ(location.getBlockZ() + 0.5);
                    }
                    arena.setCenter(CustomLocation.fromBukkitLocation(location));
                    sender.sendMessage(ChatColor.GREEN + "Successfully set position B for arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "min": {
                if (arena != null) {
                    arena.setMin(CustomLocation.fromBukkitLocation(player.getLocation()));
                    sender.sendMessage(ChatColor.GREEN + "Successfully set minimum position for arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "max": {
                if (arena != null) {
                    arena.setMax(CustomLocation.fromBukkitLocation(player.getLocation()));
                    sender.sendMessage(ChatColor.GREEN + "Successfully set maximum position for arena " + args[1] + ".");
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "disable":
            case "enable": {
                if (arena != null) {
                    arena.setEnabled(!arena.isEnabled());
                    sender.sendMessage(arena.isEnabled() ? (ChatColor.GREEN + "Successfully enabled arena " + args[1] + ".") : (ChatColor.RED + "Successfully disabled arena " + args[1] + "."));
                    break;
                }
                sender.sendMessage(ArenaCommand.NO_ARENA);
                break;
            }
            case "generate": {
                if (args.length == 3) {
                    int arenas = Integer.parseInt(args[2]);
                    this.plugin.getServer().getScheduler().runTask(this.plugin, new ArenaCommandRunnable(this.plugin, arena, arenas));
                    this.plugin.getArenaManager().setGeneratingArenaRunnables(this.plugin.getArenaManager().getGeneratingArenaRunnables() + 1);
                    break;
                }
                sender.sendMessage(ChatColor.RED + "Usage: /arena generate <arena> <arenas>");
                break;
            }
            case "save": {
                this.plugin.getArenaManager().reloadArenas();
                sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the arenas.");
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
