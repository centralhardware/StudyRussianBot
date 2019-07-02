package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class ChartTest {

    @Test
    public void genOneLineGraf() {
        double[] xData = new double[]{01, 10};
        double[] yData = new double[]{01, 10};
        File file = new Chart().genOneLineGraf("test", "line", xData, yData);
        assertTrue(file.isFile());
        file.delete();
    }
}