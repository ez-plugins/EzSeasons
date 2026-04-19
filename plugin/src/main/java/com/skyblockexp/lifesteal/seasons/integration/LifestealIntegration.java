package com.skyblockexp.lifesteal.seasons.integration;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public interface LifestealIntegration {

    CompletableFuture<Void> resetAllHeartsAsync();

    Optional<Profile> getLoadedProfile(UUID uniqueId);

    void applyHearts(Player player, Profile profile);

    void sendHeartStatus(Player player, double hearts);

    void requestTopHologramUpdate();

    interface Profile {
        double getHearts();
    }
}
