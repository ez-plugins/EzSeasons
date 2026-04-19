---
title: Home
nav_order: 1
description: "EzSeasons — standalone season reset scheduler for Minecraft servers"
permalink: /
---

# EzSeasons
{: .no_toc }

EzSeasons is a **standalone season reset scheduler** for Paper / Bukkit-compatible Minecraft servers.
It schedules timed season resets, broadcasts announcements, sends configurable countdown reminders, and exposes a developer API so companion plugins can react to each reset.

---

## Feature overview

| Feature | Details |
|---|---|
| Automatic season resets | Duration-based (`length-days`) or explicit `start`/`end` windows |
| Recurring windows | Optional; re-uses a `start`/`end` window every period |
| Countdown reminders | `reminder-minutes` list; fires a chat message before each reset |
| Broadcast on reset | Configurable broadcast message sent to all online players |
| `/season` command | Players can check the time remaining until the next reset |
| Admin subcommands | `reload`, `reset`, `setnext`, `clear-next`, `status` |
| Multi-language support | `en`, `es`, `fr`, `zh`, `ru`, `nl` |
| Developer API | Pure-Java API artifact; `SeasonsIntegration` callbacks (`onRegister`, `onUnregister`, `onSeasonReset`); Bukkit events in plugin JAR |

---

## Compatibility

| Requirement | Version |
|---|---|
| Minecraft / Paper | 26.1 or later |
| Java | 25 or later |
| Plugin version | 2.1.1 |

---

## Quick navigation

- [Getting started](getting-started) — install and first-time setup
- [Configuration](configuration) — full `config.yml` reference
- [Commands](commands) — all `/season` commands and their options
- [Permissions](permissions) — permission node reference
- [API](api) — developer integration guide
