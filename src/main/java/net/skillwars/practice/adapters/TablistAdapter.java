package net.skillwars.practice.adapters;

import com.google.common.collect.Lists;
import me.joansiitoh.skillcore.apis.NametagEdit;
import me.joeleoli.nucleus.tablist.adapter.TabAdapter;
import me.joeleoli.nucleus.tablist.entry.TabEntry;
import me.joeleoli.nucleus.tablist.skin.Skin;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

/*
This Proyect has been created
by TulioTriviño#6969
*/
public class TablistAdapter implements TabAdapter {

    @Override
    public String getHeader(Player player) {
        return CC.translate("&b&lSkillWars Network");
    }

    @Override
    public String getFooter(Player player) {
        return CC.translate("&bus.skillwars.us&f, &cstore.skillwars.us&f, &awww.skillwars.us");
    }

    @Override
    public List<TabEntry> getLines(Player player) {
        List<TabEntry> lines = Lists.newArrayList();
        String bars = CC.translate("&7&m--------------------");
        lines.add(new TabEntry(0, 1, bars));
        lines.add(new TabEntry(0, 18, bars));
        lines.add(new TabEntry(1, 1, bars));
        lines.add(new TabEntry(1, 18, bars));
        lines.add(new TabEntry(2, 1, bars));
        lines.add(new TabEntry(2, 18, bars));
        lines.add(new TabEntry(1, 0, CC.translate("           &b&lPractice")));
        lines.add(new TabEntry(0, 19, CC.translate("&btienda.skillwars.us")));
        lines.add(new TabEntry(1, 19, CC.translate("&bwww.skillwars.us")));
        lines.add(new TabEntry(2, 19, CC.translate("&b@SkillWarsUs")));
        lines.add(new TabEntry(3, 5, CC.translate("&b&lUsuario:")));
        lines.add(new TabEntry(3, 6, CC.translate("&7" + NametagEdit.getPrefix(player) + player.getName())));
        lines.add(new TabEntry(3, 9, CC.translate("&b&lServer:")));
        lines.add(new TabEntry(3, 10, CC.translate("&7Practice")));
        lines.add(new TabEntry(3, 13, CC.translate("&b&lConectados")));
        lines.add(new TabEntry(3, 14, CC.translate("&7" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers())));
        for (int i = 0; i < players().size(); i++) {
            Player target = Bukkit.getPlayer(players().get(i));
            String tag = CC.translate(NametagEdit.getPrefix(target) + target.getName());
            int ping = ((CraftPlayer) player).getHandle().ping;
           if (i <= 15) {
               lines.add(new TabEntry(0, i + 2, tag).setPing(ping).setSkin(Skin.getPlayer(target)));
           }
           else if (i <= 31) {
               lines.add(new TabEntry(1, i + 2, tag).setPing(ping).setSkin(Skin.getPlayer(target)));
           }
           else if (i <= 47) {
               lines.add(new TabEntry(2, i + 2, tag).setPing(ping).setSkin(Skin.getPlayer(target)));
           }
        }
        return lines;
    }

    private List<String> players() {
        List<String> players = Lists.newArrayList();
        Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getName()));
        return players;
    }
}
