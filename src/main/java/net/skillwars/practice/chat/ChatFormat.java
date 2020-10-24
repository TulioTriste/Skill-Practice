package net.skillwars.practice.chat;

import org.bukkit.entity.Player;

public interface ChatFormat {

	String format(Player sender, Player receiver, String message);
	
	String consoleFormat(Player sender, String message);

}
