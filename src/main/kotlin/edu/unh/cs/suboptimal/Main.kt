package edu.unh.cs.suboptimal

import edu.unh.cs.configuration.Configuration
import edu.unh.cs.executive.runWAStarSlidingTilePuzzle

data class algorithmDomainToRun(val algorithm: String, val domain: String)

fun readLines(): List<String> {
    val line = readLine()
    if (line != null) {
       return listOf(line) + readLines()
    }
    return listOf()
}

fun main(args: Array<String>) {
    val configuration = Configuration(readLine()!!, readLine()!!, readLine()!!, readLine()!!, readLine()!!, readLines())
    println("config: $configuration")
    when(algorithmDomainToRun(configuration.algorithm, configuration.domain)) {
        algorithmDomainToRun("wa*", "stp") -> runWAStarSlidingTilePuzzle(configuration)
        else -> throw NotImplementedError("Configuration [${configuration.algorithm}, ${configuration.domain}] is not implemented!")
    }
}