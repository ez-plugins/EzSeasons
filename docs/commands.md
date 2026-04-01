# Commands

EzSeasons exposes one root command with optional admin subcommands.

## `/season`

Displays the remaining time until the next configured season reset.

- **Usage:** `/season` (alias: `/season status`)
- **Permissions accepted:** `lifesteal.season`, `lifesteal.season.admin`, or `lifesteal.admin`
- **Who can run it:** players and console senders with one of the permissions above
- **Tab completion root suggestions:** `admin`, `status`

## `/season admin ...`

Administrative runtime operations.

- **Usage:** `/season admin <reload|reset|setnext|clear-next|status>`
- **Permission required:** `lifesteal.season.admin` or `lifesteal.admin`

### Admin subcommands

- `/season admin reload`
  - Reloads `config.yml` and message translations, then rebuilds the active season manager.

- `/season admin reset [reason] --confirm`
  - Triggers an immediate season reset.
  - If `reason` is omitted, `admin` is used.
  - Requires `--confirm` to prevent accidental destructive actions.

- `/season admin setnext <unixMillis>`
  - Sets `season.next-reset` to the provided Unix epoch milliseconds and saves it.
  - Returns clear errors for non-numeric timestamps and out-of-range values.

- `/season admin clear-next --confirm`
  - Sets `season.next-reset` to `0` and saves it.
  - Requires `--confirm` to prevent accidental destructive actions.

- `/season admin status`
  - Shows the normal season countdown/status message, then prints raw `last-reset` and `next-reset` values.

### Tab-completion hints

- `/season admin` ⇒ `reload`, `reset`, `setnext`, `clear-next`, `status`
- `/season admin setnext` ⇒ `<unixMillis>`, `now+3600000` (hint examples)
- `/season admin reset` ⇒ `<reason...>`, `maintenance`, `manual`, `--confirm`
- `/season admin clear-next` ⇒ `--confirm`

## Notes

- Most admin subcommands require season scheduling to be enabled (`season.enabled: true`).
- Admin commands return explicit success/failure message keys that can be customized in `config.yml` under `messages.keys`.
