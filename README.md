# EzSeasons

EzSeasons is a **standalone season reset scheduler** for Minecraft servers.
It runs timed season resets, sends reminder broadcasts, and provides `/season` so players can check the time until the next reset.

> This README is written for **server owners** first. API details are included near the end for plugin developers.

> EzSeasons >= 2.0.0 is build for Minecraft 26.1 & Java 25
> EzSeasons >

## Download

- Download **EzSeasons** from Modrinth: [https://modrinth.com/plugin/ezseasons](https://modrinth.com/plugin/ezseasons)
- Optional companion plugin: [EzLifesteal](https://modrinth.com/plugin/ezlifesteal)
- Supported server software: Paper / Bukkit-compatible server
- Requires: Java 25, Minecraft 26.1+
- Plugin version: 2.0.2

## Quick setup (server owners)

1. Stop your server.
2. Put `EzSeasons-<version>.jar` in your `plugins/` folder.
3. (Optional) Install `EzLifesteal` if you want Lifesteal-specific behavior.
4. Start the server once to generate config files.
5. Edit `plugins/EzSeasons/config.yml`.
6. Restart the server.

## What EzSeasons does

- Schedules automatic season resets
- Supports either explicit `start/end` windows or duration-based scheduling (`length-days`)
- Supports one-time or recurring explicit windows (`recurring`)
- Broadcasts a reset message when a reset occurs
- Sends reminder messages before reset (`reminder-minutes`)
- Provides `/season` (and `/season admin ...`) for status and runtime admin controls

## Commands & permissions

- `/season`
  - Permissions accepted: `lifesteal.season`, `lifesteal.season.admin`, **or** `lifesteal.admin`
- `/season admin <reload|reset [reason]|setnext <unixMillis>|clear-next|status>`
  - Permission required: `lifesteal.season.admin` **or** `lifesteal.admin`

## Main config options

File: `plugins/EzSeasons/config.yml`

```yaml
messages:
  language: "en"
  prefix: "&c[EzSeasons]&r "

season:
  enabled: false
  start: 0
  end: 0
  recurring: false
  length-days: 30
  check-interval-minutes: 60
  broadcast-message: "&7A new season has begun! Hearts have been reset."
  reminder-minutes: []
  reminder-message: "&7The season will reset in &c%time%&7."
  last-reset: 0
  next-reset: 0
```

### Notes

- Set `season.enabled: true` to activate scheduling.
- `season.start` and `season.end` are Unix epoch milliseconds.
- `season.recurring` applies to valid explicit `start/end` windows.
- `reminder-minutes` defines countdown offsets in minutes.
- Language files are in `plugins/EzSeasons/messages/`.
- `messages.language` supports: `en`, `es`, `fr`, `zh`, `ru`, `nl`.

## Documentation

- [Documentation index](docs/README.md)
- [Getting started](docs/getting-started.md)
- [Configuration reference](docs/configuration.md)
- [Command reference](docs/commands.md)
- [Permissions reference](docs/permissions.md)
- [API overview](docs/api.md)
- [Security policy](SECURITY.md)

## Troubleshooting

- **Plugin loads but nothing happens:** ensure `season.enabled` is `true`.
- **No reminders:** ensure `season.reminder-minutes` is not empty.
- **Command says no permission:** grant `lifesteal.season` for status, and `lifesteal.season.admin` (or legacy `lifesteal.admin`) for admin subcommands.
- **Integration not active:** verify the companion plugin registers against the `SeasonsApi` Bukkit service.

## For developers (optional)

EzSeasons exposes a Bukkit service API (`SeasonsApi`) for integrations.

### Dependency options

You can consume the API artifact in two ways:

1. **GitHub Packages** (official)
2. **JitPack** (fallback)

#### GitHub Packages

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
    <version>2.0.2</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

#### JitPack

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
    <version>v2.0.2</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

### Obtain API via Bukkit Services

```java
RegisteredServiceProvider<SeasonsApi> registration =
    Bukkit.getServicesManager().getRegistration(SeasonsApi.class);

if (registration != null) {
    SeasonsApi api = registration.getProvider();
    api.registerIntegration(new MyIntegration());
}
```
