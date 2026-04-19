---
title: Getting Started
nav_order: 2
description: "Install EzSeasons and set up your first season schedule"
---

# Getting Started
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Requirements

| Requirement | Minimum version |
|---|---|
| Server software | Paper 26.1 (or a compatible fork) |
| Java | 25 |

EzSeasons does **not** require EzLifesteal. It is fully standalone.

---

## Installation

1. Stop your server.
2. Download `EzSeasons-2.1.2.jar` from [Modrinth](https://modrinth.com/plugin/ezseasons).
3. Copy the jar into your `plugins/` folder.
4. *(Optional)* Install [EzLifesteal](https://modrinth.com/plugin/ezlifesteal) if you want Lifesteal heart-reset behavior on each season reset.
5. Start the server once. EzSeasons generates `plugins/EzSeasons/config.yml` and the `messages/` folder.
6. Stop the server again and edit `config.yml` (see below).
7. Start the server.

---

## Basic configuration

Open `plugins/EzSeasons/config.yml`. The minimum required change is enabling the plugin and choosing a scheduling mode.

### Duration-based schedule (recommended for most servers)

The plugin calculates the next reset by adding `length-days` to the last recorded reset. This is the simplest approach.

```yaml
season:
  enabled: true
  length-days: 30          # Season lasts 30 days
  check-interval-minutes: 60
  broadcast-message: "&7A new season has begun!"
  reminder-minutes:
    - 1440   # 24 hours before
    - 60     # 1 hour before
    - 10     # 10 minutes before
  reminder-message: "&7The season will reset in &c%time%&7."
```

Leave `start`, `end`, and `recurring` at their defaults (`0` / `false`).

### Fixed-window schedule

Use `start` and `end` (Unix millisecond timestamps) for a precise one-time or recurring window.

```yaml
season:
  enabled: true
  start: 1735689600000     # Window opens  2025-01-01 00:00 UTC
  end:   1738368000000     # Window closes 2025-02-01 00:00 UTC
  recurring: true          # Repeat the same window length each period
  check-interval-minutes: 60
```

To get a Unix millisecond timestamp:

- Visit [epochconverter.com](https://www.epochconverter.com/) and multiply the displayed value by `1000`.
- Or use `/season admin setnext` at runtime (see [Commands](commands)).

---

## Verify the setup

After starting the server with a valid config:

1. Run `/season` in-game or in the console — you should see a countdown.
2. Run `/season admin status` to inspect the raw timestamps.

If you see `Season resets are currently disabled`, confirm `season.enabled: true` is in your config and that you have reloaded (`/season admin reload`).

---

## Choosing a language

Edit `messages.language` in `config.yml`:

```yaml
messages:
  language: "en"   # Supported: en, es, fr, zh, ru, nl
```

The corresponding file in `plugins/EzSeasons/messages/` is loaded automatically on startup and reload.

---

## Next steps

- Read the full [Configuration reference](configuration) for every available option.
- See [Commands](commands) for admin operations like forcing a reset or adjusting the next reset time.
- See [Permissions](permissions) to lock down admin commands on your server.
- If you are a developer, read the [API guide](api) to integrate with EzSeasons from your own plugin.
