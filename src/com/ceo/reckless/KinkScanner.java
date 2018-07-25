package com.ceo.reckless;

import com.ceo.reckless.chart.KLineChart;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.helper.AicoinDataHelper;
import com.ceo.reckless.helper.SosobtcDataHelper;
import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KinkScanner {

    public static List<KEntity> shrinkKLine(List<KEntity> kEntityList) {
        List<KEntity> shrinkedKEntityList = new ArrayList<>();

        KEntity keItem = new KEntity();
        KEntity keNext = new KEntity();
        boolean isLastUp = true;
        shrinkedKEntityList.add(kEntityList.get(0));
        for (int n = 1; n < kEntityList.size(); n++) {
            keItem = shrinkedKEntityList.get(shrinkedKEntityList.size() - 1);
            keNext = kEntityList.get(n);

            if ((keNext.high > keItem.high && keNext.low > keItem.low) || (keNext.high < keItem.high && keNext.low < keItem.low)) {
                // 下一跟k比当前高 or 下一跟k比当前低
                // 不需要与下一根合并,进入list
                shrinkedKEntityList.add(keNext);

                isLastUp = keNext.high > keItem.high && keNext.low > keItem.low;

            } else if ((keNext.high > keItem.high && keNext.low < keItem.low) || (keNext.high < keItem.high && keNext.low > keItem.low)) {
                // 下一根k比当前长 or 下一根k比当前短 -> 包含关系
                KEntity k = new KEntity();
                k.timestamp = keNext.timestamp;
                if (isLastUp) {
                    // 上升处理
                    k.high = keNext.high > keItem.high ? keNext.high : keItem.high; // 高点取高
                    k.low = keNext.low < keItem.low ? keItem.low : keNext.low;      // 低点取高
                } else {
                    // 下降处理
                    k.low = keNext.low < keItem.low ? keNext.low : keItem.low;      // 低点取低
                    k.high = keNext.high > keItem.high ? keItem.high : keNext.high; // 高点取低
                }
                k.volume = keNext.volume + keItem.volume;
                k.open = (k.high + k.low) / 2;
                k.close = (k.high + k.low) / 2;

                shrinkedKEntityList.remove(shrinkedKEntityList.size() - 1);
                shrinkedKEntityList.add(k);
            }
        }

        return shrinkedKEntityList;
    }

    /**
     * 标记顶分型底分型的顶和底
     * 输入:已经合并够的K list
     */
    public static List<KEntity> markTopBottomShape(List<KEntity> shrinkedKEntityList) {

        int MIN_DISTANCE = 4;

        int TYPE_TOP = 1;
        int TYPE_BTM = 2;
        int[] markTypeArray = new int[shrinkedKEntityList.size()];

        int lastTopIdx = -1;
        int lastBtmIdx = -1;

        KEntity keLast = new KEntity();
        KEntity keItem = new KEntity();
        KEntity keNext = new KEntity();
        for (int n = 2; n < shrinkedKEntityList.size(); n++) {
            int curIdx = n - 1;
            keLast = shrinkedKEntityList.get(curIdx - 1);
            keItem = shrinkedKEntityList.get(curIdx);
            keNext = shrinkedKEntityList.get(curIdx + 1);

            // 顶分型判断
            // 高点最高 低点最高
            if ((keItem.high > keLast.high && keItem.high > keNext.high) &&
                    (keItem.low > keLast.low && keItem.low > keNext.low)) {

                boolean operFlag = false;

                // 走一串复杂的判断来决定是否采纳当前的分型
                if (lastTopIdx == -1 && lastBtmIdx == -1) {
                    // 当前没有顶分型,没有底分型
                    operFlag = true;
                } else if (lastBtmIdx == -1) {
                    // 当前有一个顶分型,还没有底分型
                    // 处理:不算做顶分型
                } else if (lastTopIdx == -1) {
                    // 当前有一个底分型,还没有顶分型
                    if (curIdx - lastBtmIdx >= MIN_DISTANCE) {
                        // 距离上一个底分型距离符合条件
                        operFlag = true;
                    } else {
                        // 距离不符合
                    }
                } else {
                    // 当前有顶分型,有底分型
                    if (lastTopIdx > lastBtmIdx) {
                        // 顶分型离当前距离近
                        // 处理:不算做顶分型
                    }
                    if (lastBtmIdx > lastTopIdx) {
                        // 底分型离当前距离近
                        if (curIdx - lastBtmIdx >= MIN_DISTANCE) {
                            // 距离上一个底分型距离符合条件
                            operFlag = true;
                        }
                    }
                }

                if (operFlag) {
                    markTypeArray[curIdx] = TYPE_TOP;
                    lastTopIdx = curIdx;
                    // 修改形状为上丁字形
                    keItem.open = keItem.high;
                    keItem.close = keItem.high;
                }
            }

            // 底分型判断
            // 低点最低 高点最低
            if ((keItem.low < keLast.low && keItem.low < keNext.low) &&
                    (keItem.high < keLast.high && keItem.high < keNext.high)) {

                boolean operFlag = false;

                // 走一串复杂的判断来决定是否采纳当前的分型
                if (lastTopIdx == -1 && lastBtmIdx == -1) {
                    // 当前没有顶分型,没有底分型
                    operFlag = true;
                } else if (lastBtmIdx == -1) {
                    // 当前有一个顶分型,还没有底分型
                    if (curIdx - lastTopIdx >= MIN_DISTANCE) {
                        // 距离上一个顶分型距离符合条件
                        operFlag = true;
                    } else {
                        // 距离不符合条件
                    }
                } else if (lastTopIdx == -1) {
                    // 当前有一个底分型,还没有顶分型
                    // 处理:不算做顶分型
                } else {
                    // 当前有顶分型,有底分型
                    // 当前有顶分型,有底分型
                    if (lastTopIdx > lastBtmIdx) {
                        // 顶分型离当前距离近
                        if (curIdx - lastTopIdx >= MIN_DISTANCE) {
                            // 距离上一个顶分型距离符合条件
                            operFlag = true;
                        } else {
                            // 距离不符合条件
                        }
                    }
                    if (lastBtmIdx > lastTopIdx) {
                        // 底分型离当前距离近
                        // 处理:不算做顶分型
                    }
                }

                if (operFlag) {
                    markTypeArray[curIdx] = TYPE_BTM;
                    lastBtmIdx = curIdx;
                    // 修改形状为下丁字形
                    keItem.open = keItem.low;
                    keItem.close = keItem.low;
                }
            }
        }
        return shrinkedKEntityList;
    }

    public static void main(String[] args) {

        try {
//            String jsonResult = SosobtcDataHelper.httpQueryKData("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_1_HOUR, 0);
//            List<KEntity> list = SosobtcDataHelper.parseKlineToList(jsonResult);
//            List<KEntity> slist = shrinkKLine(list);
//
//            LogUtils.logDebugLine("list size " + list.size() + " slist size " + slist.size());
//
//            KLineChart.outputKLineShrinkChart("im title", list, slist, "test_double_kline_chart.html");


//            String market = "okex";
//            String targetCoin = "ethquarter";
//            String srcCoin = "usd";
//            String periodType = "1h";

            String market = "okcoinfutures";
            String targetCoin = "btcquarter";
            String srcCoin = "usd";
            String periodType = "30m";
            long since = 0;
            List<KEntity> list = AicoinDataHelper.requestKLine(market, targetCoin, srcCoin, periodType, 0);
            List<KEntity> slist = shrinkKLine(list);

            LogUtils.logDebugLine("list size " + list.size() + " slist size " + slist.size());

            KLineChart.outputKLineShrinkChart("im title", list, slist, "btcquarter_shrink_kline_chart.html");
        } catch (Exception e) {
            LogUtils.logError(e);
        }
    }
}
