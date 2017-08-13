package com.ceo.reckless.strategy;

import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.entity.RBreakerEntity;
import com.ceo.reckless.utils.LogUtils;

import java.util.List;

public class RBreaker {


    public static RBreakerEntity genPivotPoints(List<KEntity> keList) {
        if (keList.size() <= 2) {
            LogUtils.logDebugLine("too few k points");
            return null;
        }

        int idx = keList.size() - 2;
        KEntity keLastInter = keList.get(idx);
        double HH = keLastInter.high;
        double LC = keLastInter.low;
        double HC = keLastInter.close;

        double pivot = (HH + HC + LC) / 3;

        double R1, R2, R3, S1, S2, S3;
        R1 = 2 * pivot - LC;//阻力1
        R2 = pivot + (HH - LC);//阻力2
        R3 = HH + 2 * (pivot - LC);//阻力3

        S1 = 2 * pivot - HH;//支撑位1
        S2 = pivot - (HH-LC);//支撑位2
        S3 = LC - 2 * (HH - pivot);//支撑位3

        RBreakerEntity rbe = new RBreakerEntity();
        rbe.R1 = R1;
        rbe.R2 = R2;
        rbe.R3 = R3;
        rbe.S1 = S1;
        rbe.S2 = S2;
        rbe.S3 = S3;
        rbe.pivot = pivot;

        return rbe;
    }
}
