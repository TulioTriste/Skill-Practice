package net.skillwars.practice.commands.arena;

import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.Practice;
import net.skillwars.practice.arena.Arena;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.runnable.ArenaCommandRunnable;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.CustomLocation;
import net.skillwars.practice.util.MathUtil;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ArenaCommand {

    private Practice plugin = Practice.getInstance();
    private String NO_ARENA = ChatColor.RED + "Esta arena no existe!";

    @Command(names = "arena", permissionNode = "practice.arena")
    public static void help(Player player) {
        player.sendMessage(Style.translate("&a/arena list"));
        player.sendMessage(Style.translate("&a/arena create &e(name)"));
        player.sendMessage(Style.translate("&a/arena delete &e(name)"));
        player.sendMessage(Style.translate("&a/arena a &e(name)"));
        player.sendMessage(Style.translate("&a/arena b &e(name)"));
        player.sendMessage(Style.translate("&a/arena center &e(name)"));
        player.sendMessage(Style.translate("&a/arena min &e(name)"));
        player.sendMessage(Style.translate("&a/arena max &e(name)"));
        player.sendMessage(Style.translate("&a/arena enable &e(name)"));
        player.sendMessage(Style.translate("&a/arena disable &e(name)"));
        player.sendMessage(Style.translate("&a/arena generate &e(name) (numbers)"));
        player.sendMessage(Style.translate("&a/arena tp &e(name)"));
        player.sendMessage(Style.translate("&a/arena save &e(name)"));
        player.sendMessage(Style.translate("&a/arena manage"));
    }

    @Command(names = "arena list", permissionNode = "practice.arena.list")
    public static void list(Player player) {
        Config config = new Config("arenas", Practice.getInstance());
        player.sendMessage(CC.translate("&7&m--------------------------------"));
        player.sendMessage(CC.translate("&bLista de Arenas"));
        player.sendMessage(CC.translate(""));
        config.getConfig().getConfigurationSection("arenas").getKeys(false).forEach(arenaName -> {
            player.sendMessage(CC.translate(" &7- &f" + arenaName));
        });
        player.sendMessage(CC.translate("&7&m--------------------------------"));
    }

    @Command(names = "arena create", permissionNode = "practice.arena.create")
    public static void create(Player player, @Parameter(name = "name") String name) {
        Arena arena = Practice.getInstance().getArenaManager().getArena(name);
        if (arena != null) {
            player.sendMessage(ChatColor.RED + "Esta arena ya existe!");
        }

        Practice.getInstance().getArenaManager().createArena(name);
        player.sendMessage(ChatColor.GREEN + "La arena " + name + " se ha creado correctamente.");
    }

    @Command(names = "arena delete", permissionNode = "practice.arena.delete")
    public static void delete(Player player, @Parameter(name = "arena") Arena arena) {
        Practice.getInstance().getArenaManager().deleteArena(arena.getName());
        player.sendMessage(ChatColor.GREEN + "La arena " + arena.getName() + " se ha eliminado correctamente.");
    }

    @Command(names = "arena a", permissionNode = "practice.arena.a")
    public static void a(Player player, @Parameter(name = "arena") Arena arena) {
        Location location = player.getLocation();
        location.setX(location.getBlockX() + 0.5);
        location.setY(location.getBlockY() + 1.0);
        location.setZ(location.getBlockZ() + 0.5);
        arena.setA(CustomLocation.fromBukkitLocation(location));
        player.sendMessage(ChatColor.GREEN + "Se ha colocado la posición A correctamente en la arena " + arena.getName() + ".");
    }

    @Command(names = "arena b", permissionNode = "practice.arena.b")
    public static void b(Player player, @Parameter(name = "arena") Arena arena) {
        Location location = player.getLocation();
        location.setX(location.getBlockX() + 0.5);
        location.setY(location.getBlockY() + 3.0);
        location.setZ(location.getBlockZ() + 0.5);
        arena.setB(CustomLocation.fromBukkitLocation(location));
        player.sendMessage(ChatColor.GREEN + "Se ha colocado la posición B correctamente en la arena " + arena.getName() + ".");
    }

    @Command(names = "arena center", permissionNode = "practice.arena.center")
    public static void center(Player player, @Parameter(name = "arena") Arena arena) {
        Location location = player.getLocation();
        location.setX(location.getBlockX() + 0.5);
        location.setY(location.getBlockY() + 3.0);
        location.setZ(location.getBlockZ() + 0.5);
        arena.setCenter(CustomLocation.fromBukkitLocation(location));
        player.sendMessage(ChatColor.GREEN + "Se ha colocado el centro correctamente en la arena " + arena.getName() + ".");
    }

    @Command(names = "arena min", permissionNode = "practice.arena.min")
    public static void min(Player player, @Parameter(name = "arena") Arena arena) {
        arena.setMin(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Se ha colocado la posición minima correctamente en la arena " + arena.getName() + ".");
    }

    @Command(names = "arena max", permissionNode = "practice.arena.max")
    public static void max(Player player, @Parameter(name = "arena") Arena arena) {
        arena.setMax(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Se ha colocado la posición máxima correctamente en la arena " + arena.getName() + ".");
    }

    @Command(names = "arena enable", permissionNode = "practice.arena.enable")
    public static void enable(Player player, @Parameter(name = "arena") Arena arena) {
        arena.setEnabled(true);
        player.sendMessage(ChatColor.GREEN + "La arena " + arena.getName() + " se ha activado correctamente.");
    }

    @Command(names = "arena disable", permissionNode = "practice.arena.disable")
    public static void disable(Player player, @Parameter(name = "arena") Arena arena) {
        arena.setEnabled(false);
        player.sendMessage(ChatColor.GREEN + "La arena " + arena.getName() + " se ha desactivado correctamente.");
    }

    @Command(names = "arena generate", permissionNode = "practice.arena.generate")
    public static void generate(Player player, @Parameter(name = "arena") Arena arena,
                         @Parameter(name = "size") Integer size) {
        int arenas = size;
        Practice.getInstance().getServer().getScheduler().runTask(Practice.getInstance(), new ArenaCommandRunnable(Practice.getInstance(), arena, arenas));
        Practice.getInstance().getArenaManager().setGeneratingArenaRunnables(Practice.getInstance().getArenaManager().getGeneratingArenaRunnables() + 1);
    }

    @Command(names = "arena save", permissionNode = "practice.arena.save")
    public static void save(Player player, @Parameter(name = "arena") Arena arena) {
        Practice.getInstance().getArenaManager().reloadArenas();
        player.sendMessage(ChatColor.GREEN + "Todas las arenas se han reiniciado correctamente.");
    }

    @Command(names = "arena tp", permissionNode = "practice.arena.disable")
    public static void tp(Player player, @Parameter(name = "arena") Arena arena) {
        CustomLocation locationA = arena.getA();
        CustomLocation locationB = arena.getB();
        Location location =  MathUtil.getMiddle(locationA.toBukkitLocation(), locationB.toBukkitLocation());
        Location target = location.setDirection(MathUtil.getMiddle(locationA.toBukkitLocation(), locationB.toBukkitLocation()).subtract(location).toVector());
        player.teleport(target);
        player.sendMessage(ChatColor.GREEN + "Te has teletransportado hacia la arena " + arena.getName() + ".");
    }

    @Command(names = "arena manage", permissionNode = "practice.arena.manage")
    public static void manage(Player player) {
        Practice.getInstance().getArenaManager().openArenaSystemUI(player);
    }

}
