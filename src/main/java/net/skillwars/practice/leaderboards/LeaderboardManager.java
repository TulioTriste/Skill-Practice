package net.skillwars.practice.leaderboards;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.client.MongoCursor;
import lombok.Getter;

import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.mongo.PracticeMongo;
import net.skillwars.practice.player.PlayerData;
import org.bson.Document;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private List<PlayerData> playerDataList = Lists.newArrayList();
    @Getter private Map<String, List<PlayerData>> eloPlayerList = Maps.newHashMap();
    @Getter private List<PlayerData> globalplayerDataList = Lists.newArrayList();

    public LeaderboardManager(){
        try (MongoCursor<Document> cursor = PracticeMongo.getInstance().getPlayers().find().iterator()){
            cursor.forEachRemaining(document -> {
                Document statisticsDocument = (Document) document.get("statistics");

                final UUID uuid = UUID.fromString(document.getString("uuid"));
                PlayerData playerData = new PlayerData(uuid);

                statisticsDocument.keySet().forEach(key -> {
                    Document ladderDocument = (Document)statisticsDocument.get(key);

                    if(ladderDocument.containsKey("ranked-elo")){
                        playerData.getRankedElo().put(key, ladderDocument.getInteger("ranked-elo"));
                    }
                });
                playerDataList.add(playerData);

            });
        }
        for(Kit kit : Practice.getInstance().getKitManager().getKits()){
            if(kit.isRanked()){
                eloPlayerList.put(kit.getName(), Lists.newArrayList());
            }
        }
        sort();

        new BukkitRunnable(){
            @Override
            public void run() {
                playerDataList.clear();
                try (MongoCursor<Document> cursor = PracticeMongo.getInstance().getPlayers().find().iterator()){
                    cursor.forEachRemaining(document -> {
                        Document statisticsDocument = (Document) document.get("statistics");

                        final UUID uuid = UUID.fromString(document.getString("uuid"));
                        PlayerData playerData = new PlayerData(uuid);

                        statisticsDocument.keySet().forEach(key -> {
                            Document ladderDocument = (Document)statisticsDocument.get(key);

                            if(ladderDocument.containsKey("ranked-elo")){
                                playerData.getRankedElo().put(key, ladderDocument.getInteger("ranked-elo"));
                            }
                        });
                        playerDataList.add(playerData);

                    });
                }
                sort();
            }
        }.runTaskTimerAsynchronously(Practice.getInstance(), 20L * 60 * 20L, 20L * 60 * 20L);
    }

    public void sort(){
        for(Kit kit : Practice.getInstance().getKitManager().getKits()){
            if(kit.isRanked()){
                List<PlayerData> playerSorted = playerDataList.stream()
                        .sorted(new EloComparator(kit.getName()).reversed())
                        .limit(10)
                        .collect(Collectors.toList());

                eloPlayerList.put(kit.getName(), playerSorted);
            }
        }

        globalplayerDataList = playerDataList.stream()
                .sorted(new GlobalEloComparator().reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<PlayerData> getListByKit(Kit kit){
        return this.eloPlayerList.get(kit.getName());
    }

    private class EloComparator implements Comparator<PlayerData> {

        String kitName;

        EloComparator(String kitName){
            this.kitName = kitName;
        }

        @Override
        public int compare(PlayerData o1, PlayerData o2){
            return Integer.compare(o1.getElo(kitName), o2.getElo(kitName));
        }
    }
    private class GlobalEloComparator implements Comparator<PlayerData>{

        @Override
        public int compare(PlayerData o1, PlayerData o2){
            return Integer.compare(o1.getGlobalStats("ELO"), o2.getGlobalStats("ELO"));
        }
    }
}
