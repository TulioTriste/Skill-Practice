package net.skillwars.practice.stats;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class StatisticsMenu extends Menu {

    private Practice plugin = Practice.getInstance();
    private OfflinePlayer target;

    public StatisticsMenu(OfflinePlayer target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) { return "§b§l" + this.target.getName() + " Estadisticas"; }

    @Override
    public int getSize() {
        return 18;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttoms = Maps.newHashMap();
        int pos = 0;
        for (Kit kit : this.plugin.getKitManager().getKits()) {
            if (kit.isRanked()) buttoms.put(pos++, new KitsItems(kit));

        }
        return buttoms;
    }

    @AllArgsConstructor
    private class KitsItems extends Button {

        Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            PlayerData data = Practice.getInstance().getPlayerManager().getPlayerData(target.getUniqueId());
            List<String> lore = new ArrayList<>();
            lore.add(CC.translate("&bElo: &f" + data.getElo(kit.getName())));
            lore.add(CC.translate("&bGanadas: &f" + data.getRankedWins(kit.getName())));
            lore.add(CC.translate("&bPerdidas: &f" + data.getRankedLosses(kit.getName())));
            lore.add(CC.translate("&bKDR: &f" + data.getKDR(kit.getName())));
            return new ItemBuilder(kit.getIcon()).lore(lore).build();
        }
    }
}
