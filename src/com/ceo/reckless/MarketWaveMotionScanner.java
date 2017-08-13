package com.ceo.reckless;

import com.ceo.reckless.chart.TestChart;
import com.ceo.reckless.entity.*;
import com.ceo.reckless.helper.SosobtcDataHelper;
import com.ceo.reckless.helper.TalibHelper;
import com.ceo.reckless.strategy.DualThrust;
import com.ceo.reckless.strategy.RBreaker;
import com.ceo.reckless.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketWaveMotionScanner {

    public static boolean DEBUG_OUTPUT = true;

    public static void scanDeviation(String market, String coin, int type, long since, long endTime, String outputFileName) {
        String result = SosobtcDataHelper.httpQueryKData(market, coin, type, since);
        List<KEntity> keList = SosobtcDataHelper.parseKlineToList(result);

        // 根据截止时间筛选k线数据
        if (endTime == 0) {
            endTime = System.currentTimeMillis();
        }
        List<KEntity> targetKeList = new ArrayList<>();
        for (KEntity itemKe : keList) {
            if (itemKe.timestamp < endTime) {
                targetKeList.add(itemKe);
            }
        }

        MACDEntity me = TalibHelper.genMacd(targetKeList);
        if (me != null) {
            if (DEBUG_OUTPUT) {
                LogUtils.logDebugLine("bar array len " + me.barArray.length + " dea array len" + me.deaArray.length + " dif array len " + me.difArray.length);

                for (int i = 0; i < me.barArray.length && i < me.deaArray.length && i < me.difArray.length; i++) {
                    long time = me.timeArray[i];
                    double bar = me.barArray[i];
                    double dif = me.difArray[i];
                    double dea = me.deaArray[i];
                    LogUtils.logDebugLine("time " + time + " dif " + dif + " dea " + dea + " bar " + bar);
                }
            }

            List<TestTimeLineEntity> tList = new ArrayList<>();
            List<Long> topTimeList = new ArrayList<>();
            List<Double> topValueList = new ArrayList<>();
            List<Long> bottomTimeList = new ArrayList<>();
            List<Double> bottomValueList = new ArrayList<>();
            double targetValue;
            double maxValue = 0, minValue = 0;

            for (int i = 0; i < me.size; i++) {
                long time = me.timeArray[i];
                double bar = me.barArray[i];
                double dif = me.difArray[i];
                double dea = me.deaArray[i];

                targetValue = dea;

                TestTimeLineEntity tte = new TestTimeLineEntity();
                tte.timestamp = time;
                tte.value = targetValue;
//                if (i > 0) {
//                    // 斜率
//                    tte.value = dea - me.deaArray[i-1];
//                } else {
//                    tte.value = 0;
//                }
                tList.add(tte);

                // 更新max min值
                if (targetValue > maxValue) {
                    maxValue = targetValue;
                } else if (targetValue < minValue) {
                    minValue = targetValue;
                }

                // 挑选波峰波谷
                if (i > 0 && i < me.size - 1) {
                    if (targetValue > 0) {
                        if (targetValue > me.deaArray[i - 1] && targetValue > me.deaArray[i + 1]) {
                            topTimeList.add(time);
                            topValueList.add(targetValue);
                        }
                    } else if (targetValue < 0) {
                        if (targetValue < me.deaArray[i - 1] && targetValue < me.deaArray[i + 1]) {
                            bottomTimeList.add(time);
                            bottomValueList.add(targetValue);
                        }
                    }
                }
//                if (i > 4 && i < me.size - 5) {
//                    double leftAve = (me.deaArray[i-5] + me.deaArray[i-4] + me.deaArray[i-3] + me.deaArray[i-2] + me.deaArray[i-1]) / 5;
//                    double rightAve = (me.deaArray[i+5] + me.deaArray[i+4] + me.deaArray[i+3] + me.deaArray[i+2] + me.deaArray[i+1]) / 5;
//                    if (dea > 0) {
//                        if (dea > leftAve && dea > rightAve) {
//                            topTimeList.add(time);
//                            topValueList.add(dea);
//                        }
//                    } else {
//                        if (dea < leftAve && dea < rightAve) {
//
//                        }
//                    }
//                }
            }

            if (DEBUG_OUTPUT) {
                LogUtils.logDebugLine("=========top list=========");
                int j = 0;
                for (Long itemTime : topTimeList) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    LogUtils.logDebugLine(sdf.format(itemTime) + " " + topValueList.get(j++));
                }
                LogUtils.logDebugLine("=========btm list=========");
                j = 0;
                for (Long itemTime : bottomTimeList) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    LogUtils.logDebugLine(sdf.format(itemTime) + " " + bottomValueList.get(j++));
                }
            }

            // 将top list 与 bottom list的值改为百分比的值
            double absMaxValue = Math.abs(maxValue) > Math.abs(minValue) ? Math.abs(maxValue) : Math.abs(minValue);

            if (DEBUG_OUTPUT) {
                LogUtils.logDebugLine("=========percent top value list=========");
                LogUtils.logDebugLine("max value " + maxValue + " min value " + minValue + " max abs value" + absMaxValue);
            }
            List<Double> topPercentValueList = new ArrayList<>();
            for (int i = 0; i < topTimeList.size(); i++) {
                double percent = topValueList.get(i) / absMaxValue;
                topPercentValueList.add(percent);
                if (DEBUG_OUTPUT) {
                    LogUtils.logDebugLine(percent + "");
                }
            }
            if (DEBUG_OUTPUT) {
                LogUtils.logDebugLine("=========percent btm value list=========");
            }
            List<Double> bottomPercentValueList = new ArrayList<>();
            for (int i = 0; i < bottomTimeList.size(); i++) {
                double percent = bottomValueList.get(i) / absMaxValue;
                bottomPercentValueList.add(percent);
                if (DEBUG_OUTPUT) {
                    LogUtils.logDebugLine(percent + "");
                }
            }

            // K线数据存入map
            Map<Long, KEntity> timeKMap = new HashMap<>();
            for (KEntity item : keList) {
                timeKMap.put(item.timestamp, item);
            }

            /**
             * 判断顶背离
             * 获取最近的两个top(已经拐完的形状)是否背离
             */
            if (topTimeList.size() >= 2) {
                long timeLst = topTimeList.get(topTimeList.size() - 1);
                long timeLbo = topTimeList.get(topTimeList.size() - 2);
                double macdLstValue = topPercentValueList.get(topPercentValueList.size() - 1);
                double macdLboValue = topPercentValueList.get(topPercentValueList.size() - 2);
                double kLstValue = timeKMap.get(topTimeList.get(topTimeList.size() - 1)).high;
                double kLboValue = timeKMap.get(topTimeList.get(topTimeList.size() - 2)).high;
                if (kLstValue > kLboValue && macdLstValue <= macdLboValue) {
                    // 发生背离
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    LogUtils.logDebugLine("top deviation!!! " + sdf.format(timeLst) + " " + sdf.format(timeLbo) + " top list 最后一个value " + kLstValue + " top list倒数第二个value " + kLboValue + " macd最后一个value " + macdLstValue + " macd倒数第二个value " + macdLboValue);
                }
            } else {
                LogUtils.logDebugLine("less than 2 top value");
            }

            if (outputFileName != null) {
                TestChart.outputTestTimeLineChart(tList, outputFileName);
            }
        }
    }

    /**
     *
     * @param market
     * @param coin
     * @param type
     * @param since 如果参数为0 默认会减去8小时
     */
    public static void outputResistanceSupport(String market, String coin, int type, long since) {
//        if (since == 0) {
//            since = System.currentTimeMillis() - 10 * 60 * 60 * 1000L;
//            since /= 1000L;
//        }
        String result = SosobtcDataHelper.httpQueryKData(market, coin, type, since);
        List<KEntity> keList = SosobtcDataHelper.parseKlineToList(result);

        RBreakerEntity rbe = RBreaker.genPivotPoints(keList);
        DualThrustEntity dte = DualThrust.genDualThrustPoints(keList);
        LogUtils.logDebugLine("=============Resistance Support=============");
        LogUtils.logDebugLine("r-breaker  : R1 " + rbe.R1 + " R2 " + rbe.R2 + " R3 " + rbe.R3 + " pivot " + rbe.pivot + " S1 " + rbe.S1 + " S2 " + rbe.S2 + " S3 " + rbe.S3);
        LogUtils.logDebugLine("dual thrust: open " + dte.open + " upper " + dte.upperLane + " bottom " + dte.bottomLane);

    }
}
