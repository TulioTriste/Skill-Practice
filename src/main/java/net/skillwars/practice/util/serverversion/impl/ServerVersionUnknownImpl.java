package net.skillwars.practice.util.serverversion.impl;

import org.bukkit.entity.Player;

import net.skillwars.practice.util.serverversion.IServerVersion;

public class ServerVersionUnknownImpl implements IServerVersion {

    @Override
    public void clearArrowsFromPlayer(Player player) {

    }

    @Override
    public String getPlayerLanguage(Player player) {
        return "en";
    }
}
