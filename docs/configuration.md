# Configuration

Main file: `plugins/EzSeasons/config.yml`

## `messages`

Controls language selection and prefix.

- `language` (`string`): active language code. Supported values: `en`, `es`, `fr`, `zh`, `ru`, `nl`.
- `prefix` (`string`): prepended to command/admin plain-text responses.
- `keys` (`section`): command/admin UX message overrides loaded after language files.
  - Includes keys such as `admin-reload-success`, `admin-reset-confirm-required`, `admin-reset-success`,
    `admin-setnext-invalid-timestamp`, `admin-setnext-out-of-range`, and `admin-clear-next-confirm-required`.


### `messages.keys` example

```yaml
messages:
  keys:
    admin-reset-confirm-required: "&eReset is destructive. Re-run with confirmation: &f%command%"
    admin-reset-success: "&aSeason reset triggered successfully. Reason: &f%reason%"
    admin-setnext-invalid-timestamp: "&cInvalid timestamp '%value%'. Expected unix epoch milliseconds (example: 1735689600000)."
    admin-setnext-out-of-range: "&cTimestamp %value% is out of range. Allowed range: %min%..%max%."
```

## Message files

Folder: `plugins/EzSeasons/messages/`

Language files (`en.yml`, `es.yml`, `fr.yml`, `zh.yml`, `ru.yml`, `nl.yml`) contain keys such as:

- `no-permission`
- `season-disabled`
- `season-status` (supports `%time%`)
- `season-status-unknown`
- `duration.*` formatter keys

Load order:
1. `messages/en.yml`
2. selected language file (`messages.language`), overlaying any matching keys

## `season`

Scheduling and reset behavior.

- `enabled` (`boolean`): enables/disables scheduling features and command status output.
- `start` (`long`, epoch millis): explicit window start timestamp.
- `end` (`long`, epoch millis): explicit window end timestamp.
- `recurring` (`boolean`): if `true`, valid explicit windows repeat.
- `length-days` (`long`): fallback duration scheduling length.
- `check-interval-minutes` (`long`): scheduler check cadence.
- `broadcast-message` (`string`): broadcast sent on reset.
- `reminder-minutes` (`list<int>`): minute offsets before reset.
- `reminder-message` (`string`): reminder template (supports `%time%`).
- `last-reset` (`long`, epoch millis): last reset timestamp (stored/runtime-managed).
- `next-reset` (`long`, epoch millis): next reset timestamp (stored/runtime-managed).

## Scheduling behavior notes

- When `start` and `end` are both valid (`end > start`), EzSeasons uses explicit-window scheduling.
- If explicit values are invalid, EzSeasons falls back to `length-days` scheduling.
- `recurring` is only meaningful for valid explicit windows.
- If `next-reset` is not set, EzSeasons may calculate from current scheduling mode.

## Minimal example

```yaml
messages:
  language: "en"
  prefix: "&c[EzSeasons]&r "

season:
  enabled: true
  length-days: 30
  check-interval-minutes: 60
  reminder-minutes: [60, 10, 1]
```
