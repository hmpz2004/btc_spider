package com.ceo.reckless;

import com.ceo.reckless.chart.TestChart;
import com.ceo.reckless.entity.*;
import com.ceo.reckless.helper.SosobtcDataHelper;
import com.ceo.reckless.helper.TalibHelper;
import com.ceo.reckless.strategy.DualThrust;
import com.ceo.reckless.strategy.RBreaker;
import com.ceo.reckless.utils.LogUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketWaveMotionScanner {

    private static boolean DEBUG_OUTPUT = false;

    private static long VALID_DEVIATION_INTERVAL = 4 * 3600 * 1000L;

    private static long TOP_DEVIATION_INTERVAL = 24 * 3600 * 1000L;

    private static long BOTTOM_DEVIATION_INTERVAL = 24 * 3600 * 1000L;

    public static final String KEY_TOP_DEVIATION = "top_deviation";

    public static final String KEY_BOTTOM_DEVIATION = "bottom_deviation";

    /**
     * 路径用于输出测试报表
     * @param market
     * @param coin
     * @param type
     * @param since
     * @param endTime
     * @param outputFileName
     */
    public static void scanDeviation(String market, String coin, int type, long since, long endTime, String outputFileName, Map<String, String> deviationResultMap) {
        String result = SosobtcDataHelper.httpQueryKData(market, coin, type, since);
        List<KEntity> keList = SosobtcDataHelper.parseKlineToList(result);
        if (keList == null || keList.size() == 0) {
            return;
        }

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
                long timeDiff = Math.abs(timeLst - timeLbo);
                long timeDiffCurLast = Math.abs(System.currentTimeMillis() - timeLst);
                if (kLstValue > kLboValue && macdLstValue <= macdLboValue && timeDiff < TOP_DEVIATION_INTERVAL && timeDiffCurLast < VALID_DEVIATION_INTERVAL) {
                    // 发生背离
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    LogUtils.logDebugLine(">>" + market + " " + coin + "<<");
                    LogUtils.logDebugLine(sdf.format(timeLbo) + " --> " + sdf.format(timeLst));
                    LogUtils.logDebugLine("lbo high " + kLboValue + " macd " + macdLboValue);
                    LogUtils.logDebugLine("lst high " + kLstValue + " macd " + macdLstValue);
                    LogUtils.logDebugLine("top deviation!!!");

                    if (deviationResultMap != null) {
                        String deviationValue = null;
                        if (deviationResultMap.containsKey(KEY_TOP_DEVIATION)) {
                            deviationValue = deviationResultMap.get(KEY_TOP_DEVIATION);
                        }
                        deviationValue = (deviationValue == null ? "" : deviationValue);
                        deviationResultMap.put(KEY_TOP_DEVIATION, deviationValue + " " + coin);
                    }
                } else {
                    LogUtils.logDebugLine("nothing");
                }
            } else {
                LogUtils.logDebugLine("less than 2 top value");
            }

            /**
             * 判断底背离
             */
            if (bottomTimeList.size() >= 2) {
                long timeLst = bottomTimeList.get(bottomTimeList.size() - 1);
                long timeLbo = bottomTimeList.get(bottomTimeList.size() - 2);
                double macdLstPercentValue = bottomPercentValueList.get(bottomPercentValueList.size() - 1);
                double macdLboPercentValue = bottomPercentValueList.get(bottomPercentValueList.size() - 2);
                double kLstValue = timeKMap.get(bottomTimeList.get(bottomTimeList.size() - 1)).low;
                double kLboValue = timeKMap.get(bottomTimeList.get(bottomTimeList.size() - 2)).low;
                long timeDiff = Math.abs(timeLst - timeLbo);
                long timeDiffCurLast = Math.abs(System.currentTimeMillis() - timeLst);

                if (timeDiff < BOTTOM_DEVIATION_INTERVAL && timeDiffCurLast < VALID_DEVIATION_INTERVAL) {
                    if (kLstValue < kLboValue && macdLstPercentValue >= macdLboPercentValue) {
                        // 发生背离(高低->低高背离)
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        LogUtils.logDebugLine(">>" + market + " " + coin + "<<");
                        LogUtils.logDebugLine(sdf.format(timeLbo) + " --> " + sdf.format(timeLst));
                        LogUtils.logDebugLine("lbo high " + kLboValue + " macd " + macdLboPercentValue);
                        LogUtils.logDebugLine("lst high " + kLstValue + " macd " + macdLstPercentValue);
                        LogUtils.logDebugLine("bottom deviation!!!");

                        if (deviationResultMap != null) {
                            String deviationValue = null;
                            if (deviationResultMap.containsKey(KEY_BOTTOM_DEVIATION)) {
                                deviationValue = deviationResultMap.get(KEY_BOTTOM_DEVIATION);
                            }
                            deviationValue = (deviationValue == null ? "" : deviationValue);
                            deviationResultMap.put(KEY_BOTTOM_DEVIATION, deviationValue + " " + coin);
                        }
                    } else if (kLstValue > kLboValue && macdLstPercentValue > macdLboPercentValue) {
                        // 倒序遍历得到两个地点之间的最高k线high
                        double tmpMax = 0;
                        double tmpMin = 9999999;
                        int idx = keList.size() - 1;
                        for (; idx > 0; idx--) {
                            KEntity ke = keList.get(idx);
                            if (ke.timestamp < timeLbo) {
                                break;
                            }
                            if (ke.high > tmpMax) {
                                tmpMax = ke.high;
                            } else if (ke.low < tmpMin) {
                                tmpMin = ke.low;
                            }
                        }

                        double kLstPercent = (tmpMax - kLstValue) / (tmpMax - tmpMin);
                        double kLboPercent = (tmpMax - kLboValue) / (tmpMax - tmpMin);
                        double kPercentDiff = Math.abs(kLstPercent - kLboPercent);
                        double macdPercentDiff = Math.abs(macdLstPercentValue - macdLboPercentValue);
                        if (kPercentDiff > macdPercentDiff * 1.2) {
                            // 发生背离(高低->高低背离,趋势相同k线变化大macd变化小)
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            LogUtils.logDebugLine(">>" + market + " " + coin + "<<");
                            LogUtils.logDebugLine(sdf.format(timeLbo) + " --> " + sdf.format(timeLst));
                            LogUtils.logDebugLine("lbo high " + kLboValue + " macd " + macdLboPercentValue);
                            LogUtils.logDebugLine("lst high " + kLstValue + " macd " + macdLstPercentValue);
                            LogUtils.logDebugLine("bottom deviation!!!");

                            if (deviationResultMap != null) {
                                String deviationValue = null;
                                if (deviationResultMap.containsKey(KEY_BOTTOM_DEVIATION)) {
                                    deviationValue = deviationResultMap.get(KEY_BOTTOM_DEVIATION);
                                }
                                deviationValue = (deviationValue == null ? "" : deviationValue);
                                deviationResultMap.put(KEY_BOTTOM_DEVIATION, deviationValue + " " + coin + "(weak)");
                            }
                        }
                    }
                }

            } else {
                LogUtils.logDebugLine("less than 2 bottom value");
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
        DecimalFormat df = new DecimalFormat("#.00");
        // LogUtils.logDebugLine("r-breaker  : R1 " + df.format(rbe.R1) + " R2 " + df.format(rbe.R2) + " R3 " + df.format(rbe.R3) + " pivot " + df.format(rbe.pivot) + " S1 " + df.format(rbe.S1) + " S2 " + df.format(rbe.S2) + " S3 " + df.format(rbe.S3));
        LogUtils.logDebugLine("r-breaker :");
        LogUtils.logDebugLine("   R3     " + df.format(rbe.R3));
        LogUtils.logDebugLine("   R2     " + df.format(rbe.R2));
        LogUtils.logDebugLine("   R1     " + df.format(rbe.R1));
        LogUtils.logDebugLine("   pivot  " + df.format(rbe.pivot));
        LogUtils.logDebugLine("   S1     " + df.format(rbe.S1));
        LogUtils.logDebugLine("   S2     " + df.format(rbe.S2));
        LogUtils.logDebugLine("   S3     " + df.format(rbe.S3));

        // LogUtils.logDebugLine("dual thrust : open " + dte.open + " upper " + dte.upperLane + " bottom " + dte.bottomLane);
        LogUtils.logDebugLine("dual thrust :");
        LogUtils.logDebugLine("   open   " + dte.open);
        LogUtils.logDebugLine("   upper  " + dte.upperLane);
        LogUtils.logDebugLine("   bottom " + dte.bottomLane);

    }
}
