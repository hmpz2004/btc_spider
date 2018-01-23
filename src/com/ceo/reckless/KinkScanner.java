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



    public static void main(String[] args) {

        try {
//            String jsonResult = SosobtcDataHelper.httpQueryKData("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_1_HOUR, 0);
//            List<KEntity> list = SosobtcDataHelper.parseKlineToList(jsonResult);
//            List<KEntity> slist = shrinkKLine(list);
//
//            LogUtils.logDebugLine("list size " + list.size() + " slist size " + slist.size());
//
//            KLineChart.outputKLineShrinkChart("im title", list, slist, "test_double_kline_chart.html");


            String market = "okex";
            String targetCoin = "ethquarter";
            String srcCoin = "usd";
            String periodType = "1h";
            long since = 0;
            List<KEntity> list = AicoinDataHelper.requestKLine(market, targetCoin, srcCoin, periodType, 0);
            List<KEntity> slist = shrinkKLine(list);

            LogUtils.logDebugLine("list size " + list.size() + " slist size " + slist.size());

            KLineChart.outputKLineShrinkChart("im title", list, slist, "ethquarter_shrink_kline_chart.html");
        } catch (Exception e) {
            LogUtils.logError(e);
        }
    }
}
