version = "0.1.0"
description = "Run Aegires inside Aliucord (unrooted PoC)"

aliucord {
    changelog.set(
        """
        # 0.1.0-poc
        - Milestone 1: full-screen launch PoC + shaded OkHttp 3.12.13 / Okio 1.17.5
        """.trimIndent(),
    )
    deploy.set(false)
}

dependencies {
    implementation(project(":shaded-okhttp"))
}
