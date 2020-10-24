package net.skillwars.practice.player;

import java.util.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.kit.PlayerKit;
import net.skillwars.practice.settings.ProfileOptions;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerData{

    public static final int DEFAULT_ELO = 1000;

    /*
     * The maps don't need getters as they are never accessed directly.
     */
    private final Map<String, Map<Integer, PlayerKit>> playerKits = new HashMap<>();
    private final Map<String, Integer> rankedLosses = new HashMap<>();
    private final Map<String, Integer> rankedWins = new HashMap<>();
    private final Map<String, Integer> rankedElo = new HashMap<>();
    private final Map<String, Integer> partyElo = new HashMap<>();

    @Getter private final UUID uniqueId;

    @Getter private PlayerState playerState = PlayerState.LOADING;
    @Getter private ProfileOptions options = new ProfileOptions();


    @Getter private UUID currentMatchID;
    @Getter private UUID duelSelecting;

    @Getter private int eloRange = 250;
    @Getter private int pingRange = 50;

    @Getter private int teamID = -1;
    @Getter private int rematchID = -1;
    @Getter private int missedPots;
    @Getter private int longestCombo;
    @Getter private int combo;
    @Getter private int hits;

    @Getter private int sumoEventWins;
    @Getter private int sumoEventLosses;

    @Getter private int unrankedWins;

    public int getWins(String kitName) {
        return this.rankedWins.computeIfAbsent(kitName, k -> 0);
    }

    public void setWins(String kitName, int wins) {
        this.rankedWins.put(kitName, wins);
    }

    public int getLosses(String kitName) {
        return this.rankedLosses.computeIfAbsent(kitName, k -> 0);
    }

    public void setLosses(String kitName, int losses) {
        this.rankedLosses.put(kitName, losses);
    }

    public int getElo(String kitName) {
        return this.rankedElo.computeIfAbsent(kitName, k -> PlayerData.DEFAULT_ELO);
    }

    public void setElo(String kitName, int elo) {
        this.rankedElo.put(kitName, elo);
    }

    public int getPartyElo(String kitName) {
        return this.partyElo.computeIfAbsent(kitName, k -> PlayerData.DEFAULT_ELO);
    }

    public void setPartyElo(String kitName, int elo) {
        this.partyElo.put(kitName, elo);
    }

    public void addPlayerKit(int index, PlayerKit playerKit) {
        this.getPlayerKits(playerKit.getName()).put(index, playerKit);
    }

    public Map<Integer, PlayerKit> getPlayerKits(String kitName) {
        return this.playerKits.computeIfAbsent(kitName, k -> new HashMap<>());
    }

    public int getGlobalStats(String type) {
        int i = 0;
        int count = 0;

        for (Kit kit : Practice.getInstance().getKitManager().getKits()) {

            switch (type.toUpperCase()) {
                case "ELO":
                    i += getElo(kit.getName());
                    break;
                case "WINS":
                    i += getWins(kit.getName());
                    break;
                case "LOSSES":
                    i += getLosses(kit.getName());
                    break;
            }

            count++;
        }

        if(i == 0){
            i = 1;
        }
        if(count == 0){
            count = 1;
        }

        return type.toUpperCase().equalsIgnoreCase("ELO") ? Math.round(i / count) : i;
    }
}