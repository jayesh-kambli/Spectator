# Spectator — Setup & Usage Guide

## Requirements

| Requirement | Details |
|---|---|
| **Server software** | Paper 1.20.4+ or Folia (any version) |
| **Java** | 21+ |
| **Optional** | Multiverse-Core (multi-world support), PlaceholderAPI |

> Spigot is **not** supported — the plugin uses Paper-only APIs.

---

## Installation

1. Drop `SpectatorPlugin-<version>.jar` into your server's `plugins/` folder.
2. Restart the server (not `/reload`).
3. The plugin generates `plugins/Spectator/config.yml` and language files on first start.

---

## Commands

### `/spectate` (alias: `/spec`)
Toggle spectator mode for yourself, or target another player.

| Usage | Permission required | Description |
|---|---|---|
| `/spectate` | `spectator.commands.spectate` | Toggle spectator mode on yourself |
| `/spectate <player>` | `spectator.commands.spectateothers` | Spectate a specific player |
| `/spectate <spectator> <target>` | `spectator.commands.spectatechangeothers` | Put another player into spectate mode targeting someone |
| `/spectate -armorstand` | `spectator.utils.hidearmorstand` | Toggle invisible armor-stand hiding while spectating |

---

### `/spectatehere` (alias: `/spechere`, `/spech`)
Enter free-roam spectator mode at your current location (no target locked).

| Usage | Permission required |
|---|---|
| `/spectatehere` | `spectator.commands.spectatehere` |

---

### `/spectatecycle` (alias: `/speccycle`)
Automatically cycle through online players on a timer.

| Usage | Permission required | Description |
|---|---|---|
| `/spectatecycle start <seconds> [alphabetical\|random]` | `spectator.commands.spectatecycle.default` | Start cycling every `<seconds>` seconds |
| `/spectatecycle stop` | `spectator.commands.spectatecycle.default` | Stop your own cycle |
| `/spectatecycle stop <player>` | `spectator.commands.spectatecycle.stopOthers` | Stop another player's cycle |

---

### `/spectatelist` (alias: `/speclist`)
Show all players currently in spectator mode and who they are watching.

| Usage | Permission required |
|---|---|
| `/spectatelist` | `spectator.commands.spectatelist` |

---

### `/unspectate` (alias: `/unspec`)
Force one or all spectators out of spectator mode.

| Usage | Permission required | Description |
|---|---|---|
| `/unspectate` | `spectator.commands.unspectate` | Unspectate all players (respects `bypass.unspectated`) |
| `/unspectate *` | `spectator.commands.unspectate` | Same as above, explicit wildcard |
| `/unspectate * false` | `spectator.commands.unspectate` | Unspectate all, teleport to current location instead of saved location |
| `/unspectate <player>` | `spectator.commands.unspectate` | Unspectate a specific player |
| `/unspectate <player> false` | `spectator.commands.unspectate` | Unspectate player, keep them at their current location |

---

### `/spectatereload` (alias: `/specreload`, `/specrl`)
Reload `config.yml` and language files without restarting.

| Usage | Permission required |
|---|---|
| `/spectatereload` | `spectator.commands.spectatereload` |

---

## Permissions Reference

### Command permissions

| Permission | Default | Description |
|---|---|---|
| `spectator.commands.spectate` | `false` | Use `/spectate` on yourself |
| `spectator.commands.spectateothers` | `false` | Use `/spectate <player>` to watch someone |
| `spectator.commands.spectatechangeothers` | `false` | Put another player into spectate targeting someone |
| `spectator.commands.spectatehere` | `false` | Use `/spectatehere` |
| `spectator.commands.spectatelist` | `false` | Use `/spectatelist` |
| `spectator.commands.spectatecycle.default` | `false` | Use `/spectatecycle start` and `/spectatecycle stop` (own) |
| `spectator.commands.spectatecycle.stopOthers` | `false` | Use `/spectatecycle stop <player>` |
| `spectator.commands.spectatecycle.*` | `false` | All cycle commands |
| `spectator.commands.unspectate` | `false` | Use `/unspectate` |
| `spectator.commands.spectatereload` | `false` | Use `/spectatereload` |
| `spectator.commands.admin` | `op` | All admin commands (`unspectate` + `spectatereload`) |
| `spectator.commands.*` | `op` | All commands (cycle excluded from the wildcard — must be granted separately) |

### Bypass permissions

| Permission | Default | Description |
|---|---|---|
| `spectator.bypass.tablist` | `false` | See hidden spectators in the tab list |
| `spectator.bypass.spectated` | `false` | Cannot be spectated by other players |
| `spectator.bypass.unspectated` | `false` | Cannot be force-unspectated by `/unspectate` |
| `spectator.bypass.spectateall` | `false` | Override `bypass.spectated` — can spectate anyone regardless |
| `spectator.bypass.notify` | `false` | Do not send the "you are being spectated" notification to targets |
| `spectator.bypass.*` | `op` | All bypass permissions |

### Utility permissions

| Permission | Default | Description |
|---|---|---|
| `spectator.utils.hidetab` | `false` | Hidden from other players' tab lists while spectating |
| `spectator.bypass.tablist` | `false` | Can still see hidden spectators in the tab list |
| `spectator.utils.hidearmorstand` | `false` | Use `/spectate -armorstand` to hide invisible armor stands |
| `spectator.utils.mirrorinventory` | `false` | See target's inventory mirrored in your own slots |
| `spectator.utils.mirroreffects` | `false` | Temporarily gain target's active potion effects |
| `spectator.utils.opencontainers` | `false` | See inside containers the target opens (chests, barrels, etc.) |
| `spectator.utils.openenderchest` | `false` | See inside the target's ender chest when they open it |
| `spectator.utils.*` | `op` | All utility permissions |

### Other

| Permission | Default | Description |
|---|---|---|
| `spectator.notify.update` | `op` | Receive an in-game message on join when a new plugin version is available |
| `spectator.*` | `false` | All permissions except cycle-only (grants `commands.*`, `bypass.*`, `utils.*`) |

---

## Typical Permission Setup (LuckPerms examples)

**Regular player who can spectate freely:**
```
/lp user <name> permission set spectator.commands.spectate true
/lp user <name> permission set spectator.commands.spectateothers true
/lp user <name> permission set spectator.commands.spectatehere true
```

**Staff member with full access:**
```
/lp user <name> permission set spectator.* true
/lp user <name> permission set spectator.commands.spectatecycle.* true
```

**Granting a group:**
```
/lp group moderator permission set spectator.commands.admin true
/lp group moderator permission set spectator.commands.spectatelist true
```

---

## config.yml Overview

| Setting | Default | Description |
|---|---|---|
| `Settings.Debugging` | `true` | Print debug messages to the console |
| `Settings.NotifyOnUpdate` | `true` | Alert ops on join when a new version is available |
| `Settings.Language` | `en_US` | Language file to use (`de_DE`, `en_US`, `es_ES` included) |
| `Settings.HidePlayersInTab` | `true` | Hide spectators from the tab list (requires `spectator.utils.hidetab`) |
| `Settings.NotifyCurrentTarget` | `NONE` | How to notify a player they are being watched: `CHAT`, `ACTIONBAR`, `TITLE`, `SUBTITLE`, `NONE` |
| `Settings.HideArmorStands` | `true` | Allow hiding invisible armor stands while spectating |
| `Settings.Save.PlayerLocation` | `true` | Return player to their pre-spectate location on exit |
| `Settings.Save.PlayerFlightMode` | `true` | Restore flight state on exit (requires `allow-flight=true` in server.properties) |
| `Settings.Save.PlayerData` | `true` | Restore air supply and burn time on exit |
| `Settings.Mirror.TargetInventory` | `true` | Mirror target's inventory (requires `spectator.utils.mirrorinventory`) |
| `Settings.Mirror.TargetEffects` | `true` | Mirror target's potion effects (requires `spectator.utils.mirroreffects`) |
| `Settings.Inventory.OpenContainers` | `true` | Show containers target opens (requires `spectator.utils.opencontainers`) |
| `Settings.Inventory.OpenEnderChest` | `false` | Show target's ender chest (requires `spectator.utils.openenderchest`) |
| `Settings.Cycle.KickCyclingPlayers` | `false` | Allow cycling players to be kicked |
| `Settings.Cycle.StartWhenNoPlayers` | `true` | Allow `/spectatecycle start` when no other players are online |
| `Settings.Cycle.PauseWhenNoPlayers` | `false` | Pause cycle (instead of stop) when all players leave; auto-resumes on join |
| `Settings.Cycle.ShowCurrentTarget` | `BOSSBAR` | How to display current target: `BOSSBAR`, `ACTIONBAR`, `TITLE`, `SUBTITLE`, `NONE` |
| `Settings.Cycle.MinimumIntervalTime` | `0` | Minimum allowed cycle interval in seconds (`0` = no limit) |
| `Settings.Cycle.MaximumIntervalTime` | `0` | Maximum allowed cycle interval in seconds (`0` = no limit) |
| `Settings.Cycle.BossBarColor` | `BLUE` | Bossbar color: `BLUE`, `GREEN`, `PINK`, `PURPLE`, `RED`, `WHITE`, `YELLOW` |
| `Settings.Cycle.BossBarFactor` | `1` | How fast the bossbar drains (higher = faster) |

---

## Optional Integrations

### Multiverse-Core
If installed, players are restricted from spectating targets in worlds they do not have `multiverse.access.<world>` permission for. Without Multiverse-Core all worlds are accessible.

### PlaceholderAPI
The plugin registers placeholders usable in other plugins. The identifier is the plugin name (`Spectator`):

| Placeholder | Returns |
|---|---|
| `%Spectator_target%` | Name of the player this player is spectating, or empty |
| `%Spectator_target_displayname%` | Display name of the spectate target, or empty |
| `%Spectator_target_spectators%` | Comma-separated list of players currently spectating **you** |
| `%Spectator_spectators%` | Comma-separated list of **all** players in spectator mode |
| `%Spectator_state%` | Current state: `NONE`, `SPECTATING`, `CYCLING`, or `PAUSED` |
| `%Spectator_cycle_interval%` | Cycle interval in seconds (only populated while cycling) |

---

## Folia Support

This fork is fully compatible with [Folia](https://papermc.io/software/folia). All scheduling is done through Paper's regionalized entity/global schedulers. No extra configuration is needed — the plugin detects the correct scheduler at runtime.
