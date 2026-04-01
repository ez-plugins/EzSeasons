package com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin;

import com.skyblockexp.lifesteal.seasons.EzSeasonsPlugin;
import com.skyblockexp.lifesteal.seasons.SeasonManager;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.AdminSubcommand;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.ReloadSubcommand;
import com.skyblockexp.lifesteal.seasons.config.MessageService;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminSubcommandsUnitTest {

    private EzSeasonsPlugin plugin;
    private MessageService messageService;
    private SeasonManager seasonManager;
    private SeasonCommandContext context;
    private CommandSender sender;

    @BeforeEach
    void setUp() {
        plugin = mock(EzSeasonsPlugin.class);
        messageService = mock(MessageService.class);
        seasonManager = mock(SeasonManager.class);
        sender = mock(CommandSender.class);

        when(plugin.getMessageService()).thenReturn(messageService);
        when(plugin.getSeasonManager()).thenReturn(seasonManager);
        when(sender.hasPermission("lifesteal.season.admin")).thenReturn(true);

        context = new SeasonCommandContext(plugin);
    }

    @Test
    void adminStatusSubcommandSendsDisabledOrStatusAndRawTimestamps() {
        AdminStatusSubcommand subcommand = new AdminStatusSubcommand(context);

        when(plugin.getSeasonManager()).thenReturn(null);
        assertTrue(subcommand.execute(sender, new String[0]));
        verify(messageService).sendMessage(sender, "season-disabled");

        when(plugin.getSeasonManager()).thenReturn(seasonManager);
        when(seasonManager.isEnabled()).thenReturn(true);
        when(seasonManager.getTimeUntilReset()).thenReturn(Optional.empty());
        when(seasonManager.getLastResetMillis()).thenReturn(111L);
        when(seasonManager.getNextResetMillis()).thenReturn(222L);

        assertTrue(subcommand.execute(sender, new String[0]));
        verify(messageService).sendMessage(sender, "season-status-unknown");
        verify(messageService).sendMessage(sender, "admin-status-raw", Map.of("lastReset", "111", "nextReset", "222"));
    }

    @Test
    void adminResetSubcommandCoversDisabledConfirmAndSuccessBranches() {
        AdminResetSubcommand subcommand = new AdminResetSubcommand(context);

        when(seasonManager.isEnabled()).thenReturn(false);
        assertTrue(subcommand.execute(sender, new String[0]));
        verify(messageService).sendMessage(sender, "season-disabled");

        when(seasonManager.isEnabled()).thenReturn(true);
        assertTrue(subcommand.execute(sender, new String[]{"maintenance"}));
        verify(messageService).sendMessage(sender, "admin-reset-confirm-required",
                Map.of("command", "/season admin reset <reason> --confirm"));

        assertTrue(subcommand.execute(sender, new String[]{"manual", "repair", "--confirm"}));
        verify(seasonManager).triggerSeasonReset("manual repair");
        verify(messageService).sendMessage(sender, "admin-reset-success", Map.of("reason", "manual repair"));

        assertEquals(List.of("maintenance", "manual"), subcommand.tabComplete(sender, new String[]{"ma"}));
        assertEquals(List.of("--confirm"), subcommand.tabComplete(sender, new String[]{"manual", "--"}));
        assertEquals(List.of(), subcommand.tabComplete(sender, new String[]{"manual", "yes", "later"}));
        assertEquals(List.of(), subcommand.tabComplete(sender, new String[0]));
    }

    @Test
    void adminSetNextAndClearNextSubcommandsCoverAllBranches() {
        AdminSetNextSubcommand setNext = new AdminSetNextSubcommand(context);
        AdminClearNextSubcommand clearNext = new AdminClearNextSubcommand(context);

        when(seasonManager.isEnabled()).thenReturn(false);
        assertTrue(setNext.execute(sender, new String[0]));
        assertTrue(clearNext.execute(sender, new String[0]));
        verify(messageService, times(2)).sendMessage(sender, "season-disabled");

        when(seasonManager.isEnabled()).thenReturn(true);
        assertTrue(setNext.execute(sender, new String[0]));
        verify(messageService).sendMessage(sender, "admin-setnext-usage");

        assertTrue(setNext.execute(sender, new String[]{"bad-value"}));
        verify(messageService).sendMessage(sender, "admin-setnext-invalid-timestamp",
                Map.of("value", "bad-value", "expected", "unix epoch milliseconds (example: 1735689600000)"));

        assertTrue(setNext.execute(sender, new String[]{"1735689600000"}));
        verify(seasonManager).setNextResetMillis(1735689600000L);

        assertTrue(clearNext.execute(sender, new String[0]));
        verify(messageService).sendMessage(sender, "admin-clear-next-confirm-required",
                Map.of("command", "/season admin clear-next --confirm"));

        assertTrue(clearNext.execute(sender, new String[]{"--confirm"}));
        verify(seasonManager).clearNextResetMillis();
        verify(messageService).sendMessage(sender, "admin-clear-next-success");

        assertEquals(List.of("<unixMillis>", "now+3600000"), setNext.tabComplete(sender, new String[]{""}));
        assertEquals(List.of("--confirm"), clearNext.tabComplete(sender, new String[]{""}));
        assertEquals(List.of(), setNext.tabComplete(sender, new String[]{"x", "y"}));
        assertEquals(List.of(), clearNext.tabComplete(sender, new String[]{"x", "y"}));
    }

    @Test
    void reloadAndAdminRootSubcommandsCoverPermissionAndRoutingBranches() {
        ReloadSubcommand reload = new ReloadSubcommand(context);
        when(sender.hasPermission("lifesteal.season.admin")).thenReturn(false);
        when(sender.hasPermission("lifesteal.admin")).thenReturn(false);
        assertTrue(reload.execute(sender, new String[0]));
        verify(messageService).sendMessage(sender, "no-permission");

        when(sender.hasPermission("lifesteal.season.admin")).thenReturn(true);
        assertTrue(reload.execute(sender, new String[0]));
        verify(plugin).reloadSeasonConfiguration();
        verify(messageService).sendMessage(sender, "admin-reload-success");

        AdminSubcommand admin = new AdminSubcommand(context);

        when(sender.hasPermission("lifesteal.season.admin")).thenReturn(false);
        assertTrue(admin.execute(sender, new String[]{"status"}));

        when(sender.hasPermission("lifesteal.season.admin")).thenReturn(true);
        assertTrue(admin.execute(sender, new String[0]));
        verify(messageService).sendMessage(sender, "admin-usage");

        assertTrue(admin.execute(sender, new String[]{"unknown"}));
        verify(messageService).sendMessage(sender, "admin-unknown-subcommand",
                Map.of("subcommand", "unknown", "usage", "/season admin <reload|reset|setnext|clear-next|status>"));

        when(seasonManager.isEnabled()).thenReturn(true);
        when(seasonManager.getTimeUntilReset()).thenReturn(Optional.empty());
        assertTrue(admin.execute(sender, new String[]{"status"}));

        assertTrue(admin.tabComplete(sender, new String[]{""}).contains("status"));
        assertEquals(List.of(), admin.tabComplete(sender, new String[]{"missing", "x"}));
    }
}
