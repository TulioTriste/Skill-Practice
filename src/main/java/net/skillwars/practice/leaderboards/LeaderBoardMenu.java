package net.skillwars.practice.leaderboards;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.player.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class LeaderBoardMenu extends Menu {
    private List<PlayerData> playerDataList = Lists.newArrayList();

    private Player target;

    public LeaderBoardMenu(Player target){
        this.target = target;
    }

    @Override
    public String getTitle(Player player){
        return "§9§lEstadisticas";
    }

    @Override
    public int getSize(){
        return 54;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player){
        Map<Integer, Button> buttons = Maps.newHashMap();

        buttons.put(12, new StatsButton());
        buttons.put(14, new GlobalStatsButton());

        int pos = 0;

        for(Kit kit : Practice.getInstance().getKitManager().getKits()){
            if(kit.isRanked()){
                buttons.put(28 + pos++, new KitButton(kit));
            }
        }

        return buttons;
    }

    @AllArgsConstructor
    private class KitButton extends Button {

        Kit kit;

        @Override
        public ItemStack getButtonItem(Player player){
            List<PlayerData> playerDataList2 =  Practice.getInstance().getLeaderboardManager().getListByKit(kit);
            List<String> lore = Lists.newArrayList();

            int pos = 1;

            for (PlayerData playerData : playerDataList2) {
                OfflinePlayer player1 = Bukkit.getOfflinePlayer(playerData.getUniqueId());
                if (pos == 1 || pos == 2 || pos == 3) {
                    lore.add(Style.DARK_AQUA + pos + " " + Style.GRAY + player1.getName() + ": " + Style.RESET + playerData.getElo(kit.getName()));
                } else {
                    lore.add(Style.GRAY + pos + " " + player1.getName() + ": " + Style.RESET + playerData.getElo(kit.getName()));
                }
                pos++;
            }

            return new ItemBuilder(kit.getIcon().getType())
                    .name(Style.WHITE + "Top 10 " + Style.BLUE + kit.getName())
                    .durability(kit.getIcon().getDurability())
                    .lore(lore).build();
        }
    }


    private class StatsButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player){

            PlayerData playerData = Practice.getInstance().getPlayerManager().getPlayerData(target.getUniqueId());

            List<String> lore = Lists.newArrayList();

            Practice.getInstance().getKitManager().getKits().forEach(kit -> {
                if(kit.isRanked()){
                    lore.add(Style.AQUA + kit.getName() + Style.GRAY +": " + Style.RESET + playerData.getElo(kit.getName()));
                }
            });

            lore.add(Style.DARK_AQUA + "Global" + Style.GRAY + ": " + Style.RESET + playerData.getGlobalStats("ELO"));

            ItemStack item = new ItemBuilder(Material.SKULL_ITEM)
                    .durability(3)
                    .name(Style.GRAY + "Estadisticas de " + Style.BLUE + target.getName())
                    .lore(lore)
                    .build();

            SkullMeta itemMeta = (SkullMeta)item.getItemMeta();
            itemMeta.setOwner(target.getName());
            item.setItemMeta(itemMeta);
            return item;
        }
    }

    private class GlobalStatsButton extends Button{

        @Override
        public ItemStack getButtonItem(Player player){

            List<PlayerData> playerDataList2 = Practice.getInstance().getLeaderboardManager().getGlobalplayerDataList();

            List<String> lore = Lists.newArrayList();

            int pos = 1;

            for(PlayerData playerData : playerDataList2){
                OfflinePlayer player1 = Bukkit.getOfflinePlayer(playerData.getUniqueId());
                if(pos == 1 || pos == 2 || pos == 3){
                    lore.add(Style.DARK_AQUA + pos + " " + Style.GRAY + player1.getName() + ": " + Style.RESET + playerData.getGlobalStats("ELO"));
                }else{
                    lore.add(Style.GRAY + pos + " " + player1.getName() + ": " + Style.RESET + playerData.getGlobalStats("ELO"));
                }
                pos++;
            }

            return new ItemBuilder(Material.NETHER_STAR)
                    .name(Style.WHITE + "Top 10 " + Style.BLUE + "Global")
                    .lore(lore)
                    .build();
        }
    }
}
