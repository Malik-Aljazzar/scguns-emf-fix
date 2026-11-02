# Changelog

All notable changes to ScorchedGuns EMF Compat Perf Fix are documented in this file.

## [1.1.0]

### Fixes
- Fixed first-person arms looking wrong (rotated/offset against the gun model) when holding a ScorchedGuns gun with Entity Model Features 3.x and Fresh Animations' player add-on (FA+Player) active. ScorchedGuns 1.5 decides whether EMF is installed by looking for EMF 2.x's `EMFAnimationEntityContext` class, which EMF 3.x removed - so on EMF 3.x its entire EMF compat silently switched off: the clean, unanimated first-person gun arms, the third-person gun aiming pose being re-applied on top of Fresh Animations, and the `is_scguns_gun_held` EMF animation variable. On EMF 3.x this mod now answers ScorchedGuns' three EMF context lookups (first-person-hand flag, current entity, "is the animated entity holding a gun") from EMF 3's own render-state API, which switches all of that compat back on. With EMF 2.x, or without EMF, nothing changes. If a future EMF version changes this API again, the bridge switches itself off with a single log warning instead of crashing.
- Fresh Animations' first-person arm animations stay on for everything except held guns - no need for EMF's global `preventFirstPersonHandAnimating` workaround.

## [1.0.0]

### Fixes
- Fixed severe FPS drops (observed 60 → 10 FPS, scaling with on-screen entity count) caused by ScorchedGuns' Entity Model Features (EMF) compatibility layer. `top.ribs.scguns.client.compat.OptionalClientCompat.isEntityModelFeaturesFirstPersonHand()` (plus two related methods, `getEntityModelFeaturesContextEntity()` and `isScorchedGunHeldByEntityModelFeaturesContext()`) each ran a fresh, uncached `Class.forName(...)` plus a reflective `Field`/`Method` lookup on *every single call* instead of caching the result once - unlike the same class's own `isEntityModelFeaturesLoaded()`, which already caches a similar check correctly. The first of these three hooks into EMF's generic per-model-part animate callback, which fires for every body part of every EMF-animated entity, every frame - not just the player's own first-person hand - so the cost multiplies directly with on-screen entity count (confirmed via spark profiler correlating the drops with villager-crowd density). Fixed by caching the resolved `Class`/`Field`/`Method` objects (and, symmetrically, the lookup failure if EMF's class/member genuinely isn't present) after the first call, so the expensive reflection work happens at most once per game session instead of once per entity part per frame.
