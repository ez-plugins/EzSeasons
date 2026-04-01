package com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin;

import com.skyblockexp.lifesteal.seasons.SeasonManager;
import com.skyblockexp.lifesteal.seasons.command.framework.Subcommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public final class AdminResetSubcommand extends Subcommand {

    private final SeasonCommandContext context;

    public AdminResetSubcommand(SeasonCommandContext context) {
        super("reset", List.of());
        this.context = context;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        SeasonManager seasonManager = context.requireEnabledSeasonManager(sender);
        if (seasonManager == null) {
            return true;
        }

        if (!context.containsConfirmFlag(args)) {
            context.getPlugin().getMessageService().sendMessage(sender, "admin-reset-confirm-required",
                    Map.of("command", "/season admin reset <reason> --confirm"));
            return true;
        }

        String reason = context.extractReason(args);
        seasonManager.triggerSeasonReset(reason);
        context.getPlugin().getMessageService().sendMessage(sender, "admin-reset-success", Map.of("reason", reason));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filter(List.of("<reason...>", "maintenance", "manual", "--confirm"), args[0]);
        }
        if (args.length >= 2) {
            return filter(List.of("--confirm"), args[args.length - 1]);
        }
        return List.of();
    }
}
