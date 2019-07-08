package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class ChartUtil {
    /**
     * convert list to array
     *
     * @param data integer's list
     * @return array of int
     */
    public static double[] listToArray(List<Integer> data) {
        var res = new double[data.size()];
        for (var i = 0; i < data.size(); i++) {
            res[i] = data.get(i);
        }
        ArrayUtils.reverse(res);
        return res;
    }

    /**
     * get array with increasing from single digits
     *
     * @param count count of element in graf
     * @return horizontal data
     */
    public static double[] getXData(int count) {
        var res = new double[count];
        for (int i = 0; i < count; i++) {
            res[i] = i;
        }
        return res;
    }
}
