package com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin;

import com.skyblockexp.lifesteal.seasons.SeasonManager;
import com.skyblockexp.lifesteal.seasons.command.framework.Subcommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;

public final class AdminSetNextSubcommand extends Subcommand {

    private final SeasonCommandContext context;

    public AdminSetNextSubcommand(SeasonCommandContext context) {
        super("setnext", List.of());
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        final SeasonManager seasonManager = context.requireEnabledSeasonManager(sender);
        if (seasonManager == null) {
            return true;
        }
        if (args.length < 1) {
            context.getPlugin().getMessageService().sendMessage(sender, "admin-setnext-usage");
            return true;
        }

        final Long millis = context.parseTimestamp(args[0], sender);
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
