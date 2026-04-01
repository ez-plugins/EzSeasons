package com.skyblockexp.lifesteal.seasons;

import com.skyblockexp.lifesteal.seasons.config.MessageService;
import org.bukkit.scheduler.BukkitTask;

public class Registry {

    private SeasonsApiImpl seasonsApi;
    private SeasonManager seasonManager;
    private MessageService messageService;
    private BukkitTask seasonCheckTask;

    public SeasonsApiImpl getSeasonsApi() {
        return seasonsApi;
    }

    public void setSeasonsApi(SeasonsApiImpl seasonsApi) {
        this.seasonsApi = seasonsApi;
    }

    public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public void setSeasonManager(SeasonManager seasonManager) {
        this.seasonManager = seasonManager;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public BukkitTask getSeasonCheckTask() {
        return seasonCheckTask;
    }

    public void setSeasonCheckTask(BukkitTask seasonCheckTask) {
        this.seasonCheckTask = seasonCheckTask;
    }
}
