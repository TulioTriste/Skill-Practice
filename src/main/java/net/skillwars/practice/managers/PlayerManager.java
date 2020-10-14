package net.skillwars.practice.managers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import net.skillwars.practice.Practice;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.kit.PlayerKit;
import net.skillwars.practice.mongo.PracticeMongo;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.settings.item.ProfileOptionsItemState;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.Color;
import net.skillwars.practice.util.InventoryUtil;
import net.skillwars.practice.util.ItemUtil;
import net.skillwars.practice.util.PlayerUtil;
import net.skillwars.practice.util.timer.impl.EnderpearlTimer;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
	
    private final Practice plugin = Practice.getInstance();
    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    public void createPlayerData(Player player) {
        PlayerData data = new PlayerData(player.getUniqueId());

        this.playerData.put(data.getUniqueId(), data);
        this.loadData(data);
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

    public void loadData(PlayerData playerData) {
        playerData.setPlayerState(PlayerState.SPAWN);
        Player player = Bukkit.getPlayer(playerData.getUniqueId());

        Config config = new Config("/players/" + playerData.getUniqueId().toString(), this.plugin);
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
        ConfigurationSection playerDataSelection = config.getConfig().getConfigurationSection("playerdata");

        if(playerDataSelection != null){
            if(playerDataSelection.contains("settings")){
                playerData.getOptions().setDuelRequests(playerDataSelection.getBoolean("settings.duelRequests"));
                playerData.getOptions().setScoreboard(playerDataSelection.getBoolean("settings.scoreboard"));
                playerData.getOptions().setPartyInvites(playerDataSelection.getBoolean("settings.partyInvites"));
                playerData.getOptions().setPartyInvites(playerDataSelection.getBoolean("settings.spectators"));
                playerData.getOptions().setTime(Enum.valueOf(ProfileOptionsItemState.class, playerDataSelection.getString("settings.time")));

                if(playerData.getOptions().getTime() == ProfileOptionsItemState.DAY) {
                    playerData.getOptions().setTime(ProfileOptionsItemState.DAY);
                    player.performCommand("day");
                }
                else if(playerData.getOptions().getTime() == ProfileOptionsItemState.SUNSET) {
                    playerData.getOptions().setTime(ProfileOptionsItemState.SUNSET);
                    player.performCommand("sunset");
                }

                else if(playerData.getOptions().getTime() == ProfileOptionsItemState.NIGHT) {
                    playerData.getOptions().setTime(ProfileOptionsItemState.NIGHT);
                    player.performCommand("night");
                }
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

            if(statisticsDocument.containsKey(key)){
                ladderDocument = (Document)statisticsDocument.get(key);
            }else{
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

    public PlayerData getPlayerData(final UUID uuid) {
        return this.playerData.get(uuid);
    }

    public void giveLobbyItems(final Player player) {
        final boolean inParty = this.plugin.getPartyManager().getParty(player.getUniqueId()) != null;
        final boolean inTournament = this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null;
        final boolean inEvent = this.plugin.getEventManager().getEventPlaying(player) != null;
        final boolean isRematching = this.plugin.getMatchManager().isRematching(player.getUniqueId());
        ItemStack[] items =!Practice.getInstance().getMainConfig().getConfig().getBoolean("stats") ?  this.plugin.getItemManager().getSpawnItems2() : this.plugin.getItemManager().getSpawnItems();
        if (inTournament) {
            items = this.plugin.getItemManager().getTournamentItems();
        } else if (inEvent) {
            items = this.plugin.getItemManager().getEventItems();
        } else if (inParty) {
            items = this.plugin.getItemManager().getPartyItems();
        }
        player.getInventory().setContents(items);
        if(inParty){
            Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
            player.getInventory().setItem(7, ItemUtil.createItem(Material.BEACON, CC.YELLOW + Bukkit.getPlayer(party.getLeader()).getName() + "´s Party"));
        }
        if (isRematching && !inParty && !inTournament && !inEvent) {
            player.getInventory().setItem(3, ItemUtil.createItem(Material.EMERALD, ChatColor.BLUE.toString() + "Rematch"));
        }
        player.updateInventory();
    }

    public void sendToSpawnAndReset(final Player player) {
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
			}else{
				other.showPlayer(player);
			}
			if(!other.hasPermission("practice.visibility")){
				player.hidePlayer(other);
			}else{
				player.showPlayer(other);
			}
		});
		player.teleport(Practice.getInstance().getSpawnManager().getSpawnLocation().toBukkitLocation());
    }

    public void sendToSpawnAndResetNoTP(final Player player) {
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
}
