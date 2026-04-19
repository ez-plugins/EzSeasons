package com.skyblockexp.lifesteal.seasons.api;

/**
 * Interface for plugins to implement to integrate with EzSeasons.
 * Register your implementation via {@link SeasonsApi#registerIntegration(SeasonsIntegration)}.
 * <p>
 * Threading: callbacks are invoked on the same thread that called the corresponding
 * {@link SeasonsApi} method. EzSeasons does not marshal these callbacks onto the main thread.
 */
public interface SeasonsIntegration {
    /**
     * Called when the integration is registered with EzSeasons.
     *
     * @param api the EzSeasons API instance; never {@code null}
     * @throws RuntimeException propagated to the registering caller; EzSeasons does not swallow or log it here
     */
    void onRegister(SeasonsApi api);

    /**
     * Called when the integration is unregistered from EzSeasons.
     *
     * @throws RuntimeException propagated to the unregistering caller; EzSeasons does not swallow or log it here
     */
    void onUnregister();

    /**
     * Called after EzSeasons performs a season reset.
     * <p>
     * The default implementation is a no-op, so existing implementations are not required to override this method.
     * <p>
     * Threading: invoked on the same thread that triggered the reset (typically the Bukkit main thread).
     *
     * @param previousResetMillis unix epoch ms of the reset immediately before this one
     * @param resetMillis         unix epoch ms of this reset
     * @param nextResetMillis     unix epoch ms of the next scheduled reset, or {@code 0} if unscheduled
     * @param reason              caller-provided reason; never {@code null} — defaults to {@code "unspecified"}
     */
    default void onSeasonReset(long previousResetMillis, long resetMillis, long nextResetMillis, String reason) {
    }
}
