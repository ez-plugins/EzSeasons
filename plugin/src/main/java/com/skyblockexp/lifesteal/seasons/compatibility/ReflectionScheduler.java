package com.skyblockexp.lifesteal.seasons.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class ReflectionScheduler {

    private ReflectionScheduler() { }

    // Minimal main thread execution
    public static void runMain(Plugin plugin, Runnable r) {
        Bukkit.getScheduler().runTask(plugin, r);
    }

    // Minimal delayed execution
    public static Object runDelayed(Plugin plugin, Runnable r, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, r, delayTicks);
    }

    // Minimal repeating async execution
    public static Object runRepeatingAsync(Plugin plugin, Runnable r, long seconds) {
        final long ticks = Math.max(20L, seconds * 20L);
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, r, ticks, ticks);
    }

    // Minimal cancel (no-op)
    public static void cancelHandle(Object handle) {
        // No-op for minimal compatibility
    }
}
