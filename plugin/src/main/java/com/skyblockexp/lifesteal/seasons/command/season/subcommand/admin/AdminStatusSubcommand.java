package com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin;

import com.skyblockexp.lifesteal.seasons.SeasonManager;
import com.skyblockexp.lifesteal.seasons.command.framework.Subcommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public final class AdminStatusSubcommand extends Subcommand {

    private final SeasonCommandContext context;

    public AdminStatusSubcommand(SeasonCommandContext context) {
        super("status", List.of());
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        SeasonManager seasonManager = context.requireEnabledSeasonManager(sender);
        if (seasonManager == null) {
            return true;
        }

        context.sendSeasonStatus(sender);
        context.getPlugin().getMessageService().sendMessage(sender, "admin-status-raw",
                Map.of("lastReset", String.valueOf(seasonManager.getLastResetMillis()),
                        "nextReset", String.valueOf(seasonManager.getNextResetMillis())));
        return true;
    }
}
