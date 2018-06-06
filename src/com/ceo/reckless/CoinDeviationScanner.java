package com.ceo.reckless;

import com.ceo.reckless.entity.DeviationEntity;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.helper.AicoinDataHelper;
import com.ceo.reckless.strategy.Deviation;
import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.LogUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoinDeviationScanner {

    private static final String TAB_DEVIDER = "\t";

    public static void scanMultiPeriodDeviation(String inputFilePath) {

        // 默认挑选背离的时间周期集合
        //String[] periodArray = {"5m", "30m", "4h"};
        String[] periodArray = {"1h"};

        // 币种symbol list
        List<String> symbolListToQuery = new ArrayList<>();

        // 交易所过滤
        String pre = "^(huobipro|binance|bitfinex|bittrex|gate)";    // 先暂时去掉okex(深度差)
        String mid = ".*";
        // 交易对过滤
        String suf = "(usdt|usd)$";  // 背离扫描只关注usdt usd(期货)交易对儿

        String regexTotal = pre + mid + suf;
        Pattern patternTotal = Pattern.compile(regexTotal);
        String regexPrefix = pre;
        Pattern patternPrefix = Pattern.compile(regexPrefix);
        String regexSuffix = suf;
        Pattern patternSuffix = Pattern.compile(regexSuffix);

        // 读取symbol文件并遍历symbol
        File inputFile = new File(inputFilePath);
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

            // 挑选出要判断背离的symbol
            symbolListToQuery.add(itemLine);
        }

        Map<String, Map<DeviationEntity, Long>> totalPeriodDeviationMapMap = new HashMap<>();
        // 按照时间周期分别筛选背离symbol
        for (String itemPeriod : periodArray) {

            Map<DeviationEntity, Long> itemDeviationMap = new HashMap<>();
            // 判断背离
            scanDeviation(itemPeriod, symbolListToQuery, itemDeviationMap);

            if (itemDeviationMap != null && itemDeviationMap.size() != 0) {

                // 按value来排序整个map
                itemDeviationMap = SortMapByValue.sortMap(itemDeviationMap);

                totalPeriodDeviationMapMap.put(itemPeriod, itemDeviationMap);
            }
        }

        // 输出几种时间级别的背离情况
        for (String itemPeriod : periodArray) {
            LogUtils.logDebugLine("");
            LogUtils.logDebugLine("============== " + itemPeriod + " ==============");

            // 获取该时间级别的背离结果map
            Map<DeviationEntity, Long> itemDeviationMap = totalPeriodDeviationMapMap.get(itemPeriod);
            if (itemDeviationMap == null || itemDeviationMap.size() == 0) {
                continue;
            }

            for (Map.Entry<DeviationEntity, Long> itemEntry : itemDeviationMap.entrySet()) {

                DeviationEntity itemDe = itemEntry.getKey();
                String symbol = itemDe.symbol;

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

                itemDe.coin = coin;
                itemDe.exchange = exchange;
                itemDe.exchangeFrom = lastCoin;

                LogUtils.logDebugLine(itemDe.toOutputString());
            }
        }
    }

    /**
     * 筛选背离情况币种并将DeviationEntity和timeDiff值塞入map中(已按value排序,时间由近至远)
     * @param periodType
     * @param symbolList
     * @param deviationMap
     */
    public static void scanDeviation(String periodType, List<String> symbolList, Map<DeviationEntity, Long> deviationMap) {
        for (String itemSymbol : symbolList) {
            LogUtils.logDebugLine("symbol : " + itemSymbol);

            List<KEntity> kEntityList = AicoinDataHelper.requestKLineBySymbol(itemSymbol, periodType, 0);

            DeviationEntity de3 = new DeviationEntity();
            boolean isLastPointDeviation = false;
            boolean b1 = Deviation.isDeviation(Deviation.TYPE_LINE, Deviation.TYPE_BOTTOM, kEntityList, de3, isLastPointDeviation);
            if (b1) {
                de3.symbol = itemSymbol;
                de3.deviationTypeLineBar = Deviation.TYPE_LINE;
                de3.deviationTypeTopBtm = Deviation.TYPE_BOTTOM;
                deviationMap.put(de3, de3.timeDiffFromCur);
            }

            isLastPointDeviation = false;
            DeviationEntity de4 = new DeviationEntity();
            boolean b2 = Deviation.isDeviation(Deviation.TYPE_BAR, Deviation.TYPE_BOTTOM, kEntityList, de4, isLastPointDeviation);
            if (b2) {
                de4.symbol = itemSymbol;
                de4.deviationTypeLineBar = Deviation.TYPE_BAR;
                de4.deviationTypeTopBtm = Deviation.TYPE_BOTTOM;
                deviationMap.put(de4, de4.timeDiffFromCur);
            }
            // aicoin可能有反爬虫策略,增加一个sleep
            try {
                Thread.sleep(AicoinDataHelper.AICOIN_HTTP_QUERY_INTERVAL);    // 10s、2s、1s、500ms、300ms是可以的 100ms不太稳
            } catch (Exception e) {
            }
        }
    }

    public static void main(String[] args) {
        String basePath = System.getProperty("user.dir");

        String inputFileName = "symbols.txt";
        String inputFilePath = basePath + File.separator + inputFileName;

        scanMultiPeriodDeviation(inputFilePath);
    }
}

class SortMapByValue {

    public static Map<DeviationEntity, Long> sortMap(Map<DeviationEntity, Long> unsortedMap) {
        Comparator<DeviationEntity> comparator = new ValueComparator<DeviationEntity, Long>(unsortedMap);
        TreeMap<DeviationEntity, Long> result = new TreeMap<DeviationEntity, Long>(comparator);
        result.putAll(unsortedMap);

        return result;
    }
}

class ValueComparator<DeviationEntity, Long extends  Comparable<Long>> implements Comparator<DeviationEntity> {
    HashMap<DeviationEntity, Long> map = new HashMap<DeviationEntity, Long>();

    public ValueComparator(Map<DeviationEntity, Long> map) {
        this.map.putAll(map);
    }

    @Override
    public int compare(DeviationEntity s1, DeviationEntity s2) {
        return map.get(s1).compareTo(map.get(s2));
    }
}
