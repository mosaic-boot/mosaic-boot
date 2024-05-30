plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "mosaicboot-parent"

include("mosaicboot-common")
include("mosaicboot-util-mongodb")
include("mosaicboot-account")
include("mosaicboot-account-mongodb")
include("example-web-service")
//include("mosaicboot-payment")
//include("mosaicboot-admin-web")
