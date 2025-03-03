plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "mosaic-boot"

include("mosaic-boot-core")
//include("mosaic-boot-payment")
include("mosaic-boot-mongodb-default")
//include("mosaic-boot-payment-nicepaystart")
include("sample-app")
