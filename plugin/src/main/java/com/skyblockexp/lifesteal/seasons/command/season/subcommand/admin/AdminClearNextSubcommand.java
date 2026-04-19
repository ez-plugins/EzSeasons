package com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin;

import com.skyblockexp.lifesteal.seasons.SeasonManager;
import com.skyblockexp.lifesteal.seasons.command.framework.Subcommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;

public final class AdminClearNextSubcommand extends Subcommand {

    private final SeasonCommandContext context;

    public AdminClearNextSubcommand(SeasonCommandContext context) {
        super("clear-next", List.of());
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        final SeasonManager seasonManager = context.getPlugin().getSeasonManager();
        if (seasonManager == null || !seasonManager.isEnabled()) {
            context.getPlugin().getMessageService().sendMessage(sender, "season-disabled");
            return true;
        }

        if (!context.containsConfirmFlag(args)) {
            context.getPlugin().getMessageService().sendMessage(sender, "admin-clear-next-confirm-required",
                    Map.of("command", "/season admin clear-next --confirm"));
            return true;
        }

        seasonManager.clearNextResetMillis();
        context.getPlugin().getMessageService().sendMessage(sender, "admin-clear-next-success");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filter(List.of("--confirm"), args[0]);
        }
        return List.of();
    }
}
