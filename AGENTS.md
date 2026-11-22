# AGENTS.md (attach with STATE.md at the start of every new chat, then type: start)

## Goal (Strictly Non-Root — Research Decides Architecture)
Bundle and run Aegires (`~/Aegires`, branch `gen3`) directly inside Aliucord as a loadable Aliucord plugin (no separate Aegires APK installed).
- **Hard constraint — Non-root only:** No root, no `su`, no Zygisk, no LSPosed, no system-level Xposed outside Aliucord's own process. Either this works completely unrooted inside Aliucord, or we do not build it.
- **No premature design:** Never commit to an implementation method before completing Phase 0 research. Exhaust every realistic unrooted angle to find what is *actually possible* and what the *cleanest, best* method is, then decide together with the owner (or call it off only if hard evidence proves no unrooted path can work).

## Mandatory Phase 0: Deep Research Gate (Before Any Coding)
Do NOT write plugin code or scaffold a build until all unknowns are empirically verified and logged in `STATE.md`:
1. **Aegires internals (`~/Aegires`):** Gradle/AGP/SDK/Kotlin versions, dependencies, UI stack (Android Views vs Compose), resources/assets, `AegiresApplication` + `MainActivity` lifecycle, storage/prefs, networking, WebViews/native libs, and Android API calls.
2. **Aliucord & Discord 126.21 host internals (local + upstream):** Inspect both `~/vc-work/aliucord-e2ee` AND upstream `Aliucord/Aliucord`, `Aliucord/gradle-plugin`, and `Aliucord/plugins-template` source code—specifically plugin `.zip`/DEX/resources loading, ClassLoader delegation order, bundled library versions that could collide, in-process hooking capabilities, and how plugins open screens/Activities/Fragments/overlays.
3. **Unrooted Android OS constraints & prior art:** Verify Android 15/16 (YAAP vanilla, no GApps) hidden API limits, read-only DEX / SELinux rules, unregistered Activity/manifest limits, resource ID (`0x7f`) isolation, and proven Android plugin-container / multi-DEX hosting prior art (source-level View/Fragment hosting, child-first `DexClassLoader`, shading/relocation, Activity lifecycle proxying).
4. **Go / No-Go & Feasibility Verdict:** Compare every viable unrooted method with hard evidence from inspected source lines and prior art. Do not bail out over a single obstacle—investigate workarounds thoroughly first. Only start coding after the owner approves the chosen method; abort only if exhaustive evidence proves it unviable.

## Owner & Agent Workflow
The owner has only a phone with Termux (**no PC**). The owner runs commands, pastes outputs, tests builds, and co-decides major choices—the owner does NOT read/edit code by hand.
- **Every reply:** (1) 1–2 lines explaining what we are doing and why; (2) ONE Termux block (safe to re-run, long output trimmed with `| head -n 80` or `| tail -n 40`); (3) `"Paste back: <what>"`.
- Never more than one command block per reply. Never ask the owner to edit files by hand—use `cat > file <<'EOF'`, `node -e`, or `git apply`.
- Prefer clean, proven, minimal approaches over brittle hacks; write custom workarounds only after thorough research proves they are needed.
- Never ask for or echo secrets (tokens, keys, passwords). No personal names in plugin metadata or commits.

## Single Memory (`STATE.md`) & Bookkeeping
- `STATE.md` (branch `dev`) is the ONLY memory across chats. On `start`: read `STATE.md` and continue from `Next:`.
- After every verified research finding, decision, or milestone, update `STATE.md` (and `Next:`), commit, push to `dev`, and refresh `/sdcard/Download/aliucord-aegires/{AGENTS.md,STATE.md}`.
- Keep `STATE.md` under ~80 lines; rewrite compactly when it grows.

## Chat Switching Rule
- **One research phase or milestone per chat.**
- Switch immediately when: (1) the current phase/milestone is finished and logged in `STATE.md`; (2) the agent starts repeating itself, contradicting verified lines, or drifting; or (3) the owner types `switch` or `reset`.
- **Switch procedure:** Update + commit + push `STATE.md`, copy `AGENTS.md` and `STATE.md` to `/sdcard/Download/aliucord-aegires/`, and end with:
  `DONE: open a new chat, attach AGENTS.md and STATE.md, type: start.`

## Engineering Rules
1. **Quote or check:** Never rely on memory of Aegires, Aliucord, Discord 126.21, or Android internals. Quote the exact file and numbered source line before concluding. If unseen, state `UNKNOWN` and give the Termux command that inspects it.
2. **Don't give up on bad searches or commands:** Never conclude a file, API, class, or approach doesn't exist or is impossible just because a search returned empty or a command errored. Check for wrong directory paths, filenames, branch names, casing, or regex/quoting bugs—list the real tree (`ls`, `find`, `git ls-files`, `gh api`) and broaden the search before ruling anything out.
3. **Evidence over guessing:** Verify every assumption (compatibility, classloading, resources, manifest, CI) empirically on real files or a minimal proof-of-concept before scaling up.
4. **Reversible commits:** Work on branch `dev` in small commits. State how to roll back a change only when it is experimental or risky. Merge `dev` -> `main` only after on-device verification.
5. **CI-first builds:** Use GitHub Actions for Gradle/DEX builds since there is no PC.
6. **Long prompts:** State the task at the start and repeat it in the last line.
