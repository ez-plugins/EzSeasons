## Make your server ready for wipe

Standalone season reset scheduler for Minecraft servers - EzSeasons

---

## Summary
EzSeasons schedules and broadcasts season resets for Bukkit and Paper servers. It is a focused reset Minecraft plugin designed for server owners who run periodic wipes, seasons, or server resets.

## Features
- Automatic season reset scheduling (explicit windows or duration-based)
- Explicit scheduling: `season.start` and `season.end`
- Fallback duration scheduling: `season.length-days`
- Optional recurring windows: `season.recurring`
- Reminder broadcasts: `season.reminder-minutes`, `season.reminder-message`
- Reset broadcast: `season.broadcast-message`
- Runtime admin commands and status reporting
- Developer API: `SeasonsApi`, `SeasonsIntegration`, `SeasonResetEvent`

## Quick Install (Bukkit / Paper)
1. Drop the plugin jar into your server's `plugins` folder.
2. Start or restart the server.
3. Edit `config.yml` to set schedule and messages.

## Example configuration (config.yml)
```yaml
season:
	enabled: true
	# Use explicit timestamps (millis) for a specific window, or leave 0 to use length-days
	start: 0
	end: 0
	recurring: false
	length-days: 30
	check-interval-minutes: 5
	broadcast-message: "Season resets in {time}!"
	reminder-minutes: [60, 30, 10, 1]
	reminder-message: "Season resets in {minutes} minute(s)!"
```

## Commands
- `/season` - show current status and next reset
- `/season admin reload` - reload plugin config
- `/season admin reset [reason]` - trigger an immediate reset (admin only)
- `/season admin setnext <unixMillis>` - set next reset time manually
- `/season admin clear-next` - clear next-reset override

## Permissions
- `lifesteal.season` (default: true) - allows `/season`
- `lifesteal.admin` (default: op) - allows all admin commands

## Developer API
Obtain `SeasonsApi` via Bukkit Services Manager. Available methods:
- `registerIntegration(SeasonsIntegration integration)`
- `unregisterIntegration(SeasonsIntegration integration)`
- `getIntegrations()`
- `triggerSeasonReset(String reason)`

Events published:
- `SeasonResetEvent`
- `SeasonsIntegrationRegisteredEvent`
- `SeasonsIntegrationUnregisteredEvent`

## Best Practices for Server Owners
- Always test reset behavior on a staging server before scheduling production wipes.
- Combine EzSeasons with your backup tooling - EzSeasons does not perform backups.
- Use `season.reminder-minutes` to announce the reset early and reduce player frustration.

## Support & License
- Source & docs: https://github.com/ez-plugins/EzSeasons
- Download (Modrinth): https://modrinth.com/plugin/ezseasons
- Report issues: https://github.com/ez-plugins/EzSeasons/issues
- License: MIT