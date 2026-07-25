plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)

    testImplementation(libs.lint.tests)
    testImplementation(kotlin("test"))
}

tasks.jar {
    manifest {
        attributes(
            "Lint-Registry-v2" to "com.tiendamuna.lintrules.CustomIssueRegistry",
            "Lint-Registry-v3" to "com.tiendamuna.lintrules.CustomIssueRegistry"
        )
    }
}
