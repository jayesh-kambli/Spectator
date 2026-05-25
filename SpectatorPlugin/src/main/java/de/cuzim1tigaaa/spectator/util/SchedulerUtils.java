package de.cuzim1tigaaa.spectator.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Folia-compatible scheduling helpers using Paper's entity/region schedulers,
 * which work on both regular Paper and Folia without any platform detection.
 */
public final class SchedulerUtils {

    private SchedulerUtils() {}

    /** Run a task on the entity's region thread next tick. */
    public static void runEntity(Plugin plugin, Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, st -> task.run(), null);
    }

    /**
     * Run a task on the entity's region thread next tick.
     * {@code retired} is called if the entity is removed before the task can run (Folia only).
     */
    public static void runEntity(Plugin plugin, Entity entity, Runnable task, Runnable retired) {
        entity.getScheduler().run(plugin, st -> task.run(), retired);
    }

    /** Run a delayed task on the entity's region thread. */
    public static void runEntityLater(Plugin plugin, Entity entity, Runnable task, long delay) {
        entity.getScheduler().runDelayed(plugin, st -> task.run(), null, delay);
    }

    /**
     * Run a repeating task on the entity's region thread.
     * Returns the ScheduledTask for later cancellation, or null if the entity is already gone.
     */
    public static ScheduledTask runEntityTimer(Plugin plugin, Entity entity, Runnable task, long initialDelay, long period) {
        return entity.getScheduler().runAtFixedRate(plugin, st -> task.run(), null,
                Math.max(1L, initialDelay), Math.max(1L, period));
    }

    /**
     * Run a repeating task on the global region scheduler.
     * Use for tasks that iterate across players or are not tied to a single entity/location.
     */
    public static ScheduledTask runGlobalTimer(Plugin plugin, Runnable task, long initialDelay, long period) {
        return plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(plugin, st -> task.run(), Math.max(1L, initialDelay), period);
    }

    /** Safely cancel a task, handling null. */
    public static void cancel(ScheduledTask task) {
        if (task != null && !task.isCancelled()) task.cancel();
    }
}
