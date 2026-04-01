# EzSeasons Documentation Notes (Markdown)

This file mirrors the currently implemented EzSeasons behavior.

## Implemented commands

- `/season`
- `/season admin <reload|reset [reason]|setnext <unixMillis>|clear-next|status>`

## Implemented permissions

- `lifesteal.season` (default: `true`)
- `lifesteal.admin` (default: `op`)

## Implemented configuration keys

```yaml
messages.language
messages.prefix
season.enabled
season.start
season.end
season.recurring
season.length-days
season.check-interval-minutes
season.broadcast-message
season.reminder-minutes
season.reminder-message
season.last-reset
season.next-reset
```

## API reality check

Use only the current public interfaces/classes:

- `com.skyblockexp.lifesteal.seasons.api.SeasonsApi`
  - `registerIntegration(SeasonsIntegration)`
  - `unregisterIntegration(SeasonsIntegration)`
  - `getIntegrations()`
  - `triggerSeasonReset(String reason)`
- `com.skyblockexp.lifesteal.seasons.api.SeasonsIntegration`
- Events:
  - `SeasonResetEvent`
  - `SeasonsIntegrationRegisteredEvent`
  - `SeasonsIntegrationUnregisteredEvent`

Do **not** document or integrate against non-existent season model classes/methods such as `getCurrentSeason(...)`, `SeasonChangeEvent`, or `setSeason(...)`.
