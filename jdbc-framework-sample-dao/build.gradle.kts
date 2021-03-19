plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
}

kapt {
    correctErrorTypes = true
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.h2database:h2:1.4.200")
    implementation(project(":jdbc-framework"))
    kapt(project(":jdbc-framework"))
}
