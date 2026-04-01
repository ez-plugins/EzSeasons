# Getting Started

Quick setup guide for server owners.

## 1) Install

1. Build or download the EzSeasons jar.
2. Place it in your server's `plugins/` folder.
3. Restart the server.

## 2) Verify load

On startup, confirm EzSeasons is enabled in console logs.

## 3) Configure

Edit `plugins/EzSeasons/config.yml`:

- Set `season.enabled: true`
- Adjust scheduling values (`length-days`, `start`/`end`, reminders)
- Customize `messages` for your network style

## 4) Test in game

Run:

```text
/season
```

You should see either a countdown or a clear status message describing why a countdown is unavailable.
