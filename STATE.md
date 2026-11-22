# STATE.md: shared memory. Read first on "start".
Next: Phase 0 Research — inspect `~/Aegires/app/build.gradle`, `~/Aegires/build.gradle`, Aegires source tree (`AegiresApplication`, `MainActivity`, resources, deps), `~/vc-work/aliucord-e2ee`, and upstream `Aliucord/Aliucord` plugin loader source. Do NOT pick an architecture or write code yet.

## Setup
- Repo: `~/vc-work/aliucord-aegires` (branch `dev`) = `github.com/cozmicat123/aliucord-aegires`
- Handoff folder: `/sdcard/Download/aliucord-aegires/{AGENTS.md,STATE.md}`
- Device: OnePlus 12 (`waffle`), YAAP vanilla (no GApps), Termux only (no PC).
- Hard Constraint: **Strictly non-root only** (no `su`, Zygisk, or LSPosed; abort only if exhaustive research proves unrooted unviable).
- Local references: `~/Aegires` (gen3), `~/vc-work/aliucord-e2ee`.
- Upstream references: `Aliucord/Aliucord`, `Aliucord/gradle-plugin`, `Aliucord/plugins-template`.

## Verified Facts (Phase 0 Started)
- `~/Aegires` build files within depth 3: `./build.gradle`, `./app/build.gradle`. No `.so` files or `CMakeLists.txt` found within depth 3.
- `~/Aegires/app/src/main/AndroidManifest.xml`:
  - Permission: `android.permission.INTERNET` only.
  - `<application>`: `.AegiresApplication`, `theme="@android:style/Theme.DeviceDefault.NoActionBar"`, `allowBackup="false"`, `largeHeap="true"`.
  - `<activity>`: single `.MainActivity` (`LAUNCHER`, `adjustResize`, handles configChanges).
  - Services / ContentProviders / BroadcastReceivers: none declared.

## Phase 0 Research Checklist (All Open)
- [ ] Aegires full stack (Gradle deps, Kotlin/Java, Views vs Compose, resources, storage, lifecycle)
- [ ] Aliucord host internals (upstream `Aliucord/Aliucord` plugin `.zip`/DEX/resources loader, ClassLoader behavior, Discord 126.21 dep conflicts, screen/Activity host APIs)
- [ ] Unrooted Android 15/16 OS constraints & Android plugin-container prior art
- [ ] Unrooted Go / No-Go + architecture decision with owner (only after checklist above is verified)
