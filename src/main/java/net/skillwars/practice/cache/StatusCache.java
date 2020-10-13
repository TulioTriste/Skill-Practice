package net.skillwars.practice.cache;

import lombok.Data;
import lombok.Getter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;

@Data
public class StatusCache extends Thread {

    @Getter
    private static StatusCache instance;

    private int fighting;
    private int queueing;

    public StatusCache() {
        instance = this;
    }

    @Override
    public void run() {
        while (true) {
            int fighting = 0;
            int queueing = 0;

            for (PlayerData playerData : Practice.getInstance().getPlayerManager().getAllData()) {
                if (playerData.getPlayerState() == PlayerState.FIGHTING) {
                    fighting++;
                }

                if (playerData.getPlayerState() == PlayerState.QUEUE) {
                    queueing++;
                }
            }

            this.fighting = fighting;
            this.queueing = queueing;

            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
