package com.skyblockexp.lifesteal.seasons.api.events;

import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import com.skyblockexp.lifesteal.seasons.api.SeasonsIntegration;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

/**
 * Fired when a {@link SeasonsIntegration} is unregistered through {@link SeasonsApi}.
 * <p>
 * Ordering guarantee: this event is dispatched only after the integration has already been removed from EzSeasons'
 * internal integration registry and after {@link SeasonsIntegration#onUnregister()} returns normally.
 * <p>
 * Threading: fired on the thread that invoked {@link SeasonsApi#unregisterIntegration(SeasonsIntegration)}.
 */
public final class SeasonsIntegrationUnregisteredEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final SeasonsApi api;
    private final SeasonsIntegration integration;

    /**
     * Constructs a new integration-unregistered event.
     *
     * @param api         API instance that performed the unregistration; must be non-null
     * @param integration unregistered integration instance; must be non-null
     * @throws NullPointerException if {@code api} or {@code integration} is {@code null}
     */
    public SeasonsIntegrationUnregisteredEvent(SeasonsApi api, SeasonsIntegration integration) {
        this.api = Objects.requireNonNull(api, "api");
        this.integration = Objects.requireNonNull(integration, "integration");
    }

    /**
     * @return API instance that performed the unregistration; never {@code null}
     */
    public SeasonsApi getApi() {
        return api;
    }

    /**
     * @return integration instance that was unregistered; never {@code null}
     */
    public SeasonsIntegration getIntegration() {
        return integration;
    }

    /**
     * @return Bukkit handlers for this event instance; never {@code null}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * @return Bukkit handler list for this event type; never {@code null}
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
