package net.skillwars.practice;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.joeleoli.frame.Frame;
import me.joeleoli.nucleus.command.CommandHandler;
import me.joeleoli.nucleus.config.FileConfig;
import me.joeleoli.nucleus.tablist.SevenTab;
import net.milkbowl.vault.chat.Chat;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.skillwars.practice.adapters.TablistAdapter;
import net.skillwars.practice.arena.Arena;
import net.skillwars.practice.adapters.PracticeAdapter;
import net.skillwars.practice.cache.StatusCache;
import net.skillwars.practice.chat.ChatManager;
import net.skillwars.practice.chat.PracticeChat;
import net.skillwars.practice.commands.*;
import net.skillwars.practice.commands.arena.param.ArenaParameterType;
import net.skillwars.practice.commands.duel.AcceptCommand;
import net.skillwars.practice.commands.duel.CancelRankedCommand;
import net.skillwars.practice.commands.duel.DuelCommand;
import net.skillwars.practice.commands.duel.SpectateCommand;
import net.skillwars.practice.commands.elo.EloManagerCommand;
import net.skillwars.practice.commands.elo.LeaderboardCommand;
import net.skillwars.practice.commands.event.*;
import net.skillwars.practice.commands.management.*;
import net.skillwars.practice.commands.time.DayCommand;
import net.skillwars.practice.commands.time.NightCommand;
import net.skillwars.practice.commands.time.SunsetCommand;
import net.skillwars.practice.commands.toggle.EventModeCommand;
import net.skillwars.practice.commands.toggle.SettingsCommand;
import net.skillwars.practice.commands.warp.SpawnCommand;
import net.skillwars.practice.handler.CustomMovementHandler;
import net.skillwars.practice.leaderboards.LeaderboardManager;
import net.skillwars.practice.listeners.*;
import net.skillwars.practice.managers.*;
import net.skillwars.practice.mongo.PracticeMongo;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.runnable.ExpBarRunnable;
import net.skillwars.practice.runnable.SaveDataRunnable;
import net.skillwars.practice.settings.ProfileOptionsListeners;
import net.skillwars.practice.util.inventory.UIListener;
import net.skillwars.practice.util.timer.TimerManager;
import net.skillwars.practice.util.timer.impl.EnderpearlTimer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.util.*;

import lombok.Getter;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import pt.foxspigot.jar.FoxSpigot;

@Getter
public class Practice extends JavaPlugin implements PluginMessageListener {

    @Getter
    private static Practice instance;

    private FileConfig mainConfig;

    private Chat chat;

    private InventoryManager inventoryManager;
    private EditorManager editorManager;
    private PlayerManager playerManager;
    private ArenaManager arenaManager;
    private MatchManager matchManager;
    private PartyManager partyManager;
    private QueueManager queueManager;
    private EventManager eventManager;
    private ItemManager itemManager;
    private KitManager kitManager;
    private SpawnManager spawnManager;
    private TournamentManager tournamentManager;
    private ChunkManager chunkManager;
    private TimerManager timerManager;
    private LeaderboardManager leaderboardManager;
    private ChatManager chatManager;
    private ServerManager serverManager;

    @Override
    public void onDisable() {
        matchManager.getMatches().forEach((uuid, match)->{
            if (match.getKit().isBuild() || match.getKit().isSpleef() || match.getKit().isWaterdrop()) {
                for (Location location : match.getPlacedBlockLocations()) {
                    location.getBlock().setType(Material.AIR);
                    match.removePlacedBlockLocation(location);
                }
            } else {
                for (BlockState blockState : match.getOriginalBlockChanges()) {
                    blockState.setType(blockState.getType());
                    blockState.update();
                    blockState.update(true);
                    blockState.update(true, false);
                    match.removeOriginalBlockChange(blockState);
                }
            }
        });

        for (PlayerData playerData : playerManager.getAllData()) {
            playerManager.saveData(playerData);
        }

        arenaManager.saveArenas();
        kitManager.saveKits();
        spawnManager.saveConfig();
    }

    @Override
    public void onEnable() {
        Practice.instance = this;

        mainConfig = new FileConfig(this, "config.yml");

        new PracticeMongo();
        //new SevenTab(this, new TablistAdapter());

        FoxSpigot.INSTANCE.addMovementHandler(new CustomMovementHandler());

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        this.chatManager = new ChatManager();
        this.loadVault();

        registerCommands();
        registerListeners();
        registerManagers();
        
        if(Bukkit.getWorld("events") == null) {
        	WorldCreator creator = new WorldCreator("events");
        	creator.generator(new ChunkGenerator() {
        	    @Override
        	    public byte[] generate(World world, Random random, int x, int z) {
        	        return new byte[32768]; //Empty byte array
        	    }
        	});
        	Bukkit.createWorld(creator);
        }

        getServer().getScheduler().runTaskTimerAsynchronously(this, new SaveDataRunnable(),
                20L * 60L * 5L, 20L * 60L * 5L);

        getServer().getScheduler().runTaskTimerAsynchronously(this, new ExpBarRunnable(), 2L, 2L);

        new StatusCache().start();
        new Frame(this, new PracticeAdapter());
        this.chatManager.setChatFormat(new PracticeChat());
    }

    private void registerCommands() {

        CommandHandler.registerParameterType(Arena.class, new ArenaParameterType());

        CommandHandler.loadCommandsFromPackage(this, "net.skillwars.practice.commands.arena");
        Arrays.asList(
                new ResetStatsCommand(),
                new JoinEventCommand(),
                new LeaveEventCommand(),
                new StatusEventCommand(),
                new HostCommand(),
                new EventManagerCommand(),
                new AcceptCommand(),
                new ArenaCommand(),
                new FlyCommand(),
                new PartyCommand(),
                new DuelCommand(),
                new SpectateCommand(),
                new KitCommand(),
                new StatsCommand(),
                new SpectateEventCommand(),
                new InvCommand(),
                new SpawnsCommand(),
                new SpawnCommand(),
                new TournamentCommand(),
                new DayCommand(),
                new NightCommand(),
                new SunsetCommand(),
                new SettingsCommand(),
                new EventsCommand(),
                new EloManagerCommand(),
                new PlayersCommand(),
                new PlayerStatusCommand(),
                new CancelRankedCommand(),
                new HologramCommand(),
                new LeaderboardCommand(),
                new CancelTaskCommand(),
                new PracticeCommand(),
                new PlayerResetCommand(),
                new EventModeCommand()
        ).forEach(command -> registerCommand(command, getName()));
    }

    private void registerListeners() {
        Arrays.asList(
                new EntityListener(),
                new PlayerListener(),
                new MatchListener(),
                new WorldListener(),
                //new EnderpearlListener(this),
                new UIListener(),
                new InventoryListener(),
                new ProfileOptionsListeners(),
                new ChatListener()
        ).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
    }

    private void registerManagers() {
        spawnManager = new SpawnManager();
        arenaManager = new ArenaManager();
        chunkManager = new ChunkManager();
        editorManager = new EditorManager();
        itemManager = new ItemManager();
        kitManager = new KitManager();
        matchManager = new MatchManager();
        partyManager = new PartyManager();
        playerManager = new PlayerManager();
        queueManager = new QueueManager();
        inventoryManager = new InventoryManager();
        eventManager = new EventManager();
        tournamentManager = new TournamentManager();
        timerManager = new TimerManager(this);
        leaderboardManager = new LeaderboardManager();
        serverManager = new ServerManager();

        if (timerManager.getTimer(EnderpearlTimer.class) == null) {
            timerManager.registerTimer(new EnderpearlTimer());
        }

    }

    private void registerCommand(Command cmd, String fallbackPrefix) {
        MinecraftServer.getServer().server.getCommandMap().register(cmd.getName(), fallbackPrefix, cmd);
    }

    private boolean loadVault() {
        RegisteredServiceProvider<Chat> provider = getServer().getServicesManager().getRegistration(Chat.class);
        if(provider != null) {
            chat = provider.getProvider();
        }
        return (chat != null);
    }

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        if (!s.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subchannel = in.readUTF();
        /*switch(subchannel) {
            case "SkillPracticeNotify": {
                Short len = in.readShort();
                byte[] msgBytes = Bytes.toArray(Collections.singleton(len.intValue()));
                in.readFully(msgBytes);
                String msgin = String.valueOf(new ByteArrayInputStream(msgBytes));
                for (notificationsListener in notificationsListeners) {
                    notificationsListener(suspiciousPlayerName, score, cheatType, cheatedServerName);
                    notificationsListener(() -> {

                    });
                }
            }
            break;
        }*/
    }
}