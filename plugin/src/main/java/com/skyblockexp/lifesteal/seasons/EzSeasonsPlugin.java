package com.skyblockexp.lifesteal.seasons;

import com.skyblockexp.lifesteal.seasons.config.MessageService;
import org.bukkit.plugin.java.JavaPlugin;

public class EzSeasonsPlugin extends JavaPlugin {

    private final Registry registry = new Registry();

    private final Bootstrap bootstrap = new Bootstrap(this, registry);

    @Override
    public void onEnable() {
        bootstrap.start();
    }

    @Override
    public void onDisable() {
        bootstrap.stop();
    }

    public void reloadSeasonConfiguration() {
        bootstrap.reloadSeasonConfiguration();
    }

    // Backward-compatible hook retained for tests and reflective callers.
    private void loadMessages() {
        bootstrap.loadMessages();
    }

    /**
     * Called by external integrations (for example EzLifesteal) to provide the
     * active {@link SeasonManager} implementation used by EzSeasons.
     *
     * @param manager the season manager to register; must not be {@code null}
     */
    public void registerSeasonManager(SeasonManager manager) {
        registry.setSeasonManager(manager);
    }

    public MessageService getMessageService() {
        return registry.getMessageService();
    }

    public SeasonManager getSeasonManager() {
        return registry.getSeasonManager();
    }

    public SeasonsApiImpl getSeasonsApi() {
        return registry.getSeasonsApi();
    }
}
