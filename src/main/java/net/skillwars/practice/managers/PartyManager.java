package net.skillwars.practice.managers;

import net.skillwars.practice.Practice;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.ItemBuilder;
import net.skillwars.practice.util.ItemUtil;
import net.skillwars.practice.util.TtlHashMap;
import net.skillwars.practice.util.inventory.InventoryUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.skillwars.practice.player.PlayerState;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        player.sendMessage(ChatColor.YELLOW + "Has creado una Party.");
    }

    private void disbandParty(Party party, boolean tournament) {
        this.plugin.getInventoryManager().removeParty(party);
        this.parties.remove(party.getLeader());

        if(party.getBroadcastTask() != null){
            party.getBroadcastTask().cancel();
        }

        party.broadcast(ChatColor.YELLOW + "Tu Party ha sido disbandeada.");

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
            party.broadcast(ChatColor.RED + player.getName() + " se ha ido de la Party.");
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
            player.sendMessage(ChatColor.RED + "Este jugador esta en un Tournament.");
            return;
        }

        this.partyLeaders.put(player.getUniqueId(), leader);
        party.addMember(player.getUniqueId());
        this.plugin.getInventoryManager().updateParty(party);

        this.plugin.getPlayerManager().sendToSpawnAndResetNoTP(player);

        party.broadcast(ChatColor.GREEN + player.getName() + " ha entrado a la Party.");

        party.getMembers().forEach(member -> {
            Player target = Bukkit.getPlayer(member);
            player.showPlayer(target);
            target.showPlayer(player);
        });
    }

    public void openSettingsInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, CC.PRIMARY + "Settings Party");
        Party party = plugin.getPartyManager().getParty(player.getUniqueId());

        List<String> membersLimitLore = new ArrayList<>(CC.translate(Arrays.asList("&7&m------------------------",
                "&fDa click izquierdo o derecho",
                "&fpara que puedas insertar la",
                "&fcantidad de miembros en tu Party",
                "",
                " &e\u2022 &fMiembros: " + party.getLimit(),
                "&7&m------------------------")));
        ItemStack membersLimit = new ItemBuilder(ItemUtil.createItem(Material.PAPER, CC.translate("&b&lEditar limite de Miembros"))).lore(membersLimitLore).build();

        List<String> statusItemLores = new ArrayList<>(CC.translate(Arrays.asList("&7&m------------------------",
                "&fAbrir la Party para todo el publico",
                "&fpara que cualquiera pueda unirse",
                "",
                (party.isOpen() ? "&a\u2713 " : "  ") + "&fEnabled",
                (party.isOpen() ? "  " : "&c\u2718 ") + "&fDisabled",
                "&7&m------------------------")));

        ItemStack statusParty = new ItemBuilder(ItemUtil.createItem(Material.DOUBLE_PLANT, CC.translate("&b&lHacer Publica la Party"))).lore(statusItemLores).build();

        inv.setItem(2, statusParty);
        inv.setItem(6, new ItemBuilder(membersLimit).build());

        player.openInventory(inv);
    }
}