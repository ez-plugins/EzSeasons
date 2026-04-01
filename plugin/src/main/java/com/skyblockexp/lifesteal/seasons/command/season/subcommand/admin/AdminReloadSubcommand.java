package com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin;

import com.skyblockexp.lifesteal.seasons.command.framework.Subcommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class AdminReloadSubcommand extends Subcommand {

    private final SeasonCommandContext context;

    public AdminReloadSubcommand(SeasonCommandContext context) {
        super("reload", List.of());
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        context.getPlugin().reloadSeasonConfiguration();
        context.getPlugin().getMessageService().sendMessage(sender, "admin-reload-success");
        return true;
    }
}
