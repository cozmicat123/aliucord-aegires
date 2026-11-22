version = "0.2.0"
description = "Run Aegires inside Aliucord (unrooted)"

aliucord {
    changelog.set(
        """
        # 0.2.0
        - Milestone 2: full Aegires source sync, isolated storage, ContextThemeWrapper host + AegiresFragment
        """.trimIndent(),
    )
    deploy.set(false)
}

dependencies {
    implementation(project(":shaded-okhttp"))
}
