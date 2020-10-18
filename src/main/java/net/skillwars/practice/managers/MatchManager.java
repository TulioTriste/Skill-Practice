package net.skillwars.practice.managers;

import lombok.Getter;
import me.joeleoli.nucleus.util.TaskUtil;
import net.skillwars.practice.Practice;
import net.skillwars.practice.arena.Arena;
import net.skillwars.practice.event.match.MatchEndEvent;
import net.skillwars.practice.event.match.MatchStartEvent;
import net.skillwars.practice.inventory.InventorySnapshot;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.kit.PlayerKit;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.match.MatchRequest;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.match.MatchTeam;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.queue.QueueType;
import net.skillwars.practice.runnable.RematchRunnable;
import net.skillwars.practice.util.*;
import net.skillwars.practice.util.*;
import net.skillwars.practice.util.inventory.UtilItem;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_8_R3.EntityPlayer;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MatchManager {

    private final Map<UUID, Set<MatchRequest>> matchRequests = new TtlHashMap<>(TimeUnit.SECONDS, 30);
    private final Map<UUID, UUID> rematchUUIDs = new TtlHashMap<>(TimeUnit.SECONDS, 30);
    private final Map<UUID, UUID> rematchInventories = new TtlHashMap<>(TimeUnit.SECONDS, 30);
    private final Map<UUID, UUID> spectators = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Match> matches = new ConcurrentHashMap<>();

    private final Practice plugin = Practice.getInstance();

    public int getFighters() {
        int i = 0;
        for (Match match : this.matches.values()) {
            for (MatchTeam matchTeam : match.getTeams()) {
                i += matchTeam.getAlivePlayers().size();
            }
        }
        return i;
    }

    public int getFighters(String ladder, QueueType type) {
        return (int) this.matches.entrySet().stream().filter(match -> match.getValue().getType() == type)
                .filter(match -> match.getValue().getKit().getName().equals(ladder)).count();
    }

    public void createMatchRequest(Player requester, Player requested, Arena arena, String kitName, boolean party) {
        MatchRequest request = new MatchRequest(requester.getUniqueId(), requested.getUniqueId(), arena, kitName, party);

        this.matchRequests.computeIfAbsent(requested.getUniqueId(), k -> new HashSet<>()).add(request);
    }

    public MatchRequest getMatchRequest(UUID requester, UUID requested) {
        Set<MatchRequest> requests = this.matchRequests.get(requested);

        if (requests == null) {
            return null;
        }

        return requests.stream().filter(req -> req.getRequester().equals(requester)).findAny().orElse(null);
    }

    public MatchRequest getMatchRequest(UUID requester, UUID requested, String kitName) {
        Set<MatchRequest> requests = this.matchRequests.get(requested);

        if (requests == null) {
            return null;
        }
        return requests.stream().filter(req -> req.getRequester().equals(requester) && req.getKitName().equals
                (kitName))
                .findAny()
                .orElse(null);
    }

    public Match getMatch(PlayerData playerData) {
        return this.matches.get(playerData.getCurrentMatchID());
    }

    public Match getMatch(UUID uuid) {
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(uuid);
        return this.getMatch(playerData);
    }

    public Match getMatchFromUUID(UUID uuid) {
        return this.matches.get(uuid);
    }

    public Match getSpectatingMatch(UUID uuid) {
        return this.matches.get(this.spectators.get(uuid));
    }

    public void removeMatchRequests(UUID uuid) {
        this.matchRequests.remove(uuid);
    }

    public void createMatch(Match match) {
        this.matches.put(match.getMatchId(), match);
        this.plugin.getServer().getPluginManager().callEvent(new MatchStartEvent(match));
    }

    public void removeFighter(Player player, PlayerData playerData, boolean spectateDeath) {
        Match match = this.matches.get(playerData.getCurrentMatchID());

        Player killer = player.getKiller();

        MatchTeam entityTeam = match.getTeams().get(playerData.getTeamID());
        MatchTeam winningTeam = match.isFFA() ? entityTeam : match.getTeams().get(entityTeam.getTeamID() == 0 ? 1 : 0);

        if (match.getMatchState() == MatchState.ENDING) {
            return;
        }

        String deathMessage = CC.SECONDARY + player.getName() + CC.WHITE + " ha " +
                (killer != null ? "sido asesinado por " + CC.SECONDARY + killer.getName() + CC.WHITE :
                        "muerto") + "!";

        match.broadcast(deathMessage);

        if (match.isRedrover()) {
            if (match.getMatchState() != MatchState.SWITCHING) {
                Clickable inventories = new Clickable(CC.WHITE + "Inventarios: ");
                if (killer != null) {
                    InventorySnapshot snapshot = new InventorySnapshot(killer, match);
                    this.plugin.getInventoryManager().addSnapshot(snapshot);
                    inventories.add(CC.GREEN + killer.getName() + " ",
                            CC.WHITE + "Ver inventario",
                            "/inv " + snapshot.getSnapshotId());
                }
                InventorySnapshot snapshot = new InventorySnapshot(player, match);
                this.plugin.getInventoryManager().addSnapshot(snapshot);
                inventories.add(CC.RED + player.getName() + " ",
                        CC.WHITE + "Ver inventario",
                        "/inv " + snapshot.getSnapshotId());
                match.broadcast(inventories);
                match.setMatchState(MatchState.SWITCHING);
                match.setCountdown(4);
            }
        } else {
            match.addSnapshot(player);
        }

        entityTeam.killPlayer(player.getUniqueId());

        int remaining = entityTeam.getAlivePlayers().size();

        if (remaining != 0) {
            Set<Item> items = new HashSet<>();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    items.add(player.getWorld().dropItemNaturally(player.getLocation(), item, player));
                }
            }
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    items.add(player.getWorld().dropItemNaturally(player.getLocation(), item, player));
                }
            }
            this.plugin.getMatchManager().addDroppedItems(match, items);
        }

        if (spectateDeath) {
            this.addDeathSpectator(player, playerData, match);
        }

        if ((match.isFFA() && remaining == 1) || remaining == 0) {
            this.plugin.getServer().getPluginManager().callEvent(new MatchEndEvent(match, winningTeam, entityTeam));
        }
    }

    public void removeMatch(Match match) {
        this.matches.remove(match.getMatchId());
    }

    public void giveKits(Player player, Kit kit) {
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Collection<PlayerKit> playerKits = playerData.getPlayerKits(kit.getName()).values();

        if (playerKits.size() == 0) {
            kit.applyToPlayer(player);
        } else {
            player.getInventory().setItem(0,
                    UtilItem.createItem(Material.ENCHANTED_BOOK, CC.YELLOW + "Default " + kit.getName() + " kit"));
            int slot = 1;
            for (PlayerKit playerKit : playerKits) {
                player.getInventory().setItem(++slot,
                        ItemUtil.createItem(Material.ENCHANTED_BOOK, CC.PRIMARY + playerKit.getDisplayName()));
            }
            player.updateInventory();
        }
    }

    private void addDeathSpectator(Player player, PlayerData playerData, Match match) {
        this.spectators.put(player.getUniqueId(), match.getMatchId());

        playerData.setPlayerState(PlayerState.SPECTATING);

        PlayerUtil.clearPlayer(player);

        CraftPlayer playerCp = (CraftPlayer) player;
        EntityPlayer playerEp = playerCp.getHandle();

        player.spigot().respawn();

        match.addSpectator(player.getUniqueId());

        match.addRunnable(this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            match.getTeams().forEach(team -> team.alivePlayers().forEach(member -> {
                member.hidePlayer(player);
            }));

            match.spectatorPlayers().forEach(member -> member.hidePlayer(player));

            player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
            player.setWalkSpeed(0.2F);
            player.setAllowFlight(true);
        }, 20L));

        if (match.isRedrover()) {
            for (MatchTeam team : match.getTeams()) {
                for (UUID alivePlayerUUID : team.getAlivePlayers()) {
                    Player alivePlayer = this.plugin.getServer().getPlayer(alivePlayerUUID);

                    if (alivePlayer != null) {
                        player.showPlayer(alivePlayer);
                    }
                }
            }
        }

        player.setWalkSpeed(0.0F);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000, -5));
        //player.setVelocity(player.getLocation().getDirection().setY(1));

        TaskUtil.runLater(new BukkitRunnable(){
            @Override
            public void run(){
                if (match.isParty() || match.isFFA()) {
                    player.getInventory().setContents(plugin.getItemManager().getPartySpecItems());
                }
                player.updateInventory();
            }
        }, 3L);
    }

    public void addRedroverSpectator(Player player, Match match) {
        this.spectators.put(player.getUniqueId(), match.getMatchId());

        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().setContents(this.plugin.getItemManager().getPartySpecItems());
        player.updateInventory();

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        playerData.setPlayerState(PlayerState.SPECTATING);
    }

    public void addSpectator(Player player, PlayerData playerData, Player target, Match targetMatch) {
        this.spectators.put(player.getUniqueId(), targetMatch.getMatchId());

        if (targetMatch.getMatchState() != MatchState.ENDING) {
            if (!targetMatch.haveSpectated(player.getUniqueId())) {

                String spectatorMessage = ChatColor.WHITE.toString() + ChatColor.BOLD + "* " + ChatColor.GREEN + player.getName() + ChatColor.GRAY + " is spectating your match.";

                if(!player.hasPermission("practice.staff")) {
                    targetMatch.broadcast(spectatorMessage);
                }
            }
        }

        targetMatch.addSpectator(player.getUniqueId());

        playerData.setPlayerState(PlayerState.SPECTATING);

        player.teleport(target);
        player.setAllowFlight(true);
        player.setFlying(true);

        player.getInventory().setContents(this.plugin.getItemManager().getSpecItems());
        player.updateInventory();

        this.plugin.getServer().getOnlinePlayers().forEach(online -> {
            online.hidePlayer(player);
            player.hidePlayer(online);
        });
        targetMatch.getTeams().forEach(team -> team.alivePlayers().forEach(player::showPlayer));
    }

    public void addDroppedItem(Match match, Item item) {
        match.addEntityToRemove(item);
        match.addRunnable(this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            match.removeEntityToRemove(item);
            item.remove();
        }, 300L).getTaskId());
    }

    public void addDroppedItems(Match match, Set<Item> items) {
        for (Item item : items) {
            match.addEntityToRemove(item);
        }
        match.addRunnable(this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            for (Item item : items) {
                match.removeEntityToRemove(item);
                item.remove();
            }
        }, 300L).getTaskId());
    }

    public void removeSpectator(Player player) {
        Match match = this.matches.get(this.spectators.get(player.getUniqueId()));

        match.removeSpectator(player.getUniqueId());

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (match.getTeams().size() > playerData.getTeamID() && playerData.getTeamID() >= 0) {
            MatchTeam entityTeam = match.getTeams().get(playerData.getTeamID());
            //Kill the player if they are in a redrover.
            if (entityTeam != null) {
                entityTeam.killPlayer(player.getUniqueId());
            }
        }

        if (match.getMatchState() != MatchState.ENDING) {
            if (!match.haveSpectated(player.getUniqueId())) {
                match.getTeams().forEach(team -> team.alivePlayers().forEach(player2 -> {
                    if(!player.hasPermission("practice.staff")){
                        player2.sendMessage(CC.SECONDARY + player.getName() + CC.PRIMARY + " ya no esta Especteando.");
                    }
                }));
                match.addHaveSpectated(player.getUniqueId());
            }
        }

        this.spectators.remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }

    public void pickPlayer(Match match) {
        Player playerA = this.plugin.getServer().getPlayer(match.getTeams().get(0).getAlivePlayers().get(0));
        PlayerData playerDataA = this.plugin.getPlayerManager().getPlayerData(playerA.getUniqueId());
        if (playerDataA.getPlayerState() != PlayerState.FIGHTING) {
            playerA.teleport(match.getArena().getA().toBukkitLocation());
            PlayerUtil.clearPlayer(playerA);
            if (match.getKit().isCombo()) {
                playerA.setMaximumNoDamageTicks(3);
            }
            this.plugin.getMatchManager().giveKits(playerA, match.getKit());
            playerDataA.setPlayerState(PlayerState.FIGHTING);
        }
        Player playerB = this.plugin.getServer().getPlayer(match.getTeams().get(1).getAlivePlayers().get(0));
        PlayerData playerDataB = this.plugin.getPlayerManager().getPlayerData(playerB.getUniqueId());

        if (playerDataB.getPlayerState() != PlayerState.FIGHTING) {
            playerB.teleport(match.getArena().getB().toBukkitLocation());
            PlayerUtil.clearPlayer(playerB);
            if (match.getKit().isCombo()) {
                playerB.setMaximumNoDamageTicks(3);
            }
            this.plugin.getMatchManager().giveKits(playerB, match.getKit());
            playerDataB.setPlayerState(PlayerState.FIGHTING);
        }

        for (MatchTeam team : match.getTeams()) {
            for (UUID uuid : team.getAlivePlayers()) {
                Player player = this.plugin.getServer().getPlayer(uuid);

                if (player != null) {
                    if (!playerA.equals(player) && !playerB.equals(player)) {
                        playerA.hidePlayer(player);
                        playerB.hidePlayer(player);
                    }
                }
            }
        }
        playerA.showPlayer(playerB);
        playerB.showPlayer(playerA);

        match.broadcast(CC.SECONDARY + playerA.getName() + CC.PRIMARY + " vs. " + CC.SECONDARY + playerB.getName());
    }

    public void saveRematches(Match match) {
        if (match.isParty() || match.isFFA()) {
            return;
        }
        UUID playerOne = match.getTeams().get(0).getLeader();
        UUID playerTwo = match.getTeams().get(1).getLeader();

        PlayerData dataOne = this.plugin.getPlayerManager().getPlayerData(playerOne);
        PlayerData dataTwo = this.plugin.getPlayerManager().getPlayerData(playerTwo);

        if (dataOne != null) {
            this.rematchUUIDs.put(playerOne, playerTwo);
            InventorySnapshot snapshot = match.getSnapshot(playerTwo);
            if (snapshot != null) {
                this.rematchInventories.put(playerOne, snapshot.getSnapshotId());
            }
            if (dataOne.getRematchID() > -1) {
                this.plugin.getServer().getScheduler().cancelTask(dataOne.getRematchID());
            }
            dataOne.setRematchID(
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new RematchRunnable(playerOne), 20L * 30L));
        }
        if (dataTwo != null) {
            this.rematchUUIDs.put(playerTwo, playerOne);
            InventorySnapshot snapshot = match.getSnapshot(playerOne);
            if (snapshot != null) {
                this.rematchInventories.put(playerTwo, snapshot.getSnapshotId());
            }
            if (dataTwo.getRematchID() > -1) {
                this.plugin.getServer().getScheduler().cancelTask(dataTwo.getRematchID());
            }
            dataTwo.setRematchID(
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new RematchRunnable(playerTwo), 20L * 30L));
        }
    }

    public void removeRematch(UUID uuid) {
        this.rematchUUIDs.remove(uuid);
        this.rematchInventories.remove(uuid);
    }

    public List<UUID> getOpponents(Match match, Player player) {

        for(MatchTeam team : match.getTeams()) {

            if(team.getPlayers().contains(player.getUniqueId())) {
                continue;
            }

            return team.getPlayers();
        }

        return null;

    }

    public UUID getRematcher(UUID uuid) {
        return this.rematchUUIDs.get(uuid);
    }

    public UUID getRematcherInventory(UUID uuid) {
        return this.rematchInventories.get(uuid);
    }

    public boolean isRematching(UUID uuid) {
        return this.rematchUUIDs.containsKey(uuid);
    }

}
