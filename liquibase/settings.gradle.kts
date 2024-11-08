rootProject.name = "application-database"
pluginManagement {
    val liquibasePlugin: String by settings
    val dependencyUpdatesPluginVersion: String by settings
    plugins {
        id("org.liquibase.gradle") version liquibasePlugin
        id("com.github.ben-manes.versions") version dependencyUpdatesPluginVersion
    }
}
