package com.skyblockexp.lifesteal.seasons.command.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.command.CommandSender;

public abstract class Command {

    private final String name;

    private final List<Subcommand> subcommands = new ArrayList<>();

    protected Command(String name) {
        this.name = name;
    }

    protected void registerSubcommand(Subcommand subcommand) {
        subcommands.add(subcommand);
    }

    protected List<Subcommand> getSubcommands() {
        return List.copyOf(subcommands);
    }

    public String getName() {
        return name;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return onDefault(sender);
        }

        final Optional<Subcommand> subcommand = findSubcommand(args[0]);
        if (subcommand.isEmpty()) {
            return onUnknownSubcommand(sender, args[0]);
        }

        final String[] remaining = Arrays.copyOfRange(args, 1, args.length);
        return subcommand.get().execute(sender, remaining);
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            final String input = args.length == 0 ? "" : args[0];
            return filterSubcommandNames(input);
        }

        final Optional<Subcommand> subcommand = findSubcommand(args[0]);
        if (subcommand.isEmpty()) {
            return List.of();
        }

        final String[] remaining = Arrays.copyOfRange(args, 1, args.length);
        return subcommand.get().tabComplete(sender, remaining);
    }

    protected abstract boolean onDefault(CommandSender sender);

    protected abstract boolean onUnknownSubcommand(CommandSender sender, String input);

    protected Optional<Subcommand> findSubcommand(String input) {
        return subcommands.stream().filter(subcommand -> subcommand.matches(input)).findFirst();
    }

    protected List<String> filterSubcommandNames(String input) {
        final String lowerInput = input.toLowerCase(Locale.ROOT);
        final LinkedHashSet<String> names = new LinkedHashSet<>();
        for (Subcommand subcommand : subcommands) {
            names.addAll(subcommand.names());
        }
        final LinkedHashSet<String> matches = new LinkedHashSet<>();
        for (String value : names) {
            final String lowerValue = value.toLowerCase(Locale.ROOT);
            if (lowerValue.startsWith(lowerInput)) {
                matches.add(value);
            }
        }
        return List.copyOf(matches);
    }
}
