package net.skillwars.practice.kit;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Kit {

    private final String name;

    private ItemStack[] contents = new ItemStack[36];
    private ItemStack[] armor = new ItemStack[4];
    private ItemStack[] kitEditContents = new ItemStack[36];
    private ItemStack icon;

    private List<String> excludedArenas = new ArrayList<>();
    private List<String> arenaWhiteList = new ArrayList<>();

    private boolean enabled;
    private boolean ranked;
    private boolean combo;
    private boolean sumo;
    private boolean build;
    private boolean spleef;
    private boolean parkour;
    private boolean editable;

    public void applyToPlayer(Player player) {
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armor);
        player.updateInventory();
        player.sendMessage(ChatColor.GREEN + "Giveando el kit por defecto.");
    }

    public void whitelistArena(String arena) {
        if (!this.arenaWhiteList.remove(arena)) {
            this.arenaWhiteList.add(arena);
        }
    }

    public void excludeArena(String arena) {
        if (!this.excludedArenas.remove(arena)) {
            this.excludedArenas.add(arena);
        }
    }

}
