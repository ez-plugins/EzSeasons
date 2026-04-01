package com.skyblockexp.lifesteal.seasons;

import com.skyblockexp.lifesteal.seasons.api.events.SeasonResetEvent;
import com.skyblockexp.lifesteal.seasons.config.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SeasonManager {

    private final EzSeasonsPlugin plugin;
    private final MessageService messageService;
    private final boolean enabled;
    private final Duration seasonLength;
    private final Duration checkInterval;
    private final boolean explicitSeason;
    private final boolean recurring;
    private long seasonStartMillis;
    private long seasonEndMillis;
    private final String broadcastMessage;
    private final String reminderMessage;
    private final List<Duration> reminderOffsets;
    private long lastResetMillis;
    private long nextResetMillis;

    private final ConfigurationSection configSection;

    public SeasonManager(EzSeasonsPlugin plugin,
                         ConfigurationSection section,
                         MessageService messageService) {
        this.plugin = plugin;
        this.messageService = messageService;
        this.configSection = section;

        SeasonSettings settings = SeasonSettings.from(section).validate(plugin);

        this.enabled = settings.enabled;
        this.explicitSeason = settings.explicit;
        this.recurring = settings.recurring;
        this.seasonStartMillis = settings.explicit ? settings.start : 0L;
        this.seasonEndMillis = settings.explicit ? settings.end : 0L;
        this.seasonLength = settings.explicit ? Duration.ofMillis(settings.end - settings.start) : settings.length;
        this.checkInterval = settings.checkInterval.isZero() ? Duration.ofMinutes(60) : settings.checkInterval;
        this.broadcastMessage = settings.broadcastMessage;
        this.reminderMessage = settings.reminderMessage;
        this.reminderOffsets = settings.reminderOffsets;
        this.lastResetMillis = settings.lastReset;
        this.nextResetMillis = settings.nextReset;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Duration getCheckInterval() {
        return checkInterval;
    }

    public synchronized void triggerSeasonReset(String reason) {
        long previousReset = lastResetMillis;
        long resetAt = System.currentTimeMillis();
        lastResetMillis = resetAt;
        nextResetMillis = seasonLength.toMillis() > 0 ? resetAt + seasonLength.toMillis() : 0L;
        persistResetTimestamps();

        if (Bukkit.getServer() != null) {
            Bukkit.getPluginManager().callEvent(new SeasonResetEvent(previousReset, resetAt, nextResetMillis, reason));
        }
        if (broadcastMessage != null && !broadcastMessage.isBlank() && Bukkit.getServer() != null) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMessage));
        }
    }

    public synchronized void setNextResetMillis(long unixMillis) {
        nextResetMillis = Math.max(0L, unixMillis);
        persistResetTimestamps();
    }

    public synchronized void clearNextResetMillis() {
        nextResetMillis = 0L;
        persistResetTimestamps();
    }

    public synchronized void setLastResetMillis(long unixMillis) {
        lastResetMillis = Math.max(0L, unixMillis);
        persistResetTimestamps();
    }

    public synchronized long getLastResetMillis() {
        return lastResetMillis;
    }

    public synchronized long getNextResetMillis() {
        return nextResetMillis;
    }

    public boolean shouldTriggerReset(long nowMillis) {
        if (!enabled) {
            return false;
        }

        long scheduledNextReset;
        long recordedLastReset;
        synchronized (this) {
            scheduledNextReset = nextResetMillis;
            recordedLastReset = lastResetMillis;
        }

        if (scheduledNextReset > 0L) {
            return nowMillis >= scheduledNextReset;
        }
        if (seasonLength.toMillis() > 0L && recordedLastReset > 0L) {
            return nowMillis >= recordedLastReset + seasonLength.toMillis();
        }
        if (explicitSeason && seasonEndMillis > 0L) {
            return nowMillis >= seasonEndMillis;
        }
        return false;
    }

    public Optional<Duration> getTimeUntilReset() {
        long scheduledNextReset;
        long recordedLastReset;
        synchronized (this) {
            scheduledNextReset = nextResetMillis;
            recordedLastReset = lastResetMillis;
        }

        if (scheduledNextReset > 0) {
            long now = System.currentTimeMillis();
            long diff = scheduledNextReset - now;
            if (diff > 0) {
                return Optional.of(Duration.ofMillis(diff));
            }
        }
        if (seasonLength.toMillis() > 0 && recordedLastReset > 0) {
            long next = recordedLastReset + seasonLength.toMillis();
            long now = System.currentTimeMillis();
            long diff = next - now;
            if (diff > 0) {
                return Optional.of(Duration.ofMillis(diff));
            }
        }
        return Optional.empty();
    }

    public String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format("%d:%02d:%02d", absSeconds / 3600, (absSeconds % 3600) / 60, absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

    private synchronized void persistResetTimestamps() {
        if (configSection != null) {
            configSection.set("last-reset", lastResetMillis);
            configSection.set("next-reset", nextResetMillis);
        }
        if (plugin != null && configSection != null) {
            plugin.saveConfig();
        }
    }

    private static final class SeasonSettings {

        private static final String DEFAULT_BROADCAST = "&7A new season has begun! Hearts have been reset.";
        private static final String DEFAULT_REMINDER = "&7The season will reset in &c%time%&7.";

        private final boolean enabled;
        private final long start;
        private final long end;
        private final Duration length;
        private final Duration checkInterval;
        private final String broadcastMessage;
        private final String reminderMessage;
        private final List<Duration> reminderOffsets;
        private final long lastReset;
        private final long nextReset;
        private final boolean recurring;
        private final boolean explicit;

        private SeasonSettings(boolean enabled,
                               long start,
                               long end,
                               Duration length,
                               Duration checkInterval,
                               String broadcastMessage,
                               String reminderMessage,
                               List<Duration> reminderOffsets,
                               long lastReset,
                               long nextReset,
                               boolean recurring,
                               boolean explicit) {
            this.enabled = enabled;
            this.start = start;
            this.end = end;
            this.length = length;
            this.checkInterval = checkInterval;
            this.broadcastMessage = broadcastMessage;
            this.reminderMessage = reminderMessage;
            this.reminderOffsets = List.copyOf(reminderOffsets);
            this.lastReset = lastReset;
            this.nextReset = nextReset;
            this.recurring = recurring;
            this.explicit = explicit;
        }

        private SeasonSettings withRecurring(boolean recurring) {
            return new SeasonSettings(enabled, start, end, length, checkInterval, broadcastMessage, reminderMessage, reminderOffsets, lastReset, nextReset, recurring, explicit);
        }

        private static SeasonSettings from(ConfigurationSection section) {
            if (section == null) {
                return new SeasonSettings(
                        false,
                        0L,
                        0L,
                        Duration.ZERO,
                        Duration.ZERO,
                        DEFAULT_BROADCAST,
                        DEFAULT_REMINDER,
                        List.of(),
                        0L,
                        0L,
                        false,
                        false
                );
            }

            boolean enabled = section.getBoolean("enabled", false);
            long start = section.getLong("start", 0L);
            long end = section.getLong("end", 0L);
            Duration length = safeDurationDays(section.getLong("length-days", 30L));
            Duration checkInterval = safeDurationMinutes(section.getLong("check-interval-minutes", 60L));
            String message = section.getString("broadcast-message", DEFAULT_BROADCAST);
            String reminder = section.getString("reminder-message", DEFAULT_REMINDER);
            long lastReset = section.getLong("last-reset", 0L);
            long nextReset = section.getLong("next-reset", 0L);
            boolean recurring = section.getBoolean("recurring", false);
            List<Duration> reminders = parseAndSortReminderOffsets(section.getIntegerList("reminder-minutes"));
            boolean explicit = start > 0L && end > start;

            return new SeasonSettings(enabled, start, end, length, checkInterval, message, reminder, reminders, lastReset, nextReset, recurring, explicit);
        }

        private SeasonSettings validate(EzSeasonsPlugin plugin) {
            SeasonSettings settings = this;
            if (plugin != null && (start > 0L || end > 0L) && !explicit) {
                plugin.getLogger().warning("Season end must be after the start time. Falling back to duration based scheduling.");
            }
            if (plugin != null && recurring && !explicit) {
                plugin.getLogger().warning("Season recurrence requires explicit start and end timestamps. Ignoring the recurring setting.");
                settings = settings.withRecurring(false);
            }
            return settings;
        }

        private static Duration safeDurationDays(long days) {
            return days > 0 ? Duration.ofDays(days) : Duration.ZERO;
        }

        private static Duration safeDurationMinutes(long minutes) {
            return minutes > 0 ? Duration.ofMinutes(minutes) : Duration.ofMinutes(60);
        }

        private static List<Duration> parseAndSortReminderOffsets(List<Integer> reminderMinutes) {
            List<Duration> reminders = new ArrayList<>();
            for (Integer minutes : reminderMinutes) {
                if (minutes != null && minutes > 0) {
                    reminders.add(Duration.ofMinutes(minutes));
                }
            }
            reminders.sort(Comparator.comparingLong(Duration::toMillis));
            return reminders;
        }
    }
}
