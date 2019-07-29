package ru.alexeyFedechkin.znatoki.studyRussianBot.Utils

import org.junit.Assert.assertTrue
import org.junit.Test

class ChartTest {
    @Test
    fun genOneLineGraf() {
        val xData = doubleArrayOf(1.0, 10.0)
        val yData = doubleArrayOf(1.0, 10.0)
        val file = Chart.genOneLineGraf("test", "line", yData, xData)
        assertTrue(file.isFile)
        file.delete()
    }
}