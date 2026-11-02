# ScorchedGuns EMF Compat Fix

A small client-side fix for [Scorched Guns](https://www.curseforge.com/minecraft/mc-mods/scorched-guns) when used together with **Entity Model Features (EMF)**, e.g. with **Fresh Animations**.

## What it fixes

### 1. FPS drops near villagers and other animated mobs
Scorched Guns' EMF compat looks up an EMF class via reflection **every frame, for every body part of every EMF-animated entity**, without caching the result. On current EMF versions that lookup fails every time, and each failure throws an exception. The more animated mobs are on screen, the worse it gets: we measured drops from **60 to 10 FPS** in villages.

This mod caches those lookups, so the work happens once per game session instead of thousands of times per frame.

### 2. Weird first-person arms while holding a gun (EMF 3.x + Fresh Animations)
Scorched Guns 1.5 only recognises **EMF 2.x**. On **EMF 3.x** its entire EMF compat silently switches off, which leads to:

- Fresh Animations' player add-on animating your first-person arms on top of the gun, so they end up rotated and offset against the gun model
- the third-person aiming pose being overridden by Fresh Animations
- the `is_scguns_gun_held` animation variable never being registered for resource packs

This mod bridges Scorched Guns' EMF compat to EMF 3's API, which brings all of it back: clean gun arms in first person, a proper aiming pose in third person, and Fresh Animations still running for everything else.

## Requirements

- Minecraft **1.21.1**, **NeoForge**
- **Scorched Guns** 1.5
- **Entity Model Features** (optional: without EMF this mod simply does nothing)

**Client-side only.** It is not needed on the server.

## Compatibility

- Patches only Scorched Guns' own EMF compat class (`OptionalClientCompat`) and doesn't touch any other Scorched Guns logic.
- EMF 2.x setups keep Scorched Guns' original behaviour, with only the caching applied.
- If a future EMF version changes its API, the EMF 3 bridge switches itself off with a single log warning instead of crashing.
