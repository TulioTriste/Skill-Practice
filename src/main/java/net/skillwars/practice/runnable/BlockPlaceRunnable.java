package net.skillwars.practice.runnable;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
public abstract class BlockPlaceRunnable extends BukkitRunnable {

    private World world;
    private final ConcurrentMap<Location, Block> blocks;
    private final int totalBlocks;
    private final Iterator<Location> iterator;
    private int blockIndex = 0;
    private int blocksPlaced = 0;
    private boolean completed = false;

    public BlockPlaceRunnable(World world, Map<Location, Block> blocks) {
        this.world = world;
        this.blocks = new ConcurrentHashMap<>();
        this.blocks.putAll(blocks);
        this.totalBlocks = blocks.keySet().size();
        this.iterator = blocks.keySet().iterator();
    }

    @Override
    public void run() {
        if (blocks.isEmpty() || !iterator.hasNext()) {
            finish();
            completed = true;
            cancel();
            return;
        }

        /*TaskManager.IMP.async(() -> {

            EditSession editSession = new EditSessionBuilder(this.world.getName())
                    .fastmode(true)
                    .allowedRegionsEverywhere()
                    .autoQueue(false)
                    .limitUnlimited()
                    .build();

            for(Map.Entry<Location, Block> entry : this.blocks.entrySet()) {

                try {
                    editSession.setBlock(new Vector(entry.getKey().getBlockX(), entry.getKey().getBlockY(), entry.getKey().getZ()), new BaseBlock(entry.getValue().getTypeId(), entry.getValue().getData()));
                } catch (Exception ignored) {}
            }

            editSession.flushQueue();

            TaskManager.IMP.task(this.blocks::clear);

        });*/

        for (Map.Entry<Location, Block> entry : this.blocks.entrySet()) {
//            entry.setBlock(new Vector(entry.getKey().getBlockX(), entry.getKey().getBlockY(), entry.getKey().getZ()), new BaseBlock(entry.getValue().getTypeId(), entry.getValue().getData()));
            entry.getKey().getBlock().setType(entry.getValue().getType());
        }
        this.blocks.clear();
    }

    public abstract void finish();
}
