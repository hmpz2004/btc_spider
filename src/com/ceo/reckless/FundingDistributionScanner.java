package com.ceo.reckless;

import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.helper.AicoinDataHelper;
import com.ceo.reckless.helper.HexunDataHelper;
import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.LogUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FundingDistributionScanner {

    private static boolean FUNDING_DEBUG = true;

//    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/yunbi_eos_all_2h.txt";
//    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/yunbi_snt_all_1h.txt";
//    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/bter_btm_all_1h.txt";
    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/yunbi_ans_partial_6h.txt";

//    public static String BAR_CHART_HTML_FILE_PATH = "/Users/pangzhou/Development/general_workspace/btc/web_charts/self/column-rotated-labels.html";

    public String htmlTitle = "default title";

    public boolean needDecimal = false;

    // 控制小数点右侧的精度
    public String price_formatter = "#.";

    // 控制小数点左侧的精度 target_value = target_value / baseDivisor
    public int baseDivisor = 10;

    public FundingDistributionScanner(boolean needDecimal, String formatString, int baseDivisor, String htmlTitle) {
        this.needDecimal = needDecimal;
        this.price_formatter = formatString;
        this.baseDivisor = baseDivisor;
        if (htmlTitle != null) {
            this.htmlTitle = htmlTitle;
        }
    }

    /**
     * 计算资金分布情况并返回一个排序map,顺序遍历key由小到大
     * key   成本价格
     * value 资金量
     * @param keList
     * @return
     */
    public Map<Double, BigDecimal> calculateFundingDistribution(List<KEntity> keList, long since, long end, Map<Double, BigDecimal> priceMap, Map<Double, BigDecimal> volumeMap) {
        if (keList != null && keList.size() != 0) {

//            Map<Double, BigDecimal> priceMap = new TreeMap<Double, BigDecimal>(new Comparator<Double>() {
//                public int compare(Double obj1, Double obj2) {
//                    // 降序排序
//                    return obj1.compareTo(obj2);
//                }
//            });

            int cnt = 0;
            for (KEntity keItem : keList) {

                if (keItem.timestamp < since * 1000L) {
                    continue;
                }
                if (end != 0 && keItem.timestamp > end * 1000L) {
                    continue;
                }

                cnt++;

                double priceToCal = (keItem.open + keItem.close) / 2;

                String value = "";
                if (needDecimal) {
                    DecimalFormat df = new DecimalFormat(price_formatter);
                    value = df.format(priceToCal);
                } else {
                    long close = (long) priceToCal;
                    close = (close / baseDivisor) * baseDivisor;
                    value = String.valueOf(close);
                }

                double closePrice = Double.valueOf(value);
                BigDecimal targetAmount = new BigDecimal(0);
                BigDecimal targetVolume = new BigDecimal(0);
                if (priceMap.containsKey(closePrice)) {
                    targetAmount = priceMap.get(closePrice);
                }
                if (volumeMap.containsKey(closePrice)) {
                    targetVolume = volumeMap.get(closePrice);
                }
//                target += (keItem.close * keItem.volume);
                BigDecimal c = new BigDecimal(priceToCal);
                BigDecimal v = new BigDecimal(keItem.volume);
                targetAmount = targetAmount.add(c.multiply(v));
                targetVolume = targetVolume.add(v);
                priceMap.put(closePrice, targetAmount);
                volumeMap.put(closePrice, targetVolume);
            }
            LogUtils.logDebugLine("total process " + cnt + " data");

            return null;
        } else {
            LogUtils.logDebugLine("calculateFundingDistribution() param list null");
        }
        return null;
    }

    /**
     * 根据k线数据计算当前近期走势多空两方力度
     * 如果since和end都没有设置,那就默认挑选100根k线来统计
     * @param keList
     * @return
     */
    private String calculateBullAndShortOnePoint(List<KEntity> keList, long since, long end) {

        boolean use100Break = false;
        if (since == 0 && end == 0) {
            use100Break = true;
        }

        if (end == 0) {
            end = System.currentTimeMillis();
        }

        if (keList != null && keList.size() != 0 && keList.size() > Env.BULL_VS_SHORT_K_ITEM_INTERVAL) {

            BigDecimal bullValue = new BigDecimal(0);
            BigDecimal shortValue = new BigDecimal(0);

            int cnt = 0;

            for (int i = keList.size() - 1; i >= 0; i--) {
                KEntity keItem = keList.get(i);
                if (keItem != null) {
                    if (keItem.timestamp > end) {
                        // 跳过超时的记录
                        break;
                    }

                    // 如果since和end都没有设置,那就默认挑选100根k线来统计
                    if (use100Break && cnt++ > Env.BULL_VS_SHORT_K_ITEM_INTERVAL) {
                        break;
                    }
                    BigDecimal v = new BigDecimal(keItem.volume);
                    if (keItem.open >= keItem.close) {
                        // 下降k
                        shortValue = shortValue.add(v);
                    } else {
                        // 上升k
                        bullValue = bullValue.add(v);
                    }
                }
            }
            BigDecimal total = new BigDecimal(0);
            total = total.add(bullValue).add(shortValue);

            int scale = 2;  // 小数点位数
            RoundingMode r = RoundingMode.HALF_DOWN;    // 四舍五入

            BigDecimal bullPercent = bullValue.divide(total, scale, r);
            double bullDValue = bullPercent.doubleValue();
            BigDecimal shortPercent = shortValue.divide(total, scale, r);
            double shortDValue = shortPercent.doubleValue();

            DecimalFormat df = new DecimalFormat("#0.00");
            String res = "多劲儿 " + df.format(bullDValue) + " 空劲儿 " + df.format(shortDValue);
            return res;
        } else {
            LogUtils.logDebugLine("calculateBullAndShortOnePoint() param list null or not enough k data");
        }
        return null;
    }

    /**
     * 计算每个时间点的vs值,返回list
     * @param keList
     * @param since 这个方法中暂时用不到
     * @param end
     * @return
     */
    private List<KEntity> calculateBullAndShortList(List<KEntity> keList, long since, long end) {

        boolean FUNC_DEBUG = true;

        if (keList == null || keList.size() <= Env.BULL_VS_SHORT_K_ITEM_INTERVAL) {
            LogUtils.logDebugLine("k list not enough entity");
        }

        if (end == 0) {
            end = System.currentTimeMillis();
        }

        List<KEntity> resultList = new ArrayList<>();

//        BigDecimal bullSumInIter = new BigDecimal(0);       // 上升k成交量统计
//        BigDecimal bullTotalInIter = new BigDecimal(0);     // 总成交量统计

        Queue<KEntity> kQueue = new LinkedList<>();

        // 统计上升量和下降量
        BigDecimal bullValue = new BigDecimal(0);           // 上升k成交量统计
        BigDecimal shortValue = new BigDecimal(0);          // 下降k成交量统计

        BigDecimal bullTotalInIter = new BigDecimal(0);     // 窗口内成交总量

        int idx = 0;

        // 先统计前100个的成交量vs值
        for (; idx < Env.BULL_VS_SHORT_K_ITEM_INTERVAL; idx++) {
            KEntity keItem = keList.get(idx);
            if (keItem != null) {
                if (keItem.timestamp > end) {
                    // 跳过超时的记录
                    break;
                }
                BigDecimal v = new BigDecimal(keItem.volume);
                if (keItem.open < keItem.close) {
                    // 上升k
                    bullValue = bullValue.add(v);
                } else {
                    // 下降k
                    shortValue = shortValue.add(v);
                }

                // 入窗口队列
                kQueue.offer(keItem);
                if (FUNDING_DEBUG && FUNC_DEBUG) {
                    LogUtils.logDebugLine("into queue " + keItem.timestamp + " " + keItem.volume);
                }
            }
        }

        // 计算初始窗口的total
        bullTotalInIter = bullValue.add(shortValue);

        if (FUNDING_DEBUG && FUNC_DEBUG) {
            LogUtils.logDebugLine("window values");
            LogUtils.logDebugLine("total " + bullTotalInIter.longValue());
            LogUtils.logDebugLine("bull  " + bullValue.longValue());
            LogUtils.logDebugLine("short " + shortValue.longValue());
        }

        for (; idx < keList.size(); idx++) {

            // 取出队列第一个元素
            KEntity fromQue = kQueue.poll();
            BigDecimal fromQueVolume = new BigDecimal(fromQue.volume);
            if (FUNDING_DEBUG && FUNC_DEBUG) {
                LogUtils.logDebugLine("from queue " + fromQue.timestamp + " " + fromQueVolume);
            }
            // 几个统计值删掉队头元素的成交量
            if (fromQue.open < fromQue.close) {
                // 上升k
                bullValue = bullValue.subtract(fromQueVolume);
            } else {
                // 下降k
                shortValue = shortValue.subtract(fromQueVolume);
            }
            bullTotalInIter = bullTotalInIter.subtract(fromQueVolume);
            if (FUNDING_DEBUG && FUNC_DEBUG) {
                LogUtils.logDebugLine("window values after subtract ");
                LogUtils.logDebugLine("total " + bullTotalInIter.longValue());
                LogUtils.logDebugLine("bull  " + bullValue.longValue());
                LogUtils.logDebugLine("short " + shortValue.longValue());
            }

            // 几个统计值增加队尾元素的成交量
            KEntity keItem = keList.get(idx);
            BigDecimal intoQueVolume = new BigDecimal(keItem.volume);

            if (keItem.open < keItem.close) {
                // 上升k
                bullValue = bullValue.add(intoQueVolume);
            } else {
                // 下降k
                shortValue = shortValue.add(intoQueVolume);
            }
            bullTotalInIter = bullTotalInIter.add(intoQueVolume);
            if (FUNDING_DEBUG && FUNC_DEBUG) {
                LogUtils.logDebugLine("window values after add ");
                LogUtils.logDebugLine("total " + bullTotalInIter.longValue());
                LogUtils.logDebugLine("bull  " + bullValue.longValue());
                LogUtils.logDebugLine("short " + shortValue.longValue());
            }

            // 计算当前k线的vs值,只算bull的即可
            int scale = 3;  // 小数点位数
            RoundingMode r = RoundingMode.HALF_DOWN;    // 四舍五入

            BigDecimal bullPercent = bullValue.divide(bullTotalInIter, scale, r);
            double bullDValue = bullPercent.doubleValue();

            DecimalFormat df = new DecimalFormat("#0.000");

            keItem.bull_vs_short = Double.valueOf(df.format(bullDValue));

            if (FUNDING_DEBUG && FUNC_DEBUG) {
                LogUtils.logDebugLine("bull_vs_short " + keItem.bull_vs_short);
            }

            // 入队
            kQueue.offer(keItem);
            if (FUNDING_DEBUG && FUNC_DEBUG) {
                LogUtils.logDebugLine("into queue " + keItem.timestamp + " " + keItem.volume);
            }

            resultList.add(keItem);
        }

        return resultList;
    }

    private void outputMapToConsole(Map<Double, BigDecimal> priceMap, Map<Double, BigDecimal> volumeMap) {
        if (priceMap != null && priceMap.size() != 0) {
            LogUtils.logDebugLine("====================");
            BigDecimal totalAmount = new BigDecimal(0);
            for (Map.Entry<Double, BigDecimal> itemEntry : priceMap.entrySet()) {
                LogUtils.logDebugLine(itemEntry.getKey() + " " + itemEntry.getValue().longValue());
                totalAmount = totalAmount.add(itemEntry.getValue());
            }
            LogUtils.logDebugLine("====================");
            LogUtils.logDebugLine("total amount " + totalAmount.toString());
            LogUtils.logDebugLine("====================");
            BigDecimal totalVolume = new BigDecimal(0);
            for (Map.Entry<Double, BigDecimal> itemEntry : volumeMap.entrySet()) {
                LogUtils.logDebugLine(itemEntry.getKey() + " " + itemEntry.getValue().longValue());
                totalVolume = totalVolume.add(itemEntry.getValue());
            }
            LogUtils.logDebugLine("====================");
            LogUtils.logDebugLine("total volume " + totalVolume.toString());
            LogUtils.logDebugLine("====================");
        } else {
            LogUtils.logDebugLine("outputMapToConsole() param map null");
        }
    }

    private void outputHtmlBarChart(Map<Double, BigDecimal> priceMap, String outputFileName) {
        if (priceMap != null && priceMap.size() != 0) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            sb.append("[\n");
            for (Map.Entry<Double, BigDecimal> itemEntry : priceMap.entrySet()) {
                if (i++ != 0) {
                    sb.append(",\n");
                }
                sb.append("['" + itemEntry.getKey() + "'," + itemEntry.getValue().longValue() + "]");
            }
            sb.append("\n]");

            byte[] barHtmlFileBytes = FileUtils.readFileByte(new File(Env.BAR_CHART_HTML_FILE_PATH));
            if (barHtmlFileBytes != null && barHtmlFileBytes.length != 0) {
                String barHtmlFileString = new String(barHtmlFileBytes);
                barHtmlFileString = barHtmlFileString.replace(Env.BAR_CHART_HTML_DATA_PLACE_HOLDER, sb.toString());
                barHtmlFileString = barHtmlFileString.replace(Env.HTML_TITLE_PLACE_HOLDER, htmlTitle);

                // LogUtils.logDebugLine(barHtmlFileString);

                FileUtils.writeByteFile(barHtmlFileString.getBytes(), new File(outputFileName));
            } else {
                LogUtils.logDebugLine("outputHtmlBarChart() read bar html null");
            }
        } else {
            LogUtils.logDebugLine("outputHtmlBarChart() param map null");
        }
    }

    /**
     * hicharts时间轴显示有问题,暂时用bar来展示
     * @param list
     * @param outputFileName
     */
    private void outputHtmlLineChart(List<KEntity> list, String outputFileName) {
        if (list != null && list.size() != 0) {
            StringBuilder sb = new StringBuilder();

            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//指定时间格式

            int i = 0;
            sb.append("[\n");
            for (KEntity item : list) {
//            for (Map.Entry<Long, BigDecimal> itemEntry : timeValueMap.entrySet()) {
                if (i++ != 0) {
                    sb.append(",\n");
                }
                String timeString = sdf.format(new Date(item.timestamp * 1000));
                sb.append("['" + timeString + "'," + item.bull_vs_short + "]");
            }
            sb.append("\n]");

            byte[] barHtmlFileBytes = FileUtils.readFileByte(new File(Env.LINE_CHART_HTML_FILE_PATH));
            if (barHtmlFileBytes != null && barHtmlFileBytes.length != 0) {
                String barHtmlFileString = new String(barHtmlFileBytes);
                barHtmlFileString = barHtmlFileString.replace(Env.BAR_CHART_HTML_DATA_PLACE_HOLDER, sb.toString());
                barHtmlFileString = barHtmlFileString.replace(Env.HTML_TITLE_PLACE_HOLDER, htmlTitle);

                // LogUtils.logDebugLine(barHtmlFileString);

                FileUtils.writeByteFile(barHtmlFileString.getBytes(), new File(outputFileName));
            } else {
                LogUtils.logDebugLine("outputHtmlLineChart() read bar html null");
            }
        } else {
            LogUtils.logDebugLine("outputHtmlLineChart() param map null");
        }
    }

//    public void genBtcFundingChart(String market, String coin, int type, long since, long end, String outputFileName) {
//
//        LogUtils.logDebugLine("begin ...");
//
//        String result = SosobtcDataHelper.httpQueryKData(market, coin, type, since);
//        if (result != null && result.length() != 0) {
//            List<KEntity> keList = SosobtcDataHelper.parseKlineToList(result);
//
//            Map<Double, BigDecimal> priceMap = new TreeMap<Double, BigDecimal>(new Comparator<Double>() {
//                public int compare(Double obj1, Double obj2) {
//                    // 降序排序
//                    return obj1.compareTo(obj2);
//                }
//            });
//
//            Map<Double, BigDecimal> volumeMap = new TreeMap<Double, BigDecimal>(new Comparator<Double>() {
//                public int compare(Double obj1, Double obj2) {
//                    // 降序排序
//                    return obj1.compareTo(obj2);
//                }
//            });
//
//            calculateFundingDistribution(keList, since, end, priceMap, volumeMap);
//            if (Env.DEBUG) {
//                outputMapToConsole(priceMap, volumeMap);
//            }
//            outputHtmlBarChart(priceMap, outputFileName);
//            LogUtils.logDebugLine("done!");
//        } else {
//            LogUtils.logDebugLine("http post " + SosobtcDataHelper.URL_KLINE + " query return null");
//        }
//    }

    public void genBtcFundingChart(String market, String targetCoin, String srcCoin, String type, long since, long end, String outputFileName) {

        LogUtils.logDebugLine("begin ...");

        List<KEntity> keList = AicoinDataHelper.requestKLine(market, targetCoin, srcCoin, type, since);

        Map<Double, BigDecimal> priceMap = new TreeMap<Double, BigDecimal>(new Comparator<Double>() {
            public int compare(Double obj1, Double obj2) {
                // 降序排序
                return obj1.compareTo(obj2);
            }
        });

        Map<Double, BigDecimal> volumeMap = new TreeMap<Double, BigDecimal>(new Comparator<Double>() {
            public int compare(Double obj1, Double obj2) {
                // 降序排序
                return obj1.compareTo(obj2);
            }
        });

        calculateFundingDistribution(keList, since, end, priceMap, volumeMap);
        if (Env.DEBUG) {
            outputMapToConsole(priceMap, volumeMap);
        }
        outputHtmlBarChart(priceMap, outputFileName);
        LogUtils.logDebugLine("done!");
    }

    /**
     * 生成期货资金分布图表
     */
    public void genFutureFundingChart(String code, String periodType, String startDateString, String outputFileName) {
        List<KEntity> resultList = HexunDataHelper.requestKLine(code, periodType, startDateString);
        if (resultList == null || resultList.size() == 0) {
            LogUtils.logDebugLine("kline list null");
            return;
        }

        Map<Double, BigDecimal> priceMap = new TreeMap<Double, BigDecimal>(new Comparator<Double>() {
            public int compare(Double obj1, Double obj2) {
                // 降序排序
                return obj1.compareTo(obj2);
            }
        });

        Map<Double, BigDecimal> volumeMap = new TreeMap<Double, BigDecimal>(new Comparator<Double>() {
            public int compare(Double obj1, Double obj2) {
                // 降序排序
                return obj1.compareTo(obj2);
            }
        });

        long since = 0;
        long end = System.currentTimeMillis();
        calculateFundingDistribution(resultList, since, end, priceMap, volumeMap);
        if (Env.DEBUG) {
            outputMapToConsole(priceMap, volumeMap);
        }
        outputHtmlBarChart(priceMap, outputFileName);
        LogUtils.logDebugLine("done!");
    }

    private List<KEntity> genTestKList() {
        List<KEntity> keList = new ArrayList<>();

        KEntity ke1 = new KEntity();
        ke1.timestamp = 1;
        ke1.open = 10;
        ke1.close = 5;
        ke1.volume = 3;
        keList.add(ke1);

        KEntity ke2 = new KEntity();
        ke2.timestamp = 1;
        ke2.open = 5;
        ke2.close = 10;
        ke2.volume = 2;
        keList.add(ke2);

        KEntity ke3 = new KEntity();
        ke3.timestamp = 1;
        ke3.open = 10;
        ke3.close = 5;
        ke3.volume = 7;
        keList.add(ke3);

        KEntity ke4 = new KEntity();
        ke4.timestamp = 1;
        ke4.open = 5;
        ke4.close = 10;
        ke4.volume = 6;
        keList.add(ke4);

        KEntity ke5 = new KEntity();
        ke5.timestamp = 1;
        ke5.open = 10;
        ke5.close = 5;
        ke5.volume = 4;
        keList.add(ke5);

        KEntity ke6 = new KEntity();
        ke6.timestamp = 1;
        ke6.open = 5;
        ke6.close = 10;
        ke6.volume = 2;
        keList.add(ke6);

        KEntity ke7 = new KEntity();
        ke7.timestamp = 1;
        ke7.open = 5;
        ke7.close = 10;
        ke7.volume = 9;
        keList.add(ke7);

        KEntity ke8 = new KEntity();
        ke8.timestamp = 1;
        ke8.open = 10;
        ke8.close = 5;
        ke8.volume = 5;
        keList.add(ke8);

        KEntity ke9 = new KEntity();
        ke9.timestamp = 1;
        ke9.open = 10;
        ke9.close = 5;
        ke9.volume = 6;
        keList.add(ke9);

        return keList;
    }

    public void genBullVsShortChart(String market, String targetCoin, String srcCoin, String type, long since, long end, String outputFileName) {

        LogUtils.logDebugLine("begin ...");

         List<KEntity> keList = AicoinDataHelper.requestKLine(market, targetCoin, srcCoin, type, since);

        if (outputFileName == null || "".equals(outputFileName)) {
            // 输出当前vs值
            String res = calculateBullAndShortOnePoint(keList, since, end);
            if (res != null) {
                LogUtils.logDebugLine(res);
            }
        } else {

            LogUtils.logDebugLine("k list with bull vs short");

            // 批量计算各个时间点vs值,形成折线图
            List<KEntity> res = calculateBullAndShortList(keList, since, end);
            for (KEntity kItem : res) {
                LogUtils.logDebugLine(kItem.toString());
            }

            outputHtmlLineChart(res, outputFileName);
        }
    }

    public static void main(String[] args) {

        if (Env.DEBUG) {

        } else {
//            CommandLineParser parser = new BasicParser();
//            Options options = new Options();
//            options.addOption("h", "help", false, "Print this usage information");
//            options.addOption("v", "verbose", false, "Print out VERBOSE information");
//            options.addOption("m", "market", true, "");
//            options.addOption("c", "coin", true, "");
//            options.addOption("t", "time type", true, "");
//            options.addOption("s", "since", true, "");
//            options.addOption("f", "format", true, "");
//            options.addOption("o", "output file name", true, "File to save program output to");
//
//            try {
//                CommandLine cmd = parser.parse(options, args);
//                if (cmd.hasOption("h")) {
//                    LogUtils.logDebugLine("usage :\njava -jar parser.jar -m yunbi -c eos -t 5m -s 1502382000 -f #.00 -o output.html");
//                } else if (cmd.hasOption("v")) {
//                    LogUtils.logDebugLine("version : 1.0 by 霸道总裁预科班\nwx:417249073");
//                } else if (cmd.hasOption("m") &&
//                        cmd.hasOption("c") &&
//                        cmd.hasOption("t") &&
//                        cmd.hasOption("f") &&
//                        cmd.hasOption("o")) {
//                    String market = cmd.getOptionValue("m");
//                    String coin = cmd.getOptionValue("c");
//                    String type = cmd.getOptionValue("t");
//                    int typeNum = 0;
//                    if (SosobtcDataHelper.periodTypeMap.containsKey(type)) {
//                        typeNum = SosobtcDataHelper.periodTypeMap.get(type);
//                    } else {
//                        LogUtils.logDebugLine("time type error");
//                    }
//                    String formatString = cmd.getOptionValue("f");
//                    if (formatString.contains("#")) {
//                        needDecimal = true;
//                        price_formatter = formatString;
//                    } else {
//                        baseDivisor = Integer.valueOf(cmd.getOptionValue("f"));
//                    }
//                    String outputName = cmd.getOptionValue("o");
//
//                    long since = 0;
//                    if (cmd.hasOption("s")) {
//                        since = Long.valueOf(cmd.getOptionValue("s"));
//                    }
//
//                    htmlTitle = coin + "_" + market + "_" + type;
//
//                    genBtcFundingChart(market, coin, typeNum, since, outputName);
//                }
//                if (cmd.getOptions().length == 0) {
//                    LogUtils.logDebugLine("usage :\njava -jar parser.jar -m yunbi -c eos -t 5m -s 1502382000 -o output.html");
//                }
//            } catch (ParseException e) {
//                LogUtils.logError(e);
//            }
        }

        // test genFutureFundingChart()
//        FundingDistributionScanner scanner = new FundingDistributionScanner(false, "1", 1, "期货行情");
//        String code = "DCEI1805";
//        String fileName = "DCEI1805.html";
////        String code = "SHFE3rb1805";
////        String fileName = "rb1805.html";
//        scanner.genFutureFundingChart(code, "1h", "20171218210000", fileName);


        FundingDistributionScanner scanner = new FundingDistributionScanner(true, "1", 1, "");
        String market = "okcoinfutures";
        String targetCoin = "btcquarter";
        String srcCoin = "usd";
        String type = "5m";
        // 下跌时间对儿
//        long since = 1517724000;    // 2018/2/4 14:0:0
//        long end = 1517847924;      // 2018/2/6 0:25:24
        // 上涨时间对儿
//        long since = 1517626800;    // 2018/2/3 11:0:0
//        long end = 1517670000;      // 2018/2/3 23:0:0
        long since = 0;
        long end = 0;
        String o = "bull_short_list_chart.html";
        scanner.genBullVsShortChart(market, targetCoin, srcCoin, type, since, end, o);

    }
}
