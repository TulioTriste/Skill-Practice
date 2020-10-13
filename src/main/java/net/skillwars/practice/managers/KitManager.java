package net.skillwars.practice.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.file.Config;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.util.InventoryUtil;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class KitManager {

    private Practice plugin = Practice.getInstance();

    private final Map<String, Kit> kits = new HashMap<>();

    @Getter
    private final List<String> rankedKits = new ArrayList<>();

    private Config config = new Config("kits", this.plugin);

    public KitManager() {
        this.loadKits();
        this.kits.entrySet().stream()
                .filter(kit -> kit.getValue().isEnabled())
                .filter(kit -> kit.getValue().isRanked())
                .forEach(kit -> this.rankedKits.add(kit.getKey()));
    }

    private void loadKits() {
        FileConfiguration fileConfig = this.config.getConfig();
        ConfigurationSection kitSection = fileConfig.getConfigurationSection("kits");

        if (kitSection == null) {
            return;
        }

        kitSection.getKeys(false).forEach(name -> {
            ItemStack[] contents = InventoryUtil.deserializeInventory(kitSection.getString(name + ".contents"));
            ItemStack[] armor = InventoryUtil.deserializeInventory(kitSection.getString(name + ".armor"));
            ItemStack[] kitEditContents = InventoryUtil.deserializeInventory(kitSection.getString(name + ".kitEditContents"));

            List<String> excludedArenas = kitSection.getStringList(name + ".excludedArenas");
            List<String> arenaWhiteList = kitSection.getStringList(name + ".arenaWhitelist");

            ItemStack icon = (ItemStack) kitSection.get(name + ".icon");

            boolean enabled = kitSection.getBoolean(name + ".enabled");
            boolean ranked = kitSection.getBoolean(name + ".ranked");
            boolean combo = kitSection.getBoolean(name + ".combo");
            boolean sumo = kitSection.getBoolean(name + ".sumo");
            boolean build = kitSection.getBoolean(name + ".build");
            boolean spleef = kitSection.getBoolean(name + ".spleef");
            boolean parkour = kitSection.getBoolean(name + ".parkour");
            boolean editable = kitSection.getBoolean(name + ".editable");

            Kit kit = new Kit(name, contents, armor, kitEditContents, icon, excludedArenas, arenaWhiteList, enabled,
                    ranked, combo, sumo, build, spleef, parkour, editable);
            this.kits.put(name, kit);
        });
    }

    public void saveKits() {
        FileConfiguration fileConfig = this.config.getConfig();

        fileConfig.set("kits", null);

        this.kits.forEach((kitName, kit) -> {
            if (kit.getIcon() != null && kit.getContents() != null && kit.getArmor() != null) {
                fileConfig.set("kits." + kitName + ".contents", InventoryUtil.serializeInventory(kit.getContents()));
                fileConfig.set("kits." + kitName + ".armor", InventoryUtil.serializeInventory(kit.getArmor()));
                fileConfig.set("kits." + kitName + ".kitEditContents", InventoryUtil.serializeInventory(kit.getKitEditContents()));
                fileConfig.set("kits." + kitName + ".icon", kit.getIcon());
                fileConfig.set("kits." + kitName + ".excludedArenas", kit.getExcludedArenas());
                fileConfig.set("kits." + kitName + ".arenaWhitelist", kit.getArenaWhiteList());
                fileConfig.set("kits." + kitName + ".enabled", kit.isEnabled());
                fileConfig.set("kits." + kitName + ".ranked", kit.isRanked());
                fileConfig.set("kits." + kitName + ".combo", kit.isCombo());
                fileConfig.set("kits." + kitName + ".sumo", kit.isSumo());
                fileConfig.set("kits." + kitName + ".build", kit.isBuild());
                fileConfig.set("kits." + kitName + ".spleef", kit.isSpleef());
                fileConfig.set("kits." + kitName + ".parkour", kit.isParkour());
                fileConfig.set("kits." + kitName + ".editable", kit.isEditable());
            }
        });

        this.config.save();
    }

    public void deleteKit(String name) {
        this.kits.remove(name);
    }

    public void createKit(String name) {
        this.kits.put(name, new Kit(name));
    }

    public Collection<Kit> getKits() {
        return this.kits.values();
    }

    public Kit getKit(String name) {
        return this.kits.get(name);
    }

}