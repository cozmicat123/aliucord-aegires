#!/usr/bin/env bash
set -euo pipefail

AEGIRES_SRC="${1:-$HOME/Aegires}/app/src/main/java/a/b"
DEST="$(cd "$(dirname "$0")/.." && pwd)/plugins/Aegires/src/main/java/a/b"

if [ ! -d "$AEGIRES_SRC" ]; then
  echo "ERROR: Aegires source not found at $AEGIRES_SRC" >&2
  exit 1
fi

rm -rf "$DEST"
mkdir -p "$DEST"
cp -r "$AEGIRES_SRC"/. "$DEST"/

# 1. Drop AegiresApplication (Conscrypt not needed; platform TLS 1.3 verified on-device)
rm -f "$DEST/AegiresApplication.java"

# 2. Shade OkHttp/Okio references in the 3 files that use them + fix OkHttp 3.12 RequestBody.create arg order
node -e '
const fs = require("fs");
const dest = process.argv[1];

for (const rel of ["api/ApiClient.java", "api/GatewayClient.java", "util/AndroidMedia.java"]) {
  const p = `${dest}/${rel}`;
  let s = fs.readFileSync(p, "utf8");
  s = s.replace(/\bokhttp3\./g, "aegires.okhttp3.").replace(/\bokio\./g, "aegires.okio.");
  if (rel === "api/ApiClient.java") {
    const before = "RequestBody.create(jsonBody, JSON)";
    if (!s.includes(before)) throw new Error("Expected ApiClient RequestBody.create(jsonBody, JSON) not found");
    s = s.replace(before, "RequestBody.create(JSON, jsonBody)");
  }
  fs.writeFileSync(p, s);
}

// 3. Unwrap ContextWrapper in LinkDialog.java
{
  const p = `${dest}/util/LinkDialog.java`;
  let s = fs.readFileSync(p, "utf8");
  const target = "if (!(ctx instanceof Activity)) return;\n        Activity activity = (Activity) ctx;";
  if (!s.includes(target)) throw new Error("Expected LinkDialog instanceof Activity check not found");
  s = s.replace(
    target,
    "Context c = ctx;\n" +
    "        while (c instanceof android.content.ContextWrapper && !(c instanceof Activity)) {\n" +
    "            c = ((android.content.ContextWrapper) c).getBaseContext();\n" +
    "        }\n" +
    "        if (!(c instanceof Activity)) return;\n" +
    "        Activity activity = (Activity) c;"
  );
  fs.writeFileSync(p, s);
}

// 4. Adapt MainActivity into a ContextThemeWrapper-backed host for Aliucord FragmentProxy
{
  const p = `${dest}/MainActivity.java`;
  let s = fs.readFileSync(p, "utf8");

  s = s.replace(
    "public class MainActivity extends Activity {",
    "public class MainActivity extends android.view.ContextThemeWrapper {\n" +
    "    private final Activity hostActivity;\n" +
    "    private final androidx.fragment.app.Fragment hostFragment;\n" +
    "    private final Intent hostIntent;\n\n" +
    "    public MainActivity(Activity hostActivity, androidx.fragment.app.Fragment hostFragment, Intent hostIntent) {\n" +
    "        super(hostActivity, android.R.style.Theme_DeviceDefault_NoActionBar);\n" +
    "        this.hostActivity = hostActivity;\n" +
    "        this.hostFragment = hostFragment;\n" +
    "        this.hostIntent = hostIntent != null ? hostIntent : new Intent();\n" +
    "    }\n\n" +
    "    public Activity getHostActivity() { return hostActivity; }\n" +
    "    public android.view.Window getWindow() { return hostActivity.getWindow(); }\n" +
    "    public View getCurrentFocus() { return hostActivity.getCurrentFocus(); }\n" +
    "    public void runOnUiThread(Runnable action) { hostActivity.runOnUiThread(action); }\n" +
    "    public Intent getIntent() { return hostIntent; }\n" +
    "    public boolean isFinishing() { return hostActivity.isFinishing(); }\n" +
    "    public void finish() { hostActivity.finish(); }\n" +
    "    public interface ActivityResultLauncherDelegate { void launch(Intent intent, int requestCode); }\n" +
    "    private ActivityResultLauncherDelegate resultLauncher;\n" +
    "    public void setActivityResultLauncher(ActivityResultLauncherDelegate d) { this.resultLauncher = d; }\n" +
    "    @SuppressWarnings(\"deprecation\")\n" +
    "    public void startActivityForResult(Intent intent, int requestCode) {\n" +
    "        if (resultLauncher != null) resultLauncher.launch(intent, requestCode);\n" +
    "        else hostFragment.startActivityForResult(intent, requestCode);\n" +
    "    }\n" +
    "    @Override\n" +
    "    public android.content.Context getApplicationContext() { return this; }\n" +
    "    @Override\n" +
    "    public android.content.SharedPreferences getSharedPreferences(String name, int mode) {\n" +
    "        return super.getSharedPreferences(\"aegires_\" + name, mode);\n" +
    "    }\n" +
    "    @Override\n" +
    "    public java.io.File getFilesDir() {\n" +
    "        java.io.File dir = new java.io.File(super.getFilesDir(), \"aegires\");\n" +
    "        if (!dir.exists()) dir.mkdirs();\n" +
    "        return dir;\n" +
    "    }\n" +
    "    @Override\n" +
    "    public java.io.File getCacheDir() {\n" +
    "        java.io.File dir = new java.io.File(super.getCacheDir(), \"aegires\");\n" +
    "        if (!dir.exists()) dir.mkdirs();\n" +
    "        return dir;\n" +
    "    }\n"
  );

  // Convert Activity lifecycle overrides to public host methods
  s = s.replace("@Override\n    protected void onCreate(Bundle savedInstanceState) {\n        super.onCreate(savedInstanceState);\n        setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);",
                "public void onCreate(Bundle savedInstanceState) {");
  s = s.replace("        setContentView(rootFrame);\n", "");
  s = s.replace("@Override\n    public void onBackPressed() {", "public boolean handleBackPressed() {");
  s = s.replace("if (fullscreenOverlay != null) {\n            hideFullscreen();\n            return;\n        }\n        goBack();",
                "if (fullscreenOverlay != null) {\n            hideFullscreen();\n            return true;\n        }\n        if (screenStack.size() > 1) {\n            goBack();\n            return true;\n        }\n        return false;");
  s = s.replace("@Override\n    protected void onActivityResult(int requestCode, int resultCode, Intent data) {\n        super.onActivityResult(requestCode, resultCode, data);",
                "public void onActivityResult(int requestCode, int resultCode, Intent data) {");
  s = s.replace("@Override\n    protected void onResume() {\n        super.onResume();",
                "public void onResume() {");
  s = s.replace("@Override\n    protected void onPause() {\n        super.onPause();",
                "public void onPause() {");
  s = s.replace("@Override\n    protected void onDestroy() {\n        if (gateway != null) gateway.shutdown();\n        if (apiClient != null) apiClient.shutdown();\n        super.onDestroy();\n    }",
                "public void onDestroy() {\n        if (fullscreenOverlay != null) hideFullscreen();\n        while (!screenStack.isEmpty()) {\n            Screen sc = screenStack.pop();\n            try { sc.onPause(); } catch (Exception ignored) {}\n            try { sc.onDestroy(); } catch (Exception ignored) {}\n        }\n        if (gateway != null) gateway.shutdown();\n        if (apiClient != null) apiClient.shutdown();\n    }");

  fs.writeFileSync(p, s);
}
' "$DEST"

echo "Synced $(find "$DEST" -name '*.java' | wc -l) Java files into $DEST"
