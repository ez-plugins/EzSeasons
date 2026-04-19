package com.skyblockexp.lifesteal.seasons.command;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.skyblockexp.lifesteal.seasons.EzSeasonsPlugin;
import com.skyblockexp.lifesteal.seasons.SeasonManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeasonCommandFeatureUnitTest {

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
    void seasonCommandReturnsFormattedStatusWhenSeasonIsEnabled() throws Exception {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);

        MemoryConfiguration seasonConfig = enabledSeasonConfig();

        SeasonManager seasonManager = new SeasonManager(plugin, seasonConfig, plugin.getMessageService());
        Field nextResetMillis = SeasonManager.class.getDeclaredField("nextResetMillis");
        nextResetMillis.setAccessible(true);
        nextResetMillis.setLong(seasonManager, System.currentTimeMillis() + 3_600_000L);
        plugin.registerSeasonManager(seasonManager);

        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "lifesteal.season", true);
        player.performCommand("season");

        assertNextMessageContains(player, "[EzSeasons]", "The next season reset will occur in");
    }

    @Test
    void seasonCommandRejectsPlayerWithoutSeasonOrAdminPermission() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);

        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "lifesteal.season", false);
        player.addAttachment(plugin, "lifesteal.admin", false);

        CommandExecutor executor = plugin.getCommand("season").getExecutor();
        executor.onCommand(player, plugin.getCommand("season"), "season", new String[0]);

        assertNextMessageContains(player, "[EzSeasons]", "You do not have permission");
    }

    @Test
    void seasonCommandReturnsDisabledWhenSeasonManagerIsMissing() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);

        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "lifesteal.season", true);
        player.performCommand("season");

        assertNextMessageContains(player, "[EzSeasons]", "Season resets are currently disabled");
    }

    @Test
    void seasonCommandReturnsDisabledWhenSeasonManagerIsDisabled() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);

        MemoryConfiguration seasonConfig = new MemoryConfiguration();
        seasonConfig.set("enabled", false);
        SeasonManager seasonManager = new SeasonManager(plugin, seasonConfig, plugin.getMessageService());
        plugin.registerSeasonManager(seasonManager);

        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "lifesteal.season", true);
        player.performCommand("season");

        assertNextMessageContains(player, "[EzSeasons]", "Season resets are currently disabled");
    }

    @Test
    void seasonCommandReturnsUnknownWhenResetTimeCannotBeCalculated() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);

        MemoryConfiguration seasonConfig = new MemoryConfiguration();
        seasonConfig.set("enabled", true);
        seasonConfig.set("length-days", 30L);
        seasonConfig.set("check-interval-minutes", 60L);

        SeasonManager seasonManager = new SeasonManager(plugin, seasonConfig, plugin.getMessageService());
        plugin.registerSeasonManager(seasonManager);

        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "lifesteal.season", true);
        player.performCommand("season");

        assertNextMessageContains(player, "[EzSeasons]", "has not been scheduled yet");
    }

    @Test
    void seasonCommandAllowsPlayerWithAdminPermissionOnly() throws Exception {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);

        MemoryConfiguration seasonConfig = new MemoryConfiguration();
        seasonConfig.set("enabled", true);
        seasonConfig.set("length-days", 30L);
        seasonConfig.set("check-interval-minutes", 60L);

        SeasonManager seasonManager = new SeasonManager(plugin, seasonConfig, plugin.getMessageService());
        Field nextResetMillis = SeasonManager.class.getDeclaredField("nextResetMillis");
        nextResetMillis.setAccessible(true);
        nextResetMillis.setLong(seasonManager, System.currentTimeMillis() + 3_600_000L);
        plugin.registerSeasonManager(seasonManager);

        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "lifesteal.admin", true);
        player.performCommand("season");

        assertNextMessageContains(player, "[EzSeasons]", "The next season reset will occur in");
    }

    private static void assertNextMessageContains(PlayerMock player, String... fragments) {
        String message = player.nextMessage();
        for (String fragment : fragments) {
            assertTrue(message.contains(fragment), "Expected message to contain: " + fragment + " but was: " + message);
        }
    }

    @Test
    void seasonCommandUsesSelectedTranslationWhenConfigured() throws Exception {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);

        plugin.getConfig().set("messages.language", "es");
        java.lang.reflect.Method loadMessages = EzSeasonsPlugin.class.getDeclaredMethod("loadMessages");
        loadMessages.setAccessible(true);
        loadMessages.invoke(plugin);

        MemoryConfiguration seasonConfig = enabledSeasonConfig();

        SeasonManager seasonManager = new SeasonManager(plugin, seasonConfig, plugin.getMessageService());
        Field nextResetMillis = SeasonManager.class.getDeclaredField("nextResetMillis");
        nextResetMillis.setAccessible(true);
        nextResetMillis.setLong(seasonManager, System.currentTimeMillis() + 3_600_000L);
        plugin.registerSeasonManager(seasonManager);

        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "lifesteal.season", true);
        player.performCommand("season");

        String message = player.nextMessage();
        assertTrue(message.contains("[EzSeasons]"));
        assertTrue(message.contains("El próximo reinicio de temporada ocurrirá en"));
    }

    @Test
    void adminSubcommandsRequireAdminPermission() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        plugin.registerSeasonManager(new SeasonManager(plugin, enabledSeasonConfig(), plugin.getMessageService()));

        PlayerMock player = server.addPlayer();
        player.performCommand("season admin status");

        assertTrue(player.nextMessage().contains("do not have permission"));
    }

    @Test
    void adminSetNextAndClearNextMutateSeasonManager() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonManager seasonManager = new SeasonManager(plugin, enabledSeasonConfig(), plugin.getMessageService());
        plugin.registerSeasonManager(seasonManager);

        PlayerMock admin = server.addPlayer();
        admin.addAttachment(plugin, "lifesteal.admin", true);

        admin.performCommand("season admin setnext 1735689600000");
        assertEquals(1735689600000L, seasonManager.getNextResetMillis());
        assertTrue(admin.nextMessage().contains("1735689600000"));

        admin.performCommand("season admin clear-next --confirm");
        assertEquals(0L, seasonManager.getNextResetMillis());
        assertTrue(admin.nextMessage().contains("Cleared the stored next reset timestamp."));
    }

    @Test
    void adminResetAcceptsReasonAndUpdatesLastReset() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        MemoryConfiguration seasonConfig = enabledSeasonConfig();
        seasonConfig.set("broadcast-message", "");
        SeasonManager seasonManager = new SeasonManager(plugin, seasonConfig, plugin.getMessageService());
        plugin.registerSeasonManager(seasonManager);

        PlayerMock admin = server.addPlayer();
        admin.addAttachment(plugin, "lifesteal.admin", true);

        admin.performCommand("season admin reset manual-maintenance --confirm");

        assertTrue(seasonManager.getLastResetMillis() > 0L);
        assertTrue(admin.nextMessage().contains("manual-maintenance"));
    }

    @Test
    void adminReloadRebuildsSeasonManagerFromConfig() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        PlayerMock admin = server.addPlayer();
        admin.addAttachment(plugin, "lifesteal.admin", true);

        plugin.getConfig().set("season.enabled", true);
        plugin.getConfig().set("season.next-reset", 987654321L);
        plugin.saveConfig();

        admin.performCommand("season admin reload");

        assertTrue(admin.nextMessage().contains("configuration and messages reloaded"));
        assertEquals(987654321L, plugin.getSeasonManager().getNextResetMillis());
    }

    @Test
    void adminDestructiveActionsRequireConfirmFlag() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        SeasonManager seasonManager = new SeasonManager(plugin, enabledSeasonConfig(), plugin.getMessageService());
        plugin.registerSeasonManager(seasonManager);

        PlayerMock admin = server.addPlayer();
        admin.addAttachment(plugin, "lifesteal.admin", true);

        long before = seasonManager.getLastResetMillis();
        admin.performCommand("season admin reset maintenance");
        assertTrue(admin.nextMessage().contains("Re-run with confirmation"));
        assertEquals(before, seasonManager.getLastResetMillis());

        seasonManager.setNextResetMillis(123456789L);
        admin.performCommand("season admin clear-next");
        assertTrue(admin.nextMessage().contains("Re-run with confirmation"));
        assertEquals(123456789L, seasonManager.getNextResetMillis());
    }

    @Test
    void adminSetNextReturnsClearTimestampErrors() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        plugin.registerSeasonManager(new SeasonManager(plugin, enabledSeasonConfig(), plugin.getMessageService()));

        PlayerMock admin = server.addPlayer();
        admin.addAttachment(plugin, "lifesteal.admin", true);

        admin.performCommand("season admin setnext not-a-number");
        assertTrue(admin.nextMessage().contains("Invalid timestamp"));

        admin.performCommand("season admin setnext 0");
        assertTrue(admin.nextMessage().contains("out of range"));
    }

    @Test
    void seasonTabCompleterProvidesRootAndAdminHints() {
        EzSeasonsPlugin plugin = MockBukkit.load(EzSeasonsPlugin.class);
        PlayerMock admin = server.addPlayer();
        admin.addAttachment(plugin, "lifesteal.admin", true);

        assertTrue(plugin.getCommand("season").tabComplete(admin, "season", new String[] {""}).contains("admin"));
        assertTrue(plugin.getCommand("season").tabComplete(admin, "season", new String[] {"admin", ""}).contains("setnext"));
        assertTrue(plugin.getCommand("season").tabComplete(admin, "season", new String[] {"admin", "setnext", ""}).contains("<unixMillis>"));
        assertTrue(plugin.getCommand("season").tabComplete(admin, "season", new String[] {"admin", "clear-next", ""}).contains("--confirm"));
    }

    private MemoryConfiguration enabledSeasonConfig() {
        MemoryConfiguration seasonConfig = new MemoryConfiguration();
        seasonConfig.set("enabled", true);
        seasonConfig.set("length-days", 30L);
        seasonConfig.set("check-interval-minutes", 60L);
        return seasonConfig;
    }
}
