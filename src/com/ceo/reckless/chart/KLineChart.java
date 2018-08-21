package com.ceo.reckless.chart;

import com.ceo.reckless.Env;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.entity.LinkEntity;
import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.LogUtils;

import java.io.File;
import java.util.List;

public class KLineChart {

    public static void outputPolylineChart(List<LinkEntity> linkEntityList, String outputFileName) {

        boolean DEBUG_POLY_LINE = true;

        // 拼接成二维数组
        StringBuilder matrixDataStringBuilder = new StringBuilder();
        matrixDataStringBuilder.append("[");

        int idx = 0;
        for (LinkEntity itemLink : linkEntityList) {
            long timeToWrite = 0;
            double valueToWrite = 0;
            if (idx == 0) {
                timeToWrite = itemLink.first.timestamp;
//                if (itemLink.type == LinkEntity.TYPE_UP) {
//                    // 向上一笔,价格取low
//                    valueToWrite = itemLink.first.low;
//                } else {
//                    // 向下一笔,价格取high
//                    valueToWrite = itemLink.first.high;
//                }
                valueToWrite = itemLink.getFirstValue();
                matrixDataStringBuilder.append("[");
                matrixDataStringBuilder.append(timeToWrite);
                matrixDataStringBuilder.append(",");
                matrixDataStringBuilder.append(valueToWrite);
                matrixDataStringBuilder.append("],\n");
            }

            timeToWrite = itemLink.second.timestamp;
//            if (itemLink.type == LinkEntity.TYPE_UP) {
//                // 向上一笔,价格取low
//                valueToWrite = itemLink.second.low;
//            } else {
//                // 向下一笔,价格取high
//                valueToWrite = itemLink.second.high;
//            }
            valueToWrite = itemLink.getSecondValue();

            matrixDataStringBuilder.append("[");
            matrixDataStringBuilder.append(timeToWrite);
            matrixDataStringBuilder.append(",");
            matrixDataStringBuilder.append(valueToWrite);
            matrixDataStringBuilder.append("]");

            if (idx != linkEntityList.size() - 1) {
                // 不是最后一个
                matrixDataStringBuilder.append(",");
            }
            matrixDataStringBuilder.append("\n");

            idx++;
        }

        matrixDataStringBuilder.append("]");

        String matrixString = matrixDataStringBuilder.toString();
        if (DEBUG_POLY_LINE) {
            LogUtils.logDebugLine(matrixString);
        }

        byte[] polyLineHtmlFileBytes = FileUtils.readFileByte(new File(Env.CANVAS_POLY_LINE_HTML_FILE_PATH));
        if (polyLineHtmlFileBytes != null && polyLineHtmlFileBytes.length != 0) {
            String polyLineHtmlFileString = new String(polyLineHtmlFileBytes);

            polyLineHtmlFileString = polyLineHtmlFileString.replace(Env.CANVAS_LINE_COLOR_PLACE_HOLDER, Env.CANVAS_COLOR_GREEN);
            polyLineHtmlFileString = polyLineHtmlFileString.replace(Env.DATA_MATRIX_PLACE_HOLDER, matrixString);

            FileUtils.writeByteFile(polyLineHtmlFileString.getBytes(), new File(outputFileName));
        } else {
            LogUtils.logDebugLine("outputKLineShrinkChart() read bar html null");
        }
    }

    /**
     * js报表不要超过166个k线数据
     *
     */
    public static void outputKLineChart(String htmlTitle, List<KEntity> kEntityList1, String outputFileName) {

        int LIMIT = kEntityList1.size();

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

        int LIMIT = 500;

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
            sb1.append("[" + String.valueOf(kItem.timestamp*1000) + "," +// jschar的时间戳要到毫秒
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
            sb2.append("[" + String.valueOf(kItem.timestamp*1000) + "," +
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
