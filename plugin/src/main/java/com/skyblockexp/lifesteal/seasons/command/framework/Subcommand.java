package com.skyblockexp.lifesteal.seasons.command.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;

public abstract class Subcommand {

    private final String name;

    private final List<String> aliases;

    protected Subcommand(String name, List<String> aliases) {
        this.name = name;
        this.aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }

    public String getName() {
        return name;
    }

    public boolean matches(String input) {
        if (name.equalsIgnoreCase(input)) {
            return true;
        }
        return aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(input));
    }

    public List<String> names() {
        if (aliases.isEmpty()) {
            return List.of(name);
        }
        final List<String> names = new ArrayList<>();
        names.add(name);
        names.addAll(aliases);
        return names;
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    protected List<String> filter(List<String> values, String input) {
        final String lowerInput = input.toLowerCase(Locale.ROOT);
        final List<String> matches = new ArrayList<>();
        final Stream<String> stream = values.stream();
        stream.filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerInput)).forEach(matches::add);
        return matches;
    }
}
