package com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin;

import com.skyblockexp.lifesteal.seasons.SeasonManager;
import com.skyblockexp.lifesteal.seasons.command.framework.Subcommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public final class AdminSetNextSubcommand extends Subcommand {

    private final SeasonCommandContext context;

    public AdminSetNextSubcommand(SeasonCommandContext context) {
        super("setnext", List.of());
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        SeasonManager seasonManager = context.requireEnabledSeasonManager(sender);
        if (seasonManager == null) {
            return true;
        }
        if (args.length < 1) {
            context.getPlugin().getMessageService().sendMessage(sender, "admin-setnext-usage");
            return true;
        }

        Long millis = context.parseTimestamp(args[0], sender);
        if (millis == null) {
            return true;
        }

        seasonManager.setNextResetMillis(millis);
        context.getPlugin().getMessageService().sendMessage(sender, "admin-setnext-success",
                Map.of("timestamp", String.valueOf(millis), "iso", context.formatInstant(millis)));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filter(List.of("<unixMillis>", "now+3600000"), args[0]);
        }
        return List.of();
    }
}
