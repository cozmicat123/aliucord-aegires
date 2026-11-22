# STATE.md: shared memory. Read first on "start".
Next: Milestone 3 complete on `main` (`v0.3.2` deployed to `builds`). Optional follow-ups: harden `tools/sync-aegires.sh` (all-file OkHttp/Okio rewrite + strict assertions on `MainActivity.java` patches), save `pendingRequestCode` across process death in `AegiresFragment`, or sync upstream `Curione23/Aegires` changes as needed.

## Setup
- Repo: `~/vc-work/aliucord-aegires` (active dev branch `dev`, release branch `main`, artifact branch `builds`) = `github.com/cozmicat123/aliucord-aegires`
- Handoff folder: `/sdcard/Download/aliucord-aegires/{AGENTS.md,STATE.md}`
- Device: OnePlus 12 (`waffle`), YAAP vanilla (no GApps), Termux only (no PC).
- Hard Constraint: **Strictly non-root only** (no `su`, Zygisk, or LSPosed).
- Local references: `~/Aegires` (`gen3`, `f44d575`, includes latest upstream `Curione23/Aegires` `dddead0`), `~/vc-work/aliucord-e2ee` (`aegires-sync`).

## Locked Architecture (Phase 0 Complete — Verdict: GO, Unrooted)
- **Method A + A1 + Sync Script:**
  - Bundle Aegires Java sources inside Aliucord plugin `.zip` (`classes.dex`), synced from `~/Aegires` via `tools/sync-aegires.sh` (79 Java files; drops `AegiresApplication.java`).
  - Launch full-screen inside Discord's registered `AppActivity` via `Utils.openPageWithProxy(ctx, AegiresFragment.newInstance(autoTokenHandoff))`.
  - `MainActivity` adapted into a `ContextThemeWrapper`-backed host delegating `getWindow`, `getCurrentFocus`, `runOnUiThread`, `finish`, `isFinishing`, `getIntent`, `ActivityResultLauncherDelegate`, clearing `handler` callbacks in `onDestroy()`, and isolating prefs (`aegires_*`) + `filesDir`/`cacheDir` (`aegires/`). `LinkDialog.java` unwraps `ContextWrapper`.
  - Shade pure-Java `okhttp:3.12.13` + `okio:1.17.5` to `aegires.okhttp3.*` / `aegires.okio.*` via `:shaded-okhttp` (`com.gradleup.shadow:9.6.1`).

## Verified Facts (Milestones 1–3)
- **FragmentProxy quirks verified on-device:**
  - `AegiresFragment.viewLifecycleOwner` throws `IllegalStateException` inside `FragmentProxy` -> register `OnBackPressedCallback` directly on `requireActivity().onBackPressedDispatcher` and remove in `onDestroyView()`.
  - `FragmentProxy` does not forward `onActivityResult` -> route `MainActivity.startActivityForResult` through `ComponentActivity.activityResultRegistry.register(...)` and unregister in `onDestroyView()`.
- **Milestone 2 On-Device Verified (`7a7fac4`, CI run `45014690105`, `Aegires v0.2.0`, `873 KB`):**
  - Full Aegires UI, insets, login, guilds/channels/chat, back-press navigation + clean exit to Aliucord, `LinkDialog`, and attachment picker all verified working unrooted inside Aliucord.
- **Milestone 3 On-Device & CI Verified (`v0.3.1` run `45030239752`, `v0.3.2` run `45032145272` & `main` deploy `45037732337`):**
  - Automatic `DISCORD_TOKEN` handoff, `WidgetSettings` toolbar launch button, `/aegires` command, settings toggles, isolated `aegires_auth` clear-token behavior, zero-arg `AegiresFragment` + window insets restoration, and `builds` auto-updater (`updater.json`) verified.
- Note: Aliucord gradle-plugin requires strict `x.x.x` semver.

## Milestones
- [x] Phase 0: Deep Research Gate & Unrooted Architecture Decision
- [x] Milestone 1: Gradle/CI scaffold + shaded OkHttp 3.12.13 + minimal full-screen Aegires launch PoC on-device
- [x] Milestone 2: Full Aegires sync, storage isolation, back-press/inset/LinkDialog/attachment-picker verification
- [x] Milestone 3: Automatic token handoff, clean Aliucord UI entrypoint, polish, `builds` updater, merge `dev` -> `main`
