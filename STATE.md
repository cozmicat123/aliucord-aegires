# STATE.md: shared memory. Read first on "start".
Next: Awaiting owner approval of Method A + sync/fork strategy + OkHttp fix (A1 shaded OkHttp 3.12.13 vs A2 HttpURLConnection+WS), then switch chat to start Milestone 1 (minimal CI build + on-device PoC).

## Setup
- Repo: `~/vc-work/aliucord-aegires` (branch `dev`) = `github.com/cozmicat123/aliucord-aegires`
- Handoff folder: `/sdcard/Download/aliucord-aegires/{AGENTS.md,STATE.md}`
- Device: OnePlus 12 (`waffle`), YAAP vanilla (no GApps), Termux only (no PC).
- Hard Constraint: **Strictly non-root only** (no `su`, Zygisk, or LSPosed).
- Local references: `~/Aegires` (`gen3`), `~/vc-work/aliucord-e2ee`.

## Verified Facts (Phase 0 Complete — Verdict: GO, Unrooted)
- **Aegires (`~/Aegires`, `gen3`):**
  - 80 pure Java 8 files (`package a.b`), `compileSdk 33`, `minSdk 24`, **0 `res/`**, **0 `assets/`**, **0 `.so`**, **0 Services/Providers/Receivers**, `INTERNET` only.
  - Deps (`app/build.gradle:59-63`): `okhttp:4.12.0`, `conscrypt-android:2.6.0` (optional `try/catch` in `AegiresApplication.java:14`; unneeded on Android 15/16), `androidx.recyclerview:1.3.2`.
  - UI: programmatic `View`s (`Screen` stack) on `FrameLayout rootFrame` (`MainActivity.java:37-119, 327-460`); `AttachmentPicker.java:33` uses `startActivityForResult(PICK_IMAGE_REQUEST=1001)`; `LinkDialog.java:18` checks `ctx instanceof Activity`; WebViews in `WebLoginScreen`, `EncryptedMediaSlot`, `MessageMediaViewFactory`.
  - Storage names to isolate from Discord: SharedPreferences `"auth"`, `"e2ee"`, `"mentions"`, `"chat_state"`, `"channel_state"`; paths `cacheDir/media`, `filesDir/cache`, `filesDir/last_channel_cache.json`.
- **Aliucord & Discord 126.21 (`discord-126021.apk`, 31,813 DEX classes):**
  - `PluginManager.kt:60` loads plugin `.zip` (`classes.dex`) via parent-first `PathClassLoader`. `CompileDexTask.kt` bundles `implementation` JARs via D8 (blacklists only `kotlin-stdlib`, `material`, `constraintlayout`, `appcompat`).
  - `a.b.*` = 0 classes; `org.conscrypt.*` = 0 classes; all needed `androidx.recyclerview` (`ConcatAdapter`, `DiffUtil`, `LinearLayoutManager`, `RecyclerView`) and `androidx.core` (`WindowCompat`, `WindowInsetsCompat$Type`, `Insets`, `PathParser`, `ViewCompat`) classes = **PRESENT**.
  - **Collision:** Discord ships 28 obfuscated `okhttp3.*` classes + 3 `okio.*` classes (`OkHttpClient`/`Okio` missing) and `kotlin-stdlib:1.5.21` (`Aliucord/gradle/libs.versions.toml:10`). Unshaded OkHttp 4.12.0 will fail at runtime.
  - **Screen hosting:** `Utils.openPageWithProxy(ctx, fragment)` (`Utils.kt:307-312`) launches Discord's registered `AppActivity` + `AppFragmentProxy` (`FragmentProxy.java:147-164, 245-338`), forwarding lifecycle + `onActivityResult`.

## Phase 0 Checklist
- [x] Aegires full stack verified
- [x] Aliucord & Discord 126.21 host internals + DEX class collisions verified
- [x] Unrooted Android 15/16 constraints & prior art evaluated
- [ ] Owner approval: Method A + (sync script vs fork) + (A1 shaded OkHttp 3.12 vs A2 HttpURLConnection+WS)
