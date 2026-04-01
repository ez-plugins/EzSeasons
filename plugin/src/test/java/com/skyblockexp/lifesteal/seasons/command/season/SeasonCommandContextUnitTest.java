package com.skyblockexp.lifesteal.seasons.command.season;

import com.skyblockexp.lifesteal.seasons.EzSeasonsPlugin;
import com.skyblockexp.lifesteal.seasons.SeasonManager;
import com.skyblockexp.lifesteal.seasons.config.MessageService;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeasonCommandContextUnitTest {

    private EzSeasonsPlugin plugin;
    private MessageService messageService;
    private SeasonManager seasonManager;
    private SeasonCommandContext context;

    @BeforeEach
    void setUp() {
        plugin = mock(EzSeasonsPlugin.class);
        messageService = mock(MessageService.class);
        seasonManager = mock(SeasonManager.class);

        when(plugin.getMessageService()).thenReturn(messageService);
        when(plugin.getSeasonManager()).thenReturn(seasonManager);

        context = new SeasonCommandContext(plugin);
    }

    @Test
    void hasAdminPermissionReturnsTrueForPrimaryPermissionNode() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(SeasonCommandContext.SEASON_ADMIN_NODE)).thenReturn(true);

        assertTrue(context.hasAdminPermission(sender));
    }

    @Test
    void hasAdminPermissionReturnsTrueForLegacyPermissionNode() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(SeasonCommandContext.SEASON_ADMIN_NODE)).thenReturn(false);
        when(sender.hasPermission(SeasonCommandContext.LEGACY_ADMIN_NODE)).thenReturn(true);

        assertTrue(context.hasAdminPermission(sender));
    }

    @Test
    void hasAdminPermissionReturnsFalseWhenSenderLacksBothPermissionNodes() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(SeasonCommandContext.SEASON_ADMIN_NODE)).thenReturn(false);
        when(sender.hasPermission(SeasonCommandContext.LEGACY_ADMIN_NODE)).thenReturn(false);

        assertFalse(context.hasAdminPermission(sender));
    }

    @Test
    void sendSeasonStatusReportsDisabledWhenManagerMissingOrDisabled() {
        CommandSender sender = mock(CommandSender.class);
        when(plugin.getSeasonManager()).thenReturn(null);

        assertTrue(context.sendSeasonStatus(sender));
        verify(messageService).sendMessage(sender, "season-disabled");
    }

    @Test
    void requireEnabledSeasonManagerReturnsNullAndSendsDisabledMessageOnceWhenUnavailable() {
        CommandSender sender = mock(CommandSender.class);
        when(plugin.getSeasonManager()).thenReturn(null);

        SeasonManager manager = context.requireEnabledSeasonManager(sender);

        assertNull(manager);
        verify(messageService, times(1)).sendMessage(sender, "season-disabled");
    }

    @Test
    void sendSeasonStatusReportsUnknownWhenRemainingTimeCannotBeComputed() {
        CommandSender sender = mock(CommandSender.class);
        when(seasonManager.isEnabled()).thenReturn(true);
        when(seasonManager.getTimeUntilReset()).thenReturn(Optional.empty());

        assertTrue(context.sendSeasonStatus(sender));
        verify(messageService).sendMessage(sender, "season-status-unknown");
    }

    @Test
    void sendSeasonStatusSendsFormattedTimeWhenResetEstimateExists() {
        CommandSender sender = mock(CommandSender.class);
        Duration duration = Duration.ofMinutes(10);
        when(seasonManager.isEnabled()).thenReturn(true);
        when(seasonManager.getTimeUntilReset()).thenReturn(Optional.of(duration));
        when(seasonManager.formatDuration(duration)).thenReturn("0:10:00");

        assertTrue(context.sendSeasonStatus(sender));
        verify(messageService).sendMessage(sender, "season-status", Map.of("time", "0:10:00"));
    }

    @Test
    void parseTimestampReturnsParsedMillisWhenValueIsInRange() {
        CommandSender sender = mock(CommandSender.class);

        Long millis = context.parseTimestamp("1735689600000", sender);

        assertEquals(1735689600000L, millis);
    }

    @Test
    void parseTimestampReturnsNullAndSendsMessageForInvalidNumber() {
        CommandSender sender = mock(CommandSender.class);

        Long millis = context.parseTimestamp("not-a-number", sender);

        assertNull(millis);
        verify(messageService).sendMessage(sender, "admin-setnext-invalid-timestamp",
                Map.of("value", "not-a-number", "expected", "unix epoch milliseconds (example: 1735689600000)"));
    }

    @Test
    void parseTimestampReturnsNullAndSendsMessageForOutOfRangeValue() {
        CommandSender sender = mock(CommandSender.class);

        Long millis = context.parseTimestamp("0", sender);

        assertNull(millis);
        verify(messageService).sendMessage(sender, "admin-setnext-out-of-range",
                Map.of("value", "0", "min", "1", "max", "32503680000000"));
    }

    @Test
    void containsConfirmFlagMatchesCaseInsensitively() {
        assertTrue(context.containsConfirmFlag(new String[]{"--CONFIRM", "later"}));
        assertFalse(context.containsConfirmFlag(new String[]{"--check", "later"}));
    }

    @Test
    void extractReasonDefaultsToAdminOrSkipsConfirmFlag() {
        assertEquals("admin", context.extractReason(new String[0]));
        assertEquals("admin", context.extractReason(new String[]{"--confirm"}));
        assertEquals("manual reset now", context.extractReason(new String[]{"manual", "--confirm", "reset", "now"}));
    }

    @Test
    void formatInstantReturnsNotSetForNonPositiveValueAndIsoDateForPositiveMillis() {
        assertEquals("not-set", context.formatInstant(0L));
        long millis = 1735689600000L;
        String expected = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC)
                .format(Instant.ofEpochMilli(millis));
        assertEquals(expected, context.formatInstant(millis));
    }
}
