pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        // Альтернативные репозитории
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("https://repo.maven.apache.org/maven2") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Для PhotoView
        // Альтернативные репозитории
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("https://repo.maven.apache.org/maven2") }
    }
}

rootProject.name = "curs_mobile"
include(":app")
 