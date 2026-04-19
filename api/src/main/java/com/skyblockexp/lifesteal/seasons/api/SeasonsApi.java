package com.skyblockexp.lifesteal.seasons.api;

/**
 * Public API exposed by EzSeasons so companion plugins can register for season events and features.
 * <p>
 * Threading: implementations in EzSeasons are internally synchronized for integration registry access, so
 * concurrent calls are supported. However, callbacks and Bukkit events are executed on the calling thread.
 * Callers should invoke this API from the Bukkit main thread unless they fully control thread-safety of all
 * listeners and integrations involved.
 */
public interface SeasonsApi {

    /**
     * Registers a plugin integration with EzSeasons. The integration can listen for season events
     * or interact with the API.
     *
     * Ordering guarantee: internal registration state is updated <strong>before</strong>
     * {@link SeasonsIntegration#onRegister(SeasonsApi)} is invoked and before
     * {@code SeasonsIntegrationRegisteredEvent} is dispatched.
     * <p>
     * Exception behavior: exceptions thrown by {@link SeasonsIntegration#onRegister(SeasonsApi)} are not
     * caught by EzSeasons; they propagate to the caller after the integration has already been added.
     *
     * @param integration integration implementation provided by another plugin; must be non-null
     * @return {@code true} if registration succeeded and the integration is now active, otherwise {@code false}
     * if this exact instance is already registered
     * @throws NullPointerException if {@code integration} is {@code null}
     */
    boolean registerIntegration(SeasonsIntegration integration);

    /**
     * Unregisters a previously registered integration.
     *
     * Ordering guarantee: internal registration state is updated <strong>before</strong>
     * {@link SeasonsIntegration#onUnregister()} is invoked and before
     * {@code SeasonsIntegrationUnregisteredEvent} is dispatched.
     * <p>
     * Exception behavior: exceptions thrown by {@link SeasonsIntegration#onUnregister()} are not caught by
     * EzSeasons; they propagate to the caller after the integration has already been removed.
     *
     * @param integration integration instance previously registered; {@code null} is allowed and treated as a no-op
     * @throws RuntimeException if thrown by the integration callback
     */
    void unregisterIntegration(SeasonsIntegration integration);

    /**
     * Returns the currently registered integrations.
     *
     * The returned collection is a read-only snapshot of registration state at call time.
     * Mutating the returned list is unsupported and will throw {@link UnsupportedOperationException}.
     * <p>
     * API evolution note: a future major version may widen this return type to
     * {@code Collection<SeasonsIntegration>} to avoid implying ordering or mutability semantics.
     *
     * @return an immutable snapshot list containing all currently registered integrations; never {@code null}
     */
    java.util.List<SeasonsIntegration> getIntegrations();

    /**
     * Triggers a season reset immediately and emits {@code SeasonResetEvent}.
     *
     * Ordering guarantee: season reset timestamps are updated and persisted <strong>before</strong>
     * {@code SeasonResetEvent} is dispatched.
     *
     * @param reason caller-defined reason for observability (for example: "schedule", "admin", "migration");
     *               may be {@code null}
     * @return {@code true} if a season manager exists and a reset was triggered, otherwise {@code false};
     * never {@code null}
     * @throws RuntimeException if reset processing or downstream event handling throws
     */
    boolean triggerSeasonReset(String reason);
}
