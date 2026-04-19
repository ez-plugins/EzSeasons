package com.skyblockexp.lifesteal.seasons.command.season;

import com.skyblockexp.lifesteal.seasons.EzSeasonsPlugin;
import com.skyblockexp.lifesteal.seasons.SeasonManager;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.bukkit.command.CommandSender;

public final class SeasonCommandContext {

    static final String SEASON_ADMIN_NODE = "lifesteal.season.admin";

    static final String LEGACY_ADMIN_NODE = "lifesteal.admin";

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

    private static final long MAX_TIMESTAMP_MILLIS = 32_503_680_000_000L;

    private final EzSeasonsPlugin plugin;

    public SeasonCommandContext(EzSeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public EzSeasonsPlugin getPlugin() {
        return plugin;
    }

    public boolean hasAdminPermission(CommandSender sender) {
        return sender.hasPermission(SEASON_ADMIN_NODE) || sender.hasPermission(LEGACY_ADMIN_NODE);
    }

    public SeasonManager requireEnabledSeasonManager(CommandSender sender) {
        final SeasonManager seasonManager = plugin.getSeasonManager();
        if (seasonManager == null || !seasonManager.isEnabled()) {
            plugin.getMessageService().sendMessage(sender, "season-disabled");
            return null;
        }
        return seasonManager;
    }

    public boolean sendSeasonStatus(CommandSender sender) {
        final SeasonManager seasonManager = requireEnabledSeasonManager(sender);
        if (seasonManager == null) {
            return true;
        }
        final Optional<Duration> timeUntilReset = seasonManager.getTimeUntilReset();
        if (timeUntilReset.isEmpty()) {
            plugin.getMessageService().sendMessage(sender, "season-status-unknown");
            return true;
        }
        final String formatted = seasonManager.formatDuration(timeUntilReset.get());
        plugin.getMessageService().sendMessage(sender, "season-status", Map.of("time", formatted));
        return true;
    }

    public Long parseTimestamp(String value, CommandSender sender) {
        final long millis;
        try {
            millis = Long.parseLong(value);
        }
        catch (NumberFormatException ex) {
            plugin.getMessageService().sendMessage(sender, "admin-setnext-invalid-timestamp",
                    Map.of("value", value, "expected", "unix epoch milliseconds (example: 1735689600000)"));
            return null;
        }

        if (millis <= 0L || millis > MAX_TIMESTAMP_MILLIS) {
            plugin.getMessageService().sendMessage(sender, "admin-setnext-out-of-range",
                    Map.of("value", String.valueOf(millis), "min", "1", "max", String.valueOf(MAX_TIMESTAMP_MILLIS)));
            return null;
        }
        return millis;
    }

    public boolean containsConfirmFlag(String[] args) {
        for (String arg : args) {
            if ("--confirm".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    public String extractReason(String[] args) {
        if (args.length == 0) {
            return "admin";
        }
        final String reason = Arrays.stream(args)
                .filter(token -> !"--confirm".equalsIgnoreCase(token))
                .reduce((left, right) -> left + " " + right)
                .orElse("")
                .trim();
        return reason.isEmpty() ? "admin" : reason;
    }

    public String formatInstant(long millis) {
        if (millis <= 0) {
            return "not-set";
        }
        return TIME_FORMATTER.format(Instant.ofEpochMilli(millis));
    }
}
