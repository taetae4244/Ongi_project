pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // ✅ 반드시 마지막에 위치
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "front"
include(":app")
