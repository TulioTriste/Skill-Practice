package net.skillwars.practice.util.serverversion;

import org.bukkit.entity.Player;

public interface IServerVersion {

    void clearArrowsFromPlayer(Player player);

    String getPlayerLanguage(Player player);
}
