package edu.unh.cs.suboptimal

import edu.unh.cs.configuration.Configuration
import edu.unh.cs.executive.runWAStarSlidingTilePuzzle
import edu.unh.cs.executive.runDPSSlidingTilePuzzle
import java.io.File
import java.util.*

data class algorithmDomainToRun(val algorithm: String, val domain: String)

fun readLines(inputFile: Scanner): List<String> {
    if (inputFile.hasNextLine()) {
        val line = inputFile.nextLine()
        if (line != null) {
            return listOf(line) + readLines(inputFile)
        }
    }
    return listOf()
}

fun main(args: Array<String>) {
    println("Input file: ${args[0]}")
    val inputFile = Scanner(File(args[0]))
    val domain = inputFile.nextLine()
    val algorithm = inputFile.nextLine()
    val bound = inputFile.nextLine()
    val height = inputFile.nextLine()
    val width = inputFile.nextLine()
    val grid = readLines(inputFile)
    val configuration = Configuration(domain, algorithm, bound, height, width, grid)
    println("config: $configuration")
    when(algorithmDomainToRun(configuration.algorithm, configuration.domain)) {
        algorithmDomainToRun("wa*", "stp") -> runWAStarSlidingTilePuzzle(configuration)
        algorithmDomainToRun("dps", "stp") -> runDPSSlidingTilePuzzle(configuration)
        else -> throw NotImplementedError("Configuration [${configuration.algorithm}, ${configuration.domain}] is not implemented!")
    }
}