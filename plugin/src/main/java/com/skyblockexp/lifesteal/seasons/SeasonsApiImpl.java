package com.skyblockexp.lifesteal.seasons;

import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import com.skyblockexp.lifesteal.seasons.api.SeasonsIntegration;
import com.skyblockexp.lifesteal.seasons.api.events.SeasonsIntegrationRegisteredEvent;
import com.skyblockexp.lifesteal.seasons.api.events.SeasonsIntegrationUnregisteredEvent;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

final class SeasonsApiImpl implements SeasonsApi {

    private final EzSeasonsPlugin plugin;

    private final java.util.List<SeasonsIntegration> integrations = new java.util.ArrayList<>();

    SeasonsApiImpl(EzSeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public synchronized boolean registerIntegration(SeasonsIntegration integration) {
        Objects.requireNonNull(integration, "integration");
        if (integrations.contains(integration)) {
            return false;
        }
        integrations.add(integration);
        try {
            integration.onRegister(this);
        }
        catch (Throwable throwable) {
            integrations.remove(integration);
            logLifecycleFailure("register", integration, throwable);
            throw propagate(throwable);
        }
        fireEvent(new SeasonsIntegrationRegisteredEvent(this, integration));
        return true;
    }

    @Override
    public synchronized void unregisterIntegration(SeasonsIntegration integration) {
        if (integrations.remove(integration)) {
            try {
                integration.onUnregister();
            }
            catch (Throwable throwable) {
                integrations.add(integration);
                logLifecycleFailure("unregister", integration, throwable);
                throw propagate(throwable);
            }
            fireEvent(new SeasonsIntegrationUnregisteredEvent(this, integration));
        }
    }

    @Override
    public synchronized java.util.List<SeasonsIntegration> getIntegrations() {
        return java.util.List.copyOf(integrations);
    }

    @Override
    public boolean triggerSeasonReset(String reason) {
        if (plugin == null || plugin.getSeasonManager() == null) {
            return false;
        }
        plugin.getSeasonManager().triggerSeasonReset(reason);
        return true;
    }

    synchronized void clear() {
        integrations.clear();
    }

    private void fireEvent(org.bukkit.event.Event event) {
        if (Bukkit.getServer() != null) {
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    private void logLifecycleFailure(String phase, SeasonsIntegration integration, Throwable throwable) {
        logger().log(
                Level.SEVERE,
                "Integration " + integration.getClass().getName() + " failed during " + phase + " callback.",
                throwable
        );
    }

    private Logger logger() {
        return plugin != null ? plugin.getLogger() : Logger.getLogger(SeasonsApiImpl.class.getName());
    }

    private RuntimeException propagate(Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        return new RuntimeException(throwable);
    }
}
