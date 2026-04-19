package com.skyblockexp.lifesteal.seasons.command.season.subcommand;

import com.skyblockexp.lifesteal.seasons.command.framework.Subcommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class StatusSubcommand extends Subcommand {

    private final SeasonCommandContext context;

    public StatusSubcommand(SeasonCommandContext context) {
        super("status", List.of());
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteal.season") && !context.hasAdminPermission(sender)) {
            context.getPlugin().getMessageService().sendMessage(sender, "no-permission");
            return true;
        }
        return context.sendSeasonStatus(sender);
    }
}
