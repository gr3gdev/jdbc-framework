plugins {
    kotlin("jvm") version "1.4.21"
    `maven-publish`
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.zaxxer:HikariCP:4.0.2")
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:2.21.0")
}

tasks {
    compileKotlin {
        copy {
            from("build/resources/main")
            into("build/classes/kotlin/main")
        }
    }
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
    sourceCompatibility = "11"
    targetCompatibility = "11"
    kotlinOptions {
        jvmTarget = "11"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String
            artifactId = "jdbc-framework"
            version = rootProject.version as String
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GithubPackage"
            url = uri("https://maven.pkg.github.com/gr3gdev/jdbc-framework")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getProperty("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getProperty("GITHUB_TOKEN")
            }
        }
    }
}
