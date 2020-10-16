package net.skillwars.practice.listeners;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.cooldown.Cooldown;
import me.joeleoli.nucleus.log.LogQueue;
import me.joeleoli.nucleus.log.PublicMessageLog;
import me.joeleoli.nucleus.player.DefinedSetting;
import me.joeleoli.nucleus.player.NucleusPlayer;
import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.Practice;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.util.CC;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;

public class ChatListener implements Listener {

    private final Practice plugin;

    public ChatListener() {
        this.plugin = Practice.getInstance();
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        final Player player = event.getPlayer();

        if (this.plugin.getPartyManager().getParty(player.getUniqueId()) != null
                && InventoryListener.setPlayersLimit.containsKey(player.getName())) {
            String message = event.getMessage();
            Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
            if (!StringUtils.isNumeric(message)) {
                player.sendMessage(CC.translate("&cPorfavor inserte Numeros validos."));
                return;
            }
            int number = Integer.parseInt(message);
            if (number < 1 || number > 100) {
                player.sendMessage(CC.translate("&cInserte un numero entre 1 y 100"));
                return;
            }
            party.setLimit(number);
            InventoryListener.setPlayersLimit.remove(player.getName());
            player.sendMessage(CC.translate("&aLa Party se ha limitado a " + number + " miembros."));
            return;
        }

        if (Nucleus.getInstance().getChatManager().isChatMuted() && !player.hasPermission("nucleus.staff")) {
            player.sendMessage(Style.RED + "Public chat is currently muted.");
            return;
        }

        if (!NucleusAPI.<Boolean>getSetting(player, DefinedSetting.GlobalPlayerSetting.RECEIVE_GLOBAL_MESSAGES)) {
            player.sendMessage(Style.RED + "You can't chat while you have globla chat disabled.");
            return;
        }

        final NucleusPlayer nucleusPlayer = NucleusPlayer.getByUuid(player.getUniqueId());
        final String message = event.getMessage();

        if (!player.hasPermission("nucleus.chatdelay.bypass")) {
            if (!nucleusPlayer.getChatCooldown().hasExpired()) {
                player.sendMessage(Style.RED + "You can chat again in " + Style.BOLD +
                        nucleusPlayer.getChatCooldown().getTimeLeft() + "s" + Style.RED + ".");
                return;
            } else {
                nucleusPlayer
                        .setChatCooldown(new Cooldown(Nucleus.getInstance().getChatManager().getDelayTime() * 1000));
            }
        }

        for (Player receiver : Bukkit.getOnlinePlayers()) {
            final NucleusPlayer receiverData = NucleusPlayer.getByUuid(receiver.getUniqueId());

            if (receiverData.getSettings().getBoolean(DefinedSetting.GlobalPlayerSetting.RECEIVE_GLOBAL_MESSAGES) &&
                    !receiverData.isIgnored(player.getUniqueId())) {
                receiver.sendMessage(
                        Nucleus.getInstance().getChatManager().getChatFormat()
                                .format(event.getPlayer(), receiver, message));
            }
        }

        Bukkit.getConsoleSender().sendMessage(Nucleus.getInstance().getChatManager().getChatFormat()
                .consoleFormat(event.getPlayer(), message));

        LogQueue.getPublicMessageLogs().add(
                new PublicMessageLog(
                        player.getUniqueId(),
                        message,
                        System.currentTimeMillis()
                )
        );
    }
}
