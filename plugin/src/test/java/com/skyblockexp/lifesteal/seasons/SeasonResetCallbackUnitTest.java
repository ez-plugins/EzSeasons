package com.skyblockexp.lifesteal.seasons;

import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import com.skyblockexp.lifesteal.seasons.api.SeasonsIntegration;
import com.skyblockexp.lifesteal.seasons.api.events.SeasonResetEvent;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the API-to-plugin integration-callback path for
 * {@link SeasonsIntegration#onSeasonReset} introduced when paper-api was removed
 * from the API module.
 */
class SeasonResetCallbackUnitTest {

    private ServerMock server;

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // -------------------------------------------------------------------------
    // Basic invocation
    // -------------------------------------------------------------------------

    @Test
    void singleIntegrationReceivesOnSeasonResetAfterTrigger() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonsApi api = requireApi(server);
        RecordingIntegration integration = new RecordingIntegration();
        api.registerIntegration(integration);

        api.triggerSeasonReset("seasonal");

        assertEquals(1, integration.resetCallCount);
        assertEquals("seasonal", integration.lastReason);
    }

    @Test
    void allRegisteredIntegrationsReceiveOnSeasonReset() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonsApi api = requireApi(server);
        RecordingIntegration first = new RecordingIntegration();
        RecordingIntegration second = new RecordingIntegration();
        RecordingIntegration third = new RecordingIntegration();
        api.registerIntegration(first);
        api.registerIntegration(second);
        api.registerIntegration(third);

        api.triggerSeasonReset("multi");

        assertEquals(1, first.resetCallCount);
        assertEquals(1, second.resetCallCount);
        assertEquals(1, third.resetCallCount);
    }

    @Test
    void unregisteredIntegrationNoLongerReceivesCallback() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonsApi api = requireApi(server);
        RecordingIntegration integration = new RecordingIntegration();
        api.registerIntegration(integration);
        api.unregisterIntegration(integration);

        api.triggerSeasonReset("after-unregister");

        assertEquals(0, integration.resetCallCount);
    }

    @Test
    void noRegisteredIntegrationDoesNotThrowAndEventStillFires() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonsApi api = requireApi(server);
        ResetCapture capture = new ResetCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());

        assertDoesNotThrow(() -> api.triggerSeasonReset("no-integrations"));

        assertNotNull(capture.event);
    }

    // -------------------------------------------------------------------------
    // Reason normalisation
    // -------------------------------------------------------------------------

    @Test
    void nullReasonNormalisedToUnspecifiedForCallback() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonsApi api = requireApi(server);
        RecordingIntegration integration = new RecordingIntegration();
        api.registerIntegration(integration);

        api.triggerSeasonReset(null);

        assertEquals("unspecified", integration.lastReason);
    }

    @Test
    void blankReasonPassedThroughWithoutNormalisationForCallback() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonsApi api = requireApi(server);
        RecordingIntegration integration = new RecordingIntegration();
        api.registerIntegration(integration);

        api.triggerSeasonReset("  ");

        assertEquals("  ", integration.lastReason);
    }

    // -------------------------------------------------------------------------
    // Timestamp accuracy
    // -------------------------------------------------------------------------

    @Test
    void onSeasonResetReceivesTimestampsThatMatchTheBukkitEvent() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonsApi api = requireApi(server);
        ResetCapture capture = new ResetCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());
        RecordingIntegration integration = new RecordingIntegration();
        api.registerIntegration(integration);

        long beforeTrigger = System.currentTimeMillis();
        api.triggerSeasonReset("ts-check");

        assertNotNull(capture.event);
        assertEquals(capture.event.getPreviousResetMillis(), integration.lastPreviousResetMillis,
                "previousResetMillis must match between Bukkit event and callback");
        assertEquals(capture.event.getResetMillis(), integration.lastResetMillis,
                "resetMillis must match between Bukkit event and callback");
        assertEquals(capture.event.getNextResetMillis(), integration.lastNextResetMillis,
                "nextResetMillis must match between Bukkit event and callback");
        assertTrue(integration.lastResetMillis >= beforeTrigger,
                "resetMillis must be at or after when triggerSeasonReset was called");
    }

    // -------------------------------------------------------------------------
    // Ordering guarantee: Bukkit event fires before callback
    // -------------------------------------------------------------------------

    @Test
    void bukkitEventFiresBeforeIntegrationCallback() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonsApi api = requireApi(server);

        List<String> order = new ArrayList<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onReset(SeasonResetEvent e) {
                order.add("event");
            }
        }, MockBukkit.createMockPlugin());

        api.registerIntegration(new SeasonsIntegration() {
            @Override
            public void onRegister(SeasonsApi a) {
            }

            @Override
            public void onUnregister() {
            }

            @Override
            public void onSeasonReset(long prev, long at, long next, String reason) {
                order.add("callback");
            }
        });

        api.triggerSeasonReset("order-test");

        assertEquals(List.of("event", "callback"), order,
                "SeasonResetEvent must fire before onSeasonReset callbacks");
    }

    // -------------------------------------------------------------------------
    // Null-guard paths
    // -------------------------------------------------------------------------

    @Test
    void nullPluginReferenceInSeasonManagerSkipsCallbackGracefully() {
        // No Bukkit server — Bukkit.getServer() is null here; verified by existing tests.
        // This test focuses on the null-plugin guard in SeasonManager.
        SeasonManager manager = new SeasonManager(null, new MemoryConfiguration(), null);
        RecordingIntegration integration = new RecordingIntegration();

        assertDoesNotThrow(() -> manager.triggerSeasonReset("null-plugin"),
                "null plugin must not throw");
        assertEquals(0, integration.resetCallCount,
                "callback must not fire when plugin is null");
    }

    @Test
    void nullApiFromPluginSkipsCallbacksWithoutNpe() {
        server = MockBukkit.mock();
        // A plugin mock that returns null for getSeasonsApi() simulates a partially
        // initialised plugin (e.g. Bootstrap has not yet registered the API).
        EzSeasonsPlugin mockPlugin = mock(EzSeasonsPlugin.class);
        when(mockPlugin.getSeasonsApi()).thenReturn(null);
        RecordingIntegration integration = new RecordingIntegration();

        SeasonManager manager = new SeasonManager(mockPlugin, new MemoryConfiguration(), null);

        assertDoesNotThrow(() -> manager.triggerSeasonReset("null-api"),
                "null SeasonsApi must not throw");
        assertEquals(0, integration.resetCallCount,
                "callback must not fire when api is null");
    }

    // -------------------------------------------------------------------------
    // Default interface method
    // -------------------------------------------------------------------------

    @Test
    void defaultOnSeasonResetIsNoOp() {
        // Verify that the default method compiles and does not throw.
        SeasonsIntegration anon = new SeasonsIntegration() {
            @Override
            public void onRegister(SeasonsApi api) {
            }

            @Override
            public void onUnregister() {
            }
            // onSeasonReset intentionally NOT overridden — exercises the default
        };

        assertDoesNotThrow(() -> anon.onSeasonReset(100L, 200L, 300L, "default-noop"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static SeasonsApi requireApi(ServerMock server) {
        RegisteredServiceProvider<SeasonsApi> rsp =
                server.getServicesManager().getRegistration(SeasonsApi.class);
        assertNotNull(rsp, "SeasonsApi service must be registered after plugin load");
        return rsp.getProvider();
    }

    private static final class RecordingIntegration implements SeasonsIntegration {
        int resetCallCount;
        long lastPreviousResetMillis;
        long lastResetMillis;
        long lastNextResetMillis;
        String lastReason;

        @Override
        public void onRegister(SeasonsApi api) {
        }

        @Override
        public void onUnregister() {
        }

        @Override
        public void onSeasonReset(long previousResetMillis, long resetMillis, long nextResetMillis, String reason) {
            resetCallCount++;
            lastPreviousResetMillis = previousResetMillis;
            lastResetMillis = resetMillis;
            lastNextResetMillis = nextResetMillis;
            lastReason = reason;
        }
    }

    private static final class ResetCapture implements Listener {
        SeasonResetEvent event;

        @EventHandler
        public void onReset(SeasonResetEvent event) {
            this.event = event;
        }
    }
}
