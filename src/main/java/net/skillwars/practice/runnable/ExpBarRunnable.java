package net.skillwars.practice.runnable;

import net.skillwars.practice.Practice;
import org.bukkit.entity.Player;

import net.skillwars.practice.util.timer.impl.EnderpearlTimer;

import java.util.UUID;

public class ExpBarRunnable implements Runnable {
    private final Practice plugin = Practice.getInstance();

    @Override
    public void run() {
        EnderpearlTimer timer = plugin.getTimerManager().getTimer(EnderpearlTimer.class);
        for (UUID uuid : timer.getCooldowns().keySet()) {
            Player player = this.plugin.getServer().getPlayer(uuid);

            if (player != null) {
                long time = timer.getRemaining(player);
                int seconds = (int) Math.round((double) time / 1000.0D);

                player.setLevel(seconds);
                player.setExp((float) time / 16000.0F);
            }
        }
    }
}
