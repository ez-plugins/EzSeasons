package com.skyblockexp.lifesteal.seasons.command.season;

import com.skyblockexp.lifesteal.seasons.EzSeasonsPlugin;
import com.skyblockexp.lifesteal.seasons.command.framework.Command;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.AdminSubcommand;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.ReloadSubcommand;
import com.skyblockexp.lifesteal.seasons.command.season.subcommand.StatusSubcommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Map;

public final class SeasonCommand extends Command implements CommandExecutor, TabCompleter {

    private final SeasonCommandContext context;
    private final StatusSubcommand statusSubcommand;

    public SeasonCommand(EzSeasonsPlugin plugin) {
        super("season");
        this.context = new SeasonCommandContext(plugin);
        this.statusSubcommand = new StatusSubcommand(context);
        registerSubcommand(new AdminSubcommand(context));
        registerSubcommand(statusSubcommand);
        registerSubcommand(new ReloadSubcommand(context));
    }

    @Override
    protected boolean onDefault(CommandSender sender) {
        return statusSubcommand.execute(sender, new String[0]);
    }

    @Override
    protected boolean onUnknownSubcommand(CommandSender sender, String input) {
        context.getPlugin().getMessageService().sendMessage(sender, "admin-unknown-subcommand",
                Map.of("subcommand", input, "usage", "/season <status|admin>"));
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        return tabComplete(sender, args);
    }
}
