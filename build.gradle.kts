plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.2"
}

group = "dk.itu.thesis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // JMH
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    // SnapTree
    implementation("edu.stanford.ppl:snaptree:0.1")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "--add-exports",
            "java.base/jdk.internal.vm.annotation=ALL-UNNAMED"
        )
    )
}

jmh {
    includes.set(
        listOf((findProperty("jmhIncludes") as String?) ?: "TreeBenchmark")
    )

    resultFormat.set((findProperty("jmhResultFormat") as String?) ?: "CSV")

    val resultsPath = (findProperty("jmhResultsFile") as String?)
        ?: "${layout.buildDirectory.get().asFile}/reports/jmh/results.csv"
    resultsFile.set(file(resultsPath))

    val threadsProp = findProperty("jmhThreads") as String?
    if (threadsProp != null) {
        threads.set(threadsProp.toInt())
    }

    val paramsProp = findProperty("jmhBenchmarkParameters") as String?
    if (paramsProp != null) {
        paramsProp.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { entry ->
                val parts = entry.split("=", limit = 2)
                require(parts.size == 2) {
                    "Invalid jmhBenchmarkParameters entry: '$entry'"
                }

                val key = parts[0]
                val value = parts[1]

                benchmarkParameters.put(
                    key,
                    objects.listProperty(String::class.java).apply {
                        set(listOf(value))
                    }
                )
            }
    }
    jvmArgsAppend.add("-XX:-RestrictContended")
}