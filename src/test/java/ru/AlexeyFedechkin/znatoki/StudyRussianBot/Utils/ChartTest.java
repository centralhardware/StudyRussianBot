package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

@SuppressWarnings("HardCodedStringLiteral")
public class ChartTest {

    @Test
    public void genOneLineGraf() {
        double[] xData = new double[]{1, 10};
        double[] yData = new double[]{1, 10};
        File file = new Chart().genOneLineGraf("test", "line", yData, xData);
        assertTrue(file.isFile());
        file.delete();
    }
}