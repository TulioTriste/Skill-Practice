package net.skillwars.practice.events;

import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.Nucleus;
import net.skillwars.practice.Practice;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.Clickable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

@Setter
@Getter
public abstract class EventCountdownTask extends BukkitRunnable {
    private PracticeEvent event;
    private int countdownTime;
    private int timeUntilStart;
    private boolean ended;

    public EventCountdownTask(PracticeEvent event, int countdownTime) {
        this.event = event;
        this.countdownTime = countdownTime;
        this.timeUntilStart = countdownTime;
    }

    public void run() {
        if (this.isEnded()) {
            return;
        }
        if (this.timeUntilStart <= 0) {
            if (this.canStart()) {
                Practice.getInstance().getServer().getScheduler().runTask(Practice.getInstance(), this.event::start);
            } else {
                Practice.getInstance().getServer().getScheduler().runTask(Practice.getInstance(), this::onCancel);
            }
            this.ended = true;
            return;
        }
        if (this.shouldAnnounce(this.timeUntilStart)) {
            String toSend = ChatColor.translateAlternateColorCodes('&',"&b[Evento] &c" + event.getName() + "&e Ha sido hosteado por &r" + Practice.getInstance().getChat().getPlayerPrefix(event.getHost()) + event.getHost().getName() + "&e comenzará en &c" + event.getCountdownTask().getTimeUntilStart() + "s" +
                    " &7(" + event.getPlayers().size() + "/" + event.getLimit() + ") &a[Click Aqui]");

            Clickable message = new Clickable(toSend,
                    CC.GREEN + "Click para entrar al evento.",
                    "/join " + event.getName());
            Bukkit.getServer().getOnlinePlayers().stream().filter(other -> !event.getPlayers().containsKey(other)).forEach(player -> {
                player.sendMessage(" ");
                message.sendToPlayer(player);
                player.sendMessage(" ");
            });
        }
        --this.timeUntilStart;
    }

    public abstract boolean shouldAnnounce(int p0);

    public abstract boolean canStart();

    public abstract void onCancel();

    private String getTime(int time) {
        StringBuilder timeStr = new StringBuilder();
        int minutes = 0;

        if (time % 60 == 0) {
            minutes = time / 60;
            time = 0;
        } else {
            while (time - 60 > 0) {
                minutes++;
                time -= 60;
            }
        }

        if (minutes > 0) {
            timeStr.append(minutes).append("m");
        }
        if (time > 0) {
            timeStr.append(minutes > 0 ? " " : "").append(time).append("s");
        }

        return timeStr.toString();
    }
}
