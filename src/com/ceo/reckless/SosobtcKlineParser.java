package com.ceo.reckless;

import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.HttpRequest;
import com.ceo.reckless.utils.LogUtils;
import com.tictactec.ta.lib.Core;
import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

public class SosobtcKlineParser {

//    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/yunbi_eos_all_2h.txt";
//    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/yunbi_snt_all_1h.txt";
//    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/bter_btm_all_1h.txt";
    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/yunbi_ans_partial_6h.txt";

//    public static String BAR_CHART_HTML_FILE_PATH = "/Users/pangzhou/Development/general_workspace/btc/web_charts/self/column-rotated-labels.html";
    public static String BAR_CHART_HTML_FILE_PATH = "./column-rotated-labels.html";

    public static String BAR_CHART_HTML_DATA_PLACE_HOLDER = "<<BAR_DATA_PLACE_HOLDER>>";
    public static String HTML_TITLE_PLACE_HOLDER = "<<HTML_TITLE_PLACE_HOLDER>>";
    public static String htmlTitle;

    public static boolean needDecimal = false;
    public static String price_formatter = "#.";

    public static int baseDivisor = 10;

    public static String URL_KLINE = "http://api-test.sosobtc.com/direct/v2/kline";

    public static int TYPE_LEVEL_0 = 0;// 分时
    public static int TYPE_LEVEL_1_MIN = 0;// 1分
    public static int TYPE_LEVEL_3_MIN = 7;// 3分
    public static int TYPE_LEVEL_5_MIN = 1;// 5分
    public static int TYPE_LEVEL_10_MIN = 5;// 10分
    public static int TYPE_LEVEL_15_MIN = 2;// 15分
    public static int TYPE_LEVEL_30_MIN = 9;// 30分
    public static int TYPE_LEVEL_1_HOUR = 10;// 1小时
    public static int TYPE_LEVEL_2_HOUR = 11;// 2小时
    public static int TYPE_LEVEL_4_HOUR = 12;// 4小时
    public static int TYPE_LEVEL_6_HOUR = 13;// 6小时
    public static int TYPE_LEVEL_12_HOUR = 14;// 12小时
    public static int TYPE_LEVEL_1_DAY = 3;// 1天
    public static int TYPE_LEVEL_3_DAY = 15;// 3天
    public static int TYPE_LEVEL_1_WEEK = 4;// 1周

    public static Map<String, Integer> typeMap = new HashMap<>();
    static {
        typeMap.put("0", TYPE_LEVEL_0);// 分时
        typeMap.put("1m", TYPE_LEVEL_1_MIN);// 1分
        typeMap.put("3m", TYPE_LEVEL_3_MIN);// 3分
        typeMap.put("5m", TYPE_LEVEL_5_MIN);// 5分
        typeMap.put("10m", TYPE_LEVEL_10_MIN);// 10分
        typeMap.put("15m", TYPE_LEVEL_15_MIN);// 15分
        typeMap.put("30m", TYPE_LEVEL_30_MIN);// 30分
        typeMap.put("1h", TYPE_LEVEL_1_HOUR);// 1小时
        typeMap.put("2h", TYPE_LEVEL_2_HOUR);// 2小时
        typeMap.put("4h", TYPE_LEVEL_4_HOUR);// 4小时
        typeMap.put("6h", TYPE_LEVEL_6_HOUR);// 6小时
        typeMap.put("12h", TYPE_LEVEL_12_HOUR);// 12小时
        typeMap.put("1d", TYPE_LEVEL_1_DAY);// 1天
        typeMap.put("3d", TYPE_LEVEL_3_DAY);// 3天
        typeMap.put("1w", TYPE_LEVEL_1_WEEK);// 1周
    }

    public static class KEntity {
        public long timestamp;
        public double open;
        public double high;
        public double low;
        public double close;
        public double volume;
    }

    public static String httpQueryKData(String market, String coin, int type, long since) {

        try {

            JSONObject joPostParams = new JSONObject();
            joPostParams.put("symbol", market + "_" + coin);
            joPostParams.put("type", type);
            if (since != 0) {
                joPostParams.put("since", since);
            } else {
                joPostParams.put("since", "");
            }

            String param = joPostParams.toString();

            return HttpRequest.sendPost(URL_KLINE, param);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<KEntity> parseKlineToList(String jsonContentString) {
        try {

            if (jsonContentString != null && jsonContentString.length() != 0) {

                List<KEntity> resultList = new ArrayList<>();

                JSONObject joTotal = new JSONObject(jsonContentString);

                if (joTotal.has("data")) {
                    JSONArray jaData = joTotal.getJSONArray("data");
                    if (jaData != null && jaData.length() != 0) {

                        JSONArray jaItem = null;

                        for (int i = 0; i < jaData.length(); i++) {
                            jaItem = jaData.getJSONArray(i);
                            if (jaItem != null && jaItem.length() != 0) {
                                if (jaItem.length() == 6) {
                                    // 获取每单位时间内的open high等
                                    KEntity kee = new KEntity();
                                    kee.timestamp = jaItem.getLong(0);
                                    kee.timestamp *= 1000L;
                                    kee.open = jaItem.getDouble(1);
                                    kee.high = jaItem.getDouble(2);
                                    kee.low = jaItem.getDouble(3);
                                    kee.close = jaItem.getDouble(4);
                                    kee.volume = jaItem.getDouble(5);

                                    resultList.add(kee);


                                } else {
                                    LogUtils.logDebugLine("k point err at index " + i);
                                }
                            }
                        }
                        LogUtils.logDebugLine("data size : " + jaData.length());

                        return resultList;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 计算资金分布情况并返回一个排序map,顺序遍历key由小到大
     * key   成本价格
     * value 资金量
     * @param keList
     * @return
     */
    public static Map<Double, BigDecimal> calculateFundingDistribution(List<KEntity> keList) {
        if (keList != null && keList.size() != 0) {

            Map<Double, BigDecimal> priceMap = new TreeMap<Double, BigDecimal>(new Comparator<Double>() {
                public int compare(Double obj1, Double obj2) {
                    // 降序排序
                    return obj1.compareTo(obj2);
                }
            });

            for (KEntity keItem : keList) {
                String value = "";
                if (needDecimal) {
                    DecimalFormat df = new DecimalFormat(price_formatter);
                    value = df.format(keItem.close);
                } else {
                    long close = (long) keItem.close;
                    close = (close / baseDivisor) * baseDivisor;
                    value = String.valueOf(close);
                }

                double closePrice = Double.valueOf(value);
                BigDecimal target = new BigDecimal(0);
                if (priceMap.containsKey(closePrice)) {
                    target = priceMap.get(closePrice);
                }
//                target += (keItem.close * keItem.volume);
                BigDecimal c = new BigDecimal(keItem.close);
                BigDecimal v = new BigDecimal(keItem.volume);
                target = target.add(c.multiply(v));
                priceMap.put(closePrice, target);
            }

            return priceMap;
        } else {
            LogUtils.logDebugLine("calculateFundingDistribution() param list null");
        }
        return null;
    }

    public static void outputMapToConsole(Map<Double, BigDecimal> priceMap) {
        if (priceMap != null && priceMap.size() != 0) {
            LogUtils.logDebugLine("====================");
            BigDecimal totalAmount = new BigDecimal(0);
            for (Map.Entry<Double, BigDecimal> itemEntry : priceMap.entrySet()) {
                LogUtils.logDebugLine(itemEntry.getKey() + " " + itemEntry.getValue().longValue());
                totalAmount = totalAmount.add(itemEntry.getValue());
            }
            LogUtils.logDebugLine("====================");
            LogUtils.logDebugLine("total " + totalAmount.toString());
            LogUtils.logDebugLine("====================");
        } else {
            LogUtils.logDebugLine("outputMapToConsole() param map null");
        }
    }

    public static void outputHtmlBarChart(Map<Double, BigDecimal> priceMap, String outputFileName) {
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

            byte[] barHtmlFileBytes = FileUtils.readFileByte(new File(BAR_CHART_HTML_FILE_PATH));
            if (barHtmlFileBytes != null && barHtmlFileBytes.length != 0) {
                String barHtmlFileString = new String(barHtmlFileBytes);
                barHtmlFileString = barHtmlFileString.replace(BAR_CHART_HTML_DATA_PLACE_HOLDER, sb.toString());
                barHtmlFileString = barHtmlFileString.replace(HTML_TITLE_PLACE_HOLDER, htmlTitle);

                // LogUtils.logDebugLine(barHtmlFileString);

                FileUtils.writeByteFile(barHtmlFileString.getBytes(), new File(outputFileName));
            } else {
                LogUtils.logDebugLine("outputHtmlBarChart() read bar html null");
            }
        } else {
            LogUtils.logDebugLine("outputHtmlBarChart() param map null");
        }
    }

    public static void genFundingChart(String market, String coin, int type, long since, String outputFileName) {

        LogUtils.logDebugLine("begin ...");

        String result = httpQueryKData(market, coin, type, since);
        if (result != null && result.length() != 0) {
            List<KEntity> keList = parseKlineToList(result);
            Map<Double, BigDecimal> priceMap = calculateFundingDistribution(keList);
            outputMapToConsole(priceMap);
            outputHtmlBarChart(priceMap, outputFileName);
            LogUtils.logDebugLine("total process " + keList.size() + " data");
            LogUtils.logDebugLine("done!");
        } else {
            LogUtils.logDebugLine("http post " + URL_KLINE + " query return null");
        }
    }

    public static void main(String[] args) {

        CommandLineParser parser = new BasicParser();
        Options options = new Options( );
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("v", "verbose", false, "Print out VERBOSE information");
        options.addOption("m", "market", true, "");
        options.addOption("c", "coin", true, "");
        options.addOption("t", "time type", true, "");
        options.addOption("s", "since", true, "");
        options.addOption("f", "format", true, "");
        options.addOption("o", "output file name", true, "File to save program output to");

        try {
            CommandLine cmd = parser.parse( options, args );
            if (cmd.hasOption("h")) {
                LogUtils.logDebugLine("usage :\njava -jar parser.jar -m yunbi -c eos -t 5m -s 1502382000 -f #.00 -o output.html");
            } else if (cmd.hasOption("v")) {
                LogUtils.logDebugLine("version : 1.0 by 霸道总裁预科班\nwx:417249073");
            } else if (cmd.hasOption("m") &&
                    cmd.hasOption("c") &&
                    cmd.hasOption("t") &&
                    cmd.hasOption("f") &&
                    cmd.hasOption("o")) {
                String market = cmd.getOptionValue("m");
                String coin = cmd.getOptionValue("c");
                String type = cmd.getOptionValue("t");
                int typeNum = 0;
                if (typeMap.containsKey(type)) {
                    typeNum = typeMap.get(type);
                } else {
                    LogUtils.logDebugLine("time type error");
                }
                String formatString = cmd.getOptionValue("f");
                if (formatString.contains("#")) {
                    needDecimal = true;
                    price_formatter = formatString;
                } else {
                    baseDivisor = Integer.valueOf(cmd.getOptionValue("f"));
                }
                String outputName = cmd.getOptionValue("o");

                long since = 0;
                if (cmd.hasOption("s")) {
                    since = Long.valueOf(cmd.getOptionValue("s"));
                }

                htmlTitle = coin + "_" + market + "_" + type;

                genFundingChart(market, coin, typeNum, since, outputName);
            }
            if (cmd.getOptions().length == 0) {
                LogUtils.logDebugLine("usage :\njava -jar parser.jar -m yunbi -c eos -t 5m -s 1502382000 -o output.html");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
