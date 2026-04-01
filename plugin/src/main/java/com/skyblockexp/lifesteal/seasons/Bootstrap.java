package com.skyblockexp.lifesteal.seasons;

import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import com.skyblockexp.lifesteal.seasons.command.season.SeasonCommand;
import com.skyblockexp.lifesteal.seasons.config.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

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
        plugin.getLogger().info("EzSeasons ready. Awaiting plugin integrations via API registration.");
    }

    public void stop() {
        cancelSeasonCheckTask();
        registry.setSeasonManager(null);
        SeasonsApiImpl seasonsApi = registry.getSeasonsApi();
        if (seasonsApi != null) {
            Bukkit.getServicesManager().unregister(seasonsApi);
            seasonsApi.clear();
            registry.setSeasonsApi(null);
        }
    }

    public synchronized void reloadSeasonConfiguration() {
        plugin.reloadConfig();
        loadMessages();
        ConfigurationSection seasonSection = plugin.getConfig().getConfigurationSection("season");
        registry.setSeasonManager(new SeasonManager(plugin, seasonSection, registry.getMessageService()));
        restartSeasonCheckTask();
    }

    private void registerApi() {
        SeasonsApiImpl seasonsApi = new SeasonsApiImpl(plugin);
        registry.setSeasonsApi(seasonsApi);
        Bukkit.getServicesManager().register(SeasonsApi.class, seasonsApi, plugin, ServicePriority.Normal);
    }

    public void loadMessages() {
        FileConfiguration configuration = plugin.getConfig();
        String prefix = configuration.getString("messages.prefix", "&c[EzSeasons]&r ");
        String selectedLanguage = configuration.getString("messages.language", "en").toLowerCase(Locale.ROOT);
        if (!SUPPORTED_LANGUAGES.contains(selectedLanguage)) {
            plugin.getLogger().warning("Unsupported messages.language '" + selectedLanguage + "'. Falling back to 'en'.");
            selectedLanguage = "en";
        }

        registry.setMessageService(new MessageService(prefix));

        ensureMessageFiles();

        File messagesFolder = new File(plugin.getDataFolder(), "messages");
        registerMessages(loadLanguageSection(messagesFolder, "en"), "", Set.of());

        if (!"en".equals(selectedLanguage)) {
            registerMessages(loadLanguageSection(messagesFolder, selectedLanguage), "", Set.of());
        }

        registerMessages(plugin.getConfig().getConfigurationSection("messages.keys"), "", Set.of());
    }

    private ConfigurationSection loadLanguageSection(File messagesFolder, String language) {
        File languageFile = new File(messagesFolder, language + ".yml");
        if (!languageFile.exists()) {
            plugin.getLogger().warning("Missing message file: " + languageFile.getName() + ".");
            return null;
        }
        YamlConfiguration languageConfiguration = YamlConfiguration.loadConfiguration(languageFile);
        return languageConfiguration.getConfigurationSection("messages") == null
                ? languageConfiguration
                : languageConfiguration.getConfigurationSection("messages");
    }

    private void ensureMessageFiles() {
        File messagesFolder = new File(plugin.getDataFolder(), "messages");
        if (!messagesFolder.exists() && !messagesFolder.mkdirs()) {
            plugin.getLogger().warning("Unable to create messages folder at " + messagesFolder.getAbsolutePath());
            return;
        }
        for (String language : SUPPORTED_LANGUAGES) {
            File destination = new File(messagesFolder, language + ".yml");
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
            String qualifiedKey = path.isEmpty() ? key : path + "." + key;
            if (section.isConfigurationSection(key)) {
                registerMessages(section.getConfigurationSection(key), qualifiedKey, Set.of());
            } else if (section.isString(key)) {
                registry.getMessageService().register(qualifiedKey, section.getString(key));
            }
        }
    }

    private void registerCommands() {
        PluginCommand seasonCommand = plugin.getCommand("season");
        if (seasonCommand == null) {
            plugin.getLogger().severe("Command 'season' is not defined in plugin.yml; command registration skipped.");
            return;
        }
        SeasonCommand commandHandler = new SeasonCommand(plugin);
        seasonCommand.setExecutor(commandHandler);
        seasonCommand.setTabCompleter(commandHandler);
    }

    private synchronized void restartSeasonCheckTask() {
        cancelSeasonCheckTask();

        SeasonManager seasonManager = registry.getSeasonManager();
        if (seasonManager == null || !seasonManager.isEnabled()) {
            return;
        }

        long periodTicks = durationToTicks(seasonManager.getCheckInterval());
        BukkitTask seasonCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            SeasonManager manager = registry.getSeasonManager();
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
        BukkitTask seasonCheckTask = registry.getSeasonCheckTask();
        if (seasonCheckTask != null) {
            seasonCheckTask.cancel();
            registry.setSeasonCheckTask(null);
        }
    }

    private long durationToTicks(Duration duration) {
        long seconds = Math.max(1L, duration.getSeconds());
        return Math.max(20L, seconds * 20L);
    }
}
