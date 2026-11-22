# STATE.md: shared memory. Read first on "start".
Next: Milestone 2 — write `tools/sync-aegires.sh` + minimal patchset (drop Conscrypt, shade OkHttp/Okio imports + `ApiClient.java:865`, adapt `MainActivity` into a `ContextThemeWrapper`-backed host, isolate prefs/files under `aegires_*`, unwrap `ContextWrapper` in `LinkDialog.java:18`), wire `AegiresFragment`, build via CI, and verify full Aegires UI/login/back-press/insets/WebView/attachment-picker on-device.

## Setup
- Repo: `~/vc-work/aliucord-aegires` (branch `dev`) = `github.com/cozmicat123/aliucord-aegires`
- Handoff folder: `/sdcard/Download/aliucord-aegires/{AGENTS.md,STATE.md}`
- Device: OnePlus 12 (`waffle`), YAAP vanilla (no GApps), Termux only (no PC).
- Hard Constraint: **Strictly non-root only** (no `su`, Zygisk, or LSPosed).
- Local references: `~/Aegires` (`gen3`, `f44d575`, includes latest upstream `Curione23/Aegires` `dddead0`), `~/vc-work/aliucord-e2ee` (`aegires-sync`).

## Locked Architecture (Phase 0 Complete — Verdict: GO, Unrooted)
- **Method A + A1 + Sync Script:**
  - Bundle Aegires Java sources inside Aliucord plugin `.zip` (`classes.dex`), synced from `~/Aegires` via reproducible `tools/sync-aegires.sh` + minimal patchset (~75/80 files untouched).
  - Launch full-screen inside Discord's registered `AppActivity` via `Utils.openPageWithProxy(ctx, AegiresFragment)` (`Utils.kt:307-312`, `FragmentProxy.java:147-164, 245-338`).
  - Adapt `MainActivity` into a `ContextThemeWrapper`-backed host delegating `getWindow`, `runOnUiThread`, `startActivityForResult`, `finish`, back-press handling, and namespacing prefs/files under `aegires_*`. Unwrap `ContextWrapper` in `LinkDialog.java:18`. Drop `Conscrypt` (platform TLS 1.3 verified on-device).
  - Shade pure-Java `okhttp:3.12.13` + `okio:1.17.5` to `aegires.okhttp3.*` / `aegires.okio.*` via `:shaded-okhttp` (`com.gradleup.shadow:9.6.1`).

## Verified Facts (Phase 0 & Milestone 1)
- **Aegires (`~/Aegires`, `gen3` @ `f44d575`):** 80 pure Java 8 files (`package a.b`), 0 `res/`, 0 `assets/`, 0 `.so`, 0 Services/Providers/Receivers, `INTERNET` only. Storage to isolate: prefs `"auth"`, `"e2ee"`, `"mentions"`, `"chat_state"`, `"channel_state"`; paths `cacheDir/media`, `filesDir/cache`, `filesDir/last_channel_cache.json`.
- **OkHttp/Okio/Conscrypt refs in Aegires:** only `AegiresApplication.java` (Conscrypt), `ApiClient.java`, `GatewayClient.java`, and `AndroidMedia.java`.
- **Milestone 1 On-Device Verified (`2875656`, CI run `45010880840`):**
  - `Aegires.zip` (`471 KB`, `0` unshaded `okhttp3` classes, `6352` shaded `Laegires/okhttp3/` refs).
  - Loads in Aliucord (`Aegires v0.1.0`), launches full-screen inside `com.discord.app.AppActivity` with `RecyclerView` + window insets, and completes `OK HTTP 200 (h2, TLSv1.3): {"url":"wss://gateway.discord.gg"}` via `aegires.okhttp3.OkHttpClient`.
  - Note: Aliucord gradle-plugin requires strict `x.x.x` semver (no `-poc` suffix).

## Milestones
- [x] Phase 0: Deep Research Gate & Unrooted Architecture Decision
- [x] Milestone 1: Gradle/CI scaffold + shaded OkHttp 3.12.13 + minimal full-screen Aegires launch PoC on-device
- [ ] Milestone 2: Full Aegires sync, storage isolation, back-press/inset/WebView/attachment-picker verification
- [ ] Milestone 3: Polish, clean entrypoint inside Aliucord UI, optional token handoff, merge `dev` -> `main`
