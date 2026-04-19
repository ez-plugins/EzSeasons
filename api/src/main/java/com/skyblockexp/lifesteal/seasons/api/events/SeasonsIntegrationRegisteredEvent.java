package com.skyblockexp.lifesteal.seasons.api.events;

import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import com.skyblockexp.lifesteal.seasons.api.SeasonsIntegration;
import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a {@link SeasonsIntegration} is registered through {@link SeasonsApi}.
 * <p>
 * Ordering guarantee: this event is dispatched only after the integration has already been added to EzSeasons'
 * internal integration registry and after {@link SeasonsIntegration#onRegister(SeasonsApi)} returns normally.
 * <p>
 * Threading: fired on the thread that invoked {@link SeasonsApi#registerIntegration(SeasonsIntegration)}.
 */
public final class SeasonsIntegrationRegisteredEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final SeasonsApi api;

    private final SeasonsIntegration integration;

    /**
     * Constructs a new integration-registered event.
     *
     * @param api         API instance that performed the registration; must be non-null
     * @param integration registered integration instance; must be non-null
     * @throws NullPointerException if {@code api} or {@code integration} is {@code null}
     */
    public SeasonsIntegrationRegisteredEvent(SeasonsApi api, SeasonsIntegration integration) {
        this.api = Objects.requireNonNull(api, "api");
        this.integration = Objects.requireNonNull(integration, "integration");
    }

    /**
     * @return API instance that performed the registration; never {@code null}
     */
    public SeasonsApi getApi() {
        return api;
    }

    /**
     * @return integration instance that was registered; never {@code null}
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
