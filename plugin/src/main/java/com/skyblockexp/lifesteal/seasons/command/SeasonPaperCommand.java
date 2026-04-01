package com.skyblockexp.lifesteal.seasons.command;

import com.skyblockexp.lifesteal.seasons.EzSeasonsPlugin;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * Paper command adapter that delegates execution to the structured {@link SeasonCommand} handler.
 */
public final class SeasonPaperCommand extends Command implements PluginIdentifiableCommand {

    private final Plugin plugin;
    private final SeasonCommand executor;

    public SeasonPaperCommand(EzSeasonsPlugin plugin, SeasonCommand executor) {
        super("season");
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.executor = Objects.requireNonNull(executor, "executor");
        setDescription("Display season status or reload season configuration");
        setUsage("/season [reload]");
        setPermission("lifesteal.season");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return executor.onCommand(sender, this, commandLabel, args);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}
