# STATE.md: shared memory. Read first on "start".
Next: Milestone 3 — add automatic Discord token handoff from Aliucord to Aegires (`DISCORD_TOKEN` intent extra, already supported by `MainActivity.java:110`), add a clean entrypoint inside Aliucord UI, polish settings/UX, verify on-device, and merge `dev` -> `main`.

## Setup
- Repo: `~/vc-work/aliucord-aegires` (branch `dev`) = `github.com/cozmicat123/aliucord-aegires`
- Handoff folder: `/sdcard/Download/aliucord-aegires/{AGENTS.md,STATE.md}`
- Device: OnePlus 12 (`waffle`), YAAP vanilla (no GApps), Termux only (no PC).
- Hard Constraint: **Strictly non-root only** (no `su`, Zygisk, or LSPosed).
- Local references: `~/Aegires` (`gen3`, `f44d575`, includes latest upstream `Curione23/Aegires` `dddead0`), `~/vc-work/aliucord-e2ee` (`aegires-sync`).

## Locked Architecture (Phase 0 Complete — Verdict: GO, Unrooted)
- **Method A + A1 + Sync Script:**
  - Bundle Aegires Java sources inside Aliucord plugin `.zip` (`classes.dex`), synced from `~/Aegires` via `tools/sync-aegires.sh` (79 Java files; drops `AegiresApplication.java`).
  - Launch full-screen inside Discord's registered `AppActivity` via `Utils.openPageWithProxy(ctx, AegiresFragment)`.
  - `MainActivity` adapted into a `ContextThemeWrapper`-backed host delegating `getWindow`, `getCurrentFocus`, `runOnUiThread`, `finish`, `isFinishing`, `getIntent`, `ActivityResultLauncherDelegate`, and isolating prefs (`aegires_*`) + `filesDir`/`cacheDir` (`aegires/`). `LinkDialog.java` unwraps `ContextWrapper`.
  - Shade pure-Java `okhttp:3.12.13` + `okio:1.17.5` to `aegires.okhttp3.*` / `aegires.okio.*` via `:shaded-okhttp` (`com.gradleup.shadow:9.6.1`).

## Verified Facts (Milestones 1 & 2)
- **FragmentProxy quirks verified on-device:**
  - `AegiresFragment.viewLifecycleOwner` throws `IllegalStateException` inside `FragmentProxy` -> register `OnBackPressedCallback` directly on `requireActivity().onBackPressedDispatcher` and remove in `onDestroyView()`.
  - `FragmentProxy` does not forward `onActivityResult` -> route `MainActivity.startActivityForResult` through `ComponentActivity.activityResultRegistry.register(...)` and unregister in `onDestroyView()`.
- **Milestone 2 On-Device Verified (`7a7fac4`, CI run `45014690105`, `Aegires v0.2.0`, `873 KB`):**
  - Full Aegires UI, insets, login, guilds/channels/chat, back-press navigation + clean exit to Aliucord, `LinkDialog`, and attachment picker all verified working unrooted inside Aliucord.
  - Note: Aliucord gradle-plugin requires strict `x.x.x` semver.

## Milestones
- [x] Phase 0: Deep Research Gate & Unrooted Architecture Decision
- [x] Milestone 1: Gradle/CI scaffold + shaded OkHttp 3.12.13 + minimal full-screen Aegires launch PoC on-device
- [x] Milestone 2: Full Aegires sync, storage isolation, back-press/inset/LinkDialog/attachment-picker verification
- [ ] Milestone 3: Automatic token handoff, clean Aliucord UI entrypoint, polish, merge `dev` -> `main`
