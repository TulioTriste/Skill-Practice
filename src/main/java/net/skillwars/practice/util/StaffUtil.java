package net.skillwars.practice.util;

import org.bukkit.Bukkit;

import java.util.List;

public class StaffUtil {

    public static void sendStaffMessage(String s) {
        Bukkit.getOnlinePlayers().forEach(online -> {
            if (online.hasPermission("practice.staff")) {
                online.sendMessage(CC.translate(s));
            }
        });
    }

    public static void sendStaffMessage(List<String> s) {
        Bukkit.getOnlinePlayers().forEach(online -> {
            if (online.hasPermission("practice.staff")) {
                s.forEach(string -> online.sendMessage(CC.translate(string)));
            }
        });
    }

    public static void sendStaffMessage(String[] s) {
        Bukkit.getOnlinePlayers().forEach(online -> {
            for (String s1 : s) {
                online.sendMessage(CC.translate(s1));
            }
        });
    }
}
