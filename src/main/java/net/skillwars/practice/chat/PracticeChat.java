package net.skillwars.practice.chat;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.chat.ChatFormat;
import me.joeleoli.nucleus.player.NucleusPlayer;
import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.PlayerKit;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.util.CC;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PracticeChat implements ChatFormat {

    private Practice plugin = Practice.getInstance();

    @Override
    public String format(Player sender, Player receiver, String message) {
        Party party = this.plugin.getPartyManager().getParty(sender.getUniqueId());

        if (party != null) {
            if (message.startsWith("!") || message.startsWith("@")) {

                if(!party.getMembers().contains(receiver.getUniqueId())){
                    return null;
                }

                return CC.PRIMARY + "[Party] " + CC.PRIMARY + sender.getName() + CC.R + ": " +
                        message.replaceFirst("!", "").replaceFirst("@", "");
            }
        }

        PlayerKit kitRenaming = this.plugin.getEditorManager().getRenamingKit(sender.getUniqueId());

        if (kitRenaming != null) {
            kitRenaming.setDisplayName(ChatColor.translateAlternateColorCodes('&', message));
            sender.sendMessage(
                    CC.PRIMARY + "Set kit " + CC.SECONDARY + kitRenaming.getIndex() + CC.PRIMARY + "'s name to "
                            + CC.SECONDARY + kitRenaming.getDisplayName());

            this.plugin.getEditorManager().removeRenamingKit(sender.getUniqueId());

            return null;
        }

        NucleusPlayer profile = NucleusPlayer.getByUuid(sender.getUniqueId());
        return Style.GRAY
                + Style.translate(Nucleus.getInstance().getChat().getPlayerPrefix(sender))
                + sender.getDisplayName()
                + Style.GRAY + ": " + Style.WHITE
                + (sender.hasPermission("nucleus.chat.color") ? Style.translate(message) : message);
    }

	@Override
	public String consoleFormat(Player sender, String message) {
		return Style.GRAY
                + Style.translate(Nucleus.getInstance().getChat().getPlayerPrefix(sender))
                + sender.getDisplayName()
                + Style.GRAY + ": " + Style.WHITE
                + (sender.hasPermission("nucleus.chat.color") ? Style.translate(message) : message);
	}
}
