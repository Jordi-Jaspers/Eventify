rootProject.name = "eventify-server"
pluginManagement {
    val springBootPluginVersion: String by settings
    val springDependencyPluginVersion: String by settings
    val qualityPluginVersion: String by settings
    val spotlessPluginVersion: String by settings
    val lombokPluginVersion: String by settings
    val dependencyUpdatesPluginVersion: String by settings
    val liquibasePlugin: String by settings

    plugins {
        id("org.springframework.boot") version springBootPluginVersion
        id("io.spring.dependency-management") version springDependencyPluginVersion
        id("ru.vyarus.quality") version qualityPluginVersion
        id("io.freefair.lombok") version lombokPluginVersion
        id("com.github.ben-manes.versions") version dependencyUpdatesPluginVersion
        id("com.diffplug.spotless") version spotlessPluginVersion
        id("org.liquibase.gradle") version liquibasePlugin
    }
}
