pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //noinspection JcenterRepositoryObsolete
        jcenter()
        maven { setUrl("https://storage.zego.im/maven") }
        maven{setUrl("https://jitpack.io")}
    }
}

rootProject.name = "Task 19 [21-2-24] Create a chat app using firebase"
include(":app")
