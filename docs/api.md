---
title: API
nav_order: 6
description: "Developer API documentation for integrating with EzSeasons"
---

# API
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

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
    <version>2.0.0</version>
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
    <version>v2.0.0</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

## Threading

All public API methods are safe to call from concurrent threads — the integration registry is internally synchronized. However, **callbacks** (`onRegister`, `onUnregister`) and **Bukkit events** (`SeasonResetEvent`, etc.) are fired on the calling thread. In typical usage this is the Bukkit main thread.

---

## Core interfaces

### `SeasonsApi`

Public service interface registered as a Bukkit service. Obtain it via `Bukkit.getServicesManager().getRegistration(SeasonsApi.class)`.

| Method | Returns | Description |
|---|---|---|
| `registerIntegration(SeasonsIntegration integration)` | `boolean` | Registers an integration. Returns `true` on success, `false` if already registered. Throws `NullPointerException` for `null`. |
| `unregisterIntegration(SeasonsIntegration integration)` | `void` | Unregisters an integration. A `null` argument is a no-op. |
| `getIntegrations()` | `List<SeasonsIntegration>` | Returns an **immutable snapshot** of currently registered integrations. |
| `triggerSeasonReset(String reason)` | `boolean` | Triggers an immediate reset. Returns `true` if a season manager is active. `reason` may be `null` (stored as `"unspecified"`). |

**Ordering guarantees:**
- `registerIntegration` — registry is updated **before** `onRegister` is called and before `SeasonsIntegrationRegisteredEvent` fires.
- `unregisterIntegration` — registry is updated **before** `onUnregister` is called and before `SeasonsIntegrationUnregisteredEvent` fires.
- `triggerSeasonReset` — timestamps are persisted **before** `SeasonResetEvent` fires.

### `SeasonsIntegration`

Implement this interface in your plugin to receive registration lifecycle callbacks.

| Method | Description |
|---|---|
| `onRegister(SeasonsApi api)` | Called after this integration is added to the EzSeasons registry. |
| `onUnregister()` | Called after this integration is removed from the EzSeasons registry. |

Exceptions thrown from either callback propagate to the caller; EzSeasons does not swallow them.

---

## Events

### `SeasonResetEvent`

Fired whenever EzSeasons performs a season reset (scheduled or forced). Fired **after** timestamps are updated and persisted.

| Method | Return type | Description |
|---|---|---|
| `getPreviousResetMillis()` | `long` | Unix epoch ms of the reset immediately before this one. |
| `getResetMillis()` | `long` | Unix epoch ms of this reset. |
| `getNextResetMillis()` | `long` | Unix epoch ms of the next scheduled reset, or `0` if unscheduled. |
| `getReason()` | `String` | Caller-provided reason; never `null` — defaults to `"unspecified"`. |

**Listening example:**

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

### `SeasonsIntegrationRegisteredEvent`

Fired after a `SeasonsIntegration` is registered. Fired **after** `onRegister` returns normally.

| Method | Return type | Description |
|---|---|---|
| `getApi()` | `SeasonsApi` | The API instance that performed the registration. Never `null`. |
| `getIntegration()` | `SeasonsIntegration` | The integration that was registered. Never `null`. |

### `SeasonsIntegrationUnregisteredEvent`

Fired after a `SeasonsIntegration` is unregistered. Fired **after** `onUnregister` returns normally.

| Method | Return type | Description |
|---|---|---|
| `getApi()` | `SeasonsApi` | The API instance that performed the unregistration. Never `null`. |
| `getIntegration()` | `SeasonsIntegration` | The integration that was unregistered. Never `null`. |

---

## Triggering a reset through the API

```java
import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

RegisteredServiceProvider<SeasonsApi> rsp =
    Bukkit.getServicesManager().getRegistration(SeasonsApi.class);

if (rsp != null) {
    SeasonsApi api = rsp.getProvider();
    api.triggerSeasonReset("admin");
}
```

---

## Registering an integration: full example

```java
import com.skyblockexp.lifesteal.seasons.api.SeasonsApi;
import com.skyblockexp.lifesteal.seasons.api.SeasonsIntegration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {

    private SeasonsApi seasonsApi;

    @Override
    public void onEnable() {
        RegisteredServiceProvider<SeasonsApi> rsp =
            Bukkit.getServicesManager().getRegistration(SeasonsApi.class);

        if (rsp == null) {
            getLogger().warning("EzSeasons not found — season integration disabled.");
            return;
        }

        seasonsApi = rsp.getProvider();
        seasonsApi.registerIntegration(new MyIntegration());
    }

    @Override
    public void onDisable() {
        if (seasonsApi != null) {
            seasonsApi.unregisterIntegration(/* your instance */ null);
        }
    }
}
```

---

## `plugin.yml` soft-dependency

Add EzSeasons as a soft dependency so your plugin loads after it when both are present:

```yaml
softdepend: [EzSeasons]
```
