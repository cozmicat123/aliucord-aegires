# STATE.md: shared memory. Read first on "start".
Next: Milestone 1 — scaffold Aliucord plugin Gradle project + GitHub Actions CI + shaded pure-Java OkHttp 3.12.13 / Okio 1.17.5 + minimal Aegires launch PoC via `Utils.openPageWithProxy`, then test `.zip` on-device.

## Setup
- Repo: `~/vc-work/aliucord-aegires` (branch `dev`) = `github.com/cozmicat123/aliucord-aegires`
- Handoff folder: `/sdcard/Download/aliucord-aegires/{AGENTS.md,STATE.md}`
- Device: OnePlus 12 (`waffle`), YAAP vanilla (no GApps), Termux only (no PC).
- Hard Constraint: **Strictly non-root only** (no `su`, Zygisk, or LSPosed).
- Local references: `~/Aegires` (`gen3`), `~/vc-work/aliucord-e2ee`.

## Locked Architecture (Phase 0 Complete — Verdict: GO, Unrooted)
- **Method A + A1 + Sync Script:**
  - Bundle Aegires Java sources inside Aliucord plugin `.zip` (`classes.dex`), synced from `~/Aegires` via reproducible `tools/sync-aegires.sh` + minimal patchset (~75/80 files untouched).
  - Launch full-screen inside Discord's registered `AppActivity` via `Utils.openPageWithProxy(ctx, AegiresFragment)` (`Utils.kt:307-312`, `FragmentProxy.java:147-164, 245-338`).
  - Adapt `MainActivity` into a `ContextThemeWrapper`-backed host delegating `getWindow`, `runOnUiThread`, `startActivityForResult`, `finish`, back-press handling, and namespacing prefs/files under `aegires_*`. Unwrap `ContextWrapper` in `LinkDialog.java:18`. Drop `Conscrypt` (platform TLS 1.3 handles it on Android 15/16).
  - Shade pure-Java `okhttp:3.12.13` + `okio:1.17.5` to `aegires.okhttp3.*` / `aegires.okio.*` (only import renames in `ApiClient`, `GatewayClient`, `AndroidMedia` + 1-line arg swap at `ApiClient.java:865`).

## Verified Facts (Phase 0)
- **Aegires (`~/Aegires`, `gen3`):** 80 pure Java 8 files (`package a.b`), `compileSdk 33`, `minSdk 24`, 0 `res/`, 0 `assets/`, 0 `.so`, 0 Services/Providers/Receivers, `INTERNET` only. Storage to isolate: prefs `"auth"`, `"e2ee"`, `"mentions"`, `"chat_state"`, `"channel_state"`; paths `cacheDir/media`, `filesDir/cache`, `filesDir/last_channel_cache.json`.
- **Aliucord & Discord 126.21 (`discord-126021.apk`, 31,813 classes):**
  - `PluginManager.kt:60` uses parent-first `PathClassLoader`. `CompileDexTask.kt` dexes `implementation` JARs (blacklists only `kotlin-stdlib`, `material`, `constraintlayout`, `appcompat`).
  - `a.b.*` = 0 classes; `org.conscrypt.*` = 0 classes; all needed `androidx.recyclerview` (`ConcatAdapter`, `DiffUtil`, `LinearLayoutManager`, `RecyclerView`) and `androidx.core` (`WindowCompat`, `WindowInsetsCompat$Type`, `Insets`, `PathParser`, `ViewCompat`) classes = PRESENT.
  - Discord bundles 28 obfuscated `okhttp3.*` + 3 `okio.*` classes and `kotlin-stdlib:1.5.21` -> solved cleanly by A1 shading.

## Milestones
- [x] Phase 0: Deep Research Gate & Unrooted Architecture Decision
- [ ] Milestone 1: Gradle/CI scaffold + shaded OkHttp 3.12.13 + minimal full-screen Aegires launch PoC on-device
- [ ] Milestone 2: Full Aegires sync, storage isolation, back-press/inset/WebView/attachment-picker verification
- [ ] Milestone 3: Polish, clean entrypoint inside Aliucord UI, optional token handoff, merge `dev` -> `main`
