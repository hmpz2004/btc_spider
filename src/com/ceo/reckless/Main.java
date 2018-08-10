package com.ceo.reckless;

import com.ceo.reckless.chart.KLineChart;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.entity.LinkEntity;
import com.ceo.reckless.helper.AicoinDataHelper;
import com.ceo.reckless.helper.SosobtcDataHelper;
import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.LogUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static boolean MAIN_DEBUG = false;

    public static Map<String, String> coinZhNameMap = new HashMap<>();

    public static Map<String, Set<String>> marketCoinMap = new HashMap<>();

    private static void callResistanceSupport(CommandLine cmd) {
        if (cmd.hasOption("m") &&
                cmd.hasOption("c") &&
                cmd.hasOption("t")) {
            String market = cmd.getOptionValue("m");
            String coin = cmd.getOptionValue("c");
            String type = cmd.getOptionValue("t");
            int typeNum = 0;
            if (SosobtcDataHelper.typeMap.containsKey(type)) {
                typeNum = SosobtcDataHelper.typeMap.get(type);
            } else {
                LogUtils.logDebugLine("time type error");
            }
            MarketWaveMotionScanner.scanResistanceSupport(market, coin, typeNum, 0, null, null);
        } else {
            LogUtils.logDebugLine("funding usage:\n -m yunbi -c eos -t 5m");
        }
    }

    private static void callScanResistanceSupport(CommandLine cmd) {
        if (cmd.hasOption("m") &&
                cmd.hasOption("t")) {
            String market = cmd.getOptionValue("m");
            String type = cmd.getOptionValue("t");
            int typeNum = 0;
            if (SosobtcDataHelper.typeMap.containsKey(type)) {
                typeNum = SosobtcDataHelper.typeMap.get(type);
            } else {
                LogUtils.logDebugLine("time type error");
            }

            if (marketCoinMap.containsKey(market)) {

                Map<String, String> breakMap = new HashMap<>();
                Map<String, String> dropMap = new HashMap<>();

                Set<String> marketCoinSet = marketCoinMap.get(market);
                Iterator<String> iter = marketCoinSet.iterator();
                String coin = null;
                while (iter.hasNext() && (coin = iter.next()) != null) {
                    MarketWaveMotionScanner.scanResistanceSupport(market, coin, typeNum, 0, breakMap, dropMap);
                }
                LogUtils.logDebugLine("====================break resistance !!!====================");
                LogUtils.logDebugLine("");
                for (Map.Entry<String, String> itemEntry : breakMap.entrySet()) {
                    LogUtils.logDebugLine(itemEntry.getKey() + " : " + itemEntry.getValue());
                }
                LogUtils.logDebugLine("====================drop  support    !!!====================");
                LogUtils.logDebugLine("");
                for (Map.Entry<String, String> itemEntry : dropMap.entrySet()) {
                    LogUtils.logDebugLine(itemEntry.getKey() + " : " + itemEntry.getValue());
                }
                LogUtils.logDebugLine("");
            } else {
                LogUtils.logDebugLine("market error");
            }
        } else {
            LogUtils.logDebugLine("scan_resistance_support usage:\n -m yunbi -t 5m");
        }
    }

    private static void callScanDeviationAicoin(CommandLine cmd) {
        if (cmd.hasOption("i")) {
            String inputFileName = cmd.getOptionValue("i");
            String type = "";
            if (cmd.hasOption("t")) {
                 type = cmd.getOptionValue("t");
            }

            long since = 0;

            if (cmd.hasOption("s")) {
                since = Long.valueOf(cmd.getOptionValue("s"));
            }

            String basePath = System.getProperty("user.dir");

            if (inputFileName == null || inputFileName.equals("")) {
                inputFileName = "symbols.txt";
            }

            LogUtils.logDebugLine("processing ...");

            CoinDeviationScanner.scanMultiPeriodDeviation(basePath + File.separator + inputFileName, type);
        }
    }

    private static void callScanDeviation(CommandLine cmd) {
        if (cmd.hasOption("m") &&
                cmd.hasOption("c") &&
                cmd.hasOption("t")) {

            String market = cmd.getOptionValue("m");
            String coin = cmd.getOptionValue("c");
            String type = cmd.getOptionValue("t");
            int typeNum = 0;
            if (SosobtcDataHelper.typeMap.containsKey(type)) {
                typeNum = SosobtcDataHelper.typeMap.get(type);
            } else {
                LogUtils.logDebugLine("time type error");
            }

            long since = 0;
            if (cmd.hasOption("s")) {
                since = Long.valueOf(cmd.getOptionValue("s"));
            }
            long endTime = 0;
            if (cmd.hasOption("e")) {
                endTime = Long.valueOf(cmd.getOptionValue("e"));
            }

            String outputFileName = cmd.getOptionValue("o");

            MarketWaveMotionScanner.scanDeviation(market, coin, typeNum, since, endTime, outputFileName, null);
        } else if (cmd.hasOption("a")) {
            if (cmd.hasOption("m") && cmd.hasOption("c") && cmd.hasOption("t")) {
                // 所有交易所所有币种
                LogUtils.logDebugLine("TODO...");
            } else if (cmd.hasOption("m") && cmd.hasOption("t")) {
                // 所有该交易所的币种
                String type = cmd.getOptionValue("t");
                int typeNum = 0;
                if (SosobtcDataHelper.typeMap.containsKey(type)) {
                    typeNum = SosobtcDataHelper.typeMap.get(type);
                } else {
                    LogUtils.logDebugLine("time type error");
                }
                String market = cmd.getOptionValue("m");
                if (marketCoinMap.containsKey(market)) {

                    Map<String, String> resultMap = new HashMap<>();

                    Set<String> marketCoinSet = marketCoinMap.get(market);
                    Iterator<String> iter = marketCoinSet.iterator();
                    String coin = null;
                    while (iter.hasNext() && (coin = iter.next()) != null) {
                        MarketWaveMotionScanner.scanDeviation(market, coin, typeNum, 0, 0, null, resultMap);
                    }

                    // 得到背离结果并输出
                    LogUtils.logDebugLine("------------------------------\n");
                    LogUtils.logDebugLine(MarketWaveMotionScanner.KEY_TOP_DEVIATION + " " + market + " : " + resultMap.get(MarketWaveMotionScanner.KEY_TOP_DEVIATION));
                    LogUtils.logDebugLine(MarketWaveMotionScanner.KEY_BOTTOM_DEVIATION + " " + market + " : " + resultMap.get(MarketWaveMotionScanner.KEY_BOTTOM_DEVIATION));
                    LogUtils.logDebugLine("");

                } else {
                    LogUtils.logDebugLine("market error");
                }
            } else if (cmd.hasOption("c") && cmd.hasOption("t")) {
                // 该币种在所有交易所的情况
                LogUtils.logDebugLine("TODO...");
            }
        } else {
            LogUtils.logDebugLine("scan_deviation usage:\n -m yunbi -c eos -t 5m -s 1502382000 -e 1503382000 -o output.html\n -a (scan all market all coin)\n -a -m yunbi (scan all yunbi coin) -a -c coin (scan all coin)");
        }
    }

    private static void callScanVolume(CommandLine cmd) {

        if (cmd.hasOption("i") &&
                cmd.hasOption("t")) {
            String inputFileName = cmd.getOptionValue("i");
            String type = cmd.getOptionValue("t");

            long since = 0;

            if (cmd.hasOption("s")) {
                since = Long.valueOf(cmd.getOptionValue("s"));
            }

            Map<String, List<KEntity>> strongMap = new HashMap<>();
            Map<String, List<KEntity>> weakMap = new HashMap<>();

            String basePath = System.getProperty("user.dir");


            if (inputFileName == null || inputFileName.equals("")) {
                inputFileName = "symbols.txt";
            }

            LogUtils.logDebugLine("processing ...");

            Map<String, Set<String>> strongCoinSymbolSetMap = new HashMap<>();
            Map<String, Set<String>> weakCoinSymbolSetMap = new HashMap<>();

            // 交易所过滤
            // String pre = "^(okex|huobipro|gate|binance)";
            String pre = "^(huobipro|binance|bitfinex|gate)";    // 先暂时去掉okex(深度差)
//            String pre = "^(huobipro)";    // 先暂时去掉okex(深度差)、gate(交易量小)
            String mid = ".*";
            // 交易对过滤
            String suf = "(btc|eth|usdt)$";  // 先暂时去掉bnb、bch、qc
//            String suf = "(btc|eth|usdt)$";  // 先暂时去掉bnb、bch、qc

            String regexTotal = pre + mid + suf;
            Pattern patternTotal = Pattern.compile(regexTotal);
            String regexPrefix = pre;
            Pattern patternPrefix = Pattern.compile(regexPrefix);
            String regexSuffix = suf;
            Pattern patternSuffix = Pattern.compile(regexSuffix);

            File inputFile = new File(basePath + File.separator + inputFileName);
            byte[] fileContent = FileUtils.readFileByte(inputFile);
            String fileContentString = new String(fileContent);
            String[] lineArray = fileContentString.split("\n");
            for (String itemLine : lineArray) {

                String symbol = itemLine;
                boolean status = false;

                Matcher matcherLine = patternTotal.matcher(symbol);
                while (matcherLine.find()) {
                    // 该行符合筛选条件
                    status = true;
                }
                if (!status) {
                    // 没命中交易所和币种
                    continue;
                }

                LogUtils.logDebugLine("symbol : " + symbol);

                List<KEntity> list = AicoinDataHelper.requestKLineBySymbol(symbol, type, since);

                Map<String, List<KEntity>> resultMap = VolumeScanner.scanRecentlyBigVolume(list);
                if (resultMap != null && resultMap.size() != 0) {

                    // 切分出交易所、币种、交易对币种
                    String coin = null;
                    String lastCoin = null;
                    String exchange = null;
                    Matcher matcherPrefix = patternPrefix.matcher(symbol);
                    while (matcherPrefix.find()) {
                        String prefix = matcherPrefix.group();
                        // 匹配前缀得到交易所
                        exchange = prefix;
                        coin = symbol.replace(prefix, "");

                        Matcher matcherSuffix = patternSuffix.matcher(coin);
                        if (matcherSuffix.find()) {
                            String suffix = matcherSuffix.group();
                            // 匹配后缀得到交易对币种
                            lastCoin = suffix;
                            // 切分得到币种
                            coin = coin.substring(0, coin.length() - suffix.length());
                        }
                    }

                    List<KEntity> strongList = resultMap.get(VolumeScanner.KEY_STRONG);
                    if (strongList != null && strongList.size() != 0) {
                        strongMap.put(symbol, strongList);
                        Set strongSymbolSet = strongCoinSymbolSetMap.get(coin);
                        if (strongSymbolSet == null) {
                            strongSymbolSet = new HashSet();
                        }
                        strongSymbolSet.add(exchange + "-" + lastCoin);
                        strongCoinSymbolSetMap.put(coin, strongSymbolSet);
                    }
                    List<KEntity> weakList = resultMap.get(VolumeScanner.KEY_WEAK);
                    if (weakList != null && weakList.size() != 0) {
                        weakMap.put(symbol, weakList);
                        Set weakSymbolSet = weakCoinSymbolSetMap.get(coin);
                        if (weakSymbolSet == null) {
                            weakSymbolSet = new HashSet();
                        }
                        weakSymbolSet.add(exchange + "-" + lastCoin);
                        weakCoinSymbolSetMap.put(coin, weakSymbolSet);
                    }
                }

                // aicoin可能有反爬虫策略,增加一个sleep
                try {
                    Thread.sleep(AicoinDataHelper.AICOIN_HTTP_QUERY_INTERVAL);
                } catch (Exception e) {
                }
            }

            // 结果排序,按照币种对应的symbol多少降序排列
            Comparator comparator = new Comparator<Map.Entry<String, Set<String>>> () {
                @Override
                public int compare(Map.Entry<String, Set<String>> o1, Map.Entry<String, Set<String>> o2) {
                    int size1 = o1.getValue().size();
                    int size2 = o2.getValue().size();
                    return size2 - size1;   // 降序
                }
            };
            List<Map.Entry<String, Set<String>>> strongOrderList = new ArrayList<>();
            strongOrderList.addAll(strongCoinSymbolSetMap.entrySet());
            Collections.sort(strongOrderList, comparator);

            LogUtils.logDebugLine("==============================");
            LogUtils.logDebugLine("strong filtered coin\n");
            for (Map.Entry<String, Set<String>> itemEntry : strongOrderList) {
                String coin = itemEntry.getKey();
                if (coin == null) {
                    continue;
                }
                LogUtils.logDebug(String.format("%5s", coin) + "  ");
                Set<String> valueSet = itemEntry.getValue();
                for (String itemValue : valueSet) {
                    LogUtils.logDebug(String.format("%10s", itemValue).toString() + " ");
                }
                LogUtils.logDebug("\n");
            }

//            LogUtils.logDebugLine("==============================");
//            LogUtils.logDebugLine("strong filtered symbols\n");
//            // 所有交易所所有币种已经遍历完成,输出结果
//            for (Map.Entry<String, List<KEntity>> itemEntry : strongMap.entrySet()) {
//                LogUtils.logDebugLine(itemEntry.getKey());
//            }

            List<Map.Entry<String, Set<String>>> weakOrderList = new ArrayList<>();
            weakOrderList.addAll(weakCoinSymbolSetMap.entrySet());
            Collections.sort(weakOrderList, comparator);

            LogUtils.logDebugLine("==============================");
            LogUtils.logDebugLine("weak filtered coin\n");
            for (Map.Entry<String, Set<String>> itemEntry : weakOrderList) {
                String coin = itemEntry.getKey();
                if (coin == null) {
                    continue;
                }
                LogUtils.logDebug(String.format("%5s", coin) + "  ");
                Set<String> valueSet = itemEntry.getValue();
                for (String itemValue : valueSet) {
                    LogUtils.logDebug(String.format("%10s", itemValue).toString() + " ");
                }
                LogUtils.logDebug("\n");
            }

//            LogUtils.logDebugLine("==============================");
//            LogUtils.logDebugLine("weak filtered symbols\n");
//            for (Map.Entry<String, List<KEntity>> itemEntry : weakMap.entrySet()) {
//                LogUtils.logDebugLine(itemEntry.getKey());
//            }
        }
    }

    private static void callFunding(CommandLine cmd) {
        if (cmd.hasOption("m") &&
                cmd.hasOption("tc") &&
                cmd.hasOption("sc") &&
                cmd.hasOption("t") &&
                cmd.hasOption("d") &&
                cmd.hasOption("o")) {
            String market = cmd.getOptionValue("m");
            String targetCoin = cmd.getOptionValue("tc");
            String srcCoin = cmd.getOptionValue("sc");
            String type = cmd.getOptionValue("t");
            String formatString = cmd.getOptionValue("d");
            boolean needDecimal = false;
            String price_formatter = "";
            int baseDivisor = 0;
            String htmlTitle = "";
            if (formatString.contains("#")) {
                needDecimal = true;
                price_formatter = formatString;
            } else {
                baseDivisor = Integer.valueOf(cmd.getOptionValue("d"));
            }
            String outputName = cmd.getOptionValue("o");

            long since = 0;
            if (cmd.hasOption("s")) {
                since = Long.valueOf(cmd.getOptionValue("s"));
            }
            long end = 0;
            if (cmd.hasOption("e")) {
                end = Long.valueOf(cmd.getOptionValue("e"));
            }

            htmlTitle = targetCoin + "_" + market + "_" + type;

            new FundingDistributionScanner(needDecimal, price_formatter, baseDivisor, htmlTitle).genBtcFundingChart(market, targetCoin, srcCoin, type, since, end, outputName);
        } else {
//            LogUtils.logDebugLine("funding usage:\n -m yunbi -c eos -t 5m -s 1502382000 -d \"#.00\" -o output.html\n -m yunbi -c eos -t 5m -d \"#.00\" -o eos_yunbi.html");
            LogUtils.logDebugLine("funding usage:");
            LogUtils.logDebugLine("-m  market");
            LogUtils.logDebugLine("-tc target coin");
            LogUtils.logDebugLine("-sc src    coin");
            LogUtils.logDebugLine("-s  since");
            LogUtils.logDebugLine("-t  period type");
            LogUtils.logDebugLine("-o  output file name");
            LogUtils.logDebugLine("-d  divisor 100 10 1 #.0 #.00 #.000");
            LogUtils.logDebugLine("");
            LogUtils.logDebugLine("-m huobipro -tc eos -sc usdt -t 5m -s 1502382000 -d \"#.00\" -o output.html");
            LogUtils.logDebugLine("-m huobipro -tc eos -t 5m -d \"#.00\" -o eos_usdt_huobipro.html");
            LogUtils.logDebugLine("-m okex -tc ethquarter -sc usd -t 15m -d 10 -o eth_usdt.html");
            // okcoinfuturesbtcquarterusd
            LogUtils.logDebugLine("-m okcoinfutures -tc btcquarter -sc usd -t 1h -d 100 -o btc_usdt.html");
        }
    }

    private static void callBullVsShort(CommandLine cmd) {
        if (cmd.hasOption("m") &&
                cmd.hasOption("tc") &&
                cmd.hasOption("sc") &&
                cmd.hasOption("t") &&
                cmd.hasOption("d") &&
                cmd.hasOption("o")) {
            String market = cmd.getOptionValue("m");
            String targetCoin = cmd.getOptionValue("tc");
            String srcCoin = cmd.getOptionValue("sc");
            String type = cmd.getOptionValue("t");
            String formatString = cmd.getOptionValue("d");
            boolean needDecimal = false;
            String price_formatter = "";
            int baseDivisor = 0;
            String htmlTitle = "";
            if (formatString.contains("#")) {
                needDecimal = true;
                price_formatter = formatString;
            } else {
                baseDivisor = Integer.valueOf(cmd.getOptionValue("d"));
            }
            String outputName = cmd.getOptionValue("o");

            long since = 0;
            if (cmd.hasOption("s")) {
                since = Long.valueOf(cmd.getOptionValue("s"));
            }

            long end = 0;
            if (cmd.hasOption("e")) {
                end = Long.valueOf(cmd.getOptionValue("e"));
            }

            htmlTitle = targetCoin + "_" + market + "_" + type;

            new FundingDistributionScanner(needDecimal, price_formatter, baseDivisor, htmlTitle).genBullVsShortChart(market, targetCoin, srcCoin, type, since, end, outputName);
        } else {
            LogUtils.logDebugLine("bull_short usage:");
            LogUtils.logDebugLine("-m  market");
            LogUtils.logDebugLine("-tc target coin");
            LogUtils.logDebugLine("-sc src    coin");
            LogUtils.logDebugLine("-s  since");
            LogUtils.logDebugLine("-t  period type");
            LogUtils.logDebugLine("-o  output file name");
            LogUtils.logDebugLine("-d  divisor 100 10 1 #.0 #.00 #.000");
            LogUtils.logDebugLine("");
            LogUtils.logDebugLine("-m huobipro -tc eos -sc usdt -t 5m -s 1502382000 -d \"#.00\" -o output.html");
            LogUtils.logDebugLine("-m huobipro -tc eos -t 5m -d \"#.00\" -o eos_usdt_huobipro_bull_short.html");
            LogUtils.logDebugLine("-m okex -tc ethquarter -sc usd -t 15m -d 10 -o eth_usdt_boll_short.html");
            // okcoinfuturesbtcquarterusd
            LogUtils.logDebugLine("-m okcoinfutures -tc btcquarter -sc usd -t 5m -d 100 -o btc_usdt_bull_short.html");
            LogUtils.logDebugLine("-m okex -tc ethquarter -sc usd -t 5m -d 10 -o eth_usdt_bull_short.html");
        }
    }

    static class ValueComparator implements Comparator<String> {

        Map<String, Double> base;
        public ValueComparator(Map<String, Double> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    private static void callIncreaseDrop(CommandLine cmd) {

        if (cmd.hasOption("m") &&
                cmd.hasOption("o")) {
            String market = cmd.getOptionValue("m");
            String outputName = cmd.getOptionValue("o");
            if (marketCoinMap.containsKey(market)) {

                Map<String, Double> increaseMap = new TreeMap<>();
                ValueComparator vcIncrease = new ValueComparator(increaseMap);
                TreeMap<String,Double> sortedIncreaseMap = new TreeMap<String,Double>(vcIncrease);

                Map<String, Double> dropMap = new HashMap<>();
                ValueComparator vcDrop = new ValueComparator(dropMap);
                TreeMap<String,Double> sortedDropMap = new TreeMap<String,Double>(vcDrop);

                Set<String> marketCoinSet = marketCoinMap.get(market);
                Iterator<String> iter = marketCoinSet.iterator();
                String coin = null;
                while (iter.hasNext() && (coin = iter.next()) != null) {
                    MarketWaveMotionScanner.scanDropIncreasePercent(market, coin, outputName, increaseMap, dropMap);
                }

                // 排序
                sortedIncreaseMap.putAll(increaseMap);
                sortedDropMap.putAll(dropMap);

                LogUtils.logDebugLine("------------------------------\n");
                LogUtils.logDebugLine("increase : ");
                DecimalFormat df = new DecimalFormat("#.00");
                for (Map.Entry<String, Double> entry : sortedIncreaseMap.entrySet()) {
                    double value = entry.getValue();
                    value *= 100;
                    LogUtils.logDebugLine(" " + entry.getKey() + " " + Integer.valueOf(String.valueOf((int) value)));
                }
                LogUtils.logDebugLine("");
                LogUtils.logDebugLine("drop : ");
                for (Map.Entry<String, Double> entry : sortedDropMap.entrySet()) {
                    double value = entry.getValue();
                    value *= 100;
                    LogUtils.logDebugLine(" " + entry.getKey() + " " + Integer.valueOf(String.valueOf((int) value)));
                }
                LogUtils.logDebugLine("");
            } else {
                LogUtils.logDebugLine("market error");
            }
        } else {
            LogUtils.logDebugLine("increase_drop usage:\n -i a -m yunbi -o yunbi_ins.html");
        }
    }

    private static void callKinkLink(CommandLine cmd) {
        if (cmd.hasOption("m") &&
                cmd.hasOption("c") &&
                cmd.hasOption("b") &&
                cmd.hasOption("t")) {

            String market = cmd.getOptionValue("m");
            String coin = cmd.getOptionValue("c");
            String buyCoin = cmd.getOptionValue("b");
            String type = cmd.getOptionValue("t");

            long since = 0;
            List<KEntity> list = AicoinDataHelper.requestKLine(market, coin, buyCoin, type, since);
            List<KEntity> slist = KinkScanner.shrinkKLine(list);

            LogUtils.logDebugLine("list size " + list.size() + " slist size " + slist.size());

            // 划分笔
            int[] markArray = KinkScanner.markTopBottomShape(slist);
            List<LinkEntity> linkList = KinkScanner.processMarkTypeArray(slist, markArray);
            slist = KinkScanner.changeOrigKShape(slist, linkList);

            KLineChart.outputKLineChart("title ttt", slist, "bitmexxbtusd_change_shape_kline_chart.html");
        } else {
            LogUtils.logDebugLine("kink_link usage:\n -m huobipro -c eos -b usd -t 5m");
            //<<>>
        }
    }

    private static void testMain(String args[]) {
//        MarketWaveMotionScanner.scanDeviation("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_15_MIN, 0, 0, "dif_k_spot_line.html", null);
//        MarketWaveMotionScanner.scanResistanceSupport("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_2_HOUR, 0, null, null);

//        FundingDistributionScanner.needDecimal = false;
//        FundingDistributionScanner.baseDivisor = 100;
//        FundingDistributionScanner.genBtcFundingChart("viabtc", "bcc", SosobtcDataHelper.TYPE_LEVEL_1_HOUR, 1502038836, "bcc_viabtc.html");

        callScanVolume(null);
    }

    private static void releaseMain(String[] args) {

        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("v", "verbose", false, "Print out VERBOSE information");
        options.addOption("f", "function", true, "Print out VERBOSE information");
        options.addOption("m", "market", true, "");
        options.addOption("c", "coin", true, "");
        options.addOption("b", "buycoin", true, "");    // 购买的币种
        options.addOption("t", "time type", true, "");
        options.addOption("s", "since", true, "");
        options.addOption("d", "divisor", true, "");
        options.addOption("o", "output file name", true, "File to save program output to");
        options.addOption("e", "endtime", true, "");
        options.addOption("a", "all", true, "");
        options.addOption("tc", "target_coin", true, "");
        options.addOption("sc", "src_coin", true, "");
        options.addOption("i", "input_file", true, "");
//        options.addOption("i", "increase drop", true, "");

        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                outputUsage();
            } else if (cmd.hasOption("v")) {
                LogUtils.logDebugLine("version : 1.2 by 霸道总裁预科班\nwx:417249073");
            } else if (cmd.hasOption("f")) {
                String functionString = cmd.getOptionValue("f");
                switch (functionString) {
                    case "funding" :
                        callFunding(cmd);
                        break;
                    case "bull_short":
                        callBullVsShort(cmd);
                        break;
                    case "scan_deviation":
                        callScanDeviation(cmd);
                        break;
                    case "scan_resistance_support":
                        callScanResistanceSupport(cmd);
                        break;
                    case "resistance_support":
                        callResistanceSupport(cmd);
                        break;
                    case "increase_drop":
                        callIncreaseDrop(cmd);
                        break;
                    case "volume_scan":
                        callScanVolume(cmd);
                        break;
                    case "scan_deviation_aicoin":
                        callScanDeviationAicoin(cmd);
                        break;
                    case "kink_link":
                        callKinkLink(cmd);
                }

            }
            if (cmd.getOptions().length == 0) {
                outputUsage();
            }
        } catch (ParseException e) {
            LogUtils.logError(e);
        }
    }

    private static void outputUsage() {
        LogUtils.logDebugLine("usage :\njava -jar parser.jar -f funding -m yunbi -c eos -t 5m -s 1502382000 -o output.html\n -f means function (funding,scan_deviation,resistance_support)");
        LogUtils.logDebugLine("normal : ");
        LogUtils.logDebugLine("-f scan_deviation -a a -m bter -t 15m");
        LogUtils.logDebugLine("-f scan_deviation -m yunbi -c eos -t 5m -o eos_yunbi.html");
        LogUtils.logDebugLine("-f funding -m huobipro -tc eos -sc usdt -t 1h -d \"#.00\" -o eos_usdt_huobipro.html");
        LogUtils.logDebugLine("-f resistance_support -m yunbi -c eos -t 5m");
        LogUtils.logDebugLine("-f scan_resistance_support -m yunbi -t 5m");
        LogUtils.logDebugLine("-f increase_drop -m yunbi -o yunbi_ins.html");
        LogUtils.logDebugLine("-f bull_short -m okex -tc ethquarter -sc usd -t 15m -d 10 -o eth_usdt.html");
        LogUtils.logDebugLine("-f volume_scan -i symbols.txt -t 2h");
        LogUtils.logDebugLine("-f scan_deviation_aicoin -i symbols.txt -t 30m");
        LogUtils.logDebugLine("target coin src coin : ");
        LogUtils.logDebugLine("okexethquarterusd okex eth 季度");
        LogUtils.logDebugLine("okexethweekusd okex eth 当周");
        LogUtils.logDebugLine("okexethnextweekusd okex eth 次周");
        LogUtils.logDebugLine("okcoinfuturesbtcnextweekusd okcoin btc 次周");
        LogUtils.logDebugLine("binancebnbbtc binance bnb btc");
        LogUtils.logDebugLine("gatebtcusdt gate btc usdt");
        LogUtils.logDebugLine("market : ");
        LogUtils.logDebugLine("binance");
        LogUtils.logDebugLine("okex");
        LogUtils.logDebugLine("huobipro");
        LogUtils.logDebugLine("gate");
        LogUtils.logDebugLine("bitstampbtcusd");
    }

    public static void main(String args[]) {
        if (MAIN_DEBUG) {
            testMain(args);
        } else {
            releaseMain(args);
        }
    }

    static {
//        Map<String, String> yunbiCoinMap = new HashMap<>();
//        yunbiCoinMap.put("", "");

        coinZhNameMap.put("zec", "ZCash");
        coinZhNameMap.put("ytc", "一号币");
        coinZhNameMap.put("wdc", "世界币");
        coinZhNameMap.put("cnc", "中国币");
        coinZhNameMap.put("pgc", "乐园通");
        coinZhNameMap.put("sc", "云储币");
        coinZhNameMap.put("eth", "以太坊");
        coinZhNameMap.put("qec", "企鹅币");
        coinZhNameMap.put("mgc", "众合币");
        coinZhNameMap.put("tfc", "传送币");
        coinZhNameMap.put("plc", "保罗币");
        coinZhNameMap.put("plp", "保罗积分");
        coinZhNameMap.put("ybc", "元宝币");
        coinZhNameMap.put("gxs", "公信宝");
        coinZhNameMap.put("fz", "冰河币");
        coinZhNameMap.put("brwetc", "区块赛车");
        coinZhNameMap.put("ins", "印链");
        coinZhNameMap.put("xcp", "合约币");
        coinZhNameMap.put("txp", "同行积分");
        coinZhNameMap.put("med", "地中海币");
        coinZhNameMap.put("eac", "地球币");
        coinZhNameMap.put("nmc", "域名币");
        coinZhNameMap.put("bost", "增长币");
        coinZhNameMap.put("qrk", "夸克币");
        coinZhNameMap.put("tag", "奖赏币");
        coinZhNameMap.put("src", "安全币");
        coinZhNameMap.put("ics", "小企股");
        coinZhNameMap.put("ans", "小蚁股");
        coinZhNameMap.put("emc", "崛起币");
        coinZhNameMap.put("bjc", "币久股");
        coinZhNameMap.put("bl", "币链");
        coinZhNameMap.put("tips", "帽子币");
        coinZhNameMap.put("lkc", "幸运币");
        coinZhNameMap.put("bash", "幸运链");
        coinZhNameMap.put("lsk", "应用链");
        coinZhNameMap.put("kpc", "开普勒币");
        coinZhNameMap.put("tix", "彩币");
        coinZhNameMap.put("vash", "微币");
        coinZhNameMap.put("xlm", "恒星币");
        coinZhNameMap.put("zcc", "招财币");
        coinZhNameMap.put("dgc", "数码币");
        coinZhNameMap.put("xem", "新经币");
        coinZhNameMap.put("nbc", "新赛波币");
        coinZhNameMap.put("ifc", "无限币");
        coinZhNameMap.put("tmc", "时代币");
        coinZhNameMap.put("peb", "普银");
        coinZhNameMap.put("dnc", "暗网币");
        coinZhNameMap.put("max", "最大币");
        coinZhNameMap.put("nxt", "未来币");
        coinZhNameMap.put("llc", "楼兰币");
        coinZhNameMap.put("btm", "比原链");
        coinZhNameMap.put("bec", "比奥币");
        coinZhNameMap.put("btq", "比特券");
        coinZhNameMap.put("bcc", "比特币");
        coinZhNameMap.put("bcu", "比特币");
        coinZhNameMap.put("btcnextweek", "比特币");
        coinZhNameMap.put("btcquarter", "比特币");
        coinZhNameMap.put("btcweek", "比特币");
        coinZhNameMap.put("xbt", "比特币");
        coinZhNameMap.put("xbtu17", "比特币");
        coinZhNameMap.put("bts", "比特股");
        coinZhNameMap.put("xcn", "氪石币");
        coinZhNameMap.put("zet", "泽塔币");
        coinZhNameMap.put("hlb", "活力币");
        coinZhNameMap.put("game", "游戏币");
        coinZhNameMap.put("ppc", "点点币");
        coinZhNameMap.put("etp", "熵");
        coinZhNameMap.put("puc", "爱瞳币");
        coinZhNameMap.put("pnc", "牡丹通宝");
        coinZhNameMap.put("xtc", "物联币");
        coinZhNameMap.put("doge", "狗狗币");
        coinZhNameMap.put("mtc", "猴宝币");
        coinZhNameMap.put("mc", "猴币");
        coinZhNameMap.put("xrp", "瑞波币");
        coinZhNameMap.put("1st", "第一滴血");
        coinZhNameMap.put("zecs", "算力合约");
        coinZhNameMap.put("sys", "系统币");
        coinZhNameMap.put("sts", "红人豆");
        coinZhNameMap.put("rss", "红贝壳");
        coinZhNameMap.put("vrc", "维理币");
        coinZhNameMap.put("vtc", "绿币");
        coinZhNameMap.put("mryc", "美人鱼");
        coinZhNameMap.put("mec", "美卡币");
        coinZhNameMap.put("met", "美通币");
        coinZhNameMap.put("ftc", "羽毛币");
        coinZhNameMap.put("jbc", "聚宝币");
        coinZhNameMap.put("ktc", "肯特币");
        coinZhNameMap.put("ltc", "莱特币");
        coinZhNameMap.put("ltcnextweek", "莱特币");
        coinZhNameMap.put("ltcquarter", "莱特币");
        coinZhNameMap.put("ltcweek", "莱特币");
        coinZhNameMap.put("mcc", "行云币");
        coinZhNameMap.put("INF", "讯链币");
        coinZhNameMap.put("gooc", "谷壳币");
        coinZhNameMap.put("xpm", "质数币");
        coinZhNameMap.put("ncs", "资产股");
        coinZhNameMap.put("dash", "达世币");
        coinZhNameMap.put("dsh", "达世币");
        coinZhNameMap.put("elc", "选举链");
        coinZhNameMap.put("xyc", "逍遥币");
        coinZhNameMap.put("lmc", "邻萌宝");
        coinZhNameMap.put("rio", "里约币");
        coinZhNameMap.put("qtum", "量子链");
        coinZhNameMap.put("qtumeth", "量子链");
        coinZhNameMap.put("jsz", "金手指");
        coinZhNameMap.put("tic", "钛币");
        coinZhNameMap.put("xmr", "门罗币");
        coinZhNameMap.put("anc", "阿侬币");
        coinZhNameMap.put("xas", "阿希币");
        coinZhNameMap.put("ardr", "阿朵");
        coinZhNameMap.put("xsgs", "雪山古树");
        coinZhNameMap.put("xzc", "零币");
        coinZhNameMap.put("fid", "飞币");
        coinZhNameMap.put("skt", "鲨之信");
        coinZhNameMap.put("sak", "鲨鱼基金");
        coinZhNameMap.put("zgc", "黄金链");
        coinZhNameMap.put("ric", "黎曼币");
        coinZhNameMap.put("blk", "黑币");

        Set<String> yunbiCoinSet = new HashSet<>();
        yunbiCoinSet.add("btc");
        yunbiCoinSet.add("eth");
        yunbiCoinSet.add("etc");
        yunbiCoinSet.add("btc");
        yunbiCoinSet.add("qtum");
        yunbiCoinSet.add("eth");
        yunbiCoinSet.add("zec");
        yunbiCoinSet.add("dgd");
        yunbiCoinSet.add("rep");
        yunbiCoinSet.add("1st");
        yunbiCoinSet.add("sc");
        yunbiCoinSet.add("bts");
        yunbiCoinSet.add("ans");
        yunbiCoinSet.add("etc");
        yunbiCoinSet.add("gnt");
        yunbiCoinSet.add("gxs");
        yunbiCoinSet.add("eos");
        yunbiCoinSet.add("snt");
        yunbiCoinSet.add("bcc");
        yunbiCoinSet.add("bcc");
        yunbiCoinSet.add("omg");
        yunbiCoinSet.add("lun");
        yunbiCoinSet.add("pay");
        marketCoinMap.put("yunbi", yunbiCoinSet);

        Set<String> huobiCoinSet = new HashSet<>();
        huobiCoinSet.add("btc");
        huobiCoinSet.add("ltc");
        huobiCoinSet.add("eth");
        huobiCoinSet.add("etc");
        marketCoinMap.put("huobi", huobiCoinSet);

        Set<String> jubiCoinSet = new HashSet<>();
        jubiCoinSet.add("btc");
        jubiCoinSet.add("ltc");
        jubiCoinSet.add("eth");
        jubiCoinSet.add("etc");
        jubiCoinSet.add("btc");
        jubiCoinSet.add("ltc");
        jubiCoinSet.add("eth");
        jubiCoinSet.add("etc");
        jubiCoinSet.add("xas");
        jubiCoinSet.add("xrp");
        jubiCoinSet.add("doge");
        jubiCoinSet.add("blk");
        jubiCoinSet.add("lsk");
        jubiCoinSet.add("game");
        jubiCoinSet.add("hlb");
        jubiCoinSet.add("xpm");
        jubiCoinSet.add("ifc");
        jubiCoinSet.add("eac");
        jubiCoinSet.add("plc");
        jubiCoinSet.add("ppc");
        jubiCoinSet.add("zet");
        jubiCoinSet.add("max");
        jubiCoinSet.add("tfc");
        jubiCoinSet.add("fz");
        jubiCoinSet.add("vtc");
        jubiCoinSet.add("zcc");
        jubiCoinSet.add("jbc");
        jubiCoinSet.add("wdc");
        jubiCoinSet.add("vrc");
        jubiCoinSet.add("dnc");
        jubiCoinSet.add("nxt");
        jubiCoinSet.add("gooc");
        jubiCoinSet.add("mtc");
        jubiCoinSet.add("qec");
        jubiCoinSet.add("lkc");
        jubiCoinSet.add("met");
        jubiCoinSet.add("ytc");
        jubiCoinSet.add("rss");
        jubiCoinSet.add("rio");
        jubiCoinSet.add("skt");
        jubiCoinSet.add("ans");
        jubiCoinSet.add("bts");
        jubiCoinSet.add("ktc");
        jubiCoinSet.add("peb");
        jubiCoinSet.add("pgc");
        jubiCoinSet.add("xsgs");
        jubiCoinSet.add("mryc");
        jubiCoinSet.add("mcc");
        jubiCoinSet.add("eos");
        jubiCoinSet.add("ugt");
        jubiCoinSet.add("tic");
        jubiCoinSet.add("bcc");
        jubiCoinSet.add("ico");
        jubiCoinSet.add("btk");
        jubiCoinSet.add("elc");
        jubiCoinSet.add("qtum");
        marketCoinMap.put("jubi", jubiCoinSet);

        Set<String> bterCoinSet = new HashSet<>();
        bterCoinSet.add("btc");
        bterCoinSet.add("ltc");
        bterCoinSet.add("eth");
        bterCoinSet.add("etc");
        bterCoinSet.add("doge");
        bterCoinSet.add("etp");
        bterCoinSet.add("eth");
        bterCoinSet.add("ppc");
        bterCoinSet.add("nxt");
        bterCoinSet.add("nmc");
        bterCoinSet.add("xcp");
        bterCoinSet.add("ftc");
        bterCoinSet.add("xpm");
        bterCoinSet.add("ifc");
        bterCoinSet.add("tix");
        bterCoinSet.add("tips");
        bterCoinSet.add("cnc");
        bterCoinSet.add("btq");
        bterCoinSet.add("dash");
        bterCoinSet.add("bat");
        bterCoinSet.add("bts");
        bterCoinSet.add("zec");
        bterCoinSet.add("qtum");
        bterCoinSet.add("rep");
        bterCoinSet.add("snt");
        bterCoinSet.add("eos");
        bterCoinSet.add("ico");
        bterCoinSet.add("xtc");
        bterCoinSet.add("btm");
        bterCoinSet.add("pay");
        bterCoinSet.add("cvc");
        bterCoinSet.add("storj");
        bterCoinSet.add("omg");
        bterCoinSet.add("bcc");
        marketCoinMap.put("bter", bterCoinSet);
    }
}
