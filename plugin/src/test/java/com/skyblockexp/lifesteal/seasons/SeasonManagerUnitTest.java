package com.skyblockexp.lifesteal.seasons;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.skyblockexp.lifesteal.seasons.api.events.SeasonResetEvent;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SeasonManagerUnitTest {

    private ServerMock server;

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void formatDurationFormatsHoursMinutesAndSeconds() {
        SeasonManager seasonManager = new SeasonManager(null, null, null);

        String formatted = seasonManager.formatDuration(Duration.ofSeconds(3661));

        assertEquals("1:01:01", formatted);
    }

    @Test
    void formatDurationFormatsNegativeDurations() {
        SeasonManager seasonManager = new SeasonManager(null, null, null);

        String formatted = seasonManager.formatDuration(Duration.ofSeconds(-59));

        assertEquals("-0:00:59", formatted);
    }

    @Test
    void constructorParsesBasicConfigFromMemoryConfiguration() throws Exception {
        MemoryConfiguration section = new MemoryConfiguration();
        section.set("enabled", true);
        section.set("length-days", 7L);
        section.set("check-interval-minutes", 15L);

        SeasonManager seasonManager = new SeasonManager(null, section, null);

        assertTrue(seasonManager.isEnabled());
        assertEquals(Duration.ofDays(7), readDurationField(seasonManager, "seasonLength"));
        assertEquals(Duration.ofMinutes(15), readDurationField(seasonManager, "checkInterval"));
    }

    @Test
    void constructorFallsBackWhenExplicitWindowIsInvalid() throws Exception {
        MemoryConfiguration section = new MemoryConfiguration();
        long now = System.currentTimeMillis();
        section.set("start", now + 50_000L);
        section.set("end", now);
        section.set("length-days", 3L);

        SeasonManager seasonManager = new SeasonManager(null, section, null);

        assertFalse(readBooleanField(seasonManager, "explicitSeason"));
        assertEquals(0L, readLongField(seasonManager, "seasonStartMillis"));
        assertEquals(0L, readLongField(seasonManager, "seasonEndMillis"));
        assertEquals(Duration.ofDays(3), readDurationField(seasonManager, "seasonLength"));
    }

    @Test
    void constructorTurnsOffRecurringWhenExplicitWindowIsInvalidWhenPluginIsPresent() throws Exception {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);

        MemoryConfiguration section = new MemoryConfiguration();
        long now = System.currentTimeMillis();
        section.set("start", now + 50_000L);
        section.set("end", now);
        section.set("recurring", true);

        SeasonManager seasonManager = new SeasonManager(plugin, section, null);

        assertFalse(readBooleanField(seasonManager, "recurring"));
    }

    @Test
    void getTimeUntilResetUsesNextResetMillisWhenItIsInFuture() throws Exception {
        SeasonManager seasonManager = new SeasonManager(null, null, null);
        long now = System.currentTimeMillis();
        writeLongField(seasonManager, "nextResetMillis", now + 4_000L);
        writeLongField(seasonManager, "lastResetMillis", now - 1_000L);
        writeDurationField(seasonManager, "seasonLength", Duration.ofDays(10));

        Optional<Duration> timeUntilReset = seasonManager.getTimeUntilReset();

        assertTrue(timeUntilReset.isPresent());
        assertTrue(timeUntilReset.get().toMillis() > 0);
        assertTrue(timeUntilReset.get().toMillis() <= 4_000L);
    }

    @Test
    void getTimeUntilResetFallsBackToLastResetPlusSeasonLength() throws Exception {
        SeasonManager seasonManager = new SeasonManager(null, null, null);
        long now = System.currentTimeMillis();
        writeLongField(seasonManager, "nextResetMillis", now - 1_000L);
        writeLongField(seasonManager, "lastResetMillis", now - 5_000L);
        writeDurationField(seasonManager, "seasonLength", Duration.ofSeconds(20));

        Optional<Duration> timeUntilReset = seasonManager.getTimeUntilReset();

        assertTrue(timeUntilReset.isPresent());
        assertTrue(timeUntilReset.get().toMillis() > 10_000L);
        assertTrue(timeUntilReset.get().toMillis() <= 15_000L);
    }

    @Test
    void getTimeUntilResetReturnsEmptyWhenNoFutureResetExists() throws Exception {
        SeasonManager seasonManager = new SeasonManager(null, null, null);
        long now = System.currentTimeMillis();
        writeLongField(seasonManager, "nextResetMillis", now - 1_000L);
        writeLongField(seasonManager, "lastResetMillis", now - 10_000L);
        writeDurationField(seasonManager, "seasonLength", Duration.ofSeconds(5));

        assertTrue(seasonManager.getTimeUntilReset().isEmpty());
    }

    @Test
    void triggerSeasonResetUpdatesConfigAndFiresEvent() {
        server = MockBukkit.mock();
        ResetCapture capture = new ResetCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());

        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        MemoryConfiguration root = new MemoryConfiguration();
        MemoryConfiguration seasonSection = new MemoryConfiguration();
        root.set("season", seasonSection);
        seasonSection.set("length-days", 2L);
        seasonSection.set("last-reset", 321L);

        SeasonManager seasonManager = new SeasonManager(plugin, seasonSection, null);

        seasonManager.triggerSeasonReset("manual-reset");

        long savedLastReset = seasonSection.getLong("last-reset");
        long savedNextReset = seasonSection.getLong("next-reset");

        assertTrue(savedLastReset > 0L);
        assertEquals(savedLastReset + Duration.ofDays(2).toMillis(), savedNextReset);
        assertNotNull(capture.event);
        assertEquals(321L, capture.event.getPreviousResetMillis());
        assertEquals(savedLastReset, capture.event.getResetMillis());
        assertEquals(savedNextReset, capture.event.getNextResetMillis());
        assertEquals("manual-reset", capture.event.getReason());
    }

    @Test
    void triggerSeasonResetDefaultReasonViaEventWhenReasonIsNull() {
        server = MockBukkit.mock();
        ResetCapture capture = new ResetCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());

        SeasonManager seasonManager = new SeasonManager(null, new MemoryConfiguration(), null);

        seasonManager.triggerSeasonReset(null);

        assertNotNull(capture.event);
        assertEquals("unspecified", capture.event.getReason());
    }

    @Test
    void triggerSeasonResetKeepsBlankReasonAsProvided() {
        server = MockBukkit.mock();
        ResetCapture capture = new ResetCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());

        SeasonManager seasonManager = new SeasonManager(null, new MemoryConfiguration(), null);

        seasonManager.triggerSeasonReset("   ");

        assertNotNull(capture.event);
        assertEquals("   ", capture.event.getReason());
    }

    @Test
    void constructorFiltersReminderMinutesAndSortsOffsets() throws Exception {
        MemoryConfiguration section = new MemoryConfiguration();
        section.set("reminder-minutes", java.util.Arrays.asList(30, -1, 5, 0, 15));

        SeasonManager seasonManager = new SeasonManager(null, section, null);

        @SuppressWarnings("unchecked")
        java.util.List<Duration> reminderOffsets = (java.util.List<Duration>) readField(seasonManager, "reminderOffsets");
        assertEquals(java.util.List.of(Duration.ofMinutes(5), Duration.ofMinutes(15), Duration.ofMinutes(30)), reminderOffsets);
    }

    @Test
    void setLastResetMillisClampsNegativeValuesAndPersistsConfig() {
        MemoryConfiguration section = new MemoryConfiguration();
        SeasonManager seasonManager = new SeasonManager(null, section, null);

        seasonManager.setLastResetMillis(-100L);
        assertEquals(0L, seasonManager.getLastResetMillis());
        assertEquals(0L, section.getLong("last-reset"));

        seasonManager.setLastResetMillis(1234L);
        assertEquals(1234L, seasonManager.getLastResetMillis());
        assertEquals(1234L, section.getLong("last-reset"));
    }

    @Test
    void shouldTriggerResetCoversDisabledScheduledLengthAndExplicitPaths() throws Exception {
        SeasonManager seasonManager = new SeasonManager(null, null, null);

        writeBooleanField(seasonManager, "enabled", false);
        assertFalse(seasonManager.shouldTriggerReset(System.currentTimeMillis()));

        writeBooleanField(seasonManager, "enabled", true);
        long now = System.currentTimeMillis();

        writeLongField(seasonManager, "nextResetMillis", now + 1_000L);
        assertFalse(seasonManager.shouldTriggerReset(now));
        assertTrue(seasonManager.shouldTriggerReset(now + 1_000L));

        writeLongField(seasonManager, "nextResetMillis", 0L);
        writeDurationField(seasonManager, "seasonLength", Duration.ofSeconds(5));
        writeLongField(seasonManager, "lastResetMillis", now - 2_000L);
        assertFalse(seasonManager.shouldTriggerReset(now));
        assertTrue(seasonManager.shouldTriggerReset(now + 5_000L));

        writeDurationField(seasonManager, "seasonLength", Duration.ZERO);
        writeBooleanField(seasonManager, "explicitSeason", true);
        writeLongField(seasonManager, "seasonEndMillis", now + 5_000L);
        assertFalse(seasonManager.shouldTriggerReset(now));
        assertTrue(seasonManager.shouldTriggerReset(now + 5_000L));
    }

    @Test
    void shouldTriggerResetReturnsFalseWhenNoScheduleExists() throws Exception {
        SeasonManager seasonManager = new SeasonManager(null, null, null);
        writeBooleanField(seasonManager, "enabled", true);
        writeLongField(seasonManager, "nextResetMillis", 0L);
        writeLongField(seasonManager, "lastResetMillis", 0L);
        writeDurationField(seasonManager, "seasonLength", Duration.ZERO);
        writeBooleanField(seasonManager, "explicitSeason", false);
        writeLongField(seasonManager, "seasonEndMillis", 0L);

        assertFalse(seasonManager.shouldTriggerReset(System.currentTimeMillis()));
    }

    private static Duration readDurationField(SeasonManager seasonManager, String fieldName) throws Exception {
        Field field = SeasonManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Duration) field.get(seasonManager);
    }

    private static boolean readBooleanField(SeasonManager seasonManager, String fieldName) throws Exception {
        Field field = SeasonManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getBoolean(seasonManager);
    }

    private static long readLongField(SeasonManager seasonManager, String fieldName) throws Exception {
        Field field = SeasonManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getLong(seasonManager);
    }

    private static void writeDurationField(SeasonManager seasonManager, String fieldName, Duration value) throws Exception {
        Field field = SeasonManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(seasonManager, value);
    }

    private static void writeLongField(SeasonManager seasonManager, String fieldName, long value) throws Exception {
        Field field = SeasonManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setLong(seasonManager, value);
    }

    private static Object readField(SeasonManager seasonManager, String fieldName) throws Exception {
        Field field = SeasonManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(seasonManager);
    }

    private static void writeBooleanField(SeasonManager seasonManager, String fieldName, boolean value) throws Exception {
        Field field = SeasonManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setBoolean(seasonManager, value);
    }

    private static final class ResetCapture implements Listener {
        private SeasonResetEvent event;

        @EventHandler
        public void onReset(SeasonResetEvent event) {
            this.event = event;
        }
    }
}
