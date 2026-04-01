package com.skyblockexp.lifesteal.seasons;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.skyblockexp.lifesteal.seasons.config.MessageService;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BootstrapUnitTest {

    private ServerMock server;

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void loadMessagesFallsBackToEnglishForUnsupportedLanguage() {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        Registry registry = new Registry();
        Bootstrap bootstrap = new Bootstrap(plugin, registry);

        plugin.getConfig().set("messages.language", "klingon");
        bootstrap.loadMessages();

        assertNotNull(registry.getMessageService());
        assertFalse(registry.getMessageService().getMessage("season-disabled").isEmpty());
    }

    @Test
    void privateLoadLanguageSectionHandlesMissingAndNestedMessagesSection() throws Exception {
        EzSeasonsPlugin plugin = mock(EzSeasonsPlugin.class);
        Registry registry = new Registry();
        Bootstrap bootstrap = new Bootstrap(plugin, registry);

        when(plugin.getLogger()).thenReturn(Logger.getLogger("bootstrap-test"));

        Method loadLanguageSection = Bootstrap.class.getDeclaredMethod("loadLanguageSection", File.class, String.class);
        loadLanguageSection.setAccessible(true);

        File folder = Files.createTempDirectory("ezseasons-msg").toFile();
        assertNull(loadLanguageSection.invoke(bootstrap, folder, "does-not-exist"));

        File lang = new File(folder, "custom.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("messages.sample-key", "hello");
        config.save(lang);

        Object section = loadLanguageSection.invoke(bootstrap, folder, "custom");
        assertNotNull(section);
    }

    @Test
    void ensureMessageFilesWarnsWhenMessagesFolderCannotBeCreated() throws Exception {
        EzSeasonsPlugin plugin = mock(EzSeasonsPlugin.class);
        Registry registry = new Registry();
        Bootstrap bootstrap = new Bootstrap(plugin, registry);

        File tempFolder = Files.createTempDirectory("ezseasons-data").toFile();
        File blockingFile = new File(tempFolder, "not-a-folder");
        assertTrue(blockingFile.createNewFile());

        Logger logger = mock(Logger.class);
        when(plugin.getDataFolder()).thenReturn(blockingFile);
        when(plugin.getLogger()).thenReturn(logger);

        Method ensureMessageFiles = Bootstrap.class.getDeclaredMethod("ensureMessageFiles");
        ensureMessageFiles.setAccessible(true);
        ensureMessageFiles.invoke(bootstrap);

        verify(logger).warning(org.mockito.ArgumentMatchers.contains("Unable to create messages folder"));
    }

    @Test
    void registerMessagesSkipsRootKeysAndHandlesNullSection() throws Exception {
        EzSeasonsPlugin plugin = mock(EzSeasonsPlugin.class);
        Registry registry = new Registry();
        registry.setMessageService(mock(MessageService.class));
        Bootstrap bootstrap = new Bootstrap(plugin, registry);

        Method registerMessages = Bootstrap.class.getDeclaredMethod("registerMessages", org.bukkit.configuration.ConfigurationSection.class, String.class, Set.class);
        registerMessages.setAccessible(true);

        registerMessages.invoke(bootstrap, null, "", Set.of("messages"));

        MemoryConfiguration section = new MemoryConfiguration();
        section.set("messages", "skip-me");
        section.set("shown", "value");

        registerMessages.invoke(bootstrap, section, "", Set.of("messages"));

        verify(registry.getMessageService(), never()).register(eq("messages"), any());
        verify(registry.getMessageService()).register("shown", "value");
    }

    @Test
    void registerCommandsLogsWhenSeasonCommandIsMissing() throws Exception {
        EzSeasonsPlugin plugin = mock(EzSeasonsPlugin.class);
        Registry registry = new Registry();
        Bootstrap bootstrap = new Bootstrap(plugin, registry);

        Logger logger = mock(Logger.class);
        when(plugin.getCommand("season")).thenReturn(null);
        when(plugin.getLogger()).thenReturn(logger);

        Method registerCommands = Bootstrap.class.getDeclaredMethod("registerCommands");
        registerCommands.setAccessible(true);
        registerCommands.invoke(bootstrap);

        verify(logger).severe(org.mockito.ArgumentMatchers.contains("Command 'season' is not defined"));
    }

    @Test
    void restartSeasonCheckTaskRunsScheduledResetLogic() throws Exception {
        server = MockBukkit.mock();
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        Registry registry = new Registry();
        Bootstrap bootstrap = new Bootstrap(plugin, registry);

        SeasonManager manager = mock(SeasonManager.class);
        when(manager.isEnabled()).thenReturn(true);
        when(manager.getCheckInterval()).thenReturn(Duration.ofSeconds(1));
        when(manager.shouldTriggerReset(any(Long.class))).thenReturn(true);
        registry.setSeasonManager(manager);

        Method restart = Bootstrap.class.getDeclaredMethod("restartSeasonCheckTask");
        restart.setAccessible(true);
        restart.invoke(bootstrap);

        assertNotNull(registry.getSeasonCheckTask());
        server.getScheduler().performTicks(20L);
        verify(manager).triggerSeasonReset("schedule");

        // cover lambda branch where manager is missing
        registry.setSeasonManager(null);
        server.getScheduler().performTicks(20L);
    }

    @Test
    void durationToTicksUsesMinimumTickFloor() throws Exception {
        Bootstrap bootstrap = new Bootstrap(mock(EzSeasonsPlugin.class), new Registry());
        Method durationToTicks = Bootstrap.class.getDeclaredMethod("durationToTicks", Duration.class);
        durationToTicks.setAccessible(true);

        assertEquals(20L, durationToTicks.invoke(bootstrap, Duration.ZERO));
        assertEquals(40L, durationToTicks.invoke(bootstrap, Duration.ofSeconds(2)));
    }
}
