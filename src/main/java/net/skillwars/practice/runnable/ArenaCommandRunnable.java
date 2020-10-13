package net.skillwars.practice.runnable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.skillwars.practice.Practice;
import net.skillwars.practice.arena.Arena;
import net.skillwars.practice.arena.StandaloneArena;
import net.skillwars.practice.util.CC;
import net.skillwars.practice.util.CustomLocation;

@Getter
@AllArgsConstructor
public class ArenaCommandRunnable implements Runnable {

    private final Practice plugin;
    private Arena copiedArena;
    private int times;

    @Override
    public void run() {
        this.duplicateArena( copiedArena, 10000, 10000);
    }

    private void duplicateArena(Arena arena, int offsetX, int offsetZ) {
        new DuplicateArenaRunnable(this.plugin, arena, offsetX, offsetZ, 500, 500) {
            @Override
            public void onComplete() {
                double minX = arena.getMin().getX() + this.getOffsetX();
                double minZ = arena.getMin().getZ() + this.getOffsetZ();
                double maxX = arena.getMax().getX() + this.getOffsetX();
                double maxZ = arena.getMax().getZ() + this.getOffsetZ();
                double aX = arena.getA().getX() + this.getOffsetX();
                double aZ = arena.getA().getZ() + this.getOffsetZ();
                double bX = arena.getB().getX() + this.getOffsetX();
                double bZ = arena.getB().getZ() + this.getOffsetZ();

                CustomLocation min = new CustomLocation(arena.getMin().getWorld(), minX, arena.getMin().getY(), minZ);
                CustomLocation max = new CustomLocation(arena.getMax().getWorld(), maxX, arena.getMax().getY(), maxZ);
                CustomLocation a = new CustomLocation(arena.getA().getWorld(), aX, arena.getA().getY(), aZ);
                CustomLocation b = new CustomLocation(arena.getB().getWorld(), bX, arena.getB().getY(), bZ);

                StandaloneArena standaloneArena = new StandaloneArena(a, b, min, max);

                arena.addStandaloneArena(standaloneArena);
                arena.addAvailableArena(standaloneArena);

                if (--ArenaCommandRunnable.this.times > 0) {
                    ArenaCommandRunnable.this.plugin.getServer().getConsoleSender().sendMessage(
                            CC.PRIMARY + "Se ha puesto la arena independiente " + CC.SECONDARY + arena.getName() + CC.PRIMARY
                                    + " en " + CC.SECONDARY + minX + CC.PRIMARY + ", " + CC.SECONDARY + minZ
                                    + CC.PRIMARY + ". " + CC.SECONDARY + ArenaCommandRunnable.this.times +
                                    CC.PRIMARY + " arenas restantes.");
                    ArenaCommandRunnable.this.duplicateArena(arena, (int) maxX, (int) maxZ);
                } else {
                    ArenaCommandRunnable.this.plugin.getServer().getConsoleSender().sendMessage(CC.PRIMARY + "Se ha pegado " + CC.SECONDARY
                            + ArenaCommandRunnable.this.copiedArena.getName() + CC.PRIMARY + "'s arenas independientes.");
                    ArenaCommandRunnable.this.plugin.getArenaManager().setGeneratingArenaRunnables(
                            ArenaCommandRunnable.this.plugin.getArenaManager().getGeneratingArenaRunnables() - 1);
                }
            }
        }.run();
    }
}
