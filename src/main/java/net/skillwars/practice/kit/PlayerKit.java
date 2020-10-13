package net.skillwars.practice.kit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.skillwars.practice.Practice;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
@Getter
@Setter
@AllArgsConstructor
public class PlayerKit {
    private final String name;
    private final int index;

    private ItemStack[] contents;
    private String displayName;

    public void applyToPlayer(Player player) {
        for (ItemStack itemStack : contents) {
            if (itemStack != null) {
                if (itemStack.getAmount() <= 0) {
                    itemStack.setAmount(1);
                }
            }
        }
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(Practice.getInstance().getKitManager().getKit(name).getArmor());
        player.updateInventory();
        //player.sendMessage(ChatColor.GREEN + "Giving you " + ChatColor.YELLOW + displayName + ChatColor.GREEN + ".");
    }
}
