---
title: Commands
nav_order: 3
description: "Reference for all EzSeasons commands and admin subcommands"
---

# Commands
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

EzSeasons adds one root command (`/season`) with a set of admin subcommands. All commands support full tab completion.

---

## `/season`

Shows the remaining time until the next configured season reset.

| Detail | Value |
|--------|-------|
| Alias | `/season status` |
| Permission | `lifesteal.season`, `lifesteal.season.admin`, or `lifesteal.admin` |
| Available to | Players and console |

**Example output:**
```
[EzSeasons] Next season reset in 12d 4h 33m.
```

If scheduling is disabled or `next-reset` is not set, you will see an informational message explaining why no countdown is available.

---

## `/season admin ...`

Administrative runtime operations. Requires `lifesteal.season.admin` or `lifesteal.admin`.

**Usage:** `/season admin <subcommand>`

### Subcommand overview

| Subcommand | What it does |
|-----------|-------------|
| `reload` | Reloads `config.yml` and all language files without a server restart |
| `status` | Shows the season countdown plus raw `last-reset` / `next-reset` timestamps |
| `reset [reason] --confirm` | Forces an immediate season reset |
| `setnext <unixMillis>` | Overrides the next reset time to a specific Unix timestamp |
| `clear-next --confirm` | Removes the stored next-reset time so EzSeasons recalculates from scratch |

---

### `/season admin reload`

Reloads `config.yml` and all message translation files, then rebuilds the active season manager. Use this after editing the config file so you do not need to restart the server.

```
/season admin reload
```

---

### `/season admin status`

Shows the same countdown as `/season`, plus the raw stored values for debugging purposes.

```
/season admin status
```

**Example output:**
```
[EzSeasons] Next season reset in 12d 4h 33m.
[EzSeasons] Raw values -> last-reset=1735689600000, next-reset=1738368000000
```

---

### `/season admin reset [reason] --confirm`

Forces an immediate season reset. This triggers the `SeasonResetEvent`, sends the broadcast message, and recalculates `next-reset`.

The `--confirm` flag is required to prevent accidental resets.

```
/season admin reset --confirm
/season admin reset maintenance --confirm
/season admin reset manual --confirm
```

- If `reason` is omitted, it defaults to `admin`.
- The reason is passed to `SeasonResetEvent` and visible to companion plugins.

> **Warning:** This is destructive. Companion plugins (e.g. EzLifesteal) will execute their reset logic immediately.

---

### `/season admin setnext <unixMillis>`

Sets the next season reset to a specific point in time. Useful for pushing a reset forward or delaying it without changing the `config.yml` scheduling settings.

```
/season admin setnext 1738368000000
```

The argument is a **Unix timestamp in milliseconds** (not seconds). 

**How to get a Unix timestamp in milliseconds:**
- Visit [epochconverter.com](https://www.epochconverter.com/) and multiply the result by 1000 if the site shows seconds.
- Or use tab completion — EzSeasons shows `now+3600000` as an example hint (current time + 1 hour in ms).

Returns a confirmation with the human-readable date when successful:

```
[EzSeasons] Next reset set to 1738368000 (2025-02-01T00:00:00Z).
```

---

### `/season admin clear-next --confirm`

Clears the stored `next-reset` value (sets it to `0`). EzSeasons will then recalculate the next reset from the current scheduling configuration on its next check cycle.

The `--confirm` flag is required.

```
/season admin clear-next --confirm
```

Use this if you want to let the plugin recalculate the next reset after changing scheduling settings, or after manually editing the config.

---

## Tab completion reference

| Input | Suggestions |
|-------|-------------|
| `/season` | `admin`, `status` |
| `/season admin` | `reload`, `reset`, `setnext`, `clear-next`, `status` |
| `/season admin reset` | `<reason...>`, `maintenance`, `manual`, `--confirm` |
| `/season admin setnext` | `<unixMillis>`, `now+3600000` (example hint) |
| `/season admin clear-next` | `--confirm` |

---

## Notes

- Most admin subcommands require `season.enabled: true` in `config.yml` to work correctly.
- Admin operations that write to the config (such as `setnext` and `clear-next`) save immediately to disk — no reload needed afterward.
- Admin command response messages can be customized under `messages.keys` in `config.yml`. See the [Configuration](configuration) page for the full list of available keys.
