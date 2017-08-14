package com.ceo.reckless;

import com.ceo.reckless.helper.SosobtcDataHelper;
import com.ceo.reckless.utils.LogUtils;
import org.apache.commons.cli.*;

public class Main {

    public static boolean MAIN_DEBUG = false;

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
            MarketWaveMotionScanner.outputResistanceSupport(market, coin, typeNum, 0);
        } else {
            LogUtils.logDebugLine("funding usage:\n -m yunbi -c eos -t 5m");
        }
    }

    private static void callTopDeviation(CommandLine cmd) {
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

            MarketWaveMotionScanner.scanDeviation(market, coin, typeNum, since, endTime, outputFileName);
        } else {
            LogUtils.logDebugLine("top_deviation usage:\n -m yunbi -c eos -t 5m -s 1502382000 -e 1503382000 -o output.html");
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
            LogUtils.logDebugLine("funding usage:\n -m yunbi -c eos -t 5m -s 1502382000 -d \"#.00\" -o output.html");
        }
    }

    private static void testMain(String args[]) {
        MarketWaveMotionScanner.scanDeviation("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_15_MIN, 0, 0, "dif_k_spot_line.html");
        MarketWaveMotionScanner.outputResistanceSupport("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_2_HOUR, 0);

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
                    case "top_deviation":
                        callTopDeviation(cmd);
                        break;
                    case "resistance_support":
                        callResistanceSupport(cmd);
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
        LogUtils.logDebugLine("usage :\njava -jar parser.jar -f funding -m yunbi -c eos -t 5m -s 1502382000 -o output.html\n -f means function (funding,top_deviation,resistance_support)");
    }

    public static void main(String args[]) {
        if (MAIN_DEBUG) {
            testMain(args);
        } else {
            releaseMain(args);
        }
    }
}
