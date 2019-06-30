package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * store all metrics of bot
 */
public class UserStatistic {
    private ArrayList<Integer> totalSend = new ArrayList<>();
    private ArrayList<Integer> totalReceived = new ArrayList<>();
    private HashMap<Long, ArrayList<Integer>> userSend = new HashMap<>();
    private HashMap<Long, ArrayList<Integer>> userReceived = new HashMap<>();
    private int totalCountOfSend;
    private int totalCountReceived;
    private HashMap<Long, Integer> userCountSent = new HashMap<>();
    private HashMap<Long, Integer> userReceivedSent = new HashMap<>();

    /**
     * convert list to array
     *
     * @param data integer's list
     * @return array of int
     */
    public double[] listToArray(ArrayList<Integer> data) {
        var res = new double[data.size()];
        for (var i = 0; i < data.size(); i++) {
            res[i] = data.get(i);
        }
        ArrayUtils.reverse(res);
        return res;
    }

    /**
     * @param count count of element in graf
     * @return
     */
    public double[] getXdata(int count) {
        var res = new double[count];
        for (int i = 0; i < count; i++) {
            res[i] = i;
        }
        return res;
    }

    public ArrayList<Integer> getTotalSend() {
        return totalSend;
    }

    public ArrayList<Integer> getTotalReceived() {
        return totalReceived;
    }

    public HashMap<Long, ArrayList<Integer>> getUserSend() {
        return userSend;
    }

    public void setUserSend(HashMap<Long, ArrayList<Integer>> userSend) {
        this.userSend = userSend;
    }

    public HashMap<Long, ArrayList<Integer>> getUserReceived() {
        return userReceived;
    }

    public void setUserReceived(HashMap<Long, ArrayList<Integer>> userReceived) {
        this.userReceived = userReceived;
    }

    public HashMap<Long, Integer> getUserCountSent() {
        return userCountSent;
    }

    public HashMap<Long, Integer> getUserReceivedSent() {
        return userReceivedSent;
    }

    public int getTotalCountOfSend() {
        return totalCountOfSend;
    }

    public void setTotalCountOfSend(int totalCountOfSend) {
        this.totalCountOfSend = totalCountOfSend;
    }

    public int getTotalCountReceived() {
        return totalCountReceived;
    }

    public void setTotalCountReceived(int totalCountReceived) {
        this.totalCountReceived = totalCountReceived;
    }
}
