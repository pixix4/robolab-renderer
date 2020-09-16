package de.robolab.client.app.model.base

import kotlin.math.max

interface IInfoBarContent {
}

fun adjustBoxList(boxSizeList: MutableList<Double>, targetListSize: Double, minBoxSize: Double, dynamicBox: Int?) {
    for ((box, size) in boxSizeList.withIndex()) {
        if (size < minBoxSize) {
            boxSizeList[box] = minBoxSize
        }
    }

    var currentListSize = boxSizeList.sum()
    if (currentListSize == targetListSize) return

    if (dynamicBox != null && dynamicBox >= 0 && dynamicBox < boxSizeList.size) {
        boxSizeList[dynamicBox] = max(minBoxSize, boxSizeList[dynamicBox] + targetListSize - currentListSize)
    }

    for ((box, size) in boxSizeList.withIndex().reversed()) {
        currentListSize = boxSizeList.sum()
        if (currentListSize == targetListSize) return
        boxSizeList[box] = max(minBoxSize, size + targetListSize - currentListSize)
    }
}

fun resizeBoxListBox(boxSizeList: MutableList<Double>, targetListSize: Double, minBoxSize: Double, dynamicBox: Int?, borderIndex: Int, targetPosition: Double) {
    val currentPosition = boxSizeList.take(borderIndex + 1).sum()
    boxSizeList[borderIndex] = boxSizeList[borderIndex] - (currentPosition - targetPosition)
    boxSizeList[borderIndex + 1] = boxSizeList[borderIndex + 1] + (currentPosition - targetPosition)

    adjustBoxList(boxSizeList, targetListSize, minBoxSize, dynamicBox)
}
