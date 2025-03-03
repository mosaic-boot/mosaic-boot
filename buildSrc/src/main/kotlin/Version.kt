object Version {
    val KOTLIN = "1.9.25"
    val SPRING_DEPENDENCY_MANAGEMENT = "1.1.7"
    val VANNIKTECH_MAVEN_PUBLISH = "0.30.0"

    val BCPROV = "jdk18on:1.80"
    val SPRING_BOOT by lazy {
        println("SPRING_BOOT : ${System.getProperty("springboot.version")}")
        "3.4.0"
    }

    val PROJECT = getVersionFromGit()

    private fun getVersionFromGit(): String {
        return runCatching {
            val version = (
                System.getenv("CI_COMMIT_TAG")
                    ?.takeIf { it.isNotEmpty() }
                    ?: ProcessHelper.executeCommand(listOf("git", "describe", "--tags"))
                        .split("\n")[0]
                )
                .trim()
            if (version.startsWith("v")) {
                version.substring(1)
            } else version
        }.getOrElse {
            runCatching {
                ProcessHelper.executeCommand(listOf("git", "rev-parse", "HEAD"))
                    .split("\n")[0].trim() + "-SNAPSHOT"
            }.getOrElse {
                "unknown"
            }
        }
    }
}
