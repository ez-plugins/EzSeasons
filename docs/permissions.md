---
title: Permissions
nav_order: 5
description: "Permission node reference for EzSeasons"
---

# Permissions
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Permission nodes

| Node | Default | Grants access to |
|---|---|---|
| `lifesteal.season` | **All players** (`true`) | `/season` — view the time until the next reset |
| `lifesteal.season.admin` | **Operators** (`op`) | `/season admin <reload\|reset\|setnext\|clear-next\|status>` |
| `lifesteal.admin` | **Operators** (`op`) | Legacy fallback — accepted for both `/season` and `/season admin` commands |

---

## Notes

- `lifesteal.admin` is a **legacy fallback**. It was carried over from EzLifesteal for backward compatibility. Prefer granting `lifesteal.season` and `lifesteal.season.admin` explicitly on standalone EzSeasons servers.
- Setting a permission node `default` to `op` in `plugin.yml` means only operators receive it by default. You can override this with any permissions plugin (e.g. LuckPerms).
- There is no separate per-subcommand permission. A player with `lifesteal.season.admin` (or `lifesteal.admin`) can run all admin subcommands.

---

## LuckPerms examples

Grant a `moderator` group access to admin commands:

```shell
/lp group moderator permission set lifesteal.season.admin true
```

Revoke player access to `/season` for a `restricted` group:

```shell
/lp group restricted permission set lifesteal.season false
```
