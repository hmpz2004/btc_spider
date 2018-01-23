package com.ceo.reckless.chart;

import com.ceo.reckless.Env;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.LogUtils;

import java.io.File;
import java.util.List;

public class KLineChart {

    /**
     * js报表不要超过166个k线数据
     *
     */
    public static void outputKLineChart(String htmlTitle, List<KEntity> kEntityList1, String outputFileName) {

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

        byte[] barHtmlFileBytes = FileUtils.readFileByte(new File(Env.KLINE_CHART_HTML_FILE_PATH));
        if (barHtmlFileBytes != null && barHtmlFileBytes.length != 0) {
            String barHtmlFileString = new String(barHtmlFileBytes);
            barHtmlFileString = barHtmlFileString.replace(Env.KLINE_DOUBLE_CHART_HTML_DATA_PLACE_HOLDER, sb1.toString());
            barHtmlFileString = barHtmlFileString.replace(Env.HTML_TITLE_PLACE_HOLDER, htmlTitle);

            // LogUtils.logDebugLine(barHtmlFileString);

            FileUtils.writeByteFile(barHtmlFileString.getBytes(), new File(outputFileName));
        } else {
            LogUtils.logDebugLine("outputKLineShrinkChart() read bar html null");
        }
    }

    /**
     * js报表不要超过150个k线数据
     * k线个数貌似跟网页或者js报表的宽度有关
     * @param htmlTitle
     * @param kEntityList1
     * @param kEntityList2
     * @param outputFileName
     */
    public static void outputKLineShrinkChart(String htmlTitle, List<KEntity> kEntityList1, List<KEntity> kEntityList2, String outputFileName) {

        int LIMIT = 150;

        StringBuilder sb1 = new StringBuilder();
        int i = 0, j = 0;
        sb1.append("[\n");
        j = (kEntityList1.size() - LIMIT) > 0 ? kEntityList1.size() - LIMIT : 0;
        for (j = kEntityList1.size() - LIMIT; j < kEntityList1.size(); j++) {
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
        j = (kEntityList2.size() - LIMIT) > 0 ? kEntityList2.size() - LIMIT : 0;
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
}
