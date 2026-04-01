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
}
