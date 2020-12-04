package net.skillwars.practice.managers;

import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;

/*
This Proyect has been created
by TulioTriviño#6969
*/
@Getter
public class ServerManager {

    private boolean eventMode = false;

    public void setEventMode(boolean mode) {
        String bars = CC.translate("&7&m-------------------------------");
        Bukkit.broadcastMessage(bars);
        if (mode) {
            Bukkit.broadcastMessage(CC.translate("&7&lPractice"));
            Bukkit.broadcastMessage(CC.translate(""));
            Bukkit.broadcastMessage(CC.translate("&7El modo Evento se ha activado y con esto"));
            Bukkit.broadcastMessage(CC.translate("&7Se les denegará el permiso de meterse"));
            Bukkit.broadcastMessage(CC.translate("&7a los tournaments activos durante este modo"));
        } else {
            Bukkit.broadcastMessage(CC.translate("&7&lPractice"));
            Bukkit.broadcastMessage(CC.translate(""));
            Bukkit.broadcastMessage(CC.translate("&7El modo Evento se ha desactivado y con esto"));
            Bukkit.broadcastMessage(CC.translate("&7Todos los jugadores podran entrar a los tournaments"));
            Bukkit.broadcastMessage(CC.translate("&7cuando quieran"));
        }
        Bukkit.broadcastMessage(bars);
        eventMode = mode;
    }

}
