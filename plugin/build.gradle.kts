import org.apache.tools.ant.filters.ReplaceTokens

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
}

tasks {
    processResources {
        val filterTokens: Map<String, String> by project
        from(sourceSets.main) {
            filter<ReplaceTokens>("tokens" to filterTokens)
        }
    }
}