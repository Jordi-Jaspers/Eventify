import org.gradle.jvm.toolchain.JavaLanguageVersion.of

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
    liquibaseRuntime("org.mariadb.jdbc", "mariadb-java-client", retrieve("mariadbVersion"))
}

/** Configure the Liquibase plugin with passed properties. */
project.ext["env"] = System.getProperty("env")
when {
    project.ext["env"] == "custom" -> {
        project.ext["dbUrl"] = System.getProperty("dbUrl")
        project.ext["dbUsername"] = System.getProperty("dbUsername")
        project.ext["dbPassword"] = System.getProperty("dbPassword")
        project.ext["contexts"] = System.getProperty("contexts")
        project.ext["outputFile"] = System.getProperty("outputFile")
        project.ext["changelogFile"] = System.getProperty("changelogFile")
    }

    else -> {
        // No env specified: Use the configs for local development
        project.ext["dbUrl"] = "jdbc:mariadb://localhost:3306/tst_application"
        project.ext["dbUsername"] = "tst_application"
        project.ext["dbPassword"] = "tst_application"
        project.ext["contexts"] = "test"
        project.ext["outputFile"] = "liquibaseChanges.sql"
        project.ext["changelogFile"] = "database/db.changelog.yaml"
    }
}

// ============== STATIC FUNCTIONS ================
fun retrieve(property: String): String {
    return project.findProperty(property) as String?
        ?: throw IllegalArgumentException("Property $property not found")
}

// ============== ACTIVITIES ================
liquibase {
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
