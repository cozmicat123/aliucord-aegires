version = "0.3.0"
description = "Run Aegires inside Aliucord (unrooted)"

aliucord {
    changelog.set(
        """
        # 0.3.0
        - Automatic Discord token handoff from Aliucord (configurable in settings)
        - Added "Aegires" launch button in Discord Settings toolbar and /aegires command
        - Added settings toggles and clear-token action
        """.trimIndent(),
    )
    deploy.set(false)
}

dependencies {
    implementation(project(":shaded-okhttp"))
}
