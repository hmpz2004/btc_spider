package com.ceo.reckless;

import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.helper.SosobtcDataHelper;
import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.LogUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FundingDistributionScanner {

//    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/yunbi_eos_all_2h.txt";
//    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/yunbi_snt_all_1h.txt";
//    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/bter_btm_all_1h.txt";
    public static String inputFilePath = "/Users/pangzhou/Development/Project/btc/资金筹码分析/yunbi_ans_partial_6h.txt";

//    public static String BAR_CHART_HTML_FILE_PATH = "/Users/pangzhou/Development/general_workspace/btc/web_charts/self/column-rotated-labels.html";

    public String htmlTitle = "default title";

    public boolean needDecimal = false;
    public String price_formatter = "#.";

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
    private Map<Double, BigDecimal> calculateFundingDistribution(List<KEntity> keList, long since, long end, Map<Double, BigDecimal> priceMap, Map<Double, BigDecimal> volumeMap) {
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
                BigDecimal targetAmount = new BigDecimal(0);
                BigDecimal targetVolume = new BigDecimal(0);
                if (priceMap.containsKey(closePrice)) {
                    targetAmount = priceMap.get(closePrice);
                }
                if (volumeMap.containsKey(closePrice)) {
                    targetVolume = volumeMap.get(closePrice);
                }
//                target += (keItem.close * keItem.volume);
                BigDecimal c = new BigDecimal(keItem.close);
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

    public void genFundingChart(String market, String coin, int type, long since, long end, String outputFileName) {

        LogUtils.logDebugLine("begin ...");

        String result = SosobtcDataHelper.httpQueryKData(market, coin, type, since);
        if (result != null && result.length() != 0) {
            List<KEntity> keList = SosobtcDataHelper.parseKlineToList(result);

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
        } else {
            LogUtils.logDebugLine("http post " + SosobtcDataHelper.URL_KLINE + " query return null");
        }
    }

//    public static void main(String[] args) {
//
//        if (Env.DEBUG) {
//
//        } else {
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
//                    genFundingChart(market, coin, typeNum, since, outputName);
//                }
//                if (cmd.getOptions().length == 0) {
//                    LogUtils.logDebugLine("usage :\njava -jar parser.jar -m yunbi -c eos -t 5m -s 1502382000 -o output.html");
//                }
//            } catch (ParseException e) {
//                LogUtils.logError(e);
//            }
//        }
//    }
}
