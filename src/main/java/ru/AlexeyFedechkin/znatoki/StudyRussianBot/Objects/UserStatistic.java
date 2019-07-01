package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * store all metrics of bot
 */
public class UserStatistic {
    private final ArrayList<Integer> totalSend = new ArrayList<>();
    private final ArrayList<Integer> totalReceived = new ArrayList<>();
    private HashMap<Long, ArrayList<Integer>> userSend = new HashMap<>();
    private HashMap<Long, ArrayList<Integer>> userReceived = new HashMap<>();
    private int totalCountOfSend;
    private int totalCountReceived;
    private final HashMap<Long, Integer> userCountSent = new HashMap<>();
    private final HashMap<Long, Integer> userReceivedSent = new HashMap<>();

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
     * @return horizontal data
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStatistic that = (UserStatistic) o;
        return totalCountOfSend == that.totalCountOfSend &&
                totalCountReceived == that.totalCountReceived &&
                Objects.equals(totalSend, that.totalSend) &&
                Objects.equals(totalReceived, that.totalReceived) &&
                Objects.equals(userSend, that.userSend) &&
                Objects.equals(userReceived, that.userReceived) &&
                Objects.equals(userCountSent, that.userCountSent) &&
                Objects.equals(userReceivedSent, that.userReceivedSent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalSend, totalReceived, userSend, userReceived, totalCountOfSend, totalCountReceived, userCountSent, userReceivedSent);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public String toString() {
        return "UserStatistic{" +
                "totalSend=" + totalSend +
                ", totalReceived=" + totalReceived +
                ", userSend=" + userSend +
                ", userReceived=" + userReceived +
                ", totalCountOfSend=" + totalCountOfSend +
                ", totalCountReceived=" + totalCountReceived +
                ", userCountSent=" + userCountSent +
                ", userReceivedSent=" + userReceivedSent +
                '}';
    }
}
