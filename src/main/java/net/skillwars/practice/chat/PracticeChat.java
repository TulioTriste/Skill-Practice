package net.skillwars.practice.chat;

import me.joansiitoh.datas.PlayerData;
import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.PlayerKit;
import net.skillwars.practice.listeners.InventoryListener;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.util.CC;

import org.apache.commons.lang3.StringUtils;
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
                    CC.WHITE + "Set kit " + CC.SECONDARY + kitRenaming.getIndex() + CC.WHITE + "'s name to "
                            + CC.SECONDARY + kitRenaming.getDisplayName());

            this.plugin.getEditorManager().removeRenamingKit(sender.getUniqueId());

            return null;
        }

        if (InventoryListener.setPlayersLimit.get(sender.getName()) != null) {
            if (!StringUtils.isNumeric(message)) {
                sender.sendMessage(CC.translate("&cPorfavor inserte Numeros validos."));
                return null;
            }
            int number = Integer.parseInt(message);
            if (number < 1 || number > 100) {
                sender.sendMessage(CC.translate("&cInserte un numero entre 1 y 100"));
                return null;
            }
            party.setLimit(number);
            InventoryListener.setPlayersLimit.remove(sender.getName());
            sender.sendMessage(CC.translate("&aLa Party se ha limitado a " + number + " miembros."));
            return null;
        }

        PlayerData playerData = PlayerData.getPlayer(sender.getUniqueId());
        String tag = playerData.getData("TAG") != null ? " " + playerData.getData("TAG").toString() : "";
        String color = playerData.getData("COLOR") != null ? ChatColor.valueOf(playerData.getData("COLOR").toString()).toString() : ChatColor.WHITE.toString();
        return CC.GRAY
                + CC.translate(Practice.getInstance().getChat().getPlayerPrefix(sender))
                + sender.getDisplayName()
                + CC.translate(tag)
                + CC.GRAY + ": " + color
                + (sender.hasPermission("nucleus.chat.color") ? CC.translate(message) : message);
    }

	@Override
	public String consoleFormat(Player sender, String message) {
        PlayerKit kitRenaming = this.plugin.getEditorManager().getRenamingKit(sender.getUniqueId());

        if (kitRenaming != null) {
            kitRenaming.setDisplayName(ChatColor.translateAlternateColorCodes('&', message));
            sender.sendMessage(
                    CC.WHITE + "Set kit " + CC.SECONDARY + kitRenaming.getIndex() + CC.WHITE + "'s name to "
                            + CC.SECONDARY + kitRenaming.getDisplayName());

            this.plugin.getEditorManager().removeRenamingKit(sender.getUniqueId());

            return null;
        }

        Party party = this.plugin.getPartyManager().getParty(sender.getUniqueId());

        if (InventoryListener.setPlayersLimit.get(sender.getName()) != null) {
            if (!StringUtils.isNumeric(message)) {
                sender.sendMessage(CC.translate("&cPorfavor inserte Numeros validos."));
                return null;
            }
            int number = Integer.parseInt(message);
            if (number < 1 || number > 100) {
                sender.sendMessage(CC.translate("&cInserte un numero entre 1 y 100"));
                return null;
            }
            party.setLimit(number);
            InventoryListener.setPlayersLimit.remove(sender.getName());
            sender.sendMessage(CC.translate("&aLa Party se ha limitado a " + number + " miembros."));
            return null;
        }

        PlayerData playerData = PlayerData.getPlayer(sender.getUniqueId());
        String tag = playerData.getData("TAG") != null ? " " + playerData.getData("TAG").toString() : "";
        String color = playerData.getData("COLOR") != null ? ChatColor.valueOf(playerData.getData("COLOR").toString()).toString() : ChatColor.WHITE.toString();
        return CC.GRAY
                + CC.translate(Practice.getInstance().getChat().getPlayerPrefix(sender))
                + sender.getDisplayName()
                + CC.translate(tag)
                + CC.GRAY + ": " + color
                + (sender.hasPermission("nucleus.chat.color") ? CC.translate(message) : message);
	}
}
