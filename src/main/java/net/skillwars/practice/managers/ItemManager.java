package net.skillwars.practice.managers;

import lombok.Getter;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.ItemUtil;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public class ItemManager {

    private final ItemStack[] spawnItems;
    private final ItemStack[] spawnItems2;
    private final ItemStack[] queueItems;
    private final ItemStack[] partyItems;
    private final ItemStack[] tournamentItems;
    private final ItemStack[] eventItems;
    private final ItemStack[] specItems;
    private final ItemStack[] staffSpecItems;
    private final ItemStack[] partySpecItems;

    private final ItemStack defaultBook;

    public ItemManager() {
        this.spawnItems = new ItemStack[]{
                ItemUtil.createItem(Material.IRON_SWORD, CC.YELLOW + "Entrar UnRanked Queue"),
                ItemUtil.createItem(Material.DIAMOND_SWORD, CC.GREEN + "Entrar Ranked Queue"),
                null,
                null,
                ItemUtil.createItem(Material.NETHER_STAR, CC.YELLOW + "Estadisticas"),
                null,
                ItemUtil.createItem(Material.PAPER, CC.GREEN + "Eventos"),
                ItemUtil.createItem(Material.NAME_TAG, CC.PRIMARY + "Crear Party"),
                ItemUtil.createItem(Material.BOOK, CC.GOLD + "Editar Kits"),
        };
        this.spawnItems2 = new ItemStack[]{
                ItemUtil.createItem(Material.IRON_SWORD, CC.YELLOW + "Join UnRanked Queue"),
                ItemUtil.createItem(Material.DIAMOND_SWORD, CC.GREEN + "Join Ranked Queue"),
                null,
                null,
                ItemUtil.createItem(Material.NAME_TAG, CC.PRIMARY + "Crear Party"),
                null,
                null,
                ItemUtil.createItem(Material.COMPASS, CC.PRIMARY + "Ajustes"),
                ItemUtil.createItem(Material.BOOK, CC.GOLD + "Editar Kits"),
        };
        this.queueItems = new ItemStack[]{
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ItemUtil.createItem(Material.INK_SACK, CC.RED + "Leave Queue", 1, (short)1)
        };
        this.specItems = new ItemStack[]{
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ItemUtil.createItem(Material.REDSTONE, CC.RED + "Leave Spectator Mode")
        };
        this.staffSpecItems = new ItemStack[]{
                ItemUtil.createItem(Material.BOOK, CC.translate("&bVer inventario")),
                ItemUtil.createItem(Material.PACKED_ICE, CC.translate("&bCongelador")),
                ItemUtil.createItem(Material.SKULL, CC.translate("&bStaffs conectados")),
                null,
                null,
                null,
                null,
                null,
                ItemUtil.createItem(Material.REDSTONE, CC.RED + "Leave Spectator Mode")
        };
        this.partySpecItems = new ItemStack[]{
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ItemUtil.createItem(Material.REDSTONE, CC.RED + "Salir Party")
        };
        this.tournamentItems = new ItemStack[]{
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ItemUtil.createItem(Material.REDSTONE, CC.RED + "Salir Tournament")
        };
        this.eventItems = new ItemStack[]{
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ItemUtil.createItem(Material.NETHER_STAR, CC.RED + "Leave Event"),
        };
        this.partyItems = new ItemStack[]{
                ItemUtil.createItem(Material.IRON_SWORD, CC.YELLOW + "Entrar 2v2 UnRanked Queue"),
                ItemUtil.createItem(Material.DIAMOND_SWORD, CC.GREEN + "Entrar 2v2 Ranked Queue"),
                null,
                null,
                ItemUtil.createItem(Material.DIAMOND_AXE, CC.AQUA + "Iniciar Evento de Party"),
                ItemUtil.createItem(Material.IRON_AXE, CC.PRIMARY + "Pelear contra otras Partys"),
                null,
                ItemUtil.createItem(Material.BONE, CC.YELLOW + "Opciones"),
                ItemUtil.createItem(Material.REDSTONE, CC.RED + "Salir Party")
        };
        this.defaultBook = ItemUtil.createItem(Material.ENCHANTED_BOOK, CC.PRIMARY + "Kit Default");
    }
}
