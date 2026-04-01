# API

EzSeasons exposes a Bukkit service API for companion plugins.

> If you are a server owner only, you can usually skip this page.

## Add EzSeasons to your build

You can use either GitHub Packages (official) or JitPack.

> **Artifact publishing note:** official artifacts are published from `ez-plugins/EzSeasons` to GitHub Packages (`https://maven.pkg.github.com/ez-plugins/EzSeasons`). Keep dependency snippets aligned with this repository slug.

### Option 1: GitHub Packages

```xml
<repositories>
  <repository>
    <id>github-ezseasons</id>
    <url>https://maven.pkg.github.com/ez-plugins/EzSeasons</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.skyblockexp.lifesteal</groupId>
    <artifactId>ezseasons-api</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

> GitHub Packages requires credentials in `~/.m2/settings.xml`.

### Option 2: JitPack

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.ez-plugins.EzSeasons</groupId>
    <artifactId>ezseasons-api</artifactId>
    <version>v1.0.0</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

## Core interfaces

### `SeasonsApi`

Public service interface for registering integrations and triggering season resets.

- `boolean registerIntegration(SeasonsIntegration integration)`
- `void unregisterIntegration(SeasonsIntegration integration)`
- `List<SeasonsIntegration> getIntegrations()`
- `boolean triggerSeasonReset(String reason)`

### `SeasonsIntegration`

Implement this in your plugin to receive lifecycle callbacks.

- `void onRegister(SeasonsApi api)`
- `void onUnregister()`

## Reset event hooks (recommended)

To add custom functionality when a season resets, listen for `SeasonResetEvent`.

`SeasonResetEvent` provides:
- `getPreviousResetMillis()`
- `getResetMillis()`
- `getNextResetMillis()`
- `getReason()`

```java
import com.skyblockexp.lifesteal.seasons.api.events.SeasonResetEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class SeasonResetListener implements Listener {
    @EventHandler
    public void onSeasonReset(SeasonResetEvent event) {
        // Run your custom integration logic here.
        // Example: sync databases, clear custom stats, update holograms, etc.
    }
}
```

## Triggering a reset through the API

```java
import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

RegisteredServiceProvider<SeasonsApi> registration =
    Bukkit.getServicesManager().getRegistration(SeasonsApi.class);

if (registration != null) {
    SeasonsApi api = registration.getProvider();
    api.triggerSeasonReset("admin");
}
```

## Additional lifecycle events

EzSeasons also publishes integration lifecycle events:

- `SeasonsIntegrationRegisteredEvent`
- `SeasonsIntegrationUnregisteredEvent`
