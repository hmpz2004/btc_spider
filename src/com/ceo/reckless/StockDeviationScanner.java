package com.ceo.reckless;

import com.ceo.reckless.entity.DeviationEntity;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.entity.SHStockRankEntity;
import com.ceo.reckless.helper.XueqiuDataHelper;
import com.ceo.reckless.strategy.Deviation;
import com.ceo.reckless.utils.LogUtils;
import com.ceo.reckless.utils.TimeUtils;

import java.util.*;

public class StockDeviationScanner {

    // 用于筛选股票涨跌幅范围 [x%, y%]
    private static double CONST_INCREASE_DROP_PERCENT_UPPER_BAND = -2;
    private static double CONST_INCREASE_DROP_PERCENT_BOTTOM_BAND = -10;

    private static final String KEY_TOP_LINE_DEVIATION = "TOP_LINE_DEVIATION";
    private static final String KEY_TOP_BAR_DEVIATION = "TOP_BAR_DEVIATION";
    private static final String KEY_BOTTOM_LINE_DEVIATION = "BOTTOM_LINE_DEVIATION";
    private static final String KEY_BOTTOM_BAR_DEVIATION = "BOTTOM_BAR_DEVIATION";

    private static final String KEY_TOP_LINE_PRE_DEVIATION = "TOP_LINE_PRE_DEVIATION";
    private static final String KEY_TOP_BAR_PRE_DEVIATION = "TOP_BAR_PRE_DEVIATION";
    private static final String KEY_BOTTOM_LINE_PRE_DEVIATION = "BOTTOM_LINE_PRE_DEVIATION";
    private static final String KEY_BOTTOM_BAR_PRE_DEVIATION = "BOTTOM_BAR_PRE_DEVIATION";

    // 设置背离发生时间距离现在多久,就不展示了
    private static final long OUTPUT_FILTER_INTERVAL = 7 * 24 * 3600 * 1000L;

    public static void scanTotalDeviation(String periodType) {
        scanTotalDeviation(periodType, null);
    }

    /**
     * 查看A股票当前整体背离情况
     * 默认情况下获取涨跌幅榜单数据
     * @param periodType
     */
    public static void scanTotalDeviation(String periodType, String rankOrderType) {

        if (rankOrderType == null || rankOrderType.equals("")) {
            rankOrderType = XueqiuDataHelper.ORDERBY_PERCENT;
        }
        List<SHStockRankEntity> shStockRankEntityList = XueqiuDataHelper.requestAStockDefaultRankList(rankOrderType);

        Comparator cmp = new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };

        // 存放各种扫描的结果
        List<Map<String, Map<Long, Object>>> outputResultMapList = new ArrayList<>();

        Map<String, Map<Long, Object>> totalDeviationResultMap = new TreeMap<>(cmp);
        scanDeviation(shStockRankEntityList, periodType, false, totalDeviationResultMap);
        outputResultMapList.add(totalDeviationResultMap);



        Map<String, Map<Long, Object>> totalPreDeviationResultMap = new TreeMap<>(cmp);
        scanDeviation(shStockRankEntityList, periodType, true, totalPreDeviationResultMap);
        outputResultMapList.add(totalPreDeviationResultMap);

        outputDeviationInfo(outputResultMapList);
    }

    private static void outputDeviationInfo(List<Map<String, Map<Long, Object>>> outputResultMapList) {

        // 用于对整体结果去重,方便查看
        Map<String, SHStockRankEntity> sreMap = new HashMap<>();

        for (Map<String, Map<Long, Object>> totalDeviationResultMap : outputResultMapList) {
            // 分别输出各种背离情况的股票
            for (Map.Entry<String, Map<Long, Object>> mapEntry : totalDeviationResultMap.entrySet()) {
                String key = mapEntry.getKey();
                Map<Long, Object> valueMap = mapEntry.getValue();
                if (valueMap != null && !valueMap.isEmpty()) {
                    LogUtils.logDebugLine("==================" + key + " :==================");

                    if (valueMap != null && !valueMap.isEmpty()) {
                        for (Map.Entry<Long, Object> entry : valueMap.entrySet()) {
                            SHStockRankEntity resultSre = (SHStockRankEntity) entry.getValue();

                            long timeDiff = entry.getKey().longValue();
                            if (timeDiff > OUTPUT_FILTER_INTERVAL) {
                                // 超过7天的就不显示了
                                continue;
                            }
                            String timeDiffDesc = TimeUtils.calculateTimeDistance(timeDiff);

                            LogUtils.logDebugLine(resultSre.symbol + " " + resultSre.name + " " + resultSre.percent + " " + timeDiffDesc);

                            sreMap.put(resultSre.symbol, resultSre);
                        }
                    }
                }
            }

            LogUtils.logDebugLine("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }

        LogUtils.logDebugLine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        for (Map.Entry<String, SHStockRankEntity> mapEntry : sreMap.entrySet()) {
            SHStockRankEntity resultSre = mapEntry.getValue();
            LogUtils.logDebugLine(resultSre.symbol + " " + resultSre.name + " " + resultSre.percent);
        }
    }

    /**
     *
     * @param shStockRankEntityList
     * @param periodType
     * @param isPreDeviation true : 查看已经出现拐点的背离情况 false : 当前走势的最后一个点也算进去
     * @param totalDeviationResultMap
     */
    public static void scanDeviation(List<SHStockRankEntity> shStockRankEntityList, String periodType, boolean isPreDeviation, Map<String, Map<Long, Object>> totalDeviationResultMap) {

        Comparator cmp = new Comparator<Long>() {

            @Override
            public int compare(Long o1, Long o2) {
                return o1.compareTo(o2);
            }
        };

        Map<Long, Object> topLineDeviationMap = new TreeMap<>(cmp);
        Map<Long, Object> topBarDeviationMap = new TreeMap<>(cmp);
        Map<Long, Object> btmLineDeviationMap = new TreeMap<>(cmp);
        Map<Long, Object> btmBarDeviationMap = new TreeMap<>(cmp);

        for (SHStockRankEntity sre : shStockRankEntityList) {
            String symbol = sre.symbol;
            String stockName = sre.name;

            double percent = sre.percent;

            if (percent < CONST_INCREASE_DROP_PERCENT_BOTTOM_BAND || percent > CONST_INCREASE_DROP_PERCENT_UPPER_BAND) {
                // 日跌幅超过10%的暂不分析
                continue;
            }
            // 股票代码名字及涨跌幅
//            LogUtils.logDebugLine(symbol + " " + stockName + " " + percent);

            // 请求个股数据
            List<KEntity> kEntityList = XueqiuDataHelper.requestStockDefaultKLine(symbol, periodType);

            // 判断个股K线四种背离情况
//            DeviationEntity de1 = new DeviationEntity();
//            boolean t1 = Deviation.isDeviation(Deviation.TYPE_LINE, Deviation.TYPE_TOP, kEntityList, de1, isPreDeviation);
//            if (t1) {
//                topLineDeviationMap.put(de1.timeDiffFromCur, sre);
//            }
//            DeviationEntity de2 = new DeviationEntity();
//            boolean t2 = Deviation.isDeviation(Deviation.TYPE_BAR, Deviation.TYPE_TOP, kEntityList, de2, isPreDeviation);
//            if (t2) {
//                topBarDeviationMap.put(de2.timeDiffFromCur, sre);
//            }
            DeviationEntity de3 = new DeviationEntity();
            boolean b1 = Deviation.isDeviation(Deviation.TYPE_LINE, Deviation.TYPE_BOTTOM, kEntityList, de3, isPreDeviation);
            if (b1) {
                btmLineDeviationMap.put(de3.timeDiffFromCur, sre);
            }
            DeviationEntity de4 = new DeviationEntity();
            boolean b2 = Deviation.isDeviation(Deviation.TYPE_BAR, Deviation.TYPE_BOTTOM, kEntityList, de4, isPreDeviation);
            if (b2) {
                btmBarDeviationMap.put(de4.timeDiffFromCur, sre);
            }
        }

        // 各种背离情况的结果放入总集合
        totalDeviationResultMap.put(isPreDeviation ? KEY_BOTTOM_LINE_PRE_DEVIATION : KEY_BOTTOM_LINE_DEVIATION, btmLineDeviationMap);
        totalDeviationResultMap.put(isPreDeviation ? KEY_BOTTOM_BAR_PRE_DEVIATION : KEY_BOTTOM_BAR_DEVIATION, btmBarDeviationMap);
        totalDeviationResultMap.put(isPreDeviation ? KEY_TOP_LINE_PRE_DEVIATION : KEY_TOP_LINE_DEVIATION, topLineDeviationMap);
        totalDeviationResultMap.put(isPreDeviation ? KEY_TOP_BAR_PRE_DEVIATION : KEY_TOP_BAR_DEVIATION, topBarDeviationMap);
    }

    public static void checkSpecificStockDeviation(String symbol, String name, String periodType, Map<String, String> totalResultMap) {
        long cur = System.currentTimeMillis();
        checkSpecificStockDeviation(symbol, name, periodType, cur, totalResultMap);
    }

    public static void checkSpecificStockDeviation(String symbol, String name, String periodType, long endTimeStamp, Map<String, String> totalResultMap) {

        String valueString = symbol + name;

        // 请求个股数据
        List<KEntity> kEntityList = XueqiuDataHelper.requestStockDefaultKLine(symbol, periodType, endTimeStamp);
//        boolean t1 = Deviation.isDeviation(Deviation.TYPE_LINE, Deviation.TYPE_TOP, kEntityList);
//        if (t1) {
//            totalResultMap.put(KEY_TOP_LINE_DEVIATION, valueString);
//        }
        boolean t2 = Deviation.isDeviation(Deviation.TYPE_BAR, Deviation.TYPE_TOP, kEntityList, null);
        if (t2) {
            totalResultMap.put(KEY_TOP_BAR_DEVIATION, valueString);
        }
//        boolean b1 = Deviation.isDeviation(Deviation.TYPE_LINE, Deviation.TYPE_BOTTOM, kEntityList);
//        if (b1) {
//            totalResultMap.put(KEY_BOTTOM_LINE_DEVIATION, valueString);
//        }
//        boolean b2 = Deviation.isDeviation(Deviation.TYPE_BAR, Deviation.TYPE_BOTTOM, kEntityList);
//        if (b2) {
//            totalResultMap.put(KEY_BOTTOM_BAR_DEVIATION, valueString);
//        }
    }

    public static void main(String[] args) {
//        scanTotalDeviation("30m");
        scanTotalDeviation("15m", XueqiuDataHelper.ORDERBY_AMOUNT);

        // 测试个股背离情况
//        String symbol = "SH600295";
//        String name = "鄂尔多斯";
//        String symbol = "SH600360";
//        String name = "华微电子";
//        String periodType = "1d";
//        long entTime = 1506326498 * 1000L;
//        Map<String, String> resultMap = new HashMap<>();
//        checkSpecificStockDeviation(symbol, name, periodType, entTime, resultMap);
//
//        LogUtils.logDebugLine(symbol + " " + name + " :");
//        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
//            LogUtils.logDebugLine(entry.getKey());
//        }

        // 生成个股k线
//        List<KEntity> kEntityList = XueqiuDataHelper.requestStockDefaultKLine(symbol, periodType);
//        KLineChart.outputKLineChart(symbol + "_" + name, kEntityList, symbol + "_" + name + ".html");
    }
}
