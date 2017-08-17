package com.ceo.reckless;

import com.ceo.reckless.helper.SosobtcDataHelper;
import com.ceo.reckless.utils.LogUtils;
import org.apache.commons.cli.*;

import java.text.DecimalFormat;
import java.util.*;

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

    private static void callFunding(CommandLine cmd) {
        if (cmd.hasOption("m") &&
                cmd.hasOption("c") &&
                cmd.hasOption("t") &&
                cmd.hasOption("d") &&
                cmd.hasOption("o")) {
            String market = cmd.getOptionValue("m");
            String coin = cmd.getOptionValue("c");
            String type = cmd.getOptionValue("t");
            int typeNum = 0;
            if (SosobtcDataHelper.typeMap.containsKey(type)) {
                typeNum = SosobtcDataHelper.typeMap.get(type);
            } else {
                LogUtils.logDebugLine("time type error");
            }
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

            htmlTitle = coin + "_" + market + "_" + type;

            new FundingDistributionScanner(needDecimal, price_formatter, baseDivisor, htmlTitle).genFundingChart(market, coin, typeNum, since, outputName);
        } else {
            LogUtils.logDebugLine("funding usage:\n -m yunbi -c eos -t 5m -s 1502382000 -d \"#.00\" -o output.html\n -m yunbi -c eos -t 5m -d \"#.00\" -o eos_yunbi.html");
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

    private static void testMain(String args[]) {
        MarketWaveMotionScanner.scanDeviation("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_15_MIN, 0, 0, "dif_k_spot_line.html", null);
        MarketWaveMotionScanner.scanResistanceSupport("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_2_HOUR, 0, null, null);

//        FundingDistributionScanner.needDecimal = false;
//        FundingDistributionScanner.baseDivisor = 100;
//        FundingDistributionScanner.genFundingChart("viabtc", "bcc", SosobtcDataHelper.TYPE_LEVEL_1_HOUR, 1502038836, "bcc_viabtc.html");
    }

    private static void releaseMain(String[] args) {

        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("v", "verbose", false, "Print out VERBOSE information");
        options.addOption("f", "function", true, "Print out VERBOSE information");
        options.addOption("m", "market", true, "");
        options.addOption("c", "coin", true, "");
        options.addOption("t", "time type", true, "");
        options.addOption("s", "since", true, "");
        options.addOption("d", "divisor", true, "");
        options.addOption("o", "output file name", true, "File to save program output to");
        options.addOption("e", "endtime", true, "");
        options.addOption("a", "all", true, "");
//        options.addOption("i", "increase drop", true, "");

        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                outputUsage();
            } else if (cmd.hasOption("v")) {
                LogUtils.logDebugLine("version : 1.1 by 霸道总裁预科班\nwx:417249073");
            } else if (cmd.hasOption("f")) {
                String functionString = cmd.getOptionValue("f");
                switch (functionString) {
                    case "funding" :
                        callFunding(cmd);
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
        LogUtils.logDebugLine("-f funding -m huobi -c etc -t 1h -d 1 -o etc_huobi.html");
        LogUtils.logDebugLine("-f resistance_support -m yunbi -c eos -t 5m");
        LogUtils.logDebugLine("-f scan_resistance_support -m yunbi -t 5m");
        LogUtils.logDebugLine("-f increase_drop -m yunbi -o yunbi_ins.html");
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
