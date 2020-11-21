package net.skillwars.practice.runnable;

import lombok.RequiredArgsConstructor;
import net.skillwars.practice.Practice;
import net.skillwars.practice.listeners.MatchListener;
import net.skillwars.practice.match.Match;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class MatchResetRunnable extends BukkitRunnable {

    private final Practice plugin = Practice.getInstance();
    private final Match match;

    @Override
    public void run() {
        int count = 0;

        /*if (this.match.getKit().isBuild()) {
            for (UUID uuid : MatchListener.blocks.keySet()) {
                if (uuid.equals(this.match.getMatchId())) {
                    for (Location loc : MatchListener.blocks.get(uuid).keySet()) {
                        loc.getBlock().setType(MatchListener.blocks.get(uuid).get(loc).getType());
                    }
                }
            }
        } else */
            {
            for (BlockState blockState : this.match.getOriginalBlockChanges()) {
                if (++count <= 15) {
                    blockState.setType(blockState.getType());
                    blockState.update();
                    blockState.update(true);
                    blockState.update(true, false);
                    this.match.removeOriginalBlockChange(blockState);
                } else {
                    break;
                }
            }
        }

        if (count < 15) {
            this.match.getArena().addAvailableArena(this.match.getStandaloneArena());
            this.plugin.getArenaManager().removeArenaMatchUUID(this.match.getArena());
            this.cancel();
        }
    }
}
