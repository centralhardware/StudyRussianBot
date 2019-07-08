package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils;

import org.apache.log4j.Logger;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * generate graf
 */
public class Chart {
    private static final Logger logger = Logger.getLogger(Chart.class);
    private final Random random = new Random();

    /**
     * get file with generated graf
     *
     * @param name     name of chart
     * @param lineName line's name of chart
     * @param yData    vertical data
     * @param xData    horizontal data
     * @return File with saved graf
     */
    public File genOneLineGraf(String name, String lineName, double[] yData, double[] xData) {
        var chart = new XYChart(1920, 1080);
        chart.setTitle(name);
        chart.setXAxisTitle("X");
        chart.setXAxisTitle("Y");
        var series = chart.addSeries(lineName, xData, yData);
        series.setMarker(SeriesMarkers.NONE);
        logger.info("generate chart: " + name);
        var fileName = "./" + "chart_" + random.nextInt(1000) + ".jpg";
        try {
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.JPG);
            logger.info("save chart to file: " + fileName);
        } catch (IOException e) {
            logger.warn("chart generated error", e);
        }
        return new File(fileName);
    }
}
