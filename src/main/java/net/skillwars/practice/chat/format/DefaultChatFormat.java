package net.skillwars.practice.chat.format;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.Practice;
import net.skillwars.practice.chat.ChatFormat;
import org.bukkit.entity.Player;

public class DefaultChatFormat implements ChatFormat {

	@Override
	public String format(Player sender, Player receiver, String message) {
		return Style.translate(Practice.getInstance().getChat().getPlayerPrefix(sender) + sender + Style.WHITE + ": " + Style.RESET + message);
	}
	
	@Override
	public String consoleFormat(Player sender, String message) {
		return Style.translate(Practice.getInstance().getChat().getPlayerPrefix(sender) + sender + Style.WHITE + ": " + Style.RESET + message);
	}

}
