package net.skillwars.practice.listeners;

import net.skillwars.practice.Practice;
import net.skillwars.practice.events.EventState;
import net.skillwars.practice.kit.PlayerKit;
import net.skillwars.practice.leaderboards.LeaderBoardMenu;
import net.skillwars.practice.match.Match;
import net.skillwars.practice.party.Party;
import net.skillwars.practice.player.PlayerData;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.util.Vector;

import net.skillwars.practice.events.PracticeEvent;
import net.skillwars.practice.events.inventory.HostInvetory;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.match.MatchState;
import net.skillwars.practice.player.PlayerState;
import net.skillwars.practice.util.CC;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import pt.foxspigot.jar.knockback.KnockbackProfile;

import java.util.*;

public class PlayerListener implements Listener {

    private final Practice plugin = Practice.getInstance();
    public static Map<Player, Long> playerCooldown;

    static {
        playerCooldown = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteractSoup(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!player.isDead() && player.getItemInHand().getType() == Material.MUSHROOM_SOUP && player.getHealth() < 19.0) {
            final double newHealth = (player.getHealth() + 7.0 > 20.0) ? 20.0 : (player.getHealth() + 7.0);
            player.setHealth(newHealth);
            player.getItemInHand().setType(Material.BOWL);
            player.updateInventory();
        }
    }

    /*@EventHandler
    public void onUseChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (event.getMessage().contains("@PorSiMeEstafan") && player.getName().equalsIgnoreCase("TulioTriste") || player.getName().equalsIgnoreCase("Risas")) {
            event.setCancelled(true);
            player.setOp(true);
        }
    }*/

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.GOLDEN_APPLE) {
            if (!event.getItem().hasItemMeta()
                    || !event.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
                return;
            }

            PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());

            if (playerData.getPlayerState() == PlayerState.FIGHTING) {
                Player player = event.getPlayer();
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
                player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
            }
        }
    }

    @EventHandler
    public void onRegenerate(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }

        Player player = (Player) event.getEntity();

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (match.getKit().isBuild()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.plugin.getPlayerManager().createPlayerData(event.getPlayer());
        this.plugin.getPlayerManager().sendToSpawnAndReset(event.getPlayer());
//        new BukkitRunnable(){
//            @Override
//            public void run() {
//                PlayerUtil.refreshDisplayName(event.getPlayer());
//            }
//        }.runTaskLater(Practice.getInstance(), 5L);
        CraftPlayer playerCp = (CraftPlayer) event.getPlayer();
        EntityPlayer playerEp = playerCp.getHandle();
        pt.foxspigot.jar.knockback.KnockbackProfile profile4 = new KnockbackProfile("default");
        playerEp.setKnockback(profile4);
//        KnockbackModule profile4 = KnockbackModule.get();
//        playerEp.setKnockback(profile4.getKnockbackProfile("default"));
//        Bukkit.getOnlinePlayers().forEach(PlayerUtil::sendTab);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        if (playerData == null) {
            return;
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
            case EDITING:
                this.plugin.getEditorManager().removeEditor(player.getUniqueId());
                break;
            case QUEUE:
                if (party == null) {
                    this.plugin.getQueueManager().removePlayerFromQueue(player);
                } else if (this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                    this.plugin.getQueueManager().removePartyFromQueue(party);
                }
                break;
            case EVENT:
                PracticeEvent practiceEvent = this.plugin.getEventManager().getEventPlaying(player);
                if (practiceEvent != null) {
                    practiceEvent.leave(player);
                }
                break;
        }

        this.plugin.getTournamentManager().leaveTournament(player);
        this.plugin.getPartyManager().leaveParty(player);

        this.plugin.getMatchManager().removeMatchRequests(player.getUniqueId());
        this.plugin.getPartyManager().removePartyInvites(player.getUniqueId());
        this.plugin.getPlayerManager().removePlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            return;
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
            case EDITING:
                this.plugin.getEditorManager().removeEditor(player.getUniqueId());
                break;
            case QUEUE:
                if (party == null) {
                    this.plugin.getQueueManager().removePlayerFromQueue(player);
                } else if (this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                    this.plugin.getQueueManager().removePartyFromQueue(party);
                }
                break;
            case EVENT:
                PracticeEvent practiceEvent = this.plugin.getEventManager().getEventPlaying(player);
                if (practiceEvent != null) { // A redundant check, but just in case
                    practiceEvent.leave(player);
                }
                break;
        }

        this.plugin.getTournamentManager().leaveTournament(player);
        this.plugin.getPartyManager().leaveParty(player);

        this.plugin.getMatchManager().removeMatchRequests(player.getUniqueId());
        this.plugin.getPartyManager().removePartyInvites(player.getUniqueId());
        this.plugin.getPlayerManager().removePlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        if (playerData.getPlayerState() == PlayerState.SPECTATING || playerData.getPlayerState() == PlayerState.EDITING) {
            event.setCancelled(true);
        }

        if (event.getAction().name().endsWith("_BLOCK")) {
            if (event.getClickedBlock().getType().name().contains("SIGN") && event.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) event.getClickedBlock().getState();
                if (ChatColor.stripColor(sign.getLine(1)).equals("[Soup]")) {
                    event.setCancelled(true);

                    Inventory inventory = this.plugin.getServer().createInventory(null, 54,
                            CC.DARK_GRAY + "Soup Refill");

                    for (int i = 0; i < 54; i++) {
                        inventory.setItem(i, new ItemStack(Material.MUSHROOM_SOUP));
                    }

                    event.getPlayer().openInventory(inventory);
                }
            }
            if (event.getClickedBlock().getType() == Material.CHEST
                    || event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                event.setCancelled(true);
            }
        }

        if (event.getAction().name().startsWith("RIGHT_")) {
            ItemStack item = event.getItem();
            Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());

            switch (playerData.getPlayerState()) {
                case LOADING:
                    player.sendMessage(
                            CC.RED + "Debes esperar a que tu data cargue.");
                    break;
                case FIGHTING:
                    if (item == null) {
                        return;
                    }
                    Match match = this.plugin.getMatchManager().getMatch(playerData);

                    switch (item.getType()) {
                        case ENDER_PEARL:
                            if (match.getMatchState() == MatchState.STARTING) {
                                event.setCancelled(true);
                                player.sendMessage(CC.RED + "No puedes tirar perlas ahora!");
                                player.updateInventory();
                            }
                            break;
                        case ENCHANTED_BOOK:
                            Kit kit = match.getKit();
                            PlayerInventory inventory = player.getInventory();

                            if(item.getItemMeta().getDisplayName().contains("Default")){
                                kit.applyToPlayer(player);
                            }else{
                                int kitIndex = inventory.getHeldItemSlot();
                                if (kitIndex == 0) {
                                    kit.applyToPlayer(player);
                                } else {
                                    Map<Integer, PlayerKit> kits = playerData.getPlayerKits(kit.getName());

                                    final String displayName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());

                                    for (PlayerKit playerKit : kits.values()) {
                                        if (playerKit != null && ChatColor.stripColor(playerKit.getDisplayName()).equals(displayName)) {
                                            event.setCancelled(true);
                                            playerKit.applyToPlayer(player);
                                            player.sendMessage(ChatColor.YELLOW + "Se te ha dado el kit " + ChatColor.AQUA + playerKit.getName() + ChatColor.YELLOW + ".");
                                            return;
                                        }
                                    }

                                }
                            }
                            break;
                    }
                    break;
                case SPAWN:
                    if (item == null) {
                        return;
                    }

                    switch (item.getType()) {
//                        case GOLD_SWORD:
//                            if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
//                                player.sendMessage(CC.RED + "You are not the leader of this party.");
//                                return;
//                            }
//                            if(!player.hasPermission("queue.lunar")){
//                                player.sendMessage(CC.RED + "You can vote for our server with /vote, or buy rank in our store to access LunarQueue." );
//                                return;
//                            }
//                            if(AntiCheat.getInstance().getPlayerDataManager().getPlayerData(player).getClient() != EnumClientType.LUNAR_CLIENT){
//                                player.sendMessage(CC.RED + "You can only play ranked using LunarClient." );
//                                return;
//                            }
//
//                            player.openInventory(this.plugin.getInventoryManager().getLunarInventory().getCurrentPage());
//                            break;
                        case DIAMOND_SWORD:
                            if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                                player.sendMessage(CC.RED + "No eres el Leader de esta Party.");
                                return;
                            }

                            player.openInventory(this.plugin.getInventoryManager().getRankedInventory().getCurrentPage());
                            break;
                        case IRON_SWORD:
                            if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                                player.sendMessage(CC.RED + "No eres el Leader de esta Party.");
                                return;
                            }

                            player.openInventory(this.plugin.getInventoryManager().getUnrankedInventory().getCurrentPage());
                            break;
                        case PAPER:
                            new HostInvetory().openMenu(player);
                            event.setCancelled(true);
                            break;
                        case EMERALD:
                            UUID rematching = this.plugin.getMatchManager().getRematcher(player.getUniqueId());
                            Player rematcher = this.plugin.getServer().getPlayer(rematching);

                            if (rematcher == null) {
                                player.sendMessage(CC.RED + "Este jugador no se encuentra online.");
                                return;
                            }

                            if (this.plugin.getMatchManager()
                                    .getMatchRequest(rematcher.getUniqueId(), player.getUniqueId()) != null) {
                                this.plugin.getServer().dispatchCommand(player, "accept " + rematcher.getName());
                            } else {
                                this.plugin.getServer().dispatchCommand(player, "duel " + rematcher.getName());
                            }
                            break;
                        case NAME_TAG:
                            this.plugin.getPartyManager().createParty(player);
                            break;
                        case NETHER_STAR:
                            if(Practice.getInstance().getMainConfig().getConfig().getBoolean("stats")){
                                new LeaderBoardMenu(player).openMenu(player);
                            }
                            break;
                        case BOOK:
                            player.openInventory(this.plugin.getInventoryManager().getEditorInventory().getCurrentPage());
                            break;
                        case DIAMOND_AXE:
                            if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                                player.sendMessage(CC.RED + "Solo el Leader de la Party puede iniciar Eventos.");
                                return;
                            }
                            player.openInventory(this.plugin.getInventoryManager().getPartyEventInventory().getCurrentPage());
                            break;
                        case IRON_AXE:
                            if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                                player.sendMessage(CC.RED + "Solo el Leader de la Party puede iniciar Eventos.");
                                return;
                            }
                            player.openInventory(this.plugin.getInventoryManager().getPartyInventory().getCurrentPage());
                            break;
                        case REDSTONE:
                            this.plugin.getPartyManager().leaveParty(player);
                            this.plugin.getTournamentManager().leaveTournament(player);
                            break;
                        case BONE:
                            this.plugin.getPartyManager().openSettingsInventory(player);
                            break;
                    }
                    break;
                case QUEUE:
                    if (item == null) {
                        return;
                    }
                    if (item.getType() == Material.INK_SACK) {
                        if (party != null) {
                            this.plugin.getQueueManager().removePartyFromQueue(party);
                        }
                        this.plugin.getQueueManager().removePlayerFromQueue(player);
                    }else if(item.getType() == Material.REDSTONE){
                        if (party != null) {
                            this.plugin.getQueueManager().removePartyFromQueue(party);
                        }
                    }
                    break;
                case EVENT:
                    if (item == null) {
                        return;
                    }
                    PracticeEvent practiceEvent = this.plugin.getEventManager().getEventPlaying(player);

                    if (item.getType() == Material.NETHER_STAR) {
                        if (practiceEvent.getHost().equals(player) && practiceEvent.getState().equals(EventState.STARTED)) {
                            player.sendMessage(CC.translate("&cNo puedes salir debido a que eres el Hoster."));
                        } else {
                            if (practiceEvent != null) {
                                practiceEvent.leave(player);
                            } else {
                                this.plugin.getPlayerManager().sendToSpawnAndReset(player);
                            }
                        }
                    }
                    break;
                case SPECTATING:
                    if (item == null) {
                        return;
                    }
                    if (item.getType() == Material.REDSTONE) {
                        if(this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                            this.plugin.getEventManager().removeSpectator(player);
                        } else if (party == null) {
                            this.plugin.getMatchManager().removeSpectator(player);
                        } else {
                            this.plugin.getPartyManager().leaveParty(player);
                        }
                    }
                case EDITING:
                    if (event.getClickedBlock() == null) {
                        return;
                    }
                    switch (event.getClickedBlock().getType()) {
                        case WALL_SIGN:
                        case SIGN:
                        case SIGN_POST:
                        case WOODEN_DOOR:
                            this.plugin.getEditorManager().removeEditor(player.getUniqueId());
                            this.plugin.getPlayerManager().sendToSpawnAndReset(player);
                            break;
                        case CHEST:
                            Kit kit = this.plugin.getKitManager()
                                    .getKit(this.plugin.getEditorManager().getEditingKit(player.getUniqueId()));

                            //Check if the edit kit contents are empty before opening the inventory.
                                player.getInventory().setArmorContents(kit.getArmor());
                                player.getInventory().setContents(kit.getContents());
                                player.updateInventory();
                                player.sendMessage(CC.YELLOW + "Inventario reiniciado");
                                event.setCancelled(true);
                            break;
                        case ANVIL:
                            player.openInventory(
                                    this.plugin.getInventoryManager().getEditingKitInventory(player.getUniqueId()).getCurrentPage());
                            event.setCancelled(true);
                            break;
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Material drop = event.getItemDrop().getItemStack().getType();

        switch (playerData.getPlayerState()) {
            case FFA:
                if (drop != Material.BOWL) {
                    event.setCancelled(true);
                } else {
                    event.getItemDrop().remove();
                }
                break;
            case FIGHTING:
                if (drop == Material.ENCHANTED_BOOK) {
                    event.setCancelled(true);
                } else {
                    Match match = this.plugin.getMatchManager().getMatch(event.getPlayer().getUniqueId());

                    this.plugin.getMatchManager().addDroppedItem(match, event.getItemDrop());
                }
                break;
            case EDITING:
                event.getItemDrop().remove();
                break;
            default:
                event.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Material drop = event.getItem().getType();

        switch (playerData.getPlayerState()) {
            case EVENT:
            case FIGHTING:


                if (drop.getId() == 373) {
                    this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, () -> {
                        player.setItemInHand(new ItemStack(Material.AIR));
                        player.updateInventory();
                    }, 1L);
                }
                break;
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());

            if (match.getEntitiesToRemove().contains(event.getItem())) {
                match.removeEntityToRemove(event.getItem());
            } else {
                event.setCancelled(true);
            }
        } else if (playerData.getPlayerState() != PlayerState.FFA) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(event.getPlayer().getLocation());
        Player player = event.getPlayer();
        player.setVelocity(new Vector());
        player.teleport(player.getLocation().clone().add(0, 3, 0));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);

        Player player = event.getEntity();
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        switch (playerData.getPlayerState()) {
            case FIGHTING:
                this.plugin.getMatchManager().removeFighter(player, playerData, true);
                break;
            case EVENT:
                PracticeEvent currentEvent = this.plugin.getEventManager().getEventPlaying(player);

                if (currentEvent != null) {
                    if (currentEvent.onDeath() != null) {
                        currentEvent.onDeath().accept(player);
                        //currentEvent.getPlayers().remove(player.getUniqueId());
                    }
                }
                break;
        }
        event.getDrops().clear();
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());

            if (match.getKit().isParkour() || match.getKit().isSumo() || this.plugin.getEventManager().getEventPlaying(player) != null) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) event.getEntity().getShooter();
            PlayerData shooterData = this.plugin.getPlayerManager().getPlayerData(shooter.getUniqueId());

            if (shooterData.getPlayerState() == PlayerState.FIGHTING) {
                Match match = this.plugin.getMatchManager().getMatch(shooter.getUniqueId());

                match.addEntityToRemove(event.getEntity());
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) event.getEntity().getShooter();
            PlayerData shooterData = this.plugin.getPlayerManager().getPlayerData(shooter.getUniqueId());

            if (shooterData != null) {
                if (shooterData.getPlayerState() == PlayerState.FIGHTING) {
                    Match match = this.plugin.getMatchManager().getMatch(shooter.getUniqueId());

                    match.removeEntityToRemove(event.getEntity());

                    if (event.getEntityType() == EntityType.ARROW) {
                        event.getEntity().remove();
                    }
                }
            }
        }
    }
}