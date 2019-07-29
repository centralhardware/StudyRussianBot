package ru.alexeyFedechkin.znatoki.studyRussianBot.Utils

import mu.KotlinLogging
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.XYChart
import org.knowm.xchart.style.markers.SeriesMarkers
import java.io.File
import java.io.IOException
import java.util.*

object Chart {
    private val logger = KotlinLogging.logger {  }
    private val random = Random()

    /**
     * get file with generated graf
     *
     * @param name     name of chart
     * @param lineName line's name of chart
     * @param yData    vertical data
     * @param xData    horizontal data
     * @return File with saved graf
     */
    fun genOneLineGraf(name: String, lineName: String, yData: DoubleArray, xData: DoubleArray): File {
        val chart = XYChart(1920, 1080)
        chart.title = name
        chart.xAxisTitle = "X"
        chart.xAxisTitle = "Y"
        val series = chart.addSeries(lineName, xData, yData)
        series.marker = SeriesMarkers.NONE
        logger.info("generate chart: $name")
        val fileName = "./" + "chart_" + random.nextInt(1000) + ".jpg"
        try {
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.JPG)
            logger.info("save chart to file: $fileName")
        } catch (e: IOException) {
            logger.warn("chart generated error", e)
        }

        return File(fileName)
    }
}