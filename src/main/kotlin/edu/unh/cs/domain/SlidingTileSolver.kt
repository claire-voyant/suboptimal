package edu.unh.cs.domain

import edu.unh.cs.suboptimal.planner.DynPotentialSearch
import edu.unh.cs.suboptimal.planner.WeightedAStar

data class SlidingTileState(val tiles: ArrayList<Int>, var blank: Int)
enum class SlidingTileAction { NORTH, SOUTH, EAST, WEST, START }

class SlidingTilePuzzle(val height: Int, val width: Int, val bound: Float, val initialState: SlidingTileState) {
    private val fifteenSolution = arrayListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

    fun isGoal(state: SlidingTileState): Boolean {
        return state.tiles == fifteenSolution
    }

    fun heuristicDps(state: SlidingTileState): Double {
        return heuristic(state) / bound
    }

    fun heuristic(state: SlidingTileState): Double {
        var heuristicValue = 0.0
        state.tiles.forEachIndexed { i, tile ->
            var at = i
            val isCorrect = (tile == fifteenSolution[i])
            if (!isCorrect) {
                while (!(inSameRange(at, tile))) {
                    if (tile < at) {
                        at -= 4
                        heuristicValue += 1.0
                    } else {
                        at += 4
                        heuristicValue += 1
                    }
                }
            }
            heuristicValue += Math.abs(tile - at).toDouble()
        }
        return heuristicValue * bound
    }

    private fun inSameRange(at: Int, be: Int): Boolean {
        return if (at in 0..3 && be in 0..3) {
            true
        } else if (at in 4..7 && be in 4..7) {
            true
        } else if (at in 8..11 && be in 8..11) {
            true
        } else at in 12..15 && be in 12..15
    }

    fun successors(state: SlidingTileState): ArrayList<SuccessorBundle<SlidingTileState, SlidingTileAction>> {
        data class constructedAction(val allow: Boolean, val action: SlidingTileAction, val successor: SlidingTileState)

        val constructedActions = ArrayList<constructedAction>()
        SlidingTileAction.values().forEach {
            when (it) {
                SlidingTileAction.NORTH -> constructedActions.add(constructedAction(state.blank >= width,
                        it, generateSuccessor((state.blank >= width), state, (state.blank - width))))
                SlidingTileAction.EAST -> constructedActions.add(constructedAction((state.blank % width) < (width - 1),
                        it, generateSuccessor((state.blank % width) < (width - 1), state, state.blank + 1)))
                SlidingTileAction.WEST -> constructedActions.add(constructedAction((state.blank % width) > 0,
                        it, generateSuccessor((state.blank % width) > 0, state, state.blank - 1)))
                SlidingTileAction.SOUTH -> constructedActions.add(constructedAction((state.blank < (15 - width)),
                        it, generateSuccessor((state.blank < (15 - width)), state, state.blank + width)))
                else -> {
                }
            }
        }
        val successors = ArrayList<SuccessorBundle<SlidingTileState, SlidingTileAction>>()
        constructedActions.forEach { (allow, action, successor) ->
            if (allow) {
                successors.add(SuccessorBundle(successor, action, 1.0))
            }
        }
        return successors
    }

    private fun generateSuccessor(allow: Boolean, state: SlidingTileState, blank: Int): SlidingTileState {
        return if (allow) {
            val boardCopy = ArrayList<Int>()
            state.tiles.forEach { boardCopy.add(it) }
            val newSuccessor = SlidingTileState(boardCopy, state.blank)
            newSuccessor.tiles[state.blank] = state.tiles[blank]
            newSuccessor.blank = blank
            newSuccessor.tiles[blank] = 0
            newSuccessor
        } else {
            state
        }
    }
}

class SlidingTileSolver(val tilePuzzle: SlidingTilePuzzle) {
    fun runWAStar(tilePuzzle: SlidingTilePuzzle, initialState: SlidingTileState) = {
        println("SlidingTile.WeightedAStar")
        val isGoal: (SlidingTileState) -> (Boolean) = { slidingTileState ->
            tilePuzzle.isGoal(slidingTileState)
        }
        val successorFunction: (SlidingTileState) -> ArrayList<SuccessorBundle<SlidingTileState, SlidingTileAction>>
                = { state ->
            tilePuzzle.successors(state)
        }
        val heuristicFunction: (SlidingTileState) -> Double = { state ->
            tilePuzzle.heuristic(state)
        }
        WeightedAStar<SlidingTileState, SlidingTileAction>().run(initialState, SlidingTileAction.START,
                isGoal, successorFunction, heuristicFunction)
    }

    fun runDPS(tilePuzzle: SlidingTilePuzzle, initialState: SlidingTileState) = {
        println("SlidingTile.DynPotentialSearch")
        val isGoal: (SlidingTileState) -> (Boolean) = { slidingTileState ->
            tilePuzzle.isGoal(slidingTileState)
        }
        val successorFunction: (SlidingTileState) -> ArrayList<SuccessorBundle<SlidingTileState, SlidingTileAction>>
                = { state ->
            tilePuzzle.successors(state)
        }
        val heuristicFunction: (SlidingTileState) -> Double = { state ->
            tilePuzzle.heuristicDps(state)
        }
        DynPotentialSearch<SlidingTileState, SlidingTileAction>(tilePuzzle.bound.toDouble()).run(initialState, SlidingTileAction.START,
                isGoal, successorFunction, heuristicFunction)
    }
}