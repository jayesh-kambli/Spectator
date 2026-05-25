# Why This Fork?

This is a fork of [CuzIm1Tigaaa/Spectator](https://github.com/CuzIm1Tigaaa/Spectator) with two goals: **Folia compatibility** and **bug fixes** that the upstream project has not addressed.

---

## Folia Compatibility

The original plugin uses `Bukkit.getScheduler()` and `BukkitRunnable` throughout, both of which throw `UnsupportedOperationException` on [Folia](https://papermc.io/software/folia) servers. This fork replaces every scheduler call with Paper's regionalized entity and global schedulers (`entity.getScheduler()`, `server.getGlobalRegionScheduler()`), which work transparently on both standard Paper and Folia.

`folia-supported: true` is declared in `plugin.yml`.

Changes made for Folia:
- All `Bukkit.getScheduler().runTask/runTaskLater/runTaskTimer()` calls replaced with entity or global region scheduler equivalents
- `BukkitRunnable` in cycle display logic replaced with a plain `Runnable`
- Direct `player.teleport()` calls in event handlers wrapped in entity scheduler to ensure they run on the correct region thread
- `UpdateChecker` HTTP request moved to `server.getAsyncScheduler()` — it was blocking the main thread on startup
- Cycle task cancellation migrated from `int taskId` sentinel to `ScheduledTask` reference
- Removed explicit `spigot-api` dependency that was shadowing `paper-api`'s `Entity` class and hiding the Folia scheduler methods at compile time

---

## Bug Fixes

- **NPE in `SpectateAPI.showArmorstands()`** — `hiddenArmorStands.get(uuid)` could return `null` for players who had no armor stands hidden, causing a crash
- **Empty `restoreArmorstands()` stub** — the method existed but had no implementation; it now restores armor stand visibility for all active spectators
- **`SpectateUtilsCycle.pauseCycle()` dead variable** — result of `stopTask()` was assigned to a variable that was never used
- **`SpectateList` cycling/paused detection** — both `cycling` and `paused` were set using `isPausedSpectator()`, so the cycling state was never actually shown

---

## What Is Not Changed

- No features added or removed
- Config format and language files are identical to upstream
- The public API (`SpectateAPI`) is unchanged
