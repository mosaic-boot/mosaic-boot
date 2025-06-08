/**
 * Copyright 2025 JC-Lab (mosaicboot.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


object Version {
    val KOTLIN = "2.1.21"
    val SPRING_DEPENDENCY_MANAGEMENT = "1.1.7"
    val VANNIKTECH_MAVEN_PUBLISH = "0.30.0"

    val BCPROV = "jdk18on:1.80"
    val SPRING_BOOT by lazy {
        println("SPRING_BOOT : ${System.getProperty("springboot.version")}")
        "3.4.3"
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
