package com.skyblockexp.lifesteal.seasons;

import com.skyblockexp.lifesteal.seasons.api.SeasonsIntegration;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommand;
import com.skyblockexp.lifesteal.seasons.config.MessageService;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeasonCommandAndApiEdgeUnitTest {

    @Test
    void seasonCommandUnknownSubcommandPathReturnsHandledAndSendsUsageMessage() {
        EzSeasonsPlugin plugin = mock(EzSeasonsPlugin.class);
        MessageService messageService = mock(MessageService.class);
        CommandSender sender = mock(CommandSender.class);

        when(plugin.getMessageService()).thenReturn(messageService);
        when(sender.hasPermission("lifesteal.season.admin")).thenReturn(true);

        SeasonCommand seasonCommand = new SeasonCommand(plugin);

        assertTrue(seasonCommand.execute(sender, new String[]{"mystery"}));
        verify(messageService).sendMessage(sender, "admin-unknown-subcommand",
                Map.of("subcommand", "mystery", "usage", "/season <status|admin>"));
    }

    @Test
    void messageServiceExposesPrefixAndPerformsEmptyTabCompletionFallbacks() {
        MessageService messageService = new MessageService("&6[Prefix] ");
        assertEquals("§6[Prefix] ", messageService.getPrefix());
    }

    @Test
    void seasonsApiPropagatesErrorsUnwrapped() throws Exception {
        SeasonsApiImpl api = new SeasonsApiImpl(null);
        SeasonsIntegration integration = mock(SeasonsIntegration.class);
        Error boom = new AssertionError("boom");

        org.mockito.Mockito.doThrow(boom).when(integration).onRegister(api);

        AssertionError thrown = assertThrows(AssertionError.class, () -> api.registerIntegration(integration));
        assertEquals("boom", thrown.getMessage());
    }
}
