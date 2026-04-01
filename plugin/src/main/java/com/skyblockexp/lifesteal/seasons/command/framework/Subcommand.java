package com.skyblockexp.lifesteal.seasons.command.framework;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

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
        List<String> names = new ArrayList<>();
        names.add(name);
        names.addAll(aliases);
        return names;
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    protected List<String> filter(List<String> values, String input) {
        String lowerInput = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        Stream<String> stream = values.stream();
        stream.filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowerInput)).forEach(matches::add);
        return matches;
    }
}
