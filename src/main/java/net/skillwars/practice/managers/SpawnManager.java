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
    private CustomLocation redroverLocation;
    private CustomLocation redroverFirst;
    private CustomLocation redroverSecond;
    private CustomLocation redroverMin;
    private CustomLocation redroverMax;
    private CustomLocation ffaLocation;
    private CustomLocation ffaMin;
    private CustomLocation ffaMax;

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
        if (config.contains("redroverLocation")) {
            this.redroverLocation = CustomLocation.stringToLocation(config.getString("redroverLocation"));
            this.redroverMin = CustomLocation.stringToLocation(config.getString("redroverMin"));
            this.redroverMax = CustomLocation.stringToLocation(config.getString("redroverMax"));
            this.redroverFirst = CustomLocation.stringToLocation(config.getString("redroverFirst"));
            this.redroverSecond = CustomLocation.stringToLocation(config.getString("redroverSecond"));
        }
        if (config.contains("ffaLocation")) {
            this.ffaLocation = CustomLocation.stringToLocation(config.getString("ffaLocation"));
            this.ffaMin = CustomLocation.stringToLocation(config.getString("ffaMin"));
            this.ffaMax = CustomLocation.stringToLocation(config.getString("ffaMax"));
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
        if(redroverLocation != null)
            config.set("redroverLocation", CustomLocation.locationToString(this.redroverLocation));
        if(redroverMin != null)
            config.set("redroverMin", CustomLocation.locationToString(this.redroverMin));
        if(redroverMax != null)
            config.set("redroverMax", CustomLocation.locationToString(this.redroverMax));
        if(redroverFirst != null)
            config.set("redroverFirst", CustomLocation.locationToString(this.redroverFirst));
        if(redroverSecond != null)
            config.set("redroverSecond", CustomLocation.locationToString(this.redroverSecond));
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
