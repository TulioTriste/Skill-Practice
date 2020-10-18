package net.skillwars.practice.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.kit.PlayerKit;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.PlayerUtil;
import org.bukkit.entity.Player;

public class EditorManager {
    private final Practice plugin = Practice.getInstance();
    private final Map<UUID, String> editing = new HashMap<>();
    private final Map<UUID, PlayerKit> renaming = new HashMap<>();

    public void addEditor(Player player, Kit kit) {
        this.editing.put(player.getUniqueId(), kit.getName());
        this.plugin.getInventoryManager().addEditingKitInventory(player, kit);

        PlayerUtil.clearPlayer(player);
        player.teleport(this.plugin.getSpawnManager().getEditorLocation().toBukkitLocation());
        player.getInventory().setArmorContents(kit.getArmor());
        player.getInventory().setContents(kit.getContents());
        player.sendMessage(CC.YELLOW + "Actualmente editando el kit " + CC.SECONDARY + kit.getName() + CC.YELLOW + ". La armadura será aplicada automaticamente en el kit.");
/*    	new BukkitRunnable(){
			@Override
			public void run(){
				NPC.getInstance().getNpcManager().destroydAll(player);

				ConfigurationSection npcSection = NPC.getInstance().getNpcConfig().getConfig().getConfigurationSection("npc");
				if (npcSection != null) {
					npcSection.getKeys(false).forEach((key) -> {
						String name = Style.translate(npcSection.getString(key + ".name"));
						ItemStack[] armorContents = InventoryUtil.deserializeInventory(npcSection.getString(key + ".armorcontents"));
						ItemStack hand = InventoryUtil.deserializeItemStack(npcSection.getString(key + ".hand"));
						Location location = LocationUtil.deserialize(npcSection.getString(key + ".location"));
						float yaw = (float)npcSection.getInt(key + ".yaw");
						float headYaw = (float)npcSection.getInt(key + ".head.yaw");
						float pitch = (float)npcSection.getInt(key + ".pitch");
						ItemStack[] armor = new ItemStack[]{hand, armorContents[3], armorContents[2], armorContents[1], armorContents[0]};
						NPC.getInstance().getNpcManager().spawnNPC(player, name, location, armor, headYaw, yaw, pitch);
					});
				}
			}
		}.runTaskLater(plugin, 5);
		
		new BukkitRunnable(){
			@Override
			public void run(){
				NPC.getInstance().getNpcManager().destroydAll(player);
				ConfigurationSection npcSection = NPC.getInstance().getNpcConfig().getConfig().getConfigurationSection("npc");
				if (npcSection != null) {
					npcSection.getKeys(false).forEach((key) -> {
						String name = Style.translate(npcSection.getString(key + ".name"));
						ItemStack[] armorContents = InventoryUtil.deserializeInventory(npcSection.getString(key + ".armorcontents"));
						ItemStack hand = InventoryUtil.deserializeItemStack(npcSection.getString(key + ".hand"));
						Location location = LocationUtil.deserialize(npcSection.getString(key + ".location"));
						float yaw = (float)npcSection.getInt(key + ".yaw");
						float headYaw = (float)npcSection.getInt(key + ".head.yaw");
						float pitch = (float)npcSection.getInt(key + ".pitch");
						ItemStack[] armor = new ItemStack[]{hand, armorContents[3], armorContents[2], armorContents[1], armorContents[0]};
						NPC.getInstance().getNpcManager().spawnNPC(player, name, location, armor, headYaw, yaw, pitch);
					});
				}
			}
		}.runTaskLater(plugin, 20L * 2);*/
	}

    public void removeEditor(UUID editor) {
        this.renaming.remove(editor);
        this.editing.remove(editor);
        this.plugin.getInventoryManager().removeEditingKitInventory(editor);
    }

    public String getEditingKit(UUID editor) {
        return this.editing.get(editor);
    }

    public void addRenamingKit(UUID uuid, PlayerKit playerKit) {
        this.renaming.put(uuid, playerKit);
    }

    public void removeRenamingKit(UUID uuid) {
        this.renaming.remove(uuid);
    }

    public PlayerKit getRenamingKit(UUID uuid) {
        return this.renaming.get(uuid);
    }
}
