package net.skillwars.practice.listeners;

import net.skillwars.practice.Practice;
import net.skillwars.practice.arena.Arena;
import net.skillwars.practice.arena.StandaloneArena;
import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.player.PlayerState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.skillwars.practice.match.MatchState;

import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldListener implements Listener {

    private final Practice plugin = Practice.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            if (event.getPlayer().getLocation().getBlockY() > event.getBlock().getLocation().getBlockY()) {
                event.getPlayer().teleport(event.getPlayer().getLocation());
                event.getPlayer().setVelocity(new Vector());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().setVelocity(new Vector(0, -0.25, 0));
                    }
                }.runTaskLaterAsynchronously(plugin, 4L);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            event.setCancelled(true);
            return;
        }
        PracticeEvent playerEvent = this.plugin.getEventManager().getEventPlaying(player);

        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (match.getKit().isBuild()) {
                if (!match.getPlacedBlockLocations().contains(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                }
            }
            else if (match.getKit().isSpleef()) {
                double minX = match.getArena().getMin().getX();
                double minZ = match.getArena().getMin().getZ();
                double maxX = match.getArena().getMax().getX();
                double maxZ = match.getArena().getMax().getZ();
                if (minX > maxX) {
                    double lastMinX = minX;
                    minX = maxX;
                    maxX = lastMinX;
                }

                if (minZ > maxZ) {
                    double lastMinZ = minZ;
                    minZ = maxZ;
                    maxZ = lastMinZ;
                }
                if (match.getMatchState() == MatchState.STARTING) {
                    event.setCancelled(true);
                    return;
                }
                if (player.getLocation().getX() >= minX && player.getLocation().getX() <= maxX
                        && player.getLocation().getZ() >= minZ && player.getLocation().getZ() <= maxZ) {
                    if (event.getBlock().getType() == Material.SNOW_BLOCK && player.getItemInHand().getType() == Material.DIAMOND_SPADE) {
                        Location blockLocation = event.getBlock().getLocation();

                        event.setCancelled(true);
                        match.addOriginalBlockChange(event.getBlock().getState());
                        Set<Item> items = new HashSet<>();
                        event.getBlock().getDrops().forEach(itemStack -> items.add(player.getWorld().dropItemNaturally(blockLocation.add(0.0D, 0.25D, 0.0D), itemStack)));
                        this.plugin.getMatchManager().addDroppedItems(match, items);
                        event.getBlock().setType(Material.AIR);
                    } else {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else {
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            event.setCancelled(true);
            return;
        }
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (!match.getKit().isBuild()) {
                event.setCancelled(true);
            } else {
                double minX = match.getArena().getMin().getX();
                double minZ = match.getArena().getMin().getZ();
                double maxX = match.getArena().getMax().getX();
                double maxZ = match.getArena().getMax().getZ();

                if (minX > maxX) {
                    double lastMinX = minX;
                    minX = maxX;
                    maxX = lastMinX;
                }

                if (minZ > maxZ) {
                    double lastMinZ = minZ;
                    minZ = maxZ;
                    maxZ = lastMinZ;
                }

                if (player.getLocation().getX() >= minX && player.getLocation().getX() <= maxX
                        && player.getLocation().getZ() >= minZ && player.getLocation().getZ() <= maxZ) {
                    if ((player.getLocation().getY() - match.getArena().getA().getY()) < 5.0D) {
                        match.addPlacedBlockLocation(event.getBlockPlaced().getLocation());
                    } else {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            this.plugin.getLogger().warning(player.getName() + "'s player data is null");
            event.setCancelled(true);
            return;
        }
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (!match.getKit().isBuild() && !match.getKit().isWaterdrop()) {
                event.setCancelled(true);
            } else {
                double minX = match.getArena().getMin().getX();
                double minZ = match.getArena().getMin().getZ();
                double maxX = match.getArena().getMax().getX();
                double maxZ = match.getArena().getMax().getZ();
                if (minX > maxX) {
                    double lastMinX = minX;
                    minX = maxX;
                    maxX = lastMinX;
                }

                if (minZ > maxZ) {
                    double lastMinZ = minZ;
                    minZ = maxZ;
                    maxZ = lastMinZ;
                }
                if (player.getLocation().getX() >= minX && player.getLocation().getX() <= maxX
                        && player.getLocation().getZ() >= minZ && player.getLocation().getZ() <= maxZ) {
                    if ((player.getLocation().getY() - match.getArena().getA().getY()) < 5.0D) {
                        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
                        match.addPlacedBlockLocation(block.getLocation());
                    } else {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
            return;
        }

        if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getToBlock() == null) {
            return;
        }

        for (Arena arena : this.plugin.getArenaManager().getArenaMatchUUIDs().keySet()) {
            double minX = arena.getMin().getX();
            double minZ = arena.getMin().getZ();
            double maxX = arena.getMax().getX();
            double maxZ = arena.getMax().getZ();
            if (minX > maxX) {
                double lastMinX = minX;
                minX = maxX;
                maxX = lastMinX;
            }

            if (minZ > maxZ) {
                double lastMinZ = minZ;
                minZ = maxZ;
                maxZ = lastMinZ;
            }

            Block b = event.getToBlock();
            if (b.getX() >= minX && b.getZ() >= minZ
                    && b.getX() <= maxX && b.getZ() <= maxZ) {
                UUID matchUUID = this.plugin.getArenaManager().getArenaMatchUUID(arena);
                Match match = this.plugin.getMatchManager().getMatchFromUUID(matchUUID);

                if (match.getKit().isWaterdrop()) {
                    event.setCancelled(true);
                    return;
                }

                    /*if (type == Material.WATER || type == Material.STATIONARY_WATER || type == Material.LAVA || type == Material.STATIONARY_LAVA) {
                        if (b.getType() == Material.AIR) {
                            if (generatesCobble(type, b)) {
                                Bukkit.getPlayer("TulioTriste").sendMessage("test");
                                match.addPlacedBlockLocation(b.getLocation());
                            }
                        }
                    }*/

                match.addPlacedBlockLocation(b.getLocation());
                break;
            }
        }
    }

    /*@EventHandler
    public void onFromTo(BlockFromToEvent event){
        Material type = event.getBlock().getType();
        if (type == Material.WATER || type == Material.STATIONARY_WATER || type == Material.LAVA || type == Material.STATIONARY_LAVA){
            Block b = event.getToBlock();
            if (b.getType() == Material.AIR) {
                if (generatesCobble(type, b)) {

                    event.getToBlock().setType(Material.COBBLESTONE);
                }
            }
        }
    }*/

    private final BlockFace[] faces = new BlockFace[]{
            BlockFace.SELF,
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    public boolean generatesCobble(Material type, Block b){
        Material mirrorID1 = (type == Material.WATER || type == Material.STATIONARY_WATER ? Material.LAVA : Material.WATER);
        Material mirrorID2 = (type == Material.WATER || type == Material.STATIONARY_WATER ? Material.STATIONARY_LAVA : Material.STATIONARY_WATER);
        for (BlockFace face : faces){
            Block r = b.getRelative(face, 1);
            if (r.getType() == mirrorID1 || r.getType() == mirrorID2){
                return true;
            }
        }
        return false;
    }

    /*@EventHandler
    public void onChunkLoad(PreChunkLoadEvent event) {
        if (this.plugin.getChunkManager().isChunksLoaded()
                && this.plugin.getArenaManager().getGeneratingArenaRunnables() == 0) {
            event.setCancelled(true);
        }
    }*/

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event){
        event.getWorld().getEntities().clear();
        event.getWorld().setDifficulty(Difficulty.HARD);
        event.getWorld().setStorm(false);
    }
}
