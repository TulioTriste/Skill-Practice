package net.skillwars.practice.adapters;

import com.google.common.collect.Lists;
import me.joansiitoh.skillcore.apis.NametagEdit;
import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.tablist.adapter.TabAdapter;
import me.joeleoli.nucleus.tablist.entry.TabEntry;
import me.joeleoli.nucleus.tablist.skin.Skin;
import net.luckperms.api.model.group.Group;
import net.skillwars.practice.util.CC;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

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
        lines.add(new TabEntry(1, 0, CC.translate("&b&lPractice")));
        lines.add(new TabEntry(0, 19, CC.translate("&btienda.skillwars.us")));
        lines.add(new TabEntry(1, 19, CC.translate("&bwww.skillwars.us")));
        lines.add(new TabEntry(2, 19, CC.translate("&b@SkillWarsUs")));
        lines.add(new TabEntry(3, 5, CC.translate("&b&lUsuario:")));
        lines.add(new TabEntry(3, 6, CC.translate("&7" + NametagEdit.getPrefix(player) + player.getName())));
        lines.add(new TabEntry(3, 9, CC.translate("&b&lServer:")));
        lines.add(new TabEntry(3, 10, CC.translate("&7Practice")));
        lines.add(new TabEntry(3, 13, CC.translate("&b&lConectados")));
        lines.add(new TabEntry(3, 14, CC.translate("&7" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers())));
        int number = 0;
        for (int i = 0; i < 48; i++) {
            Player target = players().get(i);
            String tag = CC.translate(NametagEdit.getPrefix(target) + target.getName());
            int ping = ((CraftPlayer) target).getHandle().ping;
           if (i <= 15) {
               lines.add(new TabEntry(0, number + 2, tag).setPing(ping).setSkin(Skin.getPlayer(target)));
               number++;
           }
           else if (i <= 31) {
               lines.add(new TabEntry(1, number + 2, tag).setPing(ping).setSkin(Skin.getPlayer(target)));
               number++;
           }
           else {
               lines.add(new TabEntry(2, number + 2, tag).setPing(ping).setSkin(Skin.getPlayer(target)));
               number++;
           }
           if (i == 15 || i == 31 || i == 47) {
               number = 0;
           }
        }
        return lines;
    }

    private List<Player> players() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Map<UUID, Integer> test = new HashMap<>();
        Bukkit.getOnlinePlayers().forEach(player -> {
            try {
                Group group = Nucleus.getInstance().getLp().getGroupManager().getGroup(Nucleus.getInstance().getLp().getUserManager().getUser(player.getUniqueId()).getPrimaryGroup());
                test.put(player.getUniqueId(), group.getWeight().getAsInt());
            } catch (Exception exception) {
                test.put(player.getUniqueId(), 0);
            }
        });
        players.sort(new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                return Integer.compare(test.getOrDefault(o1.getUniqueId(), 0), test.getOrDefault(o2.getUniqueId(), 0));
            }
        });
        Collections.reverse(players);
        return players;
    }
}
