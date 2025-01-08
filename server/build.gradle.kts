import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.cyclonedx.gradle.CycloneDxTask
import org.gradle.api.file.DuplicatesStrategy.INCLUDE
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.toolchain.JavaLanguageVersion.of
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import org.springframework.boot.loader.tools.LoaderImplementation.CLASSIC
import ru.vyarus.gradle.plugin.quality.QualityExtension
import org.liquibase.gradle.LiquibaseExtension

group = retrieve("group")
version = retrieve("version")

/** Java 21 is long term supported and therefore chosen as the default. */
java {
    toolchain {
        languageVersion.set(of(21))
    }
}

/** The repositories used to download the dependencies. */
repositories {
    mavenCentral()
    mavenLocal()
}

/** Configure the Plugins required within the project. */
plugins {
    // IntelliJ IDEA Management
    id("idea")

    // Java Management
    id("java-library")

    // Spring boot Management
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    // Quality plugin for Checkstyle, PMD and Spotbugs.
    id("ru.vyarus.quality")

    // Spotless is a code formatter that uses a set of pre-defined rules to format the code.
    id("com.diffplug.spotless")

    // The CycloneDX Gradle plugin creates an aggregate of all direct and transitive dependencies of a project.
    id("org.cyclonedx.bom")

    // Automatically generates a list of updatable dependencies.
    id("com.github.ben-manes.versions")

    // The project-report plugin provides file reports on dependencies, tasks, etc.
    id("project-report")

    // Automatic lombok and delombok configuration.
    id("io.freefair.lombok")

    // Liquibase plugin for managing database changes.
    id("org.liquibase.gradle")
}

/** Configure the dependencies required within the project. */
dependencies {
    // ======= ANNOTATION PROCESSORS =======
    // annotation processor that generates metadata about classes in your application that are annotated with @ConfigurationProperties.
    annotationProcessor("org.springframework.boot", "spring-boot-configuration-processor")

    // Provides Mapstruct annotations for spring.
    annotationProcessor("org.mapstruct", "mapstruct-processor", retrieve("mapStructVersion"))

    // ======= RUNTIME DEPENDENCIES =======
    // Jdbc driver to connect with the PostgreSQL database.
    runtimeOnly("org.postgresql", "postgresql", retrieve("postgresVersion"))

    // jolokia support for spring boot 3
    runtimeOnly("org.jolokia", "jolokia-support-spring", retrieve("jolokiaVersion"))

    // ======= SPRINGBOOT DEPENDENCIES =======
    implementation("org.springframework.boot", "spring-boot-starter-webflux")
    implementation("org.springframework.boot", "spring-boot-starter-actuator")
    implementation("org.springframework.boot", "spring-boot-starter-web")
    implementation("org.springframework.boot", "spring-boot-starter-security")
    implementation("org.springframework.boot", "spring-boot-starter-data-jpa")
    implementation("org.springframework.boot", "spring-boot-starter-validation")
    implementation("org.springframework.boot", "spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot", "spring-boot-starter-mail")
    implementation("org.springframework.boot", "spring-boot-starter-security")
    implementation("org.springframework.boot", "spring-boot-starter-oauth2-resource-server")

    implementation("org.springframework.boot", "spring-boot-starter-jdbc") {
        exclude("org.apache.tomcat", module = "tomcat-jdbc")
    }

    // ======= IMPLEMENTATION DEPENDENCIES =======
    // Open API documentation generation.
    implementation("org.springdoc", "springdoc-openapi-starter-webmvc-ui", retrieve("springdocVersion"))

    // provides the core of hawaii framework such as the response entity exception handling.
    implementation("org.hawaiiframework", "hawaii-starter-rest", retrieve("hawaiiFrameworkVersion"))
    implementation("org.hawaiiframework", "hawaii-starter-logging", retrieve("hawaiiFrameworkVersion"))

    // Provide a datasource proxy that can inject your own logic into all queries.
    implementation("net.ttddyy", "datasource-proxy", retrieve("datasourceProxyVersion"))

    // Used to validate entities and beans
    implementation("jakarta.servlet", "jakarta.servlet-api", retrieve("jakartaServletVersion"))

    // Mail service provider that supports thymeleaf templating.
    implementation("jakarta.mail", "jakarta.mail-api", retrieve("jakartaMailVersion"))

    // Mapstruct is used to generate code to map from domain model classes to rest application model classes
    implementation("org.mapstruct", "mapstruct", retrieve("mapStructVersion"))

    // Hibernate's core ORM functionality
    implementation("org.hibernate.orm", "hibernate-core", retrieve("hibernateCoreVersion"))
    implementation("org.hibernate.validator", "hibernate-validator", retrieve("hibernateValidatorVersion"))

    // Library for checking that a password complies with a custom set of rules
    implementation("org.passay", "passay", retrieve("passayVersion"))

    // Java library for Javascript Object Signing and Encryption (JOSE) and JSON Web Tokens (JWT)
    implementation("com.nimbusds", "nimbus-jose-jwt", retrieve("nimbusJoseJwtVersion"))

    // LogstashEncoder is used to encode log messages in logstash format
    implementation("net.logstash.logback", "logstash-logback-encoder", retrieve("logstashEncoderVersion"))

    // ======= TEST DEPENDENCIES =======
    testImplementation("org.springframework.boot", "spring-boot-test")
    testImplementation("org.springframework.boot", "spring-boot-testcontainers")
    testImplementation("org.springframework.boot", "spring-boot-starter-test") {
        exclude("com.vaadin.external.google", module = "android-json")
    }

    testImplementation("org.springframework.security", "spring-security-test", retrieve("springSecurityTestVersion"))
    testImplementation("org.testcontainers", "postgresql", retrieve("testContainerVersion"))
    testImplementation("org.testcontainers", "junit-jupiter", retrieve("testContainerVersion"))
    testImplementation("org.liquibase", "liquibase-core", retrieve("liquibaseVersion"))

    testImplementation("io.rest-assured", "rest-assured", retrieve("restAssuredVersion"))
    testImplementation("io.rest-assured", "spring-mock-mvc", retrieve("restAssuredVersion"))

    // ======= LIQUIBASE PLUGIN DEPENDENCIES =======
    liquibaseRuntime("org.liquibase", "liquibase-core", retrieve("liquibaseVersion"))
    liquibaseRuntime("info.picocli", "picocli", retrieve("picocliVersion"))
    liquibaseRuntime("org.yaml", "snakeyaml", retrieve("snakeyaml"))
    liquibaseRuntime("org.postgresql", "postgresql", retrieve("postgresVersion"))
}

/**
 * Removing vulnerability by persisting to a specified version.
 * Note: Remove once they are patched in the parent dependency.
 */
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.apache.logging.log4j") {
            useVersion("2.17.1")
            because("Apache Log4j2 vulnerable to RCE via JDBC Appender when attacker controls configuration.")
        }
        if (requested.group == "org.yaml" && requested.name == "snakeyaml") {
            useVersion("2.2")
            because("Vulnerability in SnakeYAML 1.33: CVE-2022-1471")
        }
        if (requested.group == "com.jayway.jsonpath" && requested.name == "json-path") {
            useVersion("2.9.0")
            because("Vulnerability in jsonpath 2.7.0: CVE-2023-51074")
        }
    }
}

// ============== PLUGIN CONFIGURATION ================
/** IDE Settings: Point to the correct directories for source and resources. */
configure<IdeaModel> {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
        testSources.setFrom(file("src/test/java"))
        testResources.setFrom(file("src/test/resources"))
        sourceDirs.add(file("src/main/java"))
        resourceDirs.add(file("src/main/resources"))
    }
}

/** Configuration for the Spotless plugin. */
configure<SpotlessExtension> {
    spotless {
        java {
            cleanthat()
            toggleOffOn()
            eclipse().configFile("src/quality/config/spotless/HawaiiFrameworkStyle.xml")

            endWithNewline()
            removeUnusedImports()
            trimTrailingWhitespace()
            importOrder("", "java|jakarta|javax", "\\#")
        }
    }
}

/** Configuration propertied for the quality plugin. */
configure<QualityExtension> {
    autoRegistration = true
    configDir = "src/quality/config/"

    spotbugsVersion = retrieve("spotbugsVersion")
    spotbugs = true

    pmdVersion = retrieve("pmdVersion")
    pmd = true

    checkstyleVersion = retrieve("checkstyleVersion")
    checkstyle = true

    codenarcVersion = retrieve("codenarcVersion")
    codenarc = true
}

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
        project.ext["dbUrl"] = "jdbc:postgresql://localhost:5432/tst_eventify"
        project.ext["dbUsername"] = "tst_eventify"
        project.ext["dbPassword"] = "tst_eventify"
        project.ext["contexts"] = "test"
        project.ext["outputFile"] = "build/liquibase/updateSQL.sql"
        project.ext["changelogFile"] = "src/main/resources/db/changelog/db.changelog-master.yaml"
    }
}

// ============== STATIC FUNCTIONS ================
fun retrieve(property: String): String {
    val foundProperty = project.findProperty(property) as String?
        ?: throw IllegalArgumentException("Property $property not found")
    return foundProperty.replace("\"", "")
}

// ============== TASK CONFIGURATION ================
tasks.getByName<BootJar>("bootJar") {
    loaderImplementation = CLASSIC
    duplicatesStrategy = INCLUDE
    archiveVersion.set(project.version.toString())
    archiveBaseName.set(retrieve("artifactName"))

    manifest {
        attributes("Name" to retrieve("artifactName"))
        attributes("Implementation-Title" to retrieve("artifactDescription"))
        attributes("Implementation-Version" to version)
        attributes("Implementation-Vendor" to retrieve("vendor"))
        attributes("Implementation-Vendor-Url" to retrieve("vendorUrl"))
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    testLogging {
        showCauses = true
        showExceptions = true
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}

tasks.withType<ProcessResources> {
    filesMatching("application.yml") {
        filter { line ->
            line.replace("APPLICATION_VERSION", version as String)
        }
    }
}

tasks.withType<DependencyUpdatesTask> {
    checkForGradleUpdate = true
    gradleReleaseChannel = "current"
}

tasks.withType<CycloneDxTask> {
    setProjectType("application")
    setSchemaVersion("1.5")
    setDestination(project.file("build/reports"))
    setOutputName("application-sbom")
    setOutputFormat("json")
    setIncludeBomSerialNumber(true)
    setIncludeLicenseText(true)
    setComponentVersion(version.toString())
}

tasks.withType<JavaCompile> {
    dependsOn("spotlessApply")
    options.isDeprecation = true
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        arrayOf(
            "-Xlint:all",
            "-Xlint:-serial",
            "-Xlint:-processing",
            "-Xlint:-this-escape",
            "-Werror"
        )
    )
}

tasks.named<BootRun>("bootRun") {
    systemProperty("application.version", version)
    val arguments = ArrayList<String>()
    if (System.getenv("TRUSTSTORE_LOCATION") != null && System.getenv("TRUSTSTORE_PASSWORD") != null) {
        val trustStoreLocation = System.getenv("TRUSTSTORE_LOCATION")
        val trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD")
        println("[Gradle Config] 'TRUSTSTORE' properties found, setting trustStore to: $trustStoreLocation")
        arguments.add("-Djavax.net.ssl.trustStore=$trustStoreLocation")
        arguments.add("-Djavax.net.ssl.trustStorePassword=$trustStorePassword")
    } else {
        val defaultTrustStoreLocation = "../../smtvagrant/pki/truststore.jks"
        val defaultTrustStorePassword = "changeit"
        println("[Gradle Config] 'TRUSTSTORE_LOCATION' and 'TRUSTSTORE_PASSWORD' properties not found, setting trustStore to default location: $defaultTrustStoreLocation")
        arguments.add("-Djavax.net.ssl.trustStore=$defaultTrustStoreLocation")
        arguments.add("-Djavax.net.ssl.trustStorePassword=$defaultTrustStorePassword")
    }

    arguments.addAll(
        arrayOf(
            "-Xms512m",
            "-Xmx4096m",
            "-XX:MetaspaceSize=512m",
            "-XX:MaxMetaspaceSize=1024m",
            "-XX:MaxMetaspaceFreeRatio=60",
            "-Djava.awt.headless=true",
            "-XX:+UseG1GC",
            "-Dspring.output.ansi.enabled=ALWAYS",
        )
    )
    jvmArgs(arguments)
}
