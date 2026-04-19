package com.skyblockexp.lifesteal.seasons;

import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommand;
import com.skyblockexp.lifesteal.seasons.compatibility.ServerEnvironment;
import com.skyblockexp.lifesteal.seasons.config.MessageService;
import java.io.File;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.scheduler.BukkitTask;

public class Bootstrap {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "es", "fr", "zh", "ru", "nl");

    private final EzSeasonsPlugin plugin;

    private final Registry registry;

    public Bootstrap(EzSeasonsPlugin plugin, Registry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void start() {
        plugin.saveDefaultConfig();
        registerApi();
        reloadSeasonConfiguration();
        registerCommands();
        logStartupSummary();
    }

    public void stop() {
        cancelSeasonCheckTask();
        registry.setSeasonManager(null);
        final SeasonsApiImpl seasonsApi = registry.getSeasonsApi();
        if (seasonsApi != null) {
            Bukkit.getServicesManager().unregister(seasonsApi);
            seasonsApi.clear();
            registry.setSeasonsApi(null);
        }
    }

    public synchronized void reloadSeasonConfiguration() {
        plugin.reloadConfig();
        loadMessages();
        final ConfigurationSection seasonSection = plugin.getConfig().getConfigurationSection("season");
        registry.setSeasonManager(new SeasonManager(plugin, seasonSection, registry.getMessageService()));
        restartSeasonCheckTask();
    }

    private void logStartupSummary() {
        final SeasonManager manager = registry.getSeasonManager();
        final String version = plugin.getDescription().getVersion();
        final String language = plugin.getConfig().getString("messages.language", "en");
        final String java = Runtime.version().feature() + "." + Runtime.version().interim();

        plugin.getLogger().info("EzSeasons v" + version + " | " + ServerEnvironment.brand() + " | Java " + java);

        if (manager != null && manager.isEnabled()) {
            final long intervalMins = manager.getCheckInterval().toMinutes();
            plugin.getLogger().info("Schedule  : enabled | check every " + intervalMins + " min | language: " + language);

            final Optional<Duration> timeUntil = manager.getTimeUntilReset();
            if (timeUntil.isPresent()) {
                plugin.getLogger().info("Next reset: in " + formatStartupDuration(timeUntil.get()));
            } else {
                plugin.getLogger().info("Next reset: not yet scheduled (run /season admin setnext to set one)");
            }
        } else {
            plugin.getLogger().info("Schedule  : disabled | language: " + language);
        }
    }

    private static String formatStartupDuration(Duration d) {
        final long totalSeconds = d.getSeconds();
        final long days = totalSeconds / 86400;
        final long hours = (totalSeconds % 86400) / 3600;
        final long minutes = (totalSeconds % 3600) / 60;
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return Math.max(minutes, 1) + "m";
    }

    private void registerApi() {
        final SeasonsApiImpl seasonsApi = new SeasonsApiImpl(plugin);
        registry.setSeasonsApi(seasonsApi);
        Bukkit.getServicesManager().register(SeasonsApi.class, seasonsApi, plugin, ServicePriority.Normal);
    }

    public void loadMessages() {
        final FileConfiguration configuration = plugin.getConfig();
        final String prefix = configuration.getString("messages.prefix", "&c[EzSeasons]&r ");
        String selectedLanguage = configuration.getString("messages.language", "en").toLowerCase(Locale.ROOT);
        if (!SUPPORTED_LANGUAGES.contains(selectedLanguage)) {
            plugin.getLogger().warning("Unsupported messages.language '" + selectedLanguage + "'. Falling back to 'en'.");
            selectedLanguage = "en";
        }

        registry.setMessageService(new MessageService(prefix));

        ensureMessageFiles();

        final File messagesFolder = new File(plugin.getDataFolder(), "messages");
        registerMessages(loadLanguageSection(messagesFolder, "en"), "", Set.of());

        if (!"en".equals(selectedLanguage)) {
            registerMessages(loadLanguageSection(messagesFolder, selectedLanguage), "", Set.of());
        }

        registerMessages(plugin.getConfig().getConfigurationSection("messages.keys"), "", Set.of());
    }

    private ConfigurationSection loadLanguageSection(File messagesFolder, String language) {
        final File languageFile = new File(messagesFolder, language + ".yml");
        if (!languageFile.exists()) {
            plugin.getLogger().warning("Missing message file: " + languageFile.getName() + ".");
            return null;
        }
        final YamlConfiguration languageConfiguration = YamlConfiguration.loadConfiguration(languageFile);
        return languageConfiguration.getConfigurationSection("messages") == null
                ? languageConfiguration
                : languageConfiguration.getConfigurationSection("messages");
    }

    private void ensureMessageFiles() {
        final File messagesFolder = new File(plugin.getDataFolder(), "messages");
        if (!messagesFolder.exists() && !messagesFolder.mkdirs()) {
            plugin.getLogger().warning("Unable to create messages folder at " + messagesFolder.getAbsolutePath());
            return;
        }
        for (String language : SUPPORTED_LANGUAGES) {
            final File destination = new File(messagesFolder, language + ".yml");
            if (!destination.exists()) {
                plugin.saveResource("messages/" + language + ".yml", false);
            }
        }
    }

    private void registerMessages(ConfigurationSection section, String path, Set<String> keysToSkip) {
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            if (path.isEmpty() && keysToSkip.contains(key.toLowerCase(Locale.ROOT))) {
                continue;
            }
            final String qualifiedKey = path.isEmpty() ? key : path + "." + key;
            if (section.isConfigurationSection(key)) {
                registerMessages(section.getConfigurationSection(key), qualifiedKey, Set.of());
            } else if (section.isString(key)) {
                registry.getMessageService().register(qualifiedKey, section.getString(key));
            }
        }
    }

    private void registerCommands() {
        final PluginCommand seasonCommand = plugin.getCommand("season");
        if (seasonCommand == null) {
            plugin.getLogger().severe("Command 'season' is not defined in plugin.yml; command registration skipped.");
            return;
        }
        final SeasonCommand commandHandler = new SeasonCommand(plugin);
        seasonCommand.setExecutor(commandHandler);
        seasonCommand.setTabCompleter(commandHandler);
    }

    private synchronized void restartSeasonCheckTask() {
        cancelSeasonCheckTask();

        final SeasonManager seasonManager = registry.getSeasonManager();
        if (seasonManager == null || !seasonManager.isEnabled()) {
            return;
        }

        final long periodTicks = durationToTicks(seasonManager.getCheckInterval());
        final BukkitTask seasonCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            final SeasonManager manager = registry.getSeasonManager();
            if (manager == null || !manager.isEnabled()) {
                return;
            }
            if (manager.shouldTriggerReset(System.currentTimeMillis())) {
                manager.triggerSeasonReset("schedule");
            }
        }, periodTicks, periodTicks);
        registry.setSeasonCheckTask(seasonCheckTask);
    }

    private synchronized void cancelSeasonCheckTask() {
        final BukkitTask seasonCheckTask = registry.getSeasonCheckTask();
        if (seasonCheckTask != null) {
            seasonCheckTask.cancel();
            registry.setSeasonCheckTask(null);
        }
    }

    private long durationToTicks(Duration duration) {
        final long seconds = Math.max(1L, duration.getSeconds());
        return Math.max(20L, seconds * 20L);
    }
}
