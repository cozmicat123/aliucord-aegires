version = "0.3.3"
description = "Run Aegires inside Aliucord (unrooted)"

aliucord {
    changelog.set(
        """
        # 0.3.3
        - Fixed isolated openFileOutput() so last-channel disk cache stays inside aegires/ and restores on launch
        - Fixed DiskCache and MessageCacheManager executor thread leaks on Aegires exit
        - Hardened sync-aegires.sh (all-file OkHttp/Okio shading + strict patch assertions) and wrapper-validation workflow paths
        """.trimIndent(),
    )
    deploy.set(true)
}

dependencies {
    implementation(project(":shaded-okhttp"))
}
