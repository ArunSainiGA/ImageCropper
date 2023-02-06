package com.asp.imsgepickerplayground.ui.main

import kotlin.math.min

class SizeResolver {
    fun resizeForSmallestSizeScale(eWidth: Int, eHeight: Int, iWidth: Int, iHeight: Int): Pair<Int, Int> {
        var rWidth = 0
        var rHeight = 0

        if (eWidth >= eHeight) {
            val eRatio = eWidth / eHeight
            when {
                iWidth >= iHeight -> {
                    rHeight = iHeight
                    rWidth = rHeight * eRatio
                }
                else -> {
                    rWidth = iWidth
                    rHeight = rWidth / eRatio
                }
            }
        } else {
            val eRatio = eHeight / eWidth
            when {
                iWidth >= iHeight -> {
                    rHeight = iHeight
                    rWidth = rHeight / eRatio
                }
                else -> {
                    rWidth = iWidth
                    rHeight = rWidth * eRatio
                }
            }
        }

        return Pair(rWidth, rHeight)
    }

    fun resizeForMultipleSizeScale(eWidth: Int, eHeight: Int, iWidth: Int, iHeight: Int): Pair<Int, Int> {
//        var rWidth = 0
//        var rHeight = 0
//
//        // Add logic for cases when the height or width is already less than the expected one
//        if(iWidth <= eWidth || iHeight <= eHeight) {
//            return resizeForSmallestSizeScale(eWidth, eHeight, iWidth, iHeight)
//        }

        val wd = iWidth / eWidth
        val hd = iHeight / eHeight

        if(wd <= 0 || hd <= 0) {
            return resizeForSmallestSizeScale(eWidth, eHeight, iWidth, iHeight)
        }

//        while (rWidth + eWidth <= iWidth && rHeight + eHeight <= iHeight) {
//            rWidth += eWidth
//            rHeight += eHeight
//        }
//
//        return Pair(rWidth, rHeight)
        var divider = min(wd, hd)
        return Pair(eWidth * divider, eHeight * divider)
    }
}