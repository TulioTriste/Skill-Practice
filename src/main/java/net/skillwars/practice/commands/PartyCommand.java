package net.skillwars.practice.commands;

import net.skillwars.practice.Practice;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.Clickable;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.joeleoli.nucleus.Nucleus;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.Color;
import net.skillwars.practice.util.StringUtil;

import java.util.*;

public class PartyCommand extends Command {

    private static String NOT_LEADER = CC.RED + "No eres el líder de la party!";
    private static String[] HELP_MESSAGE = new String[] {
            CC.DARK_GRAY + CC.STRIKE_THROUGH + "----------------------------------------------------",
            CC.PRIMARY + CC.BOLD + "Comandos de Party",
            CC.GRAY + " - " + CC.SECONDARY + "/party help " + CC.GRAY + "-" + CC.WHITE + " Mostrar ayuda de la party.",
            CC.GRAY + " - " + CC.SECONDARY + "/party create " + CC.GRAY + "-" + CC.WHITE + " Crear una party.",
            CC.GRAY + " - " + CC.SECONDARY + "/party leave " + CC.GRAY + "-" + CC.WHITE + " Dejar una party.",
            CC.GRAY + " - " + CC.SECONDARY + "/party info " + CC.GRAY + "-" + CC.WHITE + " Información de tu party.",
            CC.GRAY + " - " + CC.SECONDARY + "/party join (player) " + CC.GRAY  + "-" + CC.WHITE + " Entra a una party.",
            "",
            CC.PRIMARY + CC.BOLD + "Commandos de Líder",
            CC.GRAY + " - " + CC.SECONDARY + "/party open " + CC.GRAY + "-" + CC.WHITE + " Abre tu party para que otros se puedan unir.",
            CC.GRAY + " - " + CC.SECONDARY + "/party lock " + CC.GRAY + "-" + CC.WHITE + " Cerrar tu party para que otros no se puedan unir.",
            CC.GRAY + " - " + CC.SECONDARY + "/party setlimit (amount) " + CC.GRAY + "-" + CC.WHITE + "Ponle un límite a tu party.",
            CC.GRAY + " - " + CC.SECONDARY + "/party invite (player) " + CC.GRAY + "-" + CC.WHITE + " Invita a jugadores a tu party.",
            CC.GRAY + " - " + CC.SECONDARY + "/party kick (player) " + CC.GRAY + "-" + CC.WHITE + " Remueve a un jugador de tu party.",
            CC.DARK_GRAY + CC.STRIKE_THROUGH + "----------------------------------------------------"
    };

    private Practice plugin;

    public PartyCommand() {
        super("party");
        this.plugin = Practice.getInstance();
        this.setDescription("Party Command.");
        this.setUsage(CC.RED + "Usage: /party <subcommand> [player]");
        this.setAliases(Collections.singletonList("p"));
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());

        String subCommand = args.length < 1 ? "help" : args[0];

        switch (subCommand.toLowerCase()) {
            case "create":
                if (party != null) {
                    player.sendMessage(CC.RED + "Ya estas en una party.");
                } else if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "Solo puedes ejecutar este comando en el Spawn.");
                } else {
                    this.plugin.getPartyManager().createParty(player);
                }
                break;
            case "leave":
                if (party == null) {
                    player.sendMessage(CC.RED + "No estas en una party.");
                } else if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "Solo puedes ejecutar este comando en el Spawn.");
                } else {
                    this.plugin.getPartyManager().leaveParty(player);
                }
                break;
            case "inv":
            case "invite":
                if (party == null) {
                    player.sendMessage(CC.RED + "No estas en una party.");
                } else if (!this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                    player.sendMessage(CC.RED + "No eres el líder de la party.");
                } else if (this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null) {
                    player.sendMessage(CC.RED + "Actualmente estas en un torneo.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party invite (player)");
                } else if (party.isOpen()) {
                    player.sendMessage(CC.RED + "Esta party está abierta, así que cualquiera puede unirse...");
                } else if (party.getMembers().size() >= party.getLimit()) {
                    player.sendMessage(CC.RED + "El tamaño de la party ha alcanzado su límite.");
                } else {
                    if (party.getLeader() != player.getUniqueId()) {
                        player.sendMessage(PartyCommand.NOT_LEADER);
                        return true;
                    }
                    Player target = this.plugin.getServer().getPlayer(args[1]);

                    if (target == null) {
                        player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());

                    if (!targetData.getOptions().isPartyInvites()) {
                        player.sendMessage(CC.RED + "Este jugador tiene desactivada las peticiones de party.");
                        return true;
                    }

                    if (target.getUniqueId() == player.getUniqueId()) {
                        player.sendMessage(CC.RED + "No te puedes invitar a ti mismo.");
                    } else if (this.plugin.getPartyManager().getParty(target.getUniqueId()) != null) {
                        player.sendMessage(CC.RED + "Este jugador ya esta en una party.");
                    } else if (targetData.getPlayerState() != PlayerState.SPAWN) {
                        player.sendMessage(CC.RED + "Este jugador está ocupado ahora mismo.");
                    } else if (this.plugin.getPartyManager().hasPartyInvite(target.getUniqueId(), player.getUniqueId())) {
                        player.sendMessage(CC.RED + "Ya le has enviado una petición a este jugador.");
                    } else {
                        this.plugin.getPartyManager().createPartyInvite(player.getUniqueId(), target.getUniqueId());

                        Clickable partyInvite = new Clickable(CC.translate(Practice.getInstance().getChat().getPlayerPrefix((Player) sender) + sender.getName() + CC.SECONDARY + " te ha invitado a su party! " + CC.GRAY + "[Clic para aceptar]"),
                                CC.GRAY + "Clic para aceptar",
                                "/party accept " + sender.getName());

                        partyInvite.sendToPlayer(target);

                        party.broadcast(CC.translate(Practice.getInstance().getChat().getPlayerPrefix(target) + target.getName() + CC.SECONDARY + " ha sido invitado a la party."));

                    }
                }
                break;
            case "accept":
                if (party != null) {
                    player.sendMessage(CC.RED + "Ya estas en una party.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party accept <player>.");
                } else if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "Solo puedes ejecutar este comando en el Spawn.");
                } else {
                    Player target = this.plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());

                    if (targetParty == null) {
                        player.sendMessage(CC.RED + "Este jugador no esta en una party.");
                    } else if (targetParty.getMembers().size() >= targetParty.getLimit()) {
                        player.sendMessage(CC.RED + "El tamaño de la party ha alcanzado su límite.");
                    } else if (!this.plugin.getPartyManager().hasPartyInvite(player.getUniqueId(), targetParty.getLeader())) {
                        player.sendMessage(CC.RED + "No tienes ninguna solicitud pendiente.");
                    } else {
                        this.plugin.getPartyManager().joinParty(targetParty.getLeader(), player);
                    }
                }
                break;
            case "join":
                if (party != null) {
                    player.sendMessage(CC.RED + "Ya estas en una party.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party join <player>.");
                } else if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(CC.RED + "Solo puedes usar este comando en el Spawn.");
                } else {
                    Player target = this.plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());

                    if (targetParty == null || !targetParty.isOpen() || targetParty.getMembers().size() >= targetParty.getLimit()) {
                        player.sendMessage(CC.RED + "No puedes unirte a esta party.");
                    } else {
                        this.plugin.getPartyManager().joinParty(targetParty.getLeader(), player);
                    }
                }
                break;
            case "kick":
                if (party == null) {
                    player.sendMessage(CC.RED + "No estas en una party.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party kick <player>.");
                } else {
                    if (party.getLeader() != player.getUniqueId()) {
                        player.sendMessage(PartyCommand.NOT_LEADER);
                        return true;
                    }

                    Player target = this.plugin.getServer().getPlayer(args[1]);

                    if (target == null) {
                        player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());

                    if (targetParty == null || targetParty.getLeader() != party.getLeader()) {
                        player.sendMessage(CC.RED + "Este jugador no esta en tu party.");
                    } else {
                        this.plugin.getPartyManager().leaveParty(target);
                    }
                }
                break;
            case "setlimit":
                if (party == null) {
                    player.sendMessage(CC.RED + "No estas en una party.");
                } else if (args.length < 2) {
                    player.sendMessage(CC.RED + "Usage: /party setlimit <amount>.");
                } else {
                    if (party.getLeader() != player.getUniqueId()) {
                        player.sendMessage(PartyCommand.NOT_LEADER);
                        return true;
                    }
                    try {
                        int limit = Integer.parseInt(args[1]);

                        if (limit < 2 || limit > 100) {
                            player.sendMessage(CC.RED + "El límite máximo es 100.");
                        } else {
                            party.setLimit(limit);
                            player.sendMessage(CC.GREEN + "Has establecido el límite de jugadores de la party a " + CC.YELLOW + limit + " jugadores.");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(CC.RED + "No es un numero valido.");
                    }
                }
                break;
            case "open":
            case "lock":
                if (party == null) {
                    player.sendMessage(CC.RED + "No estas en una party.");
                } else {
                    if (party.getLeader() != player.getUniqueId()) {
                        player.sendMessage(PartyCommand.NOT_LEADER);
                        return true;
                    }
                    party.setOpen(!party.isOpen());

                    if(party.isOpen()){
                        if(player.hasPermission("party.announce")){
                            party.setBroadcastTask(new BukkitRunnable(){
                                @Override
                                public void run() {
                                    for (Player other : Bukkit.getOnlinePlayers()) {
                                        Party otherParty = plugin.getPartyManager().getParty(other.getUniqueId());
                                        Player leader = Bukkit.getPlayer(party.getLeader());

                                        if (otherParty == null) {
                                            Clickable partyPublic = new Clickable(Color.translate(Practice.getInstance().getChat().getPlayerPrefix(leader) + leader.getName() +
                                                    " &besta hosteando una party publica! &7" + "[Clic para entrar]"),
                                                    CC.GRAY + "Clic para entrar",
                                                    "/party join " + leader.getName());

                                            partyPublic.sendToPlayer(other);
                                        }
                                    }
                                }
                            }.runTaskTimerAsynchronously(Practice.getInstance(), 20L, 20L * 60L));
                        }
                    }else{
                        if(party.getBroadcastTask() != null){
                            party.getBroadcastTask().cancel();
                        }
                    }

                    party.broadcast(CC.translate("&eTu party está " + (party.isOpen() ? "&aAbierta" : "&cCerrada")));
                }
                break;
            case "info":
                if (party == null) {
                    player.sendMessage(CC.RED + "No estas en una party.");
                } else {

                    List<UUID> members = new ArrayList<>(party.getMembers());
                    members.remove(party.getLeader());

                    StringBuilder builder = new StringBuilder(CC.PRIMARY + "Members (" + (party.getMembers().size() + "): "));
                    members.stream().map(this.plugin.getServer()::getPlayer).filter(Objects::nonNull).forEach(member -> builder.append(CC.GRAY).append(member.getName()).append(","));

                    String[] information = new String[] {
                            CC.DARK_GRAY + CC.STRIKE_THROUGH + "----------------------------------------------------",
                            CC.PRIMARY + CC.BOLD + "Información de Party",
                            CC.SECONDARY + "Leader: " + CC.GRAY + this.plugin.getServer().getPlayer(party.getLeader()).getName(),
                            CC.PRIMARY + builder.toString(),
                            CC.SECONDARY + "Estado: " + CC.GRAY + (party.isOpen() ?
                                    CC.GREEN + "Abierta" :
                                    CC.RED + "Cerrada"),
                            CC.DARK_GRAY + CC.STRIKE_THROUGH + "----------------------------------------------------"
                    };

                    player.sendMessage(information);
                }
                break;
            case "list":
                if (party == null) {
                    player.sendMessage(CC.RED + "No estas en una party.");
                } else {
                    StringBuilder builder = new StringBuilder(CC.PRIMARY + "Tú party (" + party.getMembers().size() + "):\n");

                    List<UUID> members = new ArrayList<>(party.getMembers());

                    members.remove(party.getLeader());

                    builder.append(CC.SECONDARY).append("Líder: ").append(this.plugin.getServer().getPlayer(party.getLeader()).getName()).append("\n");

                    members.stream().map(this.plugin.getServer()::getPlayer).filter(Objects::nonNull).forEach(member -> builder.append(CC.AQUA).append(member.getName()).append("\n"));

                    player.sendMessage(builder.toString());
                }
                break;
            default:
                player.sendMessage(PartyCommand.HELP_MESSAGE);
                break;
        }
        return true;
    }
}
