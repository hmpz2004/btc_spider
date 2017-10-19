package com.ceo.reckless;

import com.ceo.reckless.entity.KEntity;
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
     * js报表不要超过166个k线数据
     * @param htmlTitle
     * @param kEntityList1
     * @param kEntityList2
     * @param outputFileName
     */
    public static void outputKLineShrinkChart(String htmlTitle, List<KEntity> kEntityList1, List<KEntity> kEntityList2, String outputFileName) {

        StringBuilder sb1 = new StringBuilder();
        int i = 0, j = 0;
        sb1.append("[\n");
        j = (kEntityList1.size() - 166) > 0 ? kEntityList1.size() - 166 : 0;
        for (j = kEntityList1.size() - 166; j < kEntityList1.size(); j++) {
            KEntity kItem = kEntityList1.get(j);
            if (i++ != 0) {
                sb1.append(",\n");
            }
            // date open high low close
            sb1.append("[" + String.valueOf(kItem.timestamp) + "," +
                    kItem.open  + "," +
                    kItem.high  + "," +
                    kItem.low  + "," +
                    kItem.close  + "," +
                    kItem.volume  +
                    "]");
        }
        sb1.append("\n]");

        StringBuilder sb2 = new StringBuilder();
        i = 0;
        sb2.append("[\n");
        j = (kEntityList2.size() - 166) > 0 ? kEntityList2.size() - 166 : 0;
        for (; j < kEntityList2.size(); j++) {
            KEntity kItem = kEntityList2.get(j);
            if (i++ != 0) {
                sb2.append(",\n");
            }
            // date open high low close
            sb2.append("[" + String.valueOf(kItem.timestamp) + "," +
                    kItem.open  + "," +
                    kItem.high  + "," +
                    kItem.low  + "," +
                    kItem.close  + "," +
                    kItem.volume  +
                    "]");
        }
        sb2.append("\n]");

        byte[] barHtmlFileBytes = FileUtils.readFileByte(new File(Env.KLINE_SHRINK_CHART_HTML_FILE_PATH));
        if (barHtmlFileBytes != null && barHtmlFileBytes.length != 0) {
            String barHtmlFileString = new String(barHtmlFileBytes);
            barHtmlFileString = barHtmlFileString.replace(Env.KLINE_DOUBLE_CHART_HTML_DATA_1_PLACE_HOLDER, sb1.toString());
            barHtmlFileString = barHtmlFileString.replace(Env.KLINE_DOUBLE_CHART_HTML_DATA_2_PLACE_HOLDER, sb2.toString());
            barHtmlFileString = barHtmlFileString.replace(Env.HTML_TITLE_PLACE_HOLDER, htmlTitle);

            // LogUtils.logDebugLine(barHtmlFileString);

            FileUtils.writeByteFile(barHtmlFileString.getBytes(), new File(outputFileName));
        } else {
            LogUtils.logDebugLine("outputKLineShrinkChart() read bar html null");
        }
    }

    public static void main(String[] args) {

        try {
            String jsonResult = SosobtcDataHelper.httpQueryKData("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_1_HOUR, 0);
            List<KEntity> list = SosobtcDataHelper.parseKlineToList(jsonResult);
            List<KEntity> slist = shrinkKLine(list);

            LogUtils.logDebugLine("list size " + list.size() + " slist size " + slist.size());

            outputKLineShrinkChart("im title", list, slist, "test_double_kline_chart.html");
        } catch (Exception e) {
            LogUtils.logError(e);
        }
    }
}
