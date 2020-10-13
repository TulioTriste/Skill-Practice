package net.skillwars.practice.managers;

import net.skillwars.practice.Practice;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.ItemUtil;
import net.skillwars.practice.util.TtlHashMap;
import net.skillwars.practice.util.inventory.InventoryUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.skillwars.practice.player.PlayerState;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class PartyManager {

    private final Practice plugin = Practice.getInstance();

    private Map<UUID, List<UUID>> partyInvites = new TtlHashMap<>(TimeUnit.SECONDS, 15);
    private Map<UUID, Party> parties = new HashMap<>();
    private Map<UUID, UUID> partyLeaders = new HashMap<>();

    public boolean isLeader(UUID uuid) {
        return this.parties.containsKey(uuid);
    }

    public void removePartyInvites(UUID uuid) {
        this.partyInvites.remove(uuid);
    }

    public boolean hasPartyInvite(UUID player, UUID other) {
        return this.partyInvites.get(player) != null && this.partyInvites.get(player).contains(other);
    }

    public void createPartyInvite(UUID requester, UUID requested) {
        this.partyInvites.computeIfAbsent(requested, k -> new ArrayList<>()).add(requester);
    }

    public boolean isInParty(UUID player, Party party) {
        Party targetParty = this.getParty(player);
        return targetParty != null && targetParty.getLeader() == party.getLeader();
    }

    public Party getParty(UUID player) {
        if (this.parties.containsKey(player)) {
            return this.parties.get(player);
        }
        if (this.partyLeaders.containsKey(player)) {
            UUID leader = this.partyLeaders.get(player);
            return this.parties.get(leader);
        }
        return null;
    }

    public void createParty(Player player) {
        Party party = new Party(player.getUniqueId());


        this.parties.put(player.getUniqueId(), party);
        this.plugin.getInventoryManager().addParty(player);
        this.plugin.getPlayerManager().sendToSpawnAndResetNoTP(player);

        player.sendMessage(ChatColor.YELLOW + "You have created a party.");
    }

    private void disbandParty(Party party, boolean tournament) {
        this.plugin.getInventoryManager().removeParty(party);
        this.parties.remove(party.getLeader());

        if(party.getBroadcastTask() != null){
            party.getBroadcastTask().cancel();
        }

        party.broadcast(ChatColor.YELLOW + "Your party has been disbanded.");

        party.members().forEach(member -> {
            PlayerData memberData = this.plugin.getPlayerManager().getPlayerData(member.getUniqueId());

            if (this.partyLeaders.get(memberData.getUniqueId()) != null) {
                this.partyLeaders.remove(memberData.getUniqueId());
            }
            if (memberData.getPlayerState() == PlayerState.SPAWN) {
                this.plugin.getPlayerManager().sendToSpawnAndReset(member);
            }
        });
    }

    public void leaveParty(Player player) {
        Party party = this.getParty(player.getUniqueId());

        if (party == null) {
            return;
        }

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        if (this.parties.containsKey(player.getUniqueId())) {
            this.disbandParty(party, false);
        } else if (this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null) {
            this.disbandParty(party, true);
        } else {
            party.broadcast(ChatColor.RED.toString() + ChatColor.BOLD + "[-] " + ChatColor.YELLOW + player.getName() + " left the party.");
            party.removeMember(player.getUniqueId());

            this.partyLeaders.remove(player.getUniqueId());

            this.plugin.getInventoryManager().updateParty(party);
        }

        switch (playerData.getPlayerState()) {
            case FIGHTING:
                this.plugin.getMatchManager().removeFighter(player, playerData, false);
                break;
            case SPECTATING:
                if(this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                    this.plugin.getEventManager().removeSpectator(player);
                } else {
                    this.plugin.getMatchManager().removeSpectator(player);
                }
                break;
        }

        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }

    public void joinParty(UUID leader, Player player) {
        Party party = this.getParty(leader);

        if (this.plugin.getTournamentManager().getTournament(leader) != null) {
            player.sendMessage(ChatColor.RED + "That player is in a tournament.");
            return;
        }

        this.partyLeaders.put(player.getUniqueId(), leader);
        party.addMember(player.getUniqueId());
        this.plugin.getInventoryManager().updateParty(party);

        this.plugin.getPlayerManager().sendToSpawnAndResetNoTP(player);

        party.broadcast(ChatColor.GREEN.toString() + ChatColor.BOLD + "[+] " + ChatColor.YELLOW + player.getName() + " joined the party.");

        party.getMembers().forEach(member -> {
            Player target = Bukkit.getPlayer(member);
            player.showPlayer(target);
            target.showPlayer(player);
        });
    }

    public void openSettingsInventory(Player player) {
        InventoryUI inv = Practice.getInstance().getInventoryManager().getPartySettingsInventory();

        inv.setItem(2, new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.PAPER, CC.translate("&f&lEditar limite de Miembros"))) {

            @Override
            public void onClick(InventoryClickEvent event) {
                event.getWhoClicked().sendMessage("test");
            }
        });

        inv.setItem(6, new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.DOUBLE_PLANT, CC.translate(""))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.getWhoClicked().sendMessage("hola");
            }
        });

        player.openInventory(inv.getCurrentPage());
    }

}