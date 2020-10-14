package net.skillwars.practice.managers;

import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.util.CustomLocation;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class SpawnManager {
	
    private Practice plugin;
    private CustomLocation spawnLocation;
    private CustomLocation spawnMin;
    private CustomLocation spawnMax;
    private CustomLocation editorLocation;
    private CustomLocation editorMin;
    private CustomLocation editorMax;
    private CustomLocation sumoLocation;
    private CustomLocation sumoFirst;
    private CustomLocation sumoSecond;
    private CustomLocation sumoMin;
    private CustomLocation sumoMax;
    private CustomLocation tournamentLocation;
    private CustomLocation tournamentFirst;
    private CustomLocation tournamentSecond;
    private CustomLocation tournamentMin;
    private CustomLocation tournamentMax;
    private CustomLocation teamFightsLocation;
    private CustomLocation teamFightsFirst;
    private CustomLocation teamFightsSecond;
    private CustomLocation teamFightsMin;
    private CustomLocation teamFightsMax;
    private CustomLocation FFALocation;
    private CustomLocation FFAMin;
    private CustomLocation FFAMax;

    public SpawnManager() {
        this.plugin = Practice.getInstance();
        this.loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = this.plugin.getMainConfig().getConfig();
        if (config.contains("spawnLocation")) {
            this.spawnLocation = CustomLocation.stringToLocation(config.getString("spawnLocation"));
            this.spawnMin = CustomLocation.stringToLocation(config.getString("spawnMin"));
            this.spawnMax = CustomLocation.stringToLocation(config.getString("spawnMax"));
        }
        if (config.contains("editorLocation")) {
            this.editorLocation = CustomLocation.stringToLocation(config.getString("editorLocation"));
            this.editorMin = CustomLocation.stringToLocation(config.getString("editorMin"));
            this.editorMax = CustomLocation.stringToLocation(config.getString("editorMax"));
        }
        if (config.contains("sumoLocation")) {
            this.sumoLocation = CustomLocation.stringToLocation(config.getString("sumoLocation"));
            this.sumoMin = CustomLocation.stringToLocation(config.getString("sumoMin"));
            this.sumoMax = CustomLocation.stringToLocation(config.getString("sumoMax"));
            this.sumoFirst = CustomLocation.stringToLocation(config.getString("sumoFirst"));
            this.sumoSecond = CustomLocation.stringToLocation(config.getString("sumoSecond"));
        }
        if (config.contains("tournamentLocation")) {
            this.tournamentLocation = CustomLocation.stringToLocation(config.getString("tournamentLocation"));
            this.tournamentMin = CustomLocation.stringToLocation(config.getString("tournamentMin"));
            this.tournamentMax = CustomLocation.stringToLocation(config.getString("tournamentMax"));
            this.tournamentFirst = CustomLocation.stringToLocation(config.getString("tournamentFirst"));
            this.tournamentSecond = CustomLocation.stringToLocation(config.getString("tournamentSecond"));
        }
        if (config.contains("teamfightsLocation")) {
            this.teamFightsLocation = CustomLocation.stringToLocation(config.getString("teamfightsLocation"));
            this.teamFightsMin = CustomLocation.stringToLocation(config.getString("teamfightsMin"));
            this.teamFightsMax = CustomLocation.stringToLocation(config.getString("teamfightsMax"));
            this.teamFightsFirst = CustomLocation.stringToLocation(config.getString("teamfightsFirst"));
            this.teamFightsSecond = CustomLocation.stringToLocation(config.getString("teamfightsSecond"));
        }
        if (config.contains("ffaLocation")) {
            this.FFALocation = CustomLocation.stringToLocation(config.getString("ffaLocation"));
            this.FFAMin = CustomLocation.stringToLocation(config.getString("ffaMin"));
            this.FFAMax = CustomLocation.stringToLocation(config.getString("ffaMax"));
        }
    }

    public void saveConfig() {
        final FileConfiguration config = this.plugin.getMainConfig().getConfig();
        if(spawnLocation != null)
            config.set("spawnLocation", CustomLocation.locationToString(this.spawnLocation));
        if(spawnMin != null)
            config.set("spawnMin", CustomLocation.locationToString(this.spawnMin));
        if(spawnMax != null)
            config.set("spawnMax", CustomLocation.locationToString(this.spawnMax));
        if(editorLocation != null)
            config.set("editorLocation", CustomLocation.locationToString(this.editorLocation));
        if(editorMin != null)
            config.set("editorMin", CustomLocation.locationToString(this.editorMin));
        if(editorMax != null)
            config.set("editorMax", CustomLocation.locationToString(this.editorMax));
        if(sumoLocation != null)
            config.set("sumoLocation", CustomLocation.locationToString(this.sumoLocation));
        if(sumoMin != null)
            config.set("sumoMin", CustomLocation.locationToString(this.sumoMin));
        if(sumoMax != null)
            config.set("sumoMax", CustomLocation.locationToString(this.sumoMax));
        if(sumoFirst != null)
            config.set("sumoFirst", CustomLocation.locationToString(this.sumoFirst));
        if(sumoSecond != null)
            config.set("sumoSecond", CustomLocation.locationToString(this.sumoSecond));
        if(tournamentLocation != null)
            config.set("tournamentLocation", CustomLocation.locationToString(this.tournamentLocation));
        if(tournamentMin != null)
            config.set("tournamentMin", CustomLocation.locationToString(this.tournamentMin));
        if(tournamentMax != null)
            config.set("tournamentMax", CustomLocation.locationToString(this.tournamentMax));
        if(tournamentFirst != null)
            config.set("tournamentFirst", CustomLocation.locationToString(this.tournamentFirst));
        if(tournamentSecond != null)
            config.set("tournamentSecond", CustomLocation.locationToString(this.tournamentSecond));
        if(teamFightsLocation != null)
            config.set("teamfightsLocation", CustomLocation.locationToString(this.teamFightsLocation));
        if(teamFightsMin != null)
            config.set("teamfightsMin", CustomLocation.locationToString(this.teamFightsMin));
        if(teamFightsMax != null)
            config.set("teamfightsMax", CustomLocation.locationToString(this.teamFightsMax));
        if(teamFightsFirst != null)
            config.set("teamfightsFirst", CustomLocation.locationToString(this.teamFightsFirst));
        if(teamFightsSecond != null)
            config.set("teamfightsSecond", CustomLocation.locationToString(this.teamFightsSecond));
        if(FFALocation != null)
            config.set("ffaLocation", CustomLocation.locationToString(this.FFALocation));
        if(FFAMin != null)
            config.set("ffaMin", CustomLocation.locationToString(this.FFAMin));
        if(FFAMax != null)
            config.set("ffaMax", CustomLocation.locationToString(this.FFAMax));
        this.plugin.getMainConfig().save();
    }

    private List<String> fromLocations(final List<CustomLocation> locations) {
        final List<String> toReturn = new ArrayList<>();
        for (final CustomLocation location : locations) {
            toReturn.add(CustomLocation.locationToString(location));
        }
        return toReturn;
    }
}
