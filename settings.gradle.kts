pluginManagement {
    repositories {
        maven { url= uri("https://jitpack.io") }
        maven { url= uri("https://maven.aliyun.com/repository/public")}
        maven { url= uri("https://plugins.gradle.org/m2/")}
        maven { url= uri("https://maven.aliyun.com/nexus/content/repositories/google")}
        maven { url= uri("https://maven.aliyun.com/nexus/content/groups/public")}
        maven { url= uri("https://maven.aliyun.com/nexus/content/repositories/jcenter")}

        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        maven { url= uri("https://jitpack.io") }
        maven { url= uri("https://maven.aliyun.com/repository/public")}
        maven { url= uri("https://plugins.gradle.org/m2/")}
        maven { url= uri("https://maven.aliyun.com/nexus/content/repositories/google")}
        maven { url= uri("https://maven.aliyun.com/nexus/content/groups/public")}
        maven { url= uri("https://maven.aliyun.com/nexus/content/repositories/jcenter")}
        google()
        mavenCentral()
    }
}

rootProject.name = "Record_Wave"
include(":app")
 