package com.ceo.reckless.entity;

import com.ceo.reckless.utils.TimeUtils;

public class LinkEntity {

    public static final int TYPE_UP = 1;
    public static final int TYPE_DOWN = 2;

    public KEntity first = null;

    public KEntity second = null;

    public int type = 0;

    public LinkEntity(KEntity a, KEntity b) {
        first = a;
        second = b;
        // 单纯内部高低判断容易出错,link的上升or下降方向由外部判断
//        if (first.high > second.high) {
//            type = TYPE_DOWN;
//        } else if (first.low < second.low) {
//            type = TYPE_UP;
//        }
    }

    public boolean isSameStart(LinkEntity tgt) {
        return tgt.first.equals(first);
    }

    public double getFirstValue() {
        if (type == LinkEntity.TYPE_UP) {
            return first.low;
        } else {
            return first.high;
        }
    }

    public double getSecondValue() {
        if (type == LinkEntity.TYPE_UP) {
            return second.high;
        } else {
            return second.low;
        }
    }

    public String toOutputString() {
        double start = 0.0;
        double end = 0.0;
        long startTime = first.timestamp;;
        long endTime = second.timestamp;
        LinkEntity item = this;
        if (item.type == LinkEntity.TYPE_UP) {
            start = item.first.low;
            end = item.second.high;
        } else if (item.type == LinkEntity.TYPE_DOWN) {
            start = item.first.high;
            end = item.second.low;
        }

        return TimeUtils.convertTimeFormat1(startTime) + "-" + start + "|" + TimeUtils.convertTimeFormat1(endTime) + "-" + end;
    }
}
