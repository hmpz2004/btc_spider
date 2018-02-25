package com.ceo.reckless.entity;

public class KEntity {
    public long timestamp;
    public double open;
    public double high;
    public double low;
    public double close;
    public double volume;
    public double bull_vs_short = 0;

    @Override
    public String toString() {
        return String.valueOf(timestamp)
                .concat("\t").concat(String.valueOf(bull_vs_short))
                .concat("\t\t").concat(String.valueOf(open))
                .concat("\t").concat(String.valueOf(high))
                .concat("\t").concat(String.valueOf(low))
                .concat("\t").concat(String.valueOf(close))
                .concat("\t").concat(String.valueOf(volume));
    }
}
