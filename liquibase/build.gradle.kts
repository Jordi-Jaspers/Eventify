import org.gradle.jvm.toolchain.JavaLanguageVersion.of
import org.liquibase.gradle.LiquibaseExtension

/** The repositories used to download the dependencies */
repositories {
    mavenLocal()
    mavenCentral()
}

/** Project Plugins */
plugins {
    id("java-library")
    id("org.liquibase.gradle")
    id("com.github.ben-manes.versions")
}

/** Java 21 is long term supported and therefore chosen as the default. */
java {
    toolchain {
        languageVersion.set(of(21))
    }
}

/** The dependencies of the project */
dependencies {
    liquibaseRuntime("org.liquibase", "liquibase-core", retrieve("liquibaseCoreVersion"))
    liquibaseRuntime("info.picocli", "picocli", retrieve("picocliVersion"))
    liquibaseRuntime("org.yaml", "snakeyaml", retrieve("snakeyaml"))
    liquibaseRuntime("org.postgresql", "postgresql", retrieve("postgresVersion"))
}



// ============== STATIC FUNCTIONS ================
fun retrieve(property: String): String {
    val foundProperty = project.findProperty(property) as String?
        ?: throw IllegalArgumentException("Property $property not found")
    return foundProperty.replace("\"", "")
}

// ============== ACTIVITIES ================
configure<LiquibaseExtension> {
    activities.register("main") {
        this.arguments = mapOf(
            "logLevel" to "info",
            "output-default-catalog" to "false",
            "output-default-schema" to "false",
            "changelogFile" to project.ext["changelogFile"],
            "url" to project.ext["dbUrl"],
            "username" to project.ext["dbUsername"],
            "password" to project.ext["dbPassword"],
            "contexts" to project.ext["contexts"],
            "outputFile" to project.ext["outputFile"]
        )
    }
    runList = "main"
}
