package com.skyblockexp.lifesteal.seasons;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import com.skyblockexp.lifesteal.seasons.api.SeasonsIntegration;
import com.skyblockexp.lifesteal.seasons.api.events.SeasonResetEvent;
import com.skyblockexp.lifesteal.seasons.api.events.SeasonsIntegrationRegisteredEvent;
import com.skyblockexp.lifesteal.seasons.api.events.SeasonsIntegrationUnregisteredEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeasonsApiImplEventUnitTest {

    private ServerMock server;

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void registerIntegrationFiresCustomBukkitEvent() {
        server = MockBukkit.mock();
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        RegisteredCapture capture = new RegisteredCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());

        TestIntegration integration = new TestIntegration();

        boolean registered = api.registerIntegration(integration);

        assertTrue(registered);
        assertSame(api, capture.api);
        assertSame(integration, capture.integration);
        assertEquals(1, capture.eventsFired);
    }

    @Test
    void duplicateRegisterIntegrationReturnsFalseAndDoesNotFireSecondEvent() {
        server = MockBukkit.mock();
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        RegisteredCapture capture = new RegisteredCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());
        TestIntegration integration = new TestIntegration();

        boolean firstRegistered = api.registerIntegration(integration);
        boolean secondRegistered = api.registerIntegration(integration);

        assertTrue(firstRegistered);
        assertFalse(secondRegistered);
        assertSame(api, capture.api);
        assertSame(integration, capture.integration);
        assertEquals(1, capture.eventsFired);
    }

    @Test
    void registerIntegrationWithNullThrowsNullPointerException() {
        SeasonsApiImpl api = new SeasonsApiImpl(null);

        assertThrows(NullPointerException.class, () -> api.registerIntegration(null));
    }

    @Test
    void unregisterIntegrationFiresCustomBukkitEvent() {
        server = MockBukkit.mock();
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        UnregisteredCapture capture = new UnregisteredCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());
        TestIntegration integration = new TestIntegration();
        api.registerIntegration(integration);

        api.unregisterIntegration(integration);

        assertSame(api, capture.api);
        assertSame(integration, capture.integration);
        assertEquals(1, capture.eventsFired);
    }

    @Test
    void unregisterUnknownIntegrationDoesNotFireEvent() {
        server = MockBukkit.mock();
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        UnregisteredCapture capture = new UnregisteredCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());

        api.unregisterIntegration(new TestIntegration());

        assertNull(capture.api);
        assertNull(capture.integration);
        assertEquals(0, capture.eventsFired);
    }

    @Test
    void unregisterNullIntegrationIsNoOp() {
        SeasonsApiImpl api = new SeasonsApiImpl(null);

        assertDoesNotThrow(() -> api.unregisterIntegration(null));
        assertTrue(api.getIntegrations().isEmpty());
    }

    @Test
    void registerIntegrationFailureRollsBackAndDoesNotFireEvent() {
        server = MockBukkit.mock();
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        RegisteredCapture capture = new RegisteredCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());
        ThrowingRegisterIntegration integration = new ThrowingRegisterIntegration();

        assertThrows(IllegalStateException.class, () -> api.registerIntegration(integration));

        assertTrue(api.getIntegrations().isEmpty());
        assertNull(capture.api);
        assertNull(capture.integration);
        assertEquals(0, capture.eventsFired);
    }

    @Test
    void checkedExceptionInRegisterIsWrappedAndRolledBack() {
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        CheckedExceptionRegisterIntegration integration = new CheckedExceptionRegisterIntegration();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> api.registerIntegration(integration));

        assertTrue(exception.getCause() instanceof Exception);
        assertEquals("checked register failure", exception.getCause().getMessage());
        assertTrue(api.getIntegrations().isEmpty());
    }

    @Test
    void unregisterIntegrationFailureRestoresAndDoesNotFireEvent() {
        server = MockBukkit.mock();
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        UnregisteredCapture capture = new UnregisteredCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());
        ThrowingUnregisterIntegration integration = new ThrowingUnregisterIntegration();
        api.registerIntegration(integration);

        assertThrows(IllegalStateException.class, () -> api.unregisterIntegration(integration));

        assertEquals(1, api.getIntegrations().size());
        assertSame(integration, api.getIntegrations().get(0));
        assertNull(capture.api);
        assertNull(capture.integration);
        assertEquals(0, capture.eventsFired);
    }

    @Test
    void checkedExceptionInUnregisterIsWrappedAndStateIsRestored() {
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        CheckedExceptionUnregisterIntegration integration = new CheckedExceptionUnregisterIntegration();
        api.registerIntegration(integration);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> api.unregisterIntegration(integration));

        assertTrue(exception.getCause() instanceof Exception);
        assertEquals("checked unregister failure", exception.getCause().getMessage());
        assertEquals(1, api.getIntegrations().size());
        assertSame(integration, api.getIntegrations().get(0));
    }

    @Test
    void triggerSeasonResetWithNullReasonFallsBackToUnspecified() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        ResetCapture capture = new ResetCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());

        RegisteredServiceProvider<SeasonsApi> registration =
                server.getServicesManager().getRegistration(SeasonsApi.class);

        assertNotNull(registration);
        boolean triggered = registration.getProvider().triggerSeasonReset(null);

        assertTrue(triggered);
        assertNotNull(capture.event);
        assertEquals("unspecified", capture.event.getReason());
    }

    @Test
    void triggerSeasonResetFiresSeasonResetEvent() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        ResetCapture capture = new ResetCapture();
        server.getPluginManager().registerEvents(capture, MockBukkit.createMockPlugin());

        RegisteredServiceProvider<SeasonsApi> registration =
                server.getServicesManager().getRegistration(SeasonsApi.class);

        assertNotNull(registration);
        boolean triggered = registration.getProvider().triggerSeasonReset("test");

        assertTrue(triggered);
        assertNotNull(capture.event);
        assertEquals("test", capture.event.getReason());
        assertTrue(capture.event.getResetMillis() > 0L);
    }

    @Test
    void triggerSeasonResetReturnsFalseWhenPluginIsNullOrHasNoSeasonManager() {
        SeasonsApiImpl nullPluginApi = new SeasonsApiImpl(null);
        assertFalse(nullPluginApi.triggerSeasonReset("test"));

        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        plugin.registerSeasonManager(null);
        SeasonsApiImpl noManagerApi = new SeasonsApiImpl(plugin);

        assertFalse(noManagerApi.triggerSeasonReset("test"));
    }

    @Test
    void getIntegrationsReturnsImmutableSnapshot() {
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        TestIntegration first = new TestIntegration();
        TestIntegration second = new TestIntegration();
        api.registerIntegration(first);

        java.util.List<SeasonsIntegration> snapshot = api.getIntegrations();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(second));
        assertThrows(UnsupportedOperationException.class, snapshot::clear);

        java.util.List<SeasonsIntegration> current = api.getIntegrations();
        assertEquals(1, current.size());
        assertSame(first, current.get(0));
    }

    @Test
    void clearRemovesAllIntegrations() {
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        api.registerIntegration(new TestIntegration());
        api.registerIntegration(new TestIntegration());

        api.clear();

        assertTrue(api.getIntegrations().isEmpty());
    }

    private static final class TestIntegration implements SeasonsIntegration {
        @Override
        public void onRegister(SeasonsApi api) {
        }

        @Override
        public void onUnregister() {
        }
    }

    private static final class ThrowingRegisterIntegration implements SeasonsIntegration {
        @Override
        public void onRegister(SeasonsApi api) {
            throw new IllegalStateException("register failed");
        }

        @Override
        public void onUnregister() {
        }
    }

    private static final class ThrowingUnregisterIntegration implements SeasonsIntegration {
        @Override
        public void onRegister(SeasonsApi api) {
        }

        @Override
        public void onUnregister() {
            throw new IllegalStateException("unregister failed");
        }
    }

    private static final class CheckedExceptionRegisterIntegration implements SeasonsIntegration {
        @Override
        public void onRegister(SeasonsApi api) {
            sneakyThrow(new Exception("checked register failure"));
        }

        @Override
        public void onUnregister() {
        }
    }

    private static final class CheckedExceptionUnregisterIntegration implements SeasonsIntegration {
        @Override
        public void onRegister(SeasonsApi api) {
        }

        @Override
        public void onUnregister() {
            sneakyThrow(new Exception("checked unregister failure"));
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable throwable) throws E {
        throw (E) throwable;
    }

    private static final class RegisteredCapture implements Listener {
        private SeasonsApi api;
        private SeasonsIntegration integration;
        private int eventsFired;

        @EventHandler
        public void onRegistered(SeasonsIntegrationRegisteredEvent event) {
            this.eventsFired++;
            this.api = event.getApi();
            this.integration = event.getIntegration();
        }
    }

    private static final class UnregisteredCapture implements Listener {
        private SeasonsApi api;
        private SeasonsIntegration integration;
        private int eventsFired;

        @EventHandler
        public void onUnregistered(SeasonsIntegrationUnregisteredEvent event) {
            this.eventsFired++;
            this.api = event.getApi();
            this.integration = event.getIntegration();
        }
    }

    private static final class ResetCapture implements Listener {
        private SeasonResetEvent event;

        @EventHandler
        public void onReset(SeasonResetEvent event) {
            this.event = event;
        }
    }
}
