package com.skyblockexp.lifesteal.seasons.compatibility;

import org.bukkit.Bukkit;

public final class ServerEnvironment {
    private static final boolean HAS_ASYNC_SCHEDULER =
            hasMethod(Bukkit.class, "getAsyncScheduler");
    private static final boolean HAS_GLOBAL_REGION_SCHEDULER =
            hasMethod(Bukkit.class, "getGlobalRegionScheduler");

    private static final boolean FOLIA = HAS_ASYNC_SCHEDULER || hasClass("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
    private static final String BRAND;

    static {
        if (FOLIA) BRAND = "Folia";
        else if (hasClass("io.papermc.paper.util.TickThread") || hasClass("com.destroystokyo.paper.PaperVersionFetcher")) BRAND = "Paper";
        else BRAND = Bukkit.getServer().getName(); // CraftBukkit/Bukkit/etc.
    }

    private ServerEnvironment() {}

    public static boolean isFolia() { return FOLIA; }
    public static boolean hasAsyncScheduler() { return HAS_ASYNC_SCHEDULER; }
    public static boolean hasGlobalRegionScheduler() { return HAS_GLOBAL_REGION_SCHEDULER; }
    public static String brand() { return BRAND; }

    private static boolean hasMethod(Class<?> type, String name, Class<?>... params) {
        try { type.getMethod(name, params); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    private static boolean hasClass(String name) {
        try { Class.forName(name, false, ServerEnvironment.class.getClassLoader()); return true; }
        catch (ClassNotFoundException e) { return false; }
    }
}
