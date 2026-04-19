---
title: Configuration
nav_order: 4
description: "Complete reference for all EzSeasons config.yml options"
---

# Configuration
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

EzSeasons stores its main configuration at `plugins/EzSeasons/config.yml`.
Run `/season admin reload` to apply changes without restarting the server.

---

## Default config.yml

```yaml
# EzSeasons configuration.
#
# EzSeasons is standalone and can run without EzLifesteal.
# Companion plugins may still integrate through the public API.

messages:
  language: "en"
  prefix: "&c[EzSeasons]&r "
  keys:
    admin-usage: "&7Usage: /season admin <reload|reset|setnext|clear-next|status>"
    admin-reload-success: "&aSeason configuration and messages reloaded."
    admin-reset-confirm-required: "&eReset is destructive. Re-run with confirmation: &f%command%"
    admin-reset-success: "&aSeason reset triggered successfully. Reason: &f%reason%"
    admin-setnext-usage: "&7Usage: /season admin setnext <unixMillis>"
    admin-setnext-success: "&aNext reset set to &f%timestamp%&a (&f%iso%&a)."
    admin-setnext-invalid-timestamp: "&cInvalid timestamp '%value%'. Expected %expected%."
    admin-setnext-out-of-range: "&cTimestamp %value% is out of range. Allowed range: %min%..%max%."
    admin-clear-next-confirm-required: "&eClearing next reset is destructive. Re-run with confirmation: &f%command%"
    admin-clear-next-success: "&aCleared the stored next reset timestamp."
    admin-status-raw: "&7Raw values -> last-reset=%lastReset%, next-reset=%nextReset%"
    admin-unknown-subcommand: "&cUnknown subcommand '%subcommand%'. %usage%"

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

---

## `messages` section

### `messages.language`

Language code for player-facing messages. The corresponding file is loaded from `plugins/EzSeasons/messages/<code>.yml`.

| Value | Language |
|---|---|
| `en` | English (default) |
| `es` | Spanish |
| `fr` | French |
| `zh` | Chinese |
| `ru` | Russian |
| `nl` | Dutch |

### `messages.prefix`

Chat prefix prepended to all EzSeasons messages. Supports `&` color codes.

**Default:** `"&c[EzSeasons]&r "`

### `messages.keys`

Overrides for individual admin-facing message strings. Each key maps to a specific system message. All keys support `&` color codes and the placeholders listed in the table below.

| Key | Placeholders | Purpose |
|---|---|---|
| `admin-usage` | — | Shown when `/season admin` is run with no subcommand |
| `admin-reload-success` | — | Shown after a successful reload |
| `admin-reset-confirm-required` | `%command%` | Shown when `--confirm` is missing from a reset call |
| `admin-reset-success` | `%reason%` | Shown after a forced reset succeeds |
| `admin-setnext-usage` | — | Shown when `setnext` is missing its argument |
| `admin-setnext-success` | `%timestamp%`, `%iso%` | Shown after `setnext` succeeds |
| `admin-setnext-invalid-timestamp` | `%value%`, `%expected%` | Shown when the timestamp argument is not a valid long |
| `admin-setnext-out-of-range` | `%value%`, `%min%`, `%max%` | Shown when the timestamp is outside the allowed range |
| `admin-clear-next-confirm-required` | `%command%` | Shown when `--confirm` is missing from a `clear-next` call |
| `admin-clear-next-success` | — | Shown after `clear-next` succeeds |
| `admin-status-raw` | `%lastReset%`, `%nextReset%` | Second line of `/season admin status` output |
| `admin-unknown-subcommand` | `%subcommand%`, `%usage%` | Shown for unrecognised subcommands |

> Player-facing message keys (e.g. `season-status`, `no-permission`) are defined in the per-language files under `plugins/EzSeasons/messages/`.

---

## `season` section

### `season.enabled`

Master switch. Set to `true` to activate all scheduling, reminders, and resets.

**Default:** `false`

---

### `season.start` and `season.end`

Unix epoch milliseconds defining the boundary of an explicit season window.

- If both are non-zero and `start < end`, EzSeasons uses the window directly to determine when a reset occurs.
- If both are `0` (default), EzSeasons ignores them and uses `length-days` instead.

**Default:** `0` (ignored)

---

### `season.recurring`

Applies only when a valid `start`/`end` window is set. When `true`, the plugin automatically advances the window by its length after each reset, so the schedule repeats indefinitely.

**Default:** `false`

---

### `season.length-days`

When `start`/`end` are not set, this is the number of days between resets. The next reset is calculated as `last-reset + length-days * 86400000`.

**Default:** `30`

---

### `season.check-interval-minutes`

How often (in minutes) EzSeasons checks whether a reset is due or a reminder should be sent. Lower values increase precision but add minor server overhead.

**Default:** `60`

---

### `season.broadcast-message`

Message broadcast to all online players when a season reset occurs. Supports `&` color codes.

**Default:** `"&7A new season has begun! Hearts have been reset."`

---

### `season.reminder-minutes`

List of minute offsets before the next reset at which a reminder message is sent. For example, `[1440, 60, 10]` sends reminders 24 hours, 1 hour, and 10 minutes before the reset.

**Default:** `[]` (no reminders)

---

### `season.reminder-message`

Message sent to all online players at each reminder interval. Supports `&` color codes.

| Placeholder | Replaced with |
|---|---|
| `%time%` | Human-readable countdown (e.g. `1 day, 2 hours and 30 minutes`) |

**Default:** `"&7The season will reset in &c%time%&7."`

---

### `season.last-reset` and `season.next-reset`

Runtime state written by EzSeasons itself. **Do not edit these manually** unless you know what you are doing; use `/season admin setnext` or `/season admin clear-next` instead.

| Key | Meaning |
|---|---|
| `last-reset` | Unix epoch milliseconds of the most recent season reset |
| `next-reset` | Unix epoch milliseconds of the next scheduled reset; `0` means unscheduled |

---

## See also

- [Getting started](getting-started) for a minimal setup walkthrough
- [Commands](commands) for runtime overrides via `/season admin`
