package com.ceo.reckless.strategy;

import com.ceo.reckless.entity.DualThrustEntity;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.utils.LogUtils;

import java.util.List;

public class DualThrust {

    public static int UNIT_NUM = 20;

    public static DualThrustEntity genDualThrustPoints(List<KEntity> keList) {
        if (keList.size() <= UNIT_NUM) {
            LogUtils.logDebugLine("too few k point data");
            return null;
        }

        // N日high的最高 close的最高 low的最低 close的最低
        double maxNHigh = 0, maxNClose = 0, minNLow = 999999999, minNClose = 999999999;

        for (int i = keList.size() - 1 - UNIT_NUM; i >= 0 && i < keList.size(); i ++) {
            KEntity ke = keList.get(i);
            if (ke.high > maxNHigh) {
                maxNHigh = ke.high;
            }
            if (ke.close > maxNClose) {
                maxNClose = ke.close;
            }
            if (ke.low < minNLow) {
                minNLow = ke.low;
            }
            if (ke.close < minNClose) {
                minNClose = ke.close;
            }
        }
        double rangeMax = Math.abs(maxNHigh - maxNClose);
        double rangeMin = Math.abs(minNClose - minNLow);

        double range = rangeMax > rangeMin ? rangeMax : rangeMin;

        DualThrustEntity dte = new DualThrustEntity();
        KEntity keLast = keList.get(keList.size() - 1);
        dte.open = keLast.open;
        dte.upperLane = keLast.open + range;
        dte.bottomLane = keLast.open - range;

        return dte;
    }
}
