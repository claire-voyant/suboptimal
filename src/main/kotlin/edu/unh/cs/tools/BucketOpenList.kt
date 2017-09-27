package edu.unh.cs.tools

import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

interface BucketNode {
    fun getFValue(): Double
    fun getGValue(): Double
    fun getIndex(): Int
    fun setIndex(i: Int)
    override fun toString(): String
}

class BucketOpenList<in T : BucketNode>(private var bound: Double, private var fMin: Double) {

    private class BucketOpenListException(message: String) : Exception(message)

    private data class GHPair(val g: Double, val h: Double)
    private data class Bucket<T : BucketNode>(var f: Double, var g: Double, var h: Double,
                                              val nodes: ArrayList<T>): Indexable {
        override var index: Int = -1

        override fun toString(): String {
            var stringRepresentation = ""
            stringRepresentation += "---\n"
            stringRepresentation += "f: $f | g: $g | h: $h\n"
            stringRepresentation += "BucketNodeArray ${nodes.size}\n"
            nodes.forEach { stringRepresentation += it.toString() + "\n" }
            stringRepresentation += "---\n"
            return stringRepresentation
        }
    }

    override fun toString(): String {
        var stringRepresentation = ""
        stringRepresentation += "fMin: $fMin\n"
        stringRepresentation += "OpenList size: ${openList.size}\n"
        openList.forEach { stringRepresentation += it.toString() + "\n" }
        stringRepresentation += "---\n"
        stringRepresentation += "BucketLookUp size: ${lookUpTable.size}"
        lookUpTable.forEach { stringRepresentation += it.value.toString() + "\n" }
        return stringRepresentation
    }

    private fun checkFMin(bucketNode: T): Boolean {
        return bucketNode.getFValue() < fMin
    }

    private fun checkFValues(): Boolean {
        var verified = true
        val currentFMin = fMin
        val bucketTable = lookUpTable
        fun checkCompareF(pair: GHPair, bucket: Bucket<T>) {
            val bucketF = pair.g + pair.h
            val nodes = bucket.nodes
            nodes.forEach { node ->
                if (node.getFValue() != bucketF) {
                    verified = false
                    println(this.toString())
                }
                if (node.getFValue() < currentFMin) {
                    verified = false
                    println(this.toString())
                }
            }
        }
        bucketTable.forEach { checkCompareF(it.key, it.value) }
        return verified
    }

    private fun verifyNodes(f: (T) -> (Boolean)): Boolean {
        var verified = true
        val bucketTable = lookUpTable
        fun checkCompareF(pair: GHPair, bucket: Bucket<T>) {
            val nodes = bucket.nodes
            val checkNode = {node: T -> f(node)}
            nodes.map(checkNode).forEach { if(!it) verified = false }
        }
        bucketTable.forEach{ checkCompareF(it.key, it.value) }
        return verified
    }

    fun verify() {
        var checkStructure = verifyNodes { T -> checkFMin(T) }
        if (!checkStructure) throw BucketOpenListException("checkFMin failed!")
        checkStructure = checkFValues()
        if (!checkStructure) throw BucketOpenListException("verifying bucket match nodes failed!")
    }

    private class potentialComparator<T>(var bound: Double, var fMin: Double) : Comparator<T> {
        override fun compare(p0: T?, p1: T?): Int {
            return if (p0 != null && p1 != null && p0 is BucketNode && p1 is BucketNode) {
                val p0Potential = ((bound * fMin) - p0.getGValue()) / (p0.getFValue() - p0.getGValue())
                val p1Potential = ((bound * fMin) - p1.getGValue()) / (p1.getFValue() - p1.getGValue())
                return if (p1Potential < p0Potential) {
                    -1
                } else {
                    1
                }
            }
            else {
                0
            }
        }

    }

    fun create(initialBound: Double): BucketOpenList<T> {
        return BucketOpenList(initialBound, Double.MAX_VALUE)
    }

    fun insert(element: T) {
        // minimum f value changes
        if(element.getFValue() < this.fMin) {
            fMin = element.getFValue()
            openList.reorder(potentialComparator(bound, fMin))
            val newGHPair = GHPair(element.getGValue(), element.getFValue() - element.getGValue())
            if (lookUpTable.containsKey(newGHPair)) {
                val knownEmptyBucket = lookUpTable[newGHPair]
                // there is an empty bucket to place the element into
                if(knownEmptyBucket != null) {
                    knownEmptyBucket.nodes.add(element)
                    knownEmptyBucket.nodes.indexOf(element)?.let { element.setIndex(it) }
                    knownEmptyBucket.let { openList.add(it) }
                } else { // that empty bucket does not exist yet so make it
                    val newBucket = Bucket<T>(element.getFValue(), element.getGValue(),
                            element.getFValue() - element.getGValue(), ArrayList())
                    lookUpTable[newGHPair] = newBucket
                    openList.add(newBucket)
                }
            }
        } else {
            val newGHPair = GHPair(element.getGValue(), element.getFValue() - element.getGValue())
            if (lookUpTable.containsKey(newGHPair)) { // node was in the lookup

            } else { // node wasn't in the lookup

            }
        }
    }

    private val openList = AdvancedPriorityQueue<Bucket<T>>(10000000, potentialComparator(bound, fMin))
    private val lookUpTable = HashMap<GHPair, Bucket<T>>(10000000, 1.toFloat())


}