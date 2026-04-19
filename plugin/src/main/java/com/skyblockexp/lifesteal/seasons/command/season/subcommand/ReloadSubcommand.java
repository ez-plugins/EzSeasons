package com.skyblockexp.lifesteal.seasons.command.season.subcommand;

import com.skyblockexp.lifesteal.seasons.command.framework.Subcommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class ReloadSubcommand extends Subcommand {

    private final SeasonCommandContext context;

    public ReloadSubcommand(SeasonCommandContext context) {
        super("reload", List.of());
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!context.hasAdminPermission(sender)) {
            context.getPlugin().getMessageService().sendMessage(sender, "no-permission");
            return true;
        }
        context.getPlugin().reloadSeasonConfiguration();
        context.getPlugin().getMessageService().sendMessage(sender, "admin-reload-success");
        return true;
    }
}
