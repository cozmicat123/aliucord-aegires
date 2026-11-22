version = "0.3.2"
description = "Run Aegires inside Aliucord (unrooted)"

aliucord {
    changelog.set(
        """
        # 0.3.2
        - Automatic Discord token handoff from Aliucord (configurable in settings)
        - Added "Aegires" launch button in Discord Settings toolbar and /aegires command
        - Added settings toggles, isolated clear-token action, and Aliucord auto-updater support
        """.trimIndent(),
    )
    deploy.set(true)
}

dependencies {
    implementation(project(":shaded-okhttp"))
}
