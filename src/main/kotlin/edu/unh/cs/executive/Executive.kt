package edu.unh.cs.executive

import edu.unh.cs.configuration.Configuration
import edu.unh.cs.domain.*
import edu.unh.cs.suboptimal.planner.DynPotentialSearch
import edu.unh.cs.suboptimal.planner.WeightedAStar
import sun.security.krb5.Config
import java.util.*
import kotlin.system.measureTimeMillis

fun displayMetaData(c: Configuration) {
    println("Domain: ${c.domain}")
    println("Algorithm: ${c.algorithm}")
    println("Bound: ${c.bound}")
}

fun runTilesWAStar(c: Configuration): Pair<WeightedAStar.Node<SlidingTileState, SlidingTileAction>, Int> {
    var blank = 0
    val tiles = ArrayList<Int>()
    c.problem.forEach {
        val tileReader = Scanner(it)
        while (tileReader.hasNextInt()) {
            val nextTile = tileReader.nextInt()
            tiles.add(nextTile)
            if (nextTile == 0) {
                blank = tiles.indexOf(0)
            }
        }
    }
    val slidingTileInitialState = SlidingTileState(tiles, blank)
    val slidingTilePuzzle = SlidingTilePuzzle(c.height.toInt(), c.width.toInt(),
            c.bound.toFloat(), slidingTileInitialState)
    if (c.algorithm == "wa*") {
        return SlidingTileSolver(SlidingTilePuzzle(c.height.toInt(), c.width.toInt(),
                c.bound.toFloat(), slidingTileInitialState))
                .runWAStar(slidingTilePuzzle, slidingTileInitialState).invoke()
    } else {
        throw InputMismatchException("Expected wa* got ${c.algorithm}!")
    }
}

fun runTilesDPS(c: Configuration): Pair<DynPotentialSearch.Node<SlidingTileState, SlidingTileAction>, Int> {
    var blank = 0
    val tiles = ArrayList<Int>()
    c.problem.forEach {
        val tileReader = Scanner(it)
        while (tileReader.hasNextInt()) {
            val nextTile = tileReader.nextInt()
            tiles.add(nextTile)
            if (nextTile == 0) {
                blank = tiles.indexOf(0)
            }
        }
    }
    val slidingTileInitialState = SlidingTileState(tiles, blank)
    val slidingTilePuzzle = SlidingTilePuzzle(c.height.toInt(), c.width.toInt(),
            c.bound.toFloat(), slidingTileInitialState)
    if (c.algorithm == "dps") {
        return SlidingTileSolver(SlidingTilePuzzle(c.height.toInt(), c.width.toInt(),
                c.bound.toFloat(), slidingTileInitialState))
                .runDPS(slidingTilePuzzle, slidingTileInitialState).invoke()
    } else {
        throw InputMismatchException("Expected dps got ${c.algorithm}!")
    }
}

fun runWAStarSlidingTilePuzzle(c: Configuration) {
    displayMetaData(c)
    val executionTime = measureTimeMillis {
        val (solution, nodes) = runTilesWAStar(c)
        print("Nodes expanded: $nodes ")
        print("Solution: $solution ")
    }
    println("Execution time: ${executionTime / 1000.0}s")
}

fun runDPSSlidingTilePuzzle(c: Configuration) {
    displayMetaData(c)
    val executionTime = measureTimeMillis {
        val (solution, nodes) = runTilesDPS(c)
        print("Nodes expanded: $nodes ")
        print("Solution: $solution ")
    }
    println("Execution time: ${executionTime / 1000.0}s")
}



