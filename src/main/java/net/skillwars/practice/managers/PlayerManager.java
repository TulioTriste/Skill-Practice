package net.skillwars.practice.managers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.SneakyThrows;
import me.joansiitoh.skillcore.commands.admins.staff.StaffDatas;
import me.joansiitoh.skillcore.commands.admins.staff.StaffUtils;
import net.skillwars.practice.Practice;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.kit.PlayerKit;
import net.skillwars.practice.mongo.PracticeMongo;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.settings.item.ProfileOptionsItemState;
import net.skillwars.practice.util.*;
import net.skillwars.practice.util.timer.impl.EnderpearlTimer;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
	
    private final Practice plugin = Practice.getInstance();
    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    public void createPlayerData(UUID uuid, InetAddress ip) {
        PlayerData data = new PlayerData(uuid);

        this.playerData.put(data.getUniqueId(), data);
        this.loadData(data, ip);
    }

    /*private void loadData(final PlayerData playerData) {
        Config config = new Config("/players/" + playerData.getUniqueId().toString(), this.plugin);
        FileConfiguration fileConfig = config.getConfig();
        ConfigurationSection playerKitsSection = config.getConfig().getConfigurationSection("playerkits");

        if (playerKitsSection != null) {
            this.plugin.getKitManager().getKits().forEach((kit) -> {
                ConfigurationSection kitSection = playerKitsSection.getConfigurationSection(kit.getName());
                if (kitSection != null) {
                    kitSection.getKeys(false).forEach((kitKey) -> {
                        int kitIndex = Integer.parseInt(kitKey);
                        String displayName = kitSection.getString(kitKey + ".displayName");
                        ItemStack[] contents = InventoryUtil.deserializeInventory(kitSection.getString(kitKey + ".contents"));
                        PlayerKit playerKit = new PlayerKit(kit.getName(), kitIndex, contents, displayName);
                        playerData.addPlayerKit(kitIndex, playerKit);
                    });
                }

            });
        }

        ConfigurationSection playerDataSelection = fileConfig.getConfigurationSection("playerdata");

        if(playerDataSelection != null){
            if(playerDataSelection.getConfigurationSection("elo") != null){
                playerDataSelection.getConfigurationSection("elo").getKeys(false).forEach(kit -> {
                    int elo = playerDataSelection.getInt("elo." + kit);
                    playerData.setElo(kit, elo);
                });
            }
            if(playerDataSelection.getConfigurationSection("losses") != null){
                playerDataSelection.getConfigurationSection("losses").getKeys(false).forEach(kit -> {
                    int elo = playerDataSelection.getInt("losses." + kit);
                    playerData.setLosses(kit, elo);
                });
            }
            if(playerDataSelection.getConfigurationSection("wins") != null){
                playerDataSelection.getConfigurationSection("wins").getKeys(false).forEach(kit -> {
                    int elo = playerDataSelection.getInt("wins." + kit);
                    playerData.setWins(kit, elo);
                });
            }

            playerData.setSumoEventWins(playerDataSelection.getInt("sumoEventWins"));
            playerData.setSumoEventLosses(playerDataSelection.getInt("sumoEventLosses"));
            playerData.setUnrankedWins(playerDataSelection.getInt("unRankedWins"));


            if(playerDataSelection.contains("settings")){
                playerData.getOptions().setDuelRequests(playerDataSelection.getBoolean("settings.duelRequests"));
                playerData.getOptions().setScoreboard(playerDataSelection.getBoolean("settings.scoreboard"));
                playerData.getOptions().setPartyInvites(playerDataSelection.getBoolean("settings.partyInvites"));
                playerData.getOptions().setTime(Enum.valueOf(ProfileOptionsItemState.class, playerDataSelection.getString("settings.time")));
            }
        }

        playerData.setPlayerState(PlayerState.SPAWN);
    }*/

    public void loadData(PlayerData playerData, InetAddress ip) {
        playerData.setPlayerState(PlayerState.SPAWN);
        new BukkitRunnable() {
            @Override
            public void run() {
                playerData.setCountry(getCountry(ip));
            }
        }.runTaskAsynchronously(this.plugin);

        Config config = new Config("/players/" + playerData.getUniqueId().toString(), this.plugin);
        ConfigurationSection playerKitsSection = config.getConfig().getConfigurationSection("playerkits");
        ConfigurationSection matchs = config.getConfig().getConfigurationSection("match");

        /*if (matchs != null) {
            matchs.getKeys(false).forEach(kitName -> {
                playerData.setRankedWins(kitName, matchs.getInt("RankedWins." + kitName));
                playerData.setRankedLosses(kitName, matchs.getInt("RankedLosses." + kitName));
            });
        } else {
            this.plugin.getKitManager().getKits().forEach(kit -> {
                if (kit.isRanked()) {
                    config.getConfig().set("match." + kit.getName() + ".RankedWins", 0);
                    config.getConfig().set("match." + kit.getName() + ".RankedLosses", 0);
                    config.save();
                    config.reload();
                }
            });
        }*/

        if (playerKitsSection != null) {
            this.plugin.getKitManager().getKits().forEach((kit) -> {
                ConfigurationSection kitSection = playerKitsSection.getConfigurationSection(kit.getName());
                if (kitSection != null) {
                    kitSection.getKeys(false).forEach((kitKey) -> {
                        int kitIndex = Integer.parseInt(kitKey);
                        String displayName = kitSection.getString(kitKey + ".displayName");
                        ItemStack[] contents = InventoryUtil.deserializeInventory(kitSection.getString(kitKey + ".contents"));
                        PlayerKit playerKit = new PlayerKit(kit.getName(), kitIndex, contents, displayName);
                        playerData.addPlayerKit(kitIndex, playerKit);
                    });
                }

            });
        }
        ConfigurationSection playerDataSelection = config.getConfig().getConfigurationSection("playerdata");

        if(playerDataSelection != null){
            if(playerDataSelection.contains("settings")){
                playerData.getOptions().setDuelRequests(playerDataSelection.getBoolean("settings.duelRequests"));
                playerData.getOptions().setScoreboard(playerDataSelection.getBoolean("settings.scoreboard"));
                playerData.getOptions().setPartyInvites(playerDataSelection.getBoolean("settings.partyInvites"));
                playerData.getOptions().setPartyInvites(playerDataSelection.getBoolean("settings.spectators"));
                playerData.getOptions().setTime(Enum.valueOf(ProfileOptionsItemState.class, playerDataSelection.getString("settings.time")));
            }
        }

        Document document = PracticeMongo.getInstance().getPlayers().find(Filters.eq("uuid", playerData.getUniqueId().toString())).first();

        if (document == null) {
            this.saveData(playerData);
            return;
        }

        Document statisticsDocument = (Document) document.get("statistics");

        statisticsDocument.keySet().forEach(key -> {
            Document ladderDocument = (Document) statisticsDocument.get(key);

            if (ladderDocument.containsKey("ranked-elo")) {
                playerData.getRankedElo().put(key, ladderDocument.getInteger("ranked-elo"));
            }

            if (ladderDocument.containsKey("ranked-wins")) {
                playerData.getRankedWins().put(key, ladderDocument.getInteger("ranked-wins"));
            }

            if (ladderDocument.containsKey("ranked-losses")) {
                playerData.getRankedLosses().put(key, ladderDocument.getInteger("ranked-losses"));
            }
        });

        Document matchStatisticsDocument = (Document) document.get("statistics");
    }

    public void removePlayerData(final UUID uuid) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            this.saveData(this.playerData.get(uuid));
            this.playerData.remove(uuid);
        });
    }

    public void saveData(final PlayerData playerData) {
        /*
         * Saving player kits.
         */

        if(playerData == null) {
            return;
        }

        Config config = new Config("/players/" + playerData.getUniqueId().toString(), this.plugin);

        Document document = new Document();
        Document statisticsDocument = new Document();

        playerData.getRankedWins().forEach((key, value) -> {
            Document ladderDocument;

            if(statisticsDocument.containsKey(key)){
                ladderDocument = (Document)statisticsDocument.get(key);
            }else{
                ladderDocument = new Document();
            }

            ladderDocument.put("ranked-wins", value);
            statisticsDocument.put(key, ladderDocument);
        });

        playerData.getRankedLosses().forEach((key, value) -> {
            Document ladderDocument;

            if(statisticsDocument.containsKey(key)){
                ladderDocument = (Document)statisticsDocument.get(key);
            }else{
                ladderDocument = new Document();
            }

            ladderDocument.put("ranked-losses", value);
            statisticsDocument.put(key, ladderDocument);
        });

        playerData.getRankedElo().forEach((key, value) -> {
            Document ladderDocument;

            if (statisticsDocument.containsKey(key)) {
                ladderDocument = (Document)statisticsDocument.get(key);
            } else {
                ladderDocument = new Document();
            }

            ladderDocument.put("ranked-elo", value);
            statisticsDocument.put(key, ladderDocument);
        });

        document.put("uuid", playerData.getUniqueId().toString());
        document.put("statistics", statisticsDocument);

        PracticeMongo.getInstance().getPlayers().replaceOne(Filters.eq("uuid", playerData.getUniqueId().toString()), document, new ReplaceOptions().upsert(true));

        this.plugin.getKitManager().getKits().forEach(kit -> {
            Map<Integer, PlayerKit> playerKits = playerData.getPlayerKits(kit.getName());

            if (playerKits != null) {
                playerKits.forEach((key, value) -> {
                    config.getConfig().set("playerkits." + kit.getName() + "." + key + ".displayName", value.getDisplayName());
                    config.getConfig().set("playerkits." + kit.getName() + "." + key + ".contents", InventoryUtil.serializeInventory(value.getContents()));
                });

                config.getConfig().set("playerdata.settings.duelRequests", playerData.getOptions().isDuelRequests());
                config.getConfig().set("playerdata.settings.scoreboard", playerData.getOptions().isScoreboard());
                config.getConfig().set("playerdata.settings.partyInvites", playerData.getOptions().isPartyInvites());
                config.getConfig().set("playerdata.settings.spectators", playerData.getOptions().isSpectators());
                config.getConfig().set("playerdata.settings.time", playerData.getOptions().getTime().name());
            }
        });

        config.save();
    }

    public Collection<PlayerData> getAllData() {
        return this.playerData.values();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return this.playerData.get(uuid);
    }

    public void giveLobbyItems(final Player player) {
        final boolean inParty = this.plugin.getPartyManager().getParty(player.getUniqueId()) != null;
        final boolean inTournament = this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null;
        final boolean inEvent = this.plugin.getEventManager().getEventPlaying(player) != null;
        final boolean isRematching = this.plugin.getMatchManager().isRematching(player.getUniqueId());
        final boolean isStaff = StaffDatas.staffs.contains(player);
        ItemStack[] items = !Practice.getInstance().getMainConfig().getConfig().getBoolean("stats") ?
                this.plugin.getItemManager().getSpawnItems2() : this.plugin.getItemManager().getSpawnItems();

        if (isStaff) {
            StaffUtils.joinStaff(player);
            return;
        }
        else if (inTournament) {
            items = this.plugin.getItemManager().getTournamentItems();
        }
        else if (inEvent) {
            items = this.plugin.getItemManager().getEventItems();
        }
        else if (inParty) {
            items = this.plugin.getItemManager().getPartyItems();
        }
        player.getInventory().setContents(items);
        if (isRematching && !inParty && !inTournament && !inEvent && !isStaff) {
            player.getInventory().setItem(3, ItemUtil.createItem(Material.EMERALD, ChatColor.BLUE.toString() + "Rematch"));
        }
        player.updateInventory();
    }

    public void sendToSpawnAndReset(Player player) {
        PlayerData playerData = this.getPlayerData(player.getUniqueId());
        if (Practice.getInstance().getSpawnManager().getSpawnLocation() == null) {
        	player.sendMessage(Color.translate("&cPlease set a spawn location!"));
        	return;
        }
        playerData.setPlayerState(PlayerState.SPAWN);
        PlayerUtil.clearPlayer(player);
		plugin.getTimerManager().getTimer(EnderpearlTimer.class).clearCooldown(player.getUniqueId());
		giveLobbyItems(player);
		if (Bukkit.getOnlinePlayers().size() > 1) {
            Bukkit.getOnlinePlayers().forEach(other -> {
                if (!player.hasPermission("practice.visibility")) {
                    other.hidePlayer(player);
                } else {
                    other.showPlayer(player);
                }
                if (!other.hasPermission("practice.visibility")) {
                    player.hidePlayer(other);
                } else {
                    player.showPlayer(other);
                }
            });
        }
		player.teleport(Practice.getInstance().getSpawnManager().getSpawnLocation().toBukkitLocation());
    }

    public void sendToSpawnAndResetNoTP(Player player) {
        final PlayerData playerData = this.getPlayerData(player.getUniqueId());
        if (!player.isOnline()) {
            return;
        }
        if (Practice.getInstance().getSpawnManager().getSpawnLocation() == null) {
        	player.sendMessage(Color.translate("&cPlease set a spawn location!"));
        	return;
        }
        playerData.setPlayerState(PlayerState.SPAWN);
        PlayerUtil.clearPlayer(player);
		plugin.getTimerManager().getTimer(EnderpearlTimer.class).clearCooldown(player.getUniqueId());
		giveLobbyItems(player);
		Bukkit.getOnlinePlayers().forEach(other -> {
			if(!player.hasPermission("practice.visibility")) {
				other.hidePlayer(player);
			}
			if(!other.hasPermission("practice.visibility")){
				player.hidePlayer(other);
			}
		});
		if(!player.getLocation().getWorld().getName()
				.equals(plugin.getSpawnManager().getSpawnLocation().toBukkitLocation().getWorld().getName())){
			player.teleport(plugin.getSpawnManager().getSpawnLocation().toBukkitLocation());
		}
    }

    @SneakyThrows
    public static String getCountry(InetAddress ip) {
        URL url = new URL("http://ip-api.com/json/" + ip.getHostAddress());
        BufferedReader stream = new BufferedReader(new InputStreamReader(
                url.openStream()));
        StringBuilder entirePage = new StringBuilder();
        String inputLine;
        while ((inputLine = stream.readLine()) != null)
            entirePage.append(inputLine);
        stream.close();
        if(!(entirePage.toString().contains("\"country\":\"")))
            return null;
        return entirePage.toString().split("\"country\":\"")[1].split("\",")[0];
    }
}
