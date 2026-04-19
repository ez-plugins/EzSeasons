package com.skyblockexp.lifesteal.seasons.compatibility;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.skyblockexp.lifesteal.seasons.EzSeasonsPlugin;
import com.skyblockexp.lifesteal.seasons.command.SeasonPaperCommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommand;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompatibilityAndPaperCommandUnitTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void reflectionSchedulerDelegatesToBukkitSchedulerAndCancelIsNoOp() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        AtomicInteger runs = new AtomicInteger();

        ReflectionScheduler.runMain(plugin, runs::incrementAndGet);
        server.getScheduler().performOneTick();
        assertEquals(1, runs.get());

        ReflectionScheduler.runDelayed(plugin, runs::incrementAndGet, 2L);
        server.getScheduler().performTicks(2L);
        assertEquals(2, runs.get());

        ReflectionScheduler.runRepeatingAsync(plugin, runs::incrementAndGet, 0L);
        server.getScheduler().performTicks(20L);

        assertDoesNotThrow(() -> ReflectionScheduler.cancelHandle(new Object()));
    }

    @Test
    void serverEnvironmentReportsStableBrandAndFlags() throws Exception {
        assertNotNull(ServerEnvironment.brand());
        assertFalse(ServerEnvironment.brand().isBlank());

        Method hasMethod = ServerEnvironment.class.getDeclaredMethod("hasMethod", Class.class, String.class, Class[].class);
        hasMethod.setAccessible(true);
        boolean present = (boolean) hasMethod.invoke(null, String.class, "substring", new Class[]{int.class});
        boolean missing = (boolean) hasMethod.invoke(null, String.class, "definitelyMissingMethod", new Class[]{});
        assertTrue(present);
        assertFalse(missing);

        Method hasClass = ServerEnvironment.class.getDeclaredMethod("hasClass", String.class);
        hasClass.setAccessible(true);
        assertTrue((boolean) hasClass.invoke(null, "java.lang.String"));
        assertFalse((boolean) hasClass.invoke(null, "example.DoesNotExist"));

        Constructor<ServerEnvironment> constructor = ServerEnvironment.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());

        ServerEnvironment.isFolia();
        ServerEnvironment.hasAsyncScheduler();
        ServerEnvironment.hasGlobalRegionScheduler();
    }

    @Test
    void seasonPaperCommandDelegatesExecuteAndExposesPlugin() {
        EzSeasonsPlugin plugin = mock(EzSeasonsPlugin.class);
        SeasonCommand executor = mock(SeasonCommand.class);
        CommandSender sender = mock(CommandSender.class);

        SeasonPaperCommand command = new SeasonPaperCommand(plugin, executor);
        when(executor.onCommand(sender, command, "season", new String[]{"status"})).thenReturn(true);

        assertEquals(plugin, command.getPlugin());
        assertEquals("season", command.getName());
        assertTrue(command.execute(sender, "season", new String[]{"status"}));
    }

    @Test
    void seasonPaperCommandConstructorRejectsNullArguments() {
        EzSeasonsPlugin plugin = mock(EzSeasonsPlugin.class);
        SeasonCommand executor = mock(SeasonCommand.class);

        assertThrows(NullPointerException.class, () -> new SeasonPaperCommand(null, executor));
        assertThrows(NullPointerException.class, () -> new SeasonPaperCommand(plugin, null));
    }
}
