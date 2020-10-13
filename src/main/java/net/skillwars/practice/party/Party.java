package net.skillwars.practice.party;

import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.Practice;
import net.skillwars.practice.match.MatchTeam;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
public class Party {
    private final Practice plugin;
    private final UUID leader;
    private final Set<UUID> members;
    private int limit;
    private boolean open;
    private BukkitTask broadcastTask;

    public Party(final UUID leader) {
        this.plugin = Practice.getInstance();
        this.members = new HashSet<>();
        this.limit = 50;
        this.leader = leader;
        this.members.add(leader);
    }

    public void addMember(final UUID uuid) {
        this.members.add(uuid);
    }

    public void removeMember(final UUID uuid) {
        this.members.remove(uuid);
    }

    public void broadcast(final String message) {
        this.members().forEach(member -> member.sendMessage(message));
    }

    public MatchTeam[] split() {
        final List<UUID> teamA = new ArrayList<>();
        final List<UUID> teamB = new ArrayList<>();
        for (final UUID member : this.members) {
            if (teamA.size() == teamB.size()) {
                teamA.add(member);
            } else {
                teamB.add(member);
            }
        }
        return new MatchTeam[]{new MatchTeam(teamA.get(0), teamA, 0), new MatchTeam(teamB.get(0), teamB, 1)};
    }

    public Stream<Player> members() {
        return this.members.stream().map(this.plugin.getServer()::getPlayer).filter(Objects::nonNull);
    }

    public class PartyTask extends BukkitRunnable{
        @Override
        public void run() {
            if(isOpen()){
                Bukkit.broadcastMessage(Style.translate("&a"));
            }
        }
    }
}
