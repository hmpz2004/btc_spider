package com.ceo.reckless.entity;

public class MACDEntity {
    public long[] timeArray;
    public double[] difArray;
    public double[] deaArray;
    public double[] barArray;
    public int emptyNum;
    public int size;
    /**
     * 记录idx从0到大的顺序也是时间从小到大的顺序
     * 末尾0的个数为emptyNum
     */
}
