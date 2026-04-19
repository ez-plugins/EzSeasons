package com.skyblockexp.lifesteal.seasons.command.season.subcommand;

import com.skyblockexp.lifesteal.seasons.command.framework.Subcommand;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommandContext;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin.AdminClearNextSubcommand;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin.AdminReloadSubcommand;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin.AdminResetSubcommand;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin.AdminSetNextSubcommand;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.admin.AdminStatusSubcommand;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.command.CommandSender;

public final class AdminSubcommand extends Subcommand {

    private final SeasonCommandContext context;

    private final List<Subcommand> subcommands;

    public AdminSubcommand(SeasonCommandContext context) {
        super("admin", List.of());
        this.context = context;
        this.subcommands = List.of(
                new AdminReloadSubcommand(context),
                new AdminResetSubcommand(context),
                new AdminSetNextSubcommand(context),
                new AdminClearNextSubcommand(context),
                new AdminStatusSubcommand(context)
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!context.hasAdminPermission(sender)) {
            context.getPlugin().getMessageService().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            context.getPlugin().getMessageService().sendMessage(sender, "admin-usage");
            return true;
        }

        final Optional<Subcommand> selected = findSubcommand(args[0]);
        if (selected.isEmpty()) {
            context.getPlugin().getMessageService().sendMessage(sender, "admin-unknown-subcommand",
                    Map.of("subcommand", args[0], "usage", "/season admin <reload|reset|setnext|clear-next|status>"));
            return true;
        }

        return selected.get().execute(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            final String input = args.length == 0 ? "" : args[0];
            return filter(subcommands.stream().flatMap(s -> s.names().stream()).toList(), input);
        }
        final Optional<Subcommand> selected = findSubcommand(args[0]);
        if (selected.isEmpty()) {
            return List.of();
        }
        return selected.get().tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    private Optional<Subcommand> findSubcommand(String input) {
        return subcommands.stream().filter(subcommand -> subcommand.matches(input)).findFirst();
    }
}
