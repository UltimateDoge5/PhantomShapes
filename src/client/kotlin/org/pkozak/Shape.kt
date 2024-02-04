package org.pkozak

import net.minecraft.util.math.Vec3d
import org.pkozak.PhantomShapesClient.logger
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max

abstract class Shape {
    abstract var color: Color
    abstract var pos: Vec3d
    abstract val name: String
    var filled = false
    var enabled = true
    var drawOnlyEdge = false

    abstract fun render(): MutableSet<Vec3d>

//    fun makeSlices(): MutableSet<Slice> {
//        val boundingBox = getBoundingBox()
//        val positions = render()
//        val slices = mutableSetOf<Slice>()
//
//        // Go through the bounding box and add slices
//        for (x in 0 until boundingBox.x.toInt()) {
//            for (y in 0 until boundingBox.y.toInt()) {
//                for (z in 0 until boundingBox.z.toInt()) {
//                    if (positions.contains(Vec3d(pos.x + x, pos.y + y, pos.z + z))) {
//                        val pos = Vec3d(pos.x + x, pos.y + y, pos.z + z)
//                        val (side, amount) = inspectBlockSideColumns(positions, pos)
//                        logger.info("Pos: $pos, Side: $side, Amount: $amount")
//                        logger.info("Positions before purge: ${positions.size}")
//                        val dimensions = when (side) {
//                            0 -> {
//                                //Remove the amount of blocks from the x dimension
//                                for (i in 0 until amount) {
//                                    positions.remove(Vec3d(pos.x + i, pos.y, pos.z))
//                                }
//                                Vec3d(amount.toDouble(), boundingBox.y, boundingBox.z)
//                            }
//                            1 -> {
//                                //Remove the amount of blocks from the x dimension
//                                for (i in 0 until amount) {
//                                    positions.remove(Vec3d(pos.x - i, pos.y, pos.z))
//                                }
//                                Vec3d(amount.toDouble(), boundingBox.y, boundingBox.z)
//                            }
//                            2 -> {
//                                //Remove the amount of blocks from the y dimension
//                                for (i in 0 until amount) {
//                                    positions.remove(Vec3d(pos.x, pos.y + i, pos.z))
//                                }
//                                Vec3d(boundingBox.x, amount.toDouble(), boundingBox.z)
//                            }
//                            3 -> {
//                                //Remove the amount of blocks from the y dimension
//                                for (i in 0 until amount) {
//                                    positions.remove(Vec3d(pos.x, pos.y - i, pos.z))
//                                }
//                                Vec3d(boundingBox.x, amount.toDouble(), boundingBox.z)
//                            }
//                            4 -> {
//                                //Remove the amount of blocks from the z dimension
//                                for (i in 0 until amount) {
//                                    positions.remove(Vec3d(pos.x, pos.y, pos.z + i))
//                                }
//                                Vec3d(boundingBox.x, boundingBox.y, amount.toDouble())
//                            }
//                            5 -> {
//                                //Remove the amount of blocks from the z dimension
//                                for (i in 0 until amount) {
//                                    positions.remove(Vec3d(pos.x, pos.y, pos.z - i))
//                                }
//                                Vec3d(boundingBox.x, boundingBox.y, amount.toDouble())
//                            }
//                            else -> Vec3d(0.0, 0.0, 0.0)
//                        }
//
//                        logger.info("Positions after purge: ${positions.size}")
//
//                        if (amount == 0) continue
//                        slices.add(Slice(pos, dimensions))
//                    }
//                }
//            }
//        }
//
//        return slices
//    }
//
//    private fun inspectBlockSideColumns(positions: MutableSet<Vec3d>, pos: Vec3d): Pair<Int, Int> {
//        val sideColumns = arrayOf(0, 0, 0, 0, 0, 0)
//        // Check every side until we don't find a block
//
//        // Check the positive x side (right)
//        for (x in 0 until getBoundingBox().x.toInt()) {
//            if (!positions.contains(Vec3d(pos.x + x, pos.y, pos.z))) break
//            sideColumns[0] += 1
//        }
//
//        // Check the negative x side (left)
//        for (x in 0 until getBoundingBox().x.toInt()) {
//            if (!positions.contains(Vec3d(pos.x - x, pos.y, pos.z))) break
//            sideColumns[1] += 1
//        }
//
//        // Check the positive y side (up)
//        for (y in 0 until getBoundingBox().y.toInt()) {
//            if (!positions.contains(Vec3d(pos.x, pos.y + y, pos.z))) break
//            sideColumns[2] += 1
//        }
//
//        // Check the negative y side (down)
//        for (y in 0 until getBoundingBox().y.toInt()) {
//            if (!positions.contains(Vec3d(pos.x, pos.y - y, pos.z))) break
//            sideColumns[3] += 1
//        }
//
//        // Check the positive z side (front)
//        for (z in 0 until getBoundingBox().z.toInt()) {
//            if (!positions.contains(Vec3d(pos.x, pos.y, pos.z + z))) break
//            sideColumns[4] += 1
//        }
//
//        // Check the negative z side (back)
//        for (z in 0 until getBoundingBox().z.toInt()) {
//            if (!positions.contains(Vec3d(pos.x, pos.y, pos.z - z))) break
//            sideColumns[5] += 1
//        }
//
//        //Return the index of the side with the most blocks and the amount of blocks
//        val max = sideColumns.max()
//        return sideColumns.indexOf(max) to max
//    }
}

//class Slice(val position: Vec3d, val dimensions: Vec3d)
