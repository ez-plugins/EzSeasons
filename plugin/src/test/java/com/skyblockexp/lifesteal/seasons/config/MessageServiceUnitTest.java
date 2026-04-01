package com.skyblockexp.lifesteal.seasons.config;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MessageServiceUnitTest {

    @Test
    void renderReplacesRegisteredPlaceholders() {
        MessageService messageService = new MessageService("&c[EzSeasons]&r ");
        messageService.register("season-status", "Next reset in %time%.");

        String rendered = messageService.render("season-status", Map.of("time", "1:00:00"));

        assertEquals("Next reset in 1:00:00.", rendered);
    }

    @Test
    void renderReturnsEmptyStringForUnknownKey() {
        MessageService messageService = new MessageService("&c[EzSeasons]&r ");

        String rendered = messageService.render("missing-key", Map.of("time", "1:00:00"));

        assertEquals("", rendered);
    }

    @Test
    void formatPrependsPrefix() {
        MessageService messageService = new MessageService("&c[EzSeasons]&r ");
        messageService.register("plain", "&7Hello");

        String formatted = messageService.format("plain", null);

        assertEquals("§c[EzSeasons]§r §7Hello", formatted);
    }

    @Test
    void formatStillPrependsPrefixWhenMessageKeyMissing() {
        MessageService messageService = new MessageService("&c[EzSeasons]&r ");

        String formatted = messageService.format("missing-key", Map.of("time", "1:00:00"));

        assertEquals("§c[EzSeasons]§r ", formatted);
    }

    @Test
    void sendMessageDoesNotCallSenderWhenRenderedMessageIsEmpty() {
        MessageService messageService = new MessageService("&c[EzSeasons]&r ");
        CommandSender sender = mock(CommandSender.class);

        messageService.sendMessage(sender, "missing-key", Map.of("time", "1:00:00"));

        verify(sender, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void renderLeavesUnknownPlaceholdersUnchanged() {
        MessageService messageService = new MessageService("&c[EzSeasons]&r ");
        messageService.register("season-status", "Next reset in %time% (%other%).");

        String rendered = messageService.render("season-status", Map.of("time", "1:00:00"));

        assertEquals("Next reset in 1:00:00 (%other%).", rendered);
    }

    @Test
    void renderWithNullPlaceholdersMatchesEmptyMapForRegisteredMessage() {
        MessageService messageService = new MessageService("&c[EzSeasons]&r ");
        messageService.register("season-status", "Next reset in %time%.");

        String renderedWithNull = messageService.render("season-status", null);
        String renderedWithEmptyMap = messageService.render("season-status", Map.of());

        assertEquals(renderedWithEmptyMap, renderedWithNull);
        assertEquals("Next reset in %time%.", renderedWithNull);
    }

    @Test
    void sendMessageSendsFormattedMessageForRegisteredMessage() {
        MessageService messageService = new MessageService("&c[EzSeasons]&r ");
        messageService.register("season-status", "Next reset in %time%.");
        CommandSender sender = mock(CommandSender.class);

        messageService.sendMessage(sender, "season-status", Map.of("time", "1:00:00"));

        verify(sender, times(1)).sendMessage("§c[EzSeasons]§r Next reset in 1:00:00.");
    }
}
