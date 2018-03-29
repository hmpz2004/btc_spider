package com.ceo.reckless.helper;

import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.utils.HttpRequest;
import com.ceo.reckless.utils.HttpUrlParamBuilder;
import com.ceo.reckless.utils.LogUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AicoinDataHelper {

    private static boolean AICOIN_DEBUG = false;

    public static String INDEX_URL = "https://www.aicoin.net.cn";

    // https://www.aicoin.net.cn/chart/api/data/period?symbol=huobiproeosusdt&step=180
    // step 表示请求的周期的秒数
    public static String URL_KLINE = "https://www.aicoin.net.cn/chart/api/data/period";

    private static final int BASE_PERIOD_INTERNAL_MINUTE = 60;
    private static final int BASE_PERIOD_INTERNAL_HOUR = 60 * BASE_PERIOD_INTERNAL_MINUTE;
    private static final int BASE_PERIOD_INTERNAL_DAY = 24 * BASE_PERIOD_INTERNAL_HOUR;

    public static int TYPE_LEVEL_1_MIN = BASE_PERIOD_INTERNAL_MINUTE;// 1分
    public static int TYPE_LEVEL_3_MIN = 3 * BASE_PERIOD_INTERNAL_MINUTE;// 3分
    public static int TYPE_LEVEL_5_MIN = 5 * BASE_PERIOD_INTERNAL_MINUTE;// 5分
    public static int TYPE_LEVEL_10_MIN = 10 * BASE_PERIOD_INTERNAL_MINUTE;// 10分
    public static int TYPE_LEVEL_15_MIN = 15 * BASE_PERIOD_INTERNAL_MINUTE;// 15分
    public static int TYPE_LEVEL_30_MIN = 30 * BASE_PERIOD_INTERNAL_MINUTE;// 30分
    public static int TYPE_LEVEL_1_HOUR = BASE_PERIOD_INTERNAL_HOUR;// 1小时
    public static int TYPE_LEVEL_2_HOUR = 2 * BASE_PERIOD_INTERNAL_HOUR;// 2小时
    public static int TYPE_LEVEL_4_HOUR = 4 * BASE_PERIOD_INTERNAL_HOUR;// 4小时
    public static int TYPE_LEVEL_6_HOUR = 6 * BASE_PERIOD_INTERNAL_HOUR;// 6小时
    public static int TYPE_LEVEL_12_HOUR = 12 * BASE_PERIOD_INTERNAL_HOUR;// 12小时
    public static int TYPE_LEVEL_1_DAY = BASE_PERIOD_INTERNAL_DAY;// 1天
    public static int TYPE_LEVEL_3_DAY = 3 * BASE_PERIOD_INTERNAL_DAY;// 3天
    public static int TYPE_LEVEL_1_WEEK = 7 * BASE_PERIOD_INTERNAL_DAY;// 1周

    public static Map<String, Integer> periodTypeMap = new HashMap<>();
    static {
        periodTypeMap.put("1m", TYPE_LEVEL_1_MIN);// 1分
        periodTypeMap.put("3m", TYPE_LEVEL_3_MIN);// 3分
        periodTypeMap.put("5m", TYPE_LEVEL_5_MIN);// 5分
        periodTypeMap.put("10m", TYPE_LEVEL_10_MIN);// 10分
        periodTypeMap.put("15m", TYPE_LEVEL_15_MIN);// 15分
        periodTypeMap.put("30m", TYPE_LEVEL_30_MIN);// 30分
        periodTypeMap.put("1h", TYPE_LEVEL_1_HOUR);// 1小时
        periodTypeMap.put("2h", TYPE_LEVEL_2_HOUR);// 2小时
        periodTypeMap.put("4h", TYPE_LEVEL_4_HOUR);// 4小时
        periodTypeMap.put("6h", TYPE_LEVEL_6_HOUR);// 6小时
        periodTypeMap.put("12h", TYPE_LEVEL_12_HOUR);// 12小时
        periodTypeMap.put("1d", TYPE_LEVEL_1_DAY);// 1天
        periodTypeMap.put("3d", TYPE_LEVEL_3_DAY);// 3天
        periodTypeMap.put("1w", TYPE_LEVEL_1_WEEK);// 1周
    }

    /**
     * since参数如果放入http中请求,server只会返回固定100条数据,可能不够用,则since筛选时间戳放在数据解析部分
     * @param market
     * @param targetCoin
     * @param srcCoin
     * @param periodType
     * @param since
     * @return
     */
    private static String httpQueryKData(String market, String targetCoin, String srcCoin, String periodType, long since) {
        try {

            if (srcCoin == null || srcCoin.equals("")) {
                srcCoin = "usdt";
            }

            /**
             * huobiproeosusdt
             * huobiproeosusdt
             * symbol格式:
             * market + 交易币种(target coin) + 购买币种(src coin)
             */
            String symbol = market + targetCoin + srcCoin;

            return httpQueryKData(symbol, periodType, since);
        } catch (Exception e) {
            LogUtils.logError(e);
        }
        return null;
    }


    private static String httpQueryKData(String symbol, String periodType, long since) {

        try {

            /**
             * 以秒数为单位,计算周期的step值
             */
            String step = String.valueOf(TYPE_LEVEL_30_MIN);
            if (periodTypeMap.containsKey(periodType)) {
                step = String.valueOf(periodTypeMap.get(periodType));
            } else {
                LogUtils.logDebugLine("period type err");
                return null;
            }

            String sinceStr = "";

            HttpUrlParamBuilder upb = new HttpUrlParamBuilder();
            upb.appendParam("symbol", symbol);
            upb.appendParam("step", step);
            if (since != 0) {
                sinceStr = String.valueOf(since);
                upb.appendParam("since", sinceStr);
            }
            String httpUrlParamString = upb.formatUrlParamString();

            Map<String, String> propMap = new HashMap<>();
            propMap.put("Accept", "*/*");
            propMap.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            // propMap.put("Accept-Encoding", "gzip, deflate, br");
            propMap.put("Connection", "keep-alive");
            propMap.put("Host", "www.aicoin.net.cn");
            propMap.put("Referer", "https://www.aicoin.net.cn/chart/5C79AC2D");
            propMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            // propMap.put("", "");

            // return HttpRequest.sendGetWithCookie(URL_KLINE, httpUrlParamString, INDEX_URL, null);
            return HttpRequest.sendGetWithCookie(URL_KLINE, httpUrlParamString, propMap, INDEX_URL, null);

        } catch (Exception e) {
            LogUtils.logError(e);
        }
        return null;
    }

    private static List<KEntity> parseKlineToList(String jsonContentString, long since) {
        try {

            if (jsonContentString != null && jsonContentString.length() != 0) {

                List<KEntity> resultList = new ArrayList<>();

                if (AICOIN_DEBUG) {
                    LogUtils.logDebugLine("response : " + jsonContentString);
                }

                JSONObject joTotal = new JSONObject(jsonContentString);

                if (joTotal.has("data")) {
                    JSONArray jaData = joTotal.getJSONArray("data");
                    if (jaData != null && jaData.length() != 0) {

                        JSONArray jaItem = null;

                        for (int i = 0; i < jaData.length(); i++) {
                            jaItem = jaData.getJSONArray(i);
                            if (jaItem != null && jaItem.length() != 0) {
                                if (jaItem.length() == 6) {
                                    long ts = jaItem.getLong(0);
                                    if (since == 0 || (since != 0 && ts >= since)) {
                                        // 获取每单位时间内的open high等
                                        KEntity kee = new KEntity();
                                        kee.timestamp = ts;
                                        kee.open = jaItem.getDouble(1);
                                        kee.high = jaItem.getDouble(2);
                                        kee.low = jaItem.getDouble(3);
                                        kee.close = jaItem.getDouble(4);
                                        kee.volume = jaItem.getDouble(5);

                                        resultList.add(kee);
                                    }
                                } else {
                                    LogUtils.logDebugLine("k point err at index " + i);
                                }
                            }
                        }
                        if (AICOIN_DEBUG) {
                            LogUtils.logDebugLine("data size : " + jaData.length());
                        }

                        return resultList;
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logError(e);
        }

        return null;
    }

    public static List<KEntity> requestKLine(String market, String targetCoin, String srcCoin, String periodType, long since) {

        List<KEntity> list = null;

        String res = httpQueryKData(market, targetCoin, srcCoin, periodType, 0);
        if (AICOIN_DEBUG) {
            LogUtils.logDebugLine(res);
        }
        if (res != null && !res.equals("")) {
            list = parseKlineToList(res, since);
        }
        return list;
    }

    public static List<KEntity> requestKLineBySymbol(String symbol, String periodType, long since) {

        List<KEntity> list = null;

        String res = httpQueryKData(symbol, periodType, 0);
        if (res != null && !res.equals("")) {
            list = parseKlineToList(res, since);
        }
        return list;
    }

    public static void main(String[] args) {

        String market = "huobipro";
        String targetCoin = "eos";
        String srcCoin = "usdt";
        String periodType = "5m";
        long since = 0;

        List<KEntity> list = requestKLine(market, targetCoin, srcCoin, periodType, since);

        for (KEntity kitem : list) {
            LogUtils.logDebugLine(kitem.toString());
        }
    }
}
