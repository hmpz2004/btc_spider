package com.ceo.reckless.chart;

import com.ceo.reckless.Env;
import com.ceo.reckless.entity.TestTimeLineEntity;
import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.LogUtils;

import java.io.File;
import java.util.List;

public class TestChart {

    public static void outputTestTimeLineChart(List<TestTimeLineEntity> tList, String outputFileName) {

        StringBuilder sb = new StringBuilder();
        int i = 0;
        sb.append("[\n");
        for (TestTimeLineEntity item : tList) {
            if (i++ != 0) {
                sb.append(",\n");
            }
            sb.append("[" + item.timestamp + "," + item.value + "]");
        }
        sb.append("\n]");

        byte[] lineHtmlFileBytes = FileUtils.readFileByte(new File(Env.Test_BASE_LINE_CHART_HTML_FILE_PATH));
        if (lineHtmlFileBytes != null && lineHtmlFileBytes.length != 0) {
            String lineHtmlFileString = new String(lineHtmlFileBytes);
            lineHtmlFileString = lineHtmlFileString.replace(Env.LINE_CHART_HTML_DATA_PLACE_HOLDER, sb.toString());

            // LogUtils.logDebugLine(barHtmlFileString);

            FileUtils.writeByteFile(lineHtmlFileString.getBytes(), new File(outputFileName));
        } else {
            LogUtils.logDebugLine("outputTestTimeLineChart() read base line html null");
        }
    }
}
