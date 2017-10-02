package edu.unh.cs.tools

import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

interface BucketNode {
    fun getFValue(): Double
    fun getGValue(): Double
    fun getHValue(): Double
    override fun toString(): String
}

class BucketOpenList<T : BucketNode>(private var bound: Double, private var fMin: Double) {

    private class BucketOpenListException(message: String) : Exception(message)

    private data class GHPair(val g: Double, val h: Double)
    private data class Bucket<T : BucketNode>(var f: Double, var g: Double, var h: Double,
                                              val nodes: ArrayList<T>) : Indexable {
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

//    private fun checkFMin(bucketNode: T): Boolean {
//        return bucketNode.getFValue() < fMin
//    }
//
//    private fun checkFValues(): Boolean {
//        var verified = true
//        val currentFMin = fMin
//        val bucketTable = lookUpTable
//        fun checkCompareF(pair: GHPair, bucket: Bucket<T>) {
//            val bucketF = pair.g + pair.h
//            val nodes = bucket.nodes
//            nodes.forEach { node ->
//                if (node.getFValue() != bucketF) {
//                    verified = false
//                    println(this.toString())
//                }
//                if (node.getFValue() < currentFMin) {
//                    verified = false
//                    println(this.toString())
//                }
//            }
//        }
//        bucketTable.forEach { checkCompareF(it.key, it.value) }
//        return verified
//    }
//
//    private fun verifyNodes(f: (T) -> (Boolean)): Boolean {
//        var verified = true
//        val bucketTable = lookUpTable
//        fun checkCompareF(bucket: Bucket<T>) {
//            val nodes = bucket.nodes
//            val checkNode = {node: T -> f(node)}
//            nodes.map(checkNode).forEach { if(!it) verified = false }
//        }
//        bucketTable.forEach{ checkCompareF(it.value) }
//        return verified
//    }
//
//    fun verify() {
//        var checkStructure = verifyNodes { T -> checkFMin(T) }
//        if (!checkStructure) throw BucketOpenListException("checkFMin failed!")
//        checkStructure = checkFValues()
//        if (!checkStructure) throw BucketOpenListException("verifying bucket match nodes failed!")
//    }

    private class PotentialComparator<T>(var bound: Double, var fMin: Double) : Comparator<T> {
        override fun compare(leftBucket: T?, rightBucket: T?): Int {
            if (leftBucket != null && rightBucket != null) {
                if (leftBucket is Bucket<*> && rightBucket is Bucket<*>) {
                    val leftBucketPotential = ((bound * fMin) - leftBucket.g) / (leftBucket.h)
                    val rightBucketPotential = ((bound * fMin) - rightBucket.g) / (rightBucket.h)
//                    println("p0Potential $leftBucketPotential || p1Potential $rightBucketPotential")
                    return Math.signum(rightBucketPotential - leftBucketPotential).toInt()
                }
            }
            return 0
        }
    }

    private fun insert(element: T) {
        // minimum f value changes
        if (element.getFValue() < this.fMin) {
            fMin = element.getFValue()
            openList.reorder(PotentialComparator(bound, fMin))
            val newGHPair = GHPair(element.getGValue(), element.getFValue() - element.getGValue())
            if (lookUpTable.containsKey(newGHPair)) {
                val knownEmptyBucket = lookUpTable[newGHPair]
                // there is an empty bucket to place the element into
                if (knownEmptyBucket != null) {
                    knownEmptyBucket.nodes.add(element)
                    openList.add(knownEmptyBucket)
                } else { // that empty bucket does not exist yet so make it
                    val newBucket = Bucket<T>(element.getFValue(), element.getGValue(),
                            element.getFValue() - element.getGValue(), ArrayList())
                    newBucket.nodes.add(element)
                    lookUpTable[newGHPair] = newBucket
                    openList.add(newBucket)
                }
            } else {
                val newBucket = Bucket<T>(element.getFValue(), element.getGValue(),
                        element.getFValue() - element.getGValue(), ArrayList())
                newBucket.nodes.add(element)
                lookUpTable[newGHPair] = newBucket
                openList.add(newBucket)
            }
        } else {
            val newGHPair = GHPair(element.getGValue(), element.getFValue() - element.getGValue())
            if (lookUpTable.containsKey(newGHPair)) { // bucket was in the lookup table
                val knownNonEmptyBucket = lookUpTable[newGHPair]
                if (knownNonEmptyBucket != null) {
                    knownNonEmptyBucket.nodes.add(element)
                    openList.add(knownNonEmptyBucket)
                }
            } else { // bucket was not in the lookup table
                val newBucket = Bucket<T>(element.getFValue(), element.getGValue(),
                        element.getFValue() - element.getGValue(), ArrayList())
                newBucket.nodes.add(element)
                lookUpTable[newGHPair] = newBucket
                openList.add(newBucket)
            }
        }
    }

    fun add(element: T) {
        insert(element)
    }

    fun checkFMin(): Boolean {
        var fMinOk = Double.MAX_VALUE
        val currentFMin = fMin
        fun checkFMinValue(bucket: Bucket<T>) {
            if (bucket.f < fMinOk) {
                fMinOk = bucket.f
            }
        }
        openList.forEach { bucket -> checkFMinValue(bucket) }
        if (fMinOk < currentFMin) {
            fMin = fMinOk
            return false
        }
        return true
    }

    fun fixOpenList() {
        openList.reorder(PotentialComparator(bound, fMin))
        openList.forEach { bucket ->
            if (bucket.nodes.isEmpty()) {
                openList.remove(bucket)
            }
        }
    }

    fun isNotEmpty(): Boolean {
        return (openList.size > 0)
    }

    private fun validateFMin(): Double {
        var validFMin = Double.MAX_VALUE
        fun checkFMinValue(bucket: Bucket<T>) {
            if (bucket.f < validFMin) {
                validFMin = bucket.f
            }
        }
        openList.forEach { bucket -> checkFMinValue(bucket) }
        return validFMin
    }

    fun replace(element: T, replacement: T) {
        val elementPair = GHPair(element.getGValue(), element.getFValue() - element.getGValue())
        val bucketLookUp = lookUpTable[elementPair]
        if (bucketLookUp != null) {
            if (bucketLookUp.nodes.size > 0) {
                bucketLookUp.nodes.remove(element)
                insert(replacement)
            }
        } else {
            insert(element)
        }
    }

    private fun remove(): T? {
        val remainingElements = openList.size
        if (remainingElements == 0) {
            throw BucketOpenListException("BucketOpenList.remove nothing left to remove!")
        }
        val topOfOpenList = openList.peek()
        if (topOfOpenList?.nodes?.size == 0) {
            // no nodes left we have to update fMin
            val topCheck = validateFMin()
            while (openList.peek()?.nodes?.size == 0) {
                openList.pop() // remove the empty bucket from open
            }
            val nextBucket = openList.peek()
            val nNode = nextBucket?.nodes?.get(0)
            nextBucket?.nodes?.remove(nNode)
            if (topCheck < fMin) {
                fMin = topCheck
            }
            openList.reorder(PotentialComparator(bound, fMin))
            return nNode
        } else {
            return if (topOfOpenList?.nodes?.size == 1) {
                val nNode = topOfOpenList.nodes[0]
                topOfOpenList.nodes.remove(nNode)
                openList.pop()
                val topCheck = validateFMin()
                if (topCheck < fMin) {
                    fMin = topCheck
                }
                openList.reorder(PotentialComparator(bound, fMin))
                nNode
            } else {
                val nNode = topOfOpenList?.nodes?.get(topOfOpenList.nodes.size - 1)
                topOfOpenList?.nodes?.remove(nNode)
                nNode
            }
        }
    }

    fun chooseNode(): T? {
        return remove()
    }


    private val openList = AdvancedPriorityQueue<Bucket<T>>(10000000, PotentialComparator(bound, fMin))
    private val lookUpTable = HashMap<GHPair, Bucket<T>>(10000000, 1.toFloat())


}