package com.ceo.reckless.strategy;

import com.ceo.reckless.entity.DeviationEntity;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.entity.MACDEntity;
import com.ceo.reckless.entity.TestTimeLineEntity;
import com.ceo.reckless.helper.TalibHelper;
import com.ceo.reckless.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deviation {

    private static final boolean DEBUG_OUTPUT = false;

    // 距离现在多久时间范围内发生的背离算作有效
    private static long VALID_DEVIATION_INTERVAL = 30 * 24 * 3600 * 1000L;

    private static long TOP_DEVIATION_INTERVAL = 30 * 24 * 3600 * 1000L;

    private static long BOTTOM_DEVIATION_INTERVAL = 30 * 24 * 3600 * 1000L;

    public static final String KEY_TOP_DEVIATION = "top_deviation";

    public static final String KEY_BOTTOM_DEVIATION = "bottom_deviation";

    public static final int TYPE_LINE = 1;

    public static final int TYPE_BAR = 2;

    public static final int TYPE_TOP = 1;

    public static final int TYPE_BOTTOM = 2;

    public static boolean isDeviation(int typeLineBar, int typeTopBottom, List<KEntity> keList, DeviationEntity de) {
        return isDeviation(typeLineBar, typeTopBottom, keList, de, false);
    }

    public static boolean isDeviation(int typeLineBar, int typeTopBottom, List<KEntity> keList) {
        return isDeviation(typeLineBar, typeTopBottom, keList, null);
    }

    /**
     * @param typeLineBar
     * @param typeTopBottom
     * @param keList
     * @param de 发生背离的时间距离当前时间的时间戳的差
     * @param isLastPointDeviation true : 当前走势的最后一个点也算进去 false : 查看已经出现拐点的背离情况
     * @return
     */
    public static boolean isDeviation(int typeLineBar, int typeTopBottom, List<KEntity> keList, DeviationEntity de, boolean isLastPointDeviation) {

        if (de == null) {
            de = new DeviationEntity();
        }

        // K线数据存入map
        Map<Long, KEntity> timeKMap = new HashMap<>();
        for (KEntity item : keList) {
            // timestamp要先转为毫秒级别
            if (item.timestamp < 10000000000L) {
                item.timestamp *= 1000L;
            }
            timeKMap.put(item.timestamp, item);
        }

        MACDEntity me = TalibHelper.genMacd(keList);
        if (me != null) {
            if (DEBUG_OUTPUT) {
                LogUtils.logDebugLine("bar array len " + me.barArray.length + " dea array len" + me.deaArray.length + " dif array len " + me.difArray.length);

                for (int i = 0; i < me.barArray.length && i < me.deaArray.length && i < me.difArray.length; i++) {
                    long time = me.timeArray[i];
                    double bar = me.barArray[i];
                    double dif = me.difArray[i];
                    double dea = me.deaArray[i];

                    if (time == 0) {
                        continue;
                    }

                    LogUtils.logDebugLine("time " + time + " dif " + dif + " dea " + dea + " bar " + bar);
                }
            }

            // 遍历macd
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

                double beforeTargetValue = 0;
                double afterTargetValue = 0;

                targetValue = dea;
                if (typeLineBar == TYPE_LINE) {
                    targetValue = dea;
                } else if (typeLineBar == TYPE_BAR) {
                    targetValue = bar;
                }

                TestTimeLineEntity tte = new TestTimeLineEntity();
                tte.timestamp = time;
                tte.value = targetValue;

                tList.add(tte);

                // 更新max min值
                if (targetValue > maxValue) {
                    maxValue = targetValue;
                } else if (targetValue < minValue) {
                    minValue = targetValue;
                }

                // 挑选波峰波谷(掐头去尾)
                if (i > 0 && i < me.size - 1) {

                    if (typeLineBar == TYPE_LINE) {
                        beforeTargetValue = me.deaArray[i - 1];
                        afterTargetValue = me.deaArray[i + 1];
                    } else if (typeLineBar == TYPE_BAR) {
                        beforeTargetValue = me.barArray[i - 1];
                        afterTargetValue = me.barArray[i + 1];
                    }

                    if (targetValue > 0) {
                        if (beforeTargetValue < targetValue && targetValue > afterTargetValue) {
                            topTimeList.add(time);
                            topValueList.add(targetValue);
                        }
                    } else if (targetValue < 0) {
                        if (beforeTargetValue > targetValue && targetValue < afterTargetValue) {
                            bottomTimeList.add(time);
                            bottomValueList.add(targetValue);
                        }
                    }
                }
                // 判断top、bottom list里是否要加入最后一个数据
                if (isLastPointDeviation && i == me.size - 1) {
                    if (typeLineBar == TYPE_LINE) {
                        beforeTargetValue = me.deaArray[i - 1];
                    } else if (typeLineBar == TYPE_BAR) {
                        beforeTargetValue = me.barArray[i - 1];
                    }

                    if (targetValue > 0) {
                        if (targetValue > beforeTargetValue) {
                            topTimeList.add(time);
                            topValueList.add(targetValue);
                        }
                    } else if (targetValue < 0) {
                        if (targetValue < beforeTargetValue) {
                            bottomTimeList.add(time);
                            bottomValueList.add(targetValue);
                        }
                    }
                }
            }

            if (DEBUG_OUTPUT) {
                LogUtils.logDebugLine("=========top list=========");
                int j = 0;
                for (Long itemTime : topTimeList) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    LogUtils.logDebugLine(itemTime + " " + sdf.format(itemTime) + " target_value : " + topValueList.get(j++));
                }
                LogUtils.logDebugLine("=========btm list=========");
                j = 0;
                for (Long itemTime : bottomTimeList) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    LogUtils.logDebugLine(itemTime + " " + sdf.format(itemTime) + " target_value : " + bottomValueList.get(j++));
                }
            }

            // 将top list 与 bottom list的值改为百分比的值
            double absMaxValue = Math.abs(maxValue) > Math.abs(minValue) ? Math.abs(maxValue) : Math.abs(minValue);

            if (DEBUG_OUTPUT) {
                LogUtils.logDebugLine("=========percent top value list=========");
                LogUtils.logDebugLine("max_value " + maxValue + " min_value " + minValue + " max_abs_value" + absMaxValue);
            }
            List<Double> topPercentValueList = new ArrayList<>();
            for (int i = 0; i < topTimeList.size(); i++) {
                long itemTime = topTimeList.get(i);
                double itemValue = topValueList.get(i);
                double percent = itemValue / absMaxValue;
                topPercentValueList.add(percent);
                if (DEBUG_OUTPUT) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    double kValue = timeKMap.get(itemTime).high;
                    LogUtils.logDebugLine("time " + itemTime + " " + sdf.format(itemTime) + " k_value " + kValue + " macd_target_value " + itemValue + " percent " + percent);
                }
            }
            if (DEBUG_OUTPUT) {
                LogUtils.logDebugLine("=========percent btm value list=========");
            }
            List<Double> bottomPercentValueList = new ArrayList<>();
            for (int i = 0; i < bottomTimeList.size(); i++) {
                long itemTime = bottomTimeList.get(i);
                double itemValue = bottomValueList.get(i);
                double percent = itemValue / absMaxValue;
                bottomPercentValueList.add(percent);
                if (DEBUG_OUTPUT) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    double kValue = timeKMap.get(itemTime).low;
                    LogUtils.logDebugLine("time " + itemTime + " " + sdf.format(itemTime) + " k_value " + kValue + " macd_target_value " + itemValue + " percent " + percent);
                }
            }

            if (typeTopBottom == TYPE_TOP) {
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
                        if (DEBUG_OUTPUT) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            LogUtils.logDebugLine(sdf.format(timeLbo) + " --> " + sdf.format(timeLst));
                            LogUtils.logDebugLine("lbo high " + kLboValue + " macd " + macdLboValue);
                            LogUtils.logDebugLine("lst high " + kLstValue + " macd " + macdLstValue);
                            LogUtils.logDebugLine("top deviation!!!");
                        }

                        de.timeDiffFromCur = new Long(timeDiffCurLast);

                        return true;
                    }
                } else {
                    if (DEBUG_OUTPUT) {
                        LogUtils.logDebugLine("less than 2 top value");
                    }
                }
            } else if (typeTopBottom == TYPE_BOTTOM) {

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
                            if (DEBUG_OUTPUT) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                LogUtils.logDebugLine(sdf.format(timeLbo) + " --> " + sdf.format(timeLst));
                                LogUtils.logDebugLine("lbo high " + kLboValue + " macd " + macdLboPercentValue);
                                LogUtils.logDebugLine("lst high " + kLstValue + " macd " + macdLstPercentValue);
                                LogUtils.logDebugLine("bottom deviation!!!");
                            }

                            de.timeDiffFromCur = new Long(timeDiffCurLast);

                            return true;
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
                                if (DEBUG_OUTPUT) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                    LogUtils.logDebugLine(sdf.format(timeLbo) + " --> " + sdf.format(timeLst));
                                    LogUtils.logDebugLine("lbo high " + kLboValue + " macd " + macdLboPercentValue);
                                    LogUtils.logDebugLine("lst high " + kLstValue + " macd " + macdLstPercentValue);
                                    LogUtils.logDebugLine("bottom deviation!!!");
                                }

                                de.timeDiffFromCur = new Long(timeDiffCurLast);

                                return true;
                            }
                        }
                    }

                } else {
                    if (DEBUG_OUTPUT) {
                        LogUtils.logDebugLine("less than 2 bottom value");
                    }
                }
            }
        }

        return false;
    }
}
