package edu.unh.cs.suboptimal.planner

import edu.unh.cs.domain.SuccessorBundle
import edu.unh.cs.tools.BucketNode
import edu.unh.cs.tools.BucketOpenList
import edu.unh.cs.tools.Indexable

class DynPotentialSearch<State, Action>(bound: Double) {
    data class Node<out State, Action>(private var f: Double, var g: Double, val state: State, var action: Action) :
            Indexable, BucketNode {
        override fun getHValue(): Double {
            return f - g
        }

        override fun getGValue(): Double {
            return g
        }

        override fun getFValue(): Double {
            return f
        }

        override var index: Int = -1
    }

    private var nodesExpanded = 0
    private val openList = BucketOpenList<Node<State, Action>>(bound, Double.MAX_VALUE)
    private val closedList = HashMap<State, Node<State, Action>>(10000000, 1.toFloat())

    private fun addToExisting(g: Double, f: Double, state: State, action: Action) {
        val newNode = Node(f, g, state, action)
        openList.add(newNode)
        closedList[state] = newNode
    }

    private fun evaluateSuccessor(heuristic: (State) -> Double, successor: State, action: Action, actionCost: Double,
                                  currentNode: Node<State, Action>) {
        if (openList.checkFMin()) {
            openList.fixOpenList()
        }
        val successorH = heuristic(successor)
        val successorG = currentNode.g + actionCost
        val successorF = successorG + successorH
        val lookUpNode = closedList[successor]
        if (lookUpNode != null) {
            if (lookUpNode.g > successorG) {
                val replaceNode = Node(successorF, successorG, successor, action)
                openList.replace(lookUpNode, replaceNode)
            }
        } else {
            addToExisting(successorG, successorF, successor, action)
        }
    }

    private fun expandCurrentNode(goalCheck: (State) -> Boolean, heuristic: (State) -> Double,
                                  successors: (State) -> ArrayList<SuccessorBundle<State, Action>>):
            Pair<Node<State, Action>, Int> {
        tailrec fun expand(): Pair<Node<State, Action>, Int> {
            nodesExpanded++
//            println("###START###")
//            println(openList)
//            println("###FINISH###")
            val currentNode = openList.chooseNode() ?: throw NullPointerException("Open list empty!")
            if ((openList.isNotEmpty()) && (goalCheck(currentNode.state!!))) {
                return Pair(currentNode, nodesExpanded)
            } else {
                successors(currentNode.state!!).forEach { (successor, action, actionCost) ->
                    evaluateSuccessor(heuristic, successor, action, actionCost, currentNode)
                }
            }
            return expand()
        }
        return expand()
    }

    fun run(initialState: State, initialAction: Action, goalCheck: (State) -> (Boolean),
            successors: (State) -> (ArrayList<SuccessorBundle<State, Action>>), heuristic: (State) -> (Double)):
            Pair<Node<State, Action>, Int> {

        val initialStateHeuristic = heuristic(initialState)
        val initialStateGValue = 0.0
        val initialStateFValue = initialStateGValue + initialStateHeuristic
        val initialNode = Node(initialStateFValue, initialStateGValue, initialState, initialAction)

        openList.add(initialNode)
        closedList[initialState] = initialNode

        return expandCurrentNode(goalCheck,heuristic,successors)
    }
}


