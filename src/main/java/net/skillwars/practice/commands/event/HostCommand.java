package net.skillwars.practice.commands.event;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.skillwars.practice.Practice;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.JSONMessage;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.events.EventState;
import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.util.Clickable;

import java.awt.*;

public class HostCommand extends Command {

    private Practice plugin;

    public HostCommand() {
        super("host");
        this.plugin = Practice.getInstance();
        this.setDescription("Host an event.");
        this.setUsage(ChatColor.RED + "Usage: /host <event>");
    }

    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(this.usageMessage);
            return true;
        }

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (this.plugin.getPartyManager().getParty(playerData.getUniqueId()) != null || playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        String eventName = args[0];
        if (eventName == null) {
            return true;
        }
        if (this.plugin.getEventManager().getByName(eventName) == null) {
            player.sendMessage(ChatColor.RED + "That event doesn't exist.");
            player.sendMessage(ChatColor.RED + "Available events: Sumo, NoDebuffLite, FFA, TeamFights");
            return true;
        }
		if (!player.hasPermission("host." + eventName.toLowerCase())) {
            player.sendMessage(Style.translate("&cYou do not have permission to host that event. &7| &cPurchase a rank at &astore.skillwars.us"));
            return true;
        }
        if (System.currentTimeMillis() < this.plugin.getEventManager().getCooldown()) {
            player.sendMessage(ChatColor.RED + "There is a cooldown. Event can't start at this moment.");
            return true;
        }
        PracticeEvent event = this.plugin.getEventManager().getByName(eventName);
        boolean inTournament = this.plugin.getTournamentManager().isInTournament(player.getUniqueId());
        if (inTournament) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        if (event == null) {
            player.sendMessage(ChatColor.RED + "That event doesn't exist.");
            return true;
        }
        if (event.getPlayers().containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in this event.");
            return true;
        }
        if (event.getState() != EventState.UNANNOUNCED) {
            player.sendMessage(ChatColor.RED + "There is currently an active event.");
            return true;
        }
        boolean eventBeingHosted = this.plugin.getEventManager().getEvents().values().stream().anyMatch(e -> e.getState() != EventState.UNANNOUNCED);
        if (eventBeingHosted) {
            player.sendMessage(ChatColor.RED + "There is currently an active event.");
            return true;
        }
        event.setLimit(50);
        if (args.length == 2 && player.hasPermission("practice.host.unlimited")) {
            if (!NumberUtils.isNumber(args[1])) {
                player.sendMessage(ChatColor.RED + "That's not a correct amount.");
                return true;
            }
            event.setLimit(Integer.parseInt(args[1]));
        }
        this.plugin.getEventManager().hostEvent(event, player);
        this.plugin.getEventManager().setName(eventName);
        event.join(player);
        String toSend = ChatColor.translateAlternateColorCodes('&',"&b[Evento] &c" + event.getName() + "&e Ha sido hosteado por &r" + Practice.getInstance().getChat().getPlayerPrefix(event.getHost()) + event.getHost().getName() + " &ecomenzará en &c" + event.getCountdownTask().getTimeUntilStart() + "s" +
                " &7(" + event.getPlayers().size() + "/" + event.getLimit() + ") &a[Click Aqui]");

        Clickable message = new Clickable(toSend,
                Style.GREEN + "Click para entrar al evento.",
                "/join " + event.getName());
        hostEventBungeeMessage(event);
        this.plugin.getServer().getOnlinePlayers().stream().filter(other -> !event.getPlayers().containsKey(other)).forEach(online -> {
            online.sendMessage(" ");
            message.sendToPlayer(online);
            online.sendMessage(" ");
        });
        return true;
    }

    private void hostEventBungeeMessage(PracticeEvent event) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("MessageRaw");
        out.writeUTF("ALL");
        out.writeUTF(JSONMessage.create("§c[Practice] §fEl evento §b" + event.getName() + " §fha sido hosteado! §a[Click Aqui]")
                .tooltip("§a¡Click Aqui!")
                .runCommand("/practice").toString());

        // If you don't care about the player
        // Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        // Else, specify them
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
