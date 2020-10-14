package net.skillwars.practice.commands.arena.param;

import me.joeleoli.nucleus.command.param.ParameterType;
import net.skillwars.practice.Practice;
import net.skillwars.practice.arena.Arena;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArenaParameterType implements ParameterType<Arena> {
    @Override
    public Arena transform(CommandSender sender, String source) {
        Arena arena = Practice.getInstance().getArenaManager().getArena(source);
        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "Esta arena no existe.");
            return (null);
        }
        return arena;
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        return Practice.getInstance().getArenaManager().getArenas().keySet().stream().filter(string -> StringUtils.startsWithIgnoreCase(string, source))
                .collect(Collectors.toList());
    }
}
