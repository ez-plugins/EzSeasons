package com.skyblockexp.lifesteal.seasons.api.events;

import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired whenever EzSeasons performs a season reset.
 * <p>
 * Ordering guarantee: this event is dispatched only after internal reset timestamps have been updated and persisted.
 * <p>
 * Threading: fired on the thread that initiated the reset. In typical Bukkit usage this should be the main
 * server thread.
 */
public final class SeasonResetEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final long previousResetMillis;

    private final long resetMillis;

    private final long nextResetMillis;

    private final String reason;

    /**
     * Constructs a new season reset event snapshot.
     *
     * @param previousResetMillis reset timestamp immediately before this reset in unix epoch milliseconds
     * @param resetMillis         reset timestamp for this reset in unix epoch milliseconds
     * @param nextResetMillis     next scheduled reset timestamp in unix epoch milliseconds, or {@code 0} if unscheduled
     * @param reason              caller-provided reason; may be {@code null}, in which case
     *                            {@code "unspecified"} is stored
     */
    public SeasonResetEvent(long previousResetMillis,
                            long resetMillis,
                            long nextResetMillis,
                            String reason) {
        this.previousResetMillis = previousResetMillis;
        this.resetMillis = resetMillis;
        this.nextResetMillis = nextResetMillis;
        this.reason = Objects.requireNonNullElse(reason, "unspecified");
    }

    /**
     * @return reset timestamp immediately before this reset in unix epoch milliseconds
     */
    public long getPreviousResetMillis() {
        return previousResetMillis;
    }

    /**
     * @return reset timestamp for this reset in unix epoch milliseconds
     */
    public long getResetMillis() {
        return resetMillis;
    }

    /**
     * @return next scheduled reset timestamp in unix epoch milliseconds, or {@code 0} if unscheduled
     */
    public long getNextResetMillis() {
        return nextResetMillis;
    }

    /**
     * @return reset reason; never {@code null}. If the trigger reason was {@code null}, returns {@code "unspecified"}
     */
    public String getReason() {
        return reason;
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
