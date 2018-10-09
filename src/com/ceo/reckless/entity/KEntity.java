package com.ceo.reckless.entity;

import com.ceo.reckless.utils.TimeUtils;

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

    public String toOutputString() {
        //<<>>
        String fmt = "%15s %5f %10f %10f %10f %10f %15f";
        return String.format(fmt, TimeUtils.convertTimeFormat1(timestamp), bull_vs_short, open, high, low, close, volume);
    }
}
