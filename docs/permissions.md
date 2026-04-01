# Permissions

EzSeasons defines three permission nodes.

## `lifesteal.season`

- **Default:** `true`
- **Purpose:** allows `/season` status usage (`/season` and `/season status`).

## `lifesteal.season.admin`

- **Default:** `op`
- **Purpose:** primary admin permission for `/season admin <reload|reset [reason]|setnext <unixMillis>|clear-next|status>`.
- **Also accepted for:** `/season` status command.

## `lifesteal.admin`

- **Default:** `op`
- **Purpose:** legacy admin fallback for `/season admin <reload|reset [reason]|setnext <unixMillis>|clear-next|status>`.
- **Also accepted for:** `/season` status command.

## Recommended setup

- Keep `lifesteal.season` at default `true` for all players.
- Grant `lifesteal.season.admin` to trusted staff.
- Keep `lifesteal.admin` only for backward compatibility with existing permission setups.
