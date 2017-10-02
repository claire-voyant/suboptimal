package edu.unh.cs.suboptimal.planner

import edu.unh.cs.domain.SuccessorBundle
import edu.unh.cs.tools.AdvancedPriorityQueue
import edu.unh.cs.tools.Indexable
import java.util.*

class WeightedAStar<State, Action> {
    data class Node<out State, Action>(var f: Double, var g: Double, val state: State, var action: Action) : Indexable {
        override fun toString(): String {
            return "Node(f: $f, g: $g, state: $state, action: $action"
        }

        override var index: Int = -1
    }

    class FComparator<State, Action> : Comparator<Node<State, Action>> {
        override fun compare(p0: Node<State, Action>?, p1: Node<State, Action>?): Int {
            if (p1 != null && p0 != null) {
                when {
                    p0.f < p1.f -> return -1
                    p0.f == p1.f -> return when {
                        p0.g < p0.g -> -1
                        p0.g > p0.g -> 1
                        else -> 0
                    }
                    p0.f > p1.f -> return 1
                    else -> {
                    }
                }
            }
            return 0
        }

    }

    private var nodesExpanded = 0
    private val openList = AdvancedPriorityQueue<Node<State, Action>>(10000000, FComparator())
    private val closedList = HashMap<State, Node<State, Action>>(10000000, 1.toFloat())

    fun run(initialState: State, initialAction: Action, goalCheck: (State) -> (Boolean),
            successors: (State) -> (ArrayList<SuccessorBundle<State, Action>>), heuristic: (State) -> (Double)):
            Pair<Node<State, Action>, Int> {

        val initialStateHeuristic = heuristic(initialState)
        val initialStateGValue = 0.0
        val initialStateFValue = initialStateGValue + initialStateHeuristic
        val initialNode = Node(initialStateFValue, initialStateGValue, initialState, initialAction)

        openList.add(initialNode)
        closedList[initialState] = initialNode

        tailrec fun expand(): Pair<Node<State, Action>, Int> {
            val curNode = openList.pop() ?: throw NullPointerException("Open list empty!")
            if (goalCheck(curNode.state)) {
                return Pair(curNode, nodesExpanded)
            } else {
                nodesExpanded += 1
                successors(curNode.state).forEach { (successor, action, actionCost) ->
                    val hValue = heuristic(successor)
                    val gValue = curNode.g + actionCost
                    val fValue = gValue + hValue
                    val nodeLookUp = closedList[successor]
                    if (nodeLookUp != null) {
                        if (nodeLookUp.g > gValue) {
                            nodeLookUp.f = fValue
                            nodeLookUp.action = action
                            openList.add(nodeLookUp)
                        }
                    } else {
                        val newNode = Node(fValue, gValue, successor, action)
                        openList.add(newNode)
                        closedList[successor] = newNode
                    }
                }
            }
            return expand()
        }
        return expand()
    }
}