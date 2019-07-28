package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils

import org.apache.commons.lang3.ArrayUtils

object ChartUtil {
    /**
     * convert list to array
     *
     * @param data integer's list
     * @return array of int
     */
    fun listToArray(data: List<Int>): DoubleArray {
        val res = DoubleArray(data.size)
        for (i in data.indices) {
            res[i] = data[i].toDouble()
        }
        ArrayUtils.reverse(res)
        return res
    }

    /**
     * get array with increasing from single digits
     *
     * @param count count of element in graf
     * @return horizontal data
     */
    fun getXData(count: Int): DoubleArray {
        val res = DoubleArray(count)
        for (i in 0 until count) {
            res[i] = i.toDouble()
        }
        return res
    }
}