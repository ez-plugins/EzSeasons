package com.skyblockexp.lifesteal.seasons.command.framework;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CommandFrameworkUnitTest {

    @Test
    void executeCallsDefaultWhenNoArgumentsAreProvided() {
        TestCommand command = new TestCommand();

        boolean handled = command.execute(mock(CommandSender.class), new String[0]);

        assertTrue(handled);
        assertTrue(command.defaultCalled);
    }

    @Test
    void executeCallsUnknownHandlerWhenSubcommandDoesNotExist() {
        TestCommand command = new TestCommand();

        boolean handled = command.execute(mock(CommandSender.class), new String[]{"missing"});

        assertFalse(handled);
        assertEquals("missing", command.unknownInput);
    }

    @Test
    void commandNameAndSubcommandSnapshotAreExposed() {
        TestCommand command = new TestCommand();
        command.add(new TestSubcommand("status", List.of("st"), true));

        assertEquals("season", command.commandName());
        assertEquals(1, command.subcommandSnapshot().size());
    }

    @Test
    void executeDelegatesToMatchingSubcommandWithRemainingArguments() {
        TestCommand command = new TestCommand();
        TestSubcommand subcommand = new TestSubcommand("status", List.of("st"), true);
        command.add(subcommand);

        boolean handled = command.execute(mock(CommandSender.class), new String[]{"st", "--confirm", "now"});

        assertTrue(handled);
        assertArrayEquals(new String[]{"--confirm", "now"}, subcommand.executedArgs);
    }

    @Test
    void tabCompleteReturnsMatchingSubcommandNamesForFirstToken() {
        TestCommand command = new TestCommand();
        command.add(new TestSubcommand("status", List.of("stat"), true));
        command.add(new TestSubcommand("start", List.of(), true));

        List<String> matches = command.tabComplete(mock(CommandSender.class), new String[]{"st"});

        assertIterableEquals(List.of("status", "stat", "start"), matches);
    }

    @Test
    void tabCompleteDelegatesToMatchingSubcommandForAdditionalTokens() {
        TestCommand command = new TestCommand();
        TestSubcommand subcommand = new TestSubcommand("admin", List.of(), true);
        subcommand.tabResult = List.of("setnext", "reset");
        command.add(subcommand);

        List<String> matches = command.tabComplete(mock(CommandSender.class), new String[]{"admin", "s"});

        assertIterableEquals(List.of("setnext", "reset"), matches);
        assertArrayEquals(new String[]{"s"}, subcommand.tabArgs);
    }

    @Test
    void tabCompleteReturnsEmptyForNoArguments() {
        TestCommand command = new TestCommand();

        assertIterableEquals(List.of(), command.tabComplete(mock(CommandSender.class), new String[0]));
    }


    @Test
    void tabCompleteReturnsEmptyWhenUnknownSubcommandIsRequestedWithAdditionalTokens() {
        TestCommand command = new TestCommand();
        command.add(new TestSubcommand("status", List.of("st"), true));

        assertIterableEquals(List.of(), command.tabComplete(mock(CommandSender.class), new String[]{"missing", "x"}));
    }

    @Test
    void baseSubcommandTabCompleteReturnsEmptyList() {
        Subcommand subcommand = new Subcommand("base", List.of()) {
            @Override
            public boolean execute(CommandSender sender, String[] args) {
                return true;
            }
        };

        assertIterableEquals(List.of(), subcommand.tabComplete(mock(CommandSender.class), new String[]{"anything"}));
    }

    @Test
    void subcommandNamesAndFilterRespectAliasesAndPrefixMatching() {
        TestSubcommand subcommand = new TestSubcommand("reload", List.of("rl", "refresh"), true);

        assertTrue(subcommand.matches("ReLoAd"));
        assertTrue(subcommand.matches("RL"));
        assertFalse(subcommand.matches("other"));
        assertIterableEquals(List.of("reload", "rl", "refresh"), subcommand.names());
        assertIterableEquals(List.of("reload", "refresh"), subcommand.filter(List.of("reload", "refresh", "status"), "re"));
    }

    @Test
    void subcommandWithoutAliasesReturnsOnlyPrimaryName() {
        TestSubcommand subcommand = new TestSubcommand("status", null, true);

        assertIterableEquals(List.of("status"), subcommand.names());
        assertEquals("status", subcommand.getName());
        assertIterableEquals(List.of(), subcommand.tabComplete(mock(CommandSender.class), new String[]{"anything"}));
    }

    private static final class TestCommand extends Command {
        private boolean defaultCalled;
        private String unknownInput;

        private TestCommand() {
            super("season");
        }

        private void add(Subcommand subcommand) {
            registerSubcommand(subcommand);
        }

        private String commandName() {
            return getName();
        }

        private List<Subcommand> subcommandSnapshot() {
            return getSubcommands();
        }

        @Override
        protected boolean onDefault(CommandSender sender) {
            defaultCalled = true;
            return true;
        }

        @Override
        protected boolean onUnknownSubcommand(CommandSender sender, String input) {
            unknownInput = input;
            return false;
        }
    }

    private static final class TestSubcommand extends Subcommand {
        private final boolean result;
        private String[] executedArgs = new String[0];
        private String[] tabArgs = new String[0];
        private List<String> tabResult = List.of();

        private TestSubcommand(String name, List<String> aliases, boolean result) {
            super(name, aliases);
            this.result = result;
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            executedArgs = args;
            return result;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            tabArgs = args;
            return tabResult;
        }
    }

    private static void assertArrayEquals(String[] expected, String[] actual) {
        assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }
}
