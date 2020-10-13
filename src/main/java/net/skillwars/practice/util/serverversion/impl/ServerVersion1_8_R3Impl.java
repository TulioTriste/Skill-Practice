package net.skillwars.practice.util.serverversion.impl;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.skillwars.practice.util.serverversion.IServerVersion;

public class ServerVersion1_8_R3Impl implements IServerVersion {

    @Override
    public void clearArrowsFromPlayer(Player player) {

        ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);
    }

    @Override
    public String getPlayerLanguage(Player player) {
        return ((CraftPlayer) player).getHandle().locale.substring(0, 2);
    }
}
