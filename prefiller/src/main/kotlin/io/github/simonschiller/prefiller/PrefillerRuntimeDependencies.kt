package io.github.simonschiller.prefiller

private object Versions {
    const val ANTLR = "4.13.1" // https://mvnrepository.com/artifact/org.antlr/antlr4
    const val ICU4J = "74.1" // https://mvnrepository.com/artifact/com.ibm.icu/icu4j
    const val SQLITE = "3.44.0.0" // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    const val JSONP = "2.0.1" // https://mvnrepository.com/artifact/org.glassfish/jakarta.json
}

private object Dependencies {
    const val ANTLR_RUNTIME = "org.antlr:antlr4-runtime:${Versions.ANTLR}"
    const val ICU4J = "com.ibm.icu:icu4j:${Versions.ICU4J}"
    const val SQLITE = "org.xerial:sqlite-jdbc:${Versions.SQLITE}"
    const val JSONP = "org.glassfish:jakarta.json:${Versions.JSONP}"
}

internal val PREFILLER_RUNTIME_DEPENDENCIES = listOf(
    Dependencies.ANTLR_RUNTIME,
    Dependencies.ICU4J,
    Dependencies.JSONP,
    Dependencies.SQLITE,
)
