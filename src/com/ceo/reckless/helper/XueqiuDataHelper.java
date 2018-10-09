package com.ceo.reckless.helper;

import com.ceo.reckless.entity.HKStockRankEntity;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.entity.SHStockRankEntity;
import com.ceo.reckless.utils.HttpRequest;
import com.ceo.reckless.utils.HttpUrlParamBuilder;
import com.ceo.reckless.utils.LogUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.security.jgss.krb5.Krb5NameElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XueqiuDataHelper {

    private static boolean XUEQIU_DEBUG = true;

    public static String INDEX_URL = "https://xueqiu.com";

    // -------------------------------------------
    // 个股参数

    // 个股K线(港股A股通用)
    // 老的url : "https://xueqiu.com/stock/forchartk/stocklist.json";
    public static String K_LINE_URL = "https://stock.xueqiu.com/v5/stock/chart/kline.json";

    // 个股信息
    public static String STOCK_INFO_URL = "https://xueqiu.com/v4/stock/quote.json";

    public static final String PARAM_KEY_SYMBOL = "symbol";
    public static final String PARAM_KEY_PERIOD = "period";
    public static final String PARAM_KEY_TYPE = "type";
    public static final String PARAM_KEY_BEGIN = "begin";
    public static final String PARAM_KEY_END = "end";
    public static final String PARAM_KEY_COUNT = "count";
    public static final String PARAM_KEY_INDICATOR = "indicator";
    public static final String PARAM_VALUE_INDICATOR = "kline,ma,macd";

    // A股类型齐全
    // 港股只有分时、日线、周线、月线
    //<<>>type发生了变化,需要逐个check一下
    public static final String PERIOD_1_DAY = "day";
    public static final String PERIOD_1_WEEK = "week";
    public static final String PERIOD_1_MONTH = "month";
    public static final String PERIOD_1_MIN = "1m";
    public static final String PERIOD_5_MIN = "5m";
    public static final String PERIOD_15_MIN = "15m";
    public static final String PERIOD_30_MIN = "30m";
    public static final String PERIOD_60_MIN = "60m";
    public static final String PERIOD_REAL_TIME = "5d";     // 分时

    public static Map<String, String> periodTypeMap = new HashMap<>();

    // 存储type字符串和真实对应时长的映射
    public static Map<String, Long> periodTypeTimeIntervalMap = new HashMap<>();

    public static final long ONE_SECOND = 1000L;
    // 默认请求的k线的个数
    public static final int DEFAULT_K_NUM = 500;

    static {
        periodTypeMap.put("0", PERIOD_REAL_TIME);// 分时
        periodTypeTimeIntervalMap.put("0", ONE_SECOND);
        periodTypeMap.put("1m", PERIOD_1_MIN);// 1分
        periodTypeTimeIntervalMap.put("1m", 60*ONE_SECOND);
        periodTypeMap.put("5m", PERIOD_5_MIN);// 5分
        periodTypeTimeIntervalMap.put("5m", 5*60*ONE_SECOND);
        periodTypeMap.put("15m", PERIOD_15_MIN);// 15分
        periodTypeTimeIntervalMap.put("15m", 15*60*ONE_SECOND);
        periodTypeMap.put("30m", PERIOD_30_MIN);// 30分
        periodTypeTimeIntervalMap.put("30m", 30*60*ONE_SECOND);
        periodTypeMap.put("1h", PERIOD_60_MIN);// 1小时
        periodTypeTimeIntervalMap.put("1h", 60*60*ONE_SECOND);
        periodTypeMap.put("1d", PERIOD_1_DAY);// 1天
        periodTypeTimeIntervalMap.put("1d", 24*60*60*ONE_SECOND);
        periodTypeMap.put("1w", PERIOD_1_WEEK);// 1周
        periodTypeTimeIntervalMap.put("1w", 7*24*60*60*ONE_SECOND);
    }

    public static final String TYPE_NORMAL = "normal";
    public static final String TYPE_BEFORE = "before";

    public static final long BEGIN_INTERVAL = 5 * 24 * 3600 * ONE_SECOND;

    // -------------------------------------------
    // 股市行情参数

    // 港股行情排行榜
    public static String HK_STOCK_RANK_LIST_URL = "https://xueqiu.com/stock/cata/stocklist.json";

    // 沪A行情排行榜
    public static String SHA_STOCK_RANK_LIST_URL = "https://xueqiu.com/stock/quote_order.json";

    public static final String PARAM_KEY_PAGE = "page";
    public static final String PARAM_KEY_SIZE = "size";         // 雪球最大会返回100个记录
    public static final String PARAM_KEY_ORDER = "order";
    public static final String PARAM_KEY_ORDERBY_HK = "orderby";
    public static final String PARAM_KEY_TYPE_HK = "type";
    public static final String PARAM_KEY_ISDELAY_HK = "isdelay";
    public static final String PARAM_KEY_TIMESTAMP = "_";

    public static final String PARAM_KEY_EXCHANGE_SHA = "exchange";
    public static final String PARAM_KEY_ORDERBY_SHA = "orderBy";
    public static final String PARAM_KEY_STOCKTYPE_SHA = "stockType";
    public static final String PARAM_KEY_COLUMN_SHA = "column";

    public static final String CONST_VALUE_COLUMN_SHA = "symbol%2Cname%2Ccurrent%2Cchg%2Cpercent%2Clast_close%2Copen%2Chigh%2Clow%2Cvolume%2Camount%2Cmarket_capital%2Cpe_ttm%2Chigh52w%2Clow52w%2Chasexist";

    public static final String ORDER_DESC = "desc";
    public static final String ORDER_ASC = "asc";
    public static final String ORDERBY_PERCENT = "percent";     // 涨跌幅
    public static final String ORDERBY_VOLUME = "volume";       // 成交量
    public static final String ORDERBY_AMOUNT = "amount";       // 成交额
    public static final String ISDELAY_1 = "1";
    public static final String TYPE_30 = "30";

    public static final String STOCKTYPE_SHA = "sha";
    public static final String STOCKTYPE_SZA = "sza";
    public static final String EXCHANGE_CN = "CN";

    public static void requestXueqiuIndex() {
        String res = HttpRequest.sendGet(INDEX_URL, null);
        if (XUEQIU_DEBUG) {
            LogUtils.logDebugLine(res);
        }
    }

    public static List<KEntity> requestStockDefaultKLine(String symbol, String periodType) {

        long cur = System.currentTimeMillis();

        return requestStockDefaultKLine(symbol, periodType, cur);
    }

    public static List<KEntity> requestStockDefaultKLine(String symbol, String periodType, long endTimeStamp) {

        String period = periodTypeMap.get(periodType);

        long cur = System.currentTimeMillis();

        if (endTimeStamp == 0) {
            endTimeStamp = cur;
        }

        // 根据请求k的根数计算k线的begin time
        long defaultBeginInterval = periodTypeTimeIntervalMap.get(periodType);
        long inter = DEFAULT_K_NUM * defaultBeginInterval;

        long begin = cur - inter;
        HttpUrlParamBuilder ut = new HttpUrlParamBuilder();
        ut.appendParam(PARAM_KEY_SYMBOL, symbol);
        ut.appendParam(PARAM_KEY_BEGIN, String.valueOf(begin));
        ut.appendParam(PARAM_KEY_PERIOD, period);
        ut.appendParam(PARAM_KEY_TYPE, TYPE_BEFORE);
        // 新版url可以不传end
        // ut.appendParam(PARAM_KEY_END, String.valueOf(endTimeStamp));
        ut.appendParam(PARAM_KEY_COUNT, "-" + DEFAULT_K_NUM);
        ut.appendParam(PARAM_KEY_INDICATOR, PARAM_VALUE_INDICATOR);

        String urlParamString = ut.formatUrlParamString();

        String res = HttpRequest.sendGetWithCookie(K_LINE_URL, urlParamString, INDEX_URL, null);
        if (XUEQIU_DEBUG) {
            LogUtils.logDebugLine(res);
        }

        List<KEntity> list = parseKlineToList(res);
        return list;
    }

    public static List<SHStockRankEntity> requestAStockDefaultRankList(String orderType, String stockType) {
        try {
            int itemCount = 100;

            HttpUrlParamBuilder baseUb = new HttpUrlParamBuilder();
            baseUb.appendParam(PARAM_KEY_SIZE, String.valueOf(itemCount));
            baseUb.appendParam(PARAM_KEY_ORDER, orderType);
            baseUb.appendParam(PARAM_KEY_EXCHANGE_SHA, EXCHANGE_CN);
            baseUb.appendParam(PARAM_KEY_STOCKTYPE_SHA, stockType);
            baseUb.appendParam(PARAM_KEY_COLUMN_SHA, CONST_VALUE_COLUMN_SHA);
            baseUb.appendParam(PARAM_KEY_ORDERBY_SHA, ORDERBY_VOLUME);
            baseUb.appendParam(PARAM_KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

            int size = 0;
            HttpUrlParamBuilder sizeUb = new HttpUrlParamBuilder(baseUb);
            sizeUb.appendParam(PARAM_KEY_PAGE, "1");
            String sizeUps = sizeUb.formatUrlParamString();
            String sizeRes = HttpRequest.sendGetWithCookie(SHA_STOCK_RANK_LIST_URL, sizeUps, INDEX_URL, null);

            // 获取记录数
            JSONObject joTotal = new JSONObject(sizeRes);
            if (joTotal.has("count")) {
                size = joTotal.getInt("count");
                if (XUEQIU_DEBUG) {
                    LogUtils.logDebugLine("page json size " + size);
                }
            }
            if (size == 0) {
                return null;
            }

            // 分页查询
            List<SHStockRankEntity> totalList = new ArrayList<>();
            int pageSize = (size + itemCount) / itemCount;
            for (int i = 1; i <= pageSize; i++) {
                HttpUrlParamBuilder pageUb = new HttpUrlParamBuilder(baseUb);
                pageUb.appendParam(PARAM_KEY_PAGE, String.valueOf(i));
                String pageUps = pageUb.formatUrlParamString();
                String pageRes = HttpRequest.sendGetWithCookie(SHA_STOCK_RANK_LIST_URL, pageUps, INDEX_URL, null);
                List<SHStockRankEntity> pageList = parseAStockRankToList(pageRes);
                totalList.addAll(pageList);
            }

            if (XUEQIU_DEBUG) {
                LogUtils.logDebugLine("total page rank rec size " + totalList.size());
            }

            return totalList;

        } catch (Exception e) {
            LogUtils.logError(e);
        }

        return null;
    }

    // 获取沪A股排名
    public static List<SHStockRankEntity> requestAStockDefaultRankList(String orderType) {

        List<SHStockRankEntity> listSha = requestAStockDefaultRankList(orderType, STOCKTYPE_SHA);
        List<SHStockRankEntity> listSza = requestAStockDefaultRankList(orderType, STOCKTYPE_SZA);

        List<SHStockRankEntity> totalList = new ArrayList<>();
        totalList.addAll(listSha);
        totalList.addAll(listSza);

        return totalList;
    }

    // 获取港股排名
    public static List<HKStockRankEntity> requestHKStockDefaultRankList(String orderType) {
        try {
            int itemCount = 100;

            HttpUrlParamBuilder baseUb = new HttpUrlParamBuilder();
            baseUb.appendParam(PARAM_KEY_SIZE, String.valueOf(itemCount));
            baseUb.appendParam(PARAM_KEY_ORDER, ORDER_DESC);
            baseUb.appendParam(PARAM_KEY_ORDERBY_HK, orderType);
            baseUb.appendParam(PARAM_KEY_TYPE_HK, TYPE_30);
            baseUb.appendParam(PARAM_KEY_ISDELAY_HK, ISDELAY_1);
            baseUb.appendParam(PARAM_KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

            int size = 0;
            HttpUrlParamBuilder sizeUb = new HttpUrlParamBuilder(baseUb);
            sizeUb.appendParam(PARAM_KEY_PAGE, "1");
            String sizeUps = sizeUb.formatUrlParamString();
            String sizeRes = HttpRequest.sendGetWithCookie(HK_STOCK_RANK_LIST_URL, sizeUps, INDEX_URL, null);

            // 获取记录数
            JSONObject joTotal = new JSONObject(sizeRes);
            if (joTotal.has("count")) {
                size = joTotal.getJSONObject("count").getInt("count");
                if (XUEQIU_DEBUG) {
                    LogUtils.logDebugLine("page json size " + size);
                }
            }
            if (size == 0) {
                return null;
            }

            // 分页查询
            List<HKStockRankEntity> totalList = new ArrayList<>();
            int pageSize = (size + itemCount) / itemCount;
            for (int i = 1; i <= pageSize; i++) {
                HttpUrlParamBuilder pageUb = new HttpUrlParamBuilder(baseUb);
                pageUb.appendParam(PARAM_KEY_PAGE, String.valueOf(i));
                String pageUps = pageUb.formatUrlParamString();
                String pageRes = HttpRequest.sendGetWithCookie(HK_STOCK_RANK_LIST_URL, pageUps, INDEX_URL, null);
                List<HKStockRankEntity> pageList = parseHKStockRankToList(pageRes);
                totalList.addAll(pageList);
            }

            // 倒序输出、确认下排序是否正确
//            for (int i = totalList.size() - 1; i > totalList.size() - 100; i--) {
//                HKStockRankEntity he = totalList.get(i);
//                LogUtils.logDebugLine(he.code);
//            }

            if (XUEQIU_DEBUG) {
                LogUtils.logDebugLine("total page rank rec size " + totalList.size());
            }

            return totalList;
        } catch (Exception e) {
            LogUtils.logError(e);
        }

        return null;
    }

    public static void requestStocklist(String orderType) {
        HttpUrlParamBuilder ut = new HttpUrlParamBuilder();
        ut.appendParam(PARAM_KEY_PAGE, "1");
        ut.appendParam(PARAM_KEY_SIZE, "60");
        ut.appendParam(PARAM_KEY_ORDER, ORDER_DESC);
        ut.appendParam(PARAM_KEY_ORDERBY_HK, ORDERBY_PERCENT);
        ut.appendParam(PARAM_KEY_TYPE_HK, TYPE_30);
        ut.appendParam(PARAM_KEY_ISDELAY_HK, ISDELAY_1);
        ut.appendParam(PARAM_KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        String urlParamString = ut.formatUrlParamString();
        if (XUEQIU_DEBUG) {
            LogUtils.logDebugLine(urlParamString);
        }

        String res = HttpRequest.sendGet(HK_STOCK_RANK_LIST_URL, urlParamString);
        if (XUEQIU_DEBUG) {
            LogUtils.logDebugLine(res);
        }
    }

    // 解析K线数据
    private static List<KEntity> parseKlineToList(String jsonContentString) {
        try {

            if (jsonContentString != null && jsonContentString.length() != 0) {

                List<KEntity> resultList = new ArrayList<>();

                JSONObject joTotal = new JSONObject(jsonContentString);

                if (joTotal.has("data") && joTotal.has("error_code") && joTotal.has("error_description")) {

                    int errorCode = joTotal.getInt("error_code");
                    if (errorCode != 0) {
                        // 输出response中的错误desc
                        LogUtils.logDebugLine("request error : " + joTotal.getString("error_description"));
                        return resultList;
                    }
                    JSONObject joData = joTotal.getJSONObject("data");
                    if (joData != null) {
                        JSONArray jaItem = joData.getJSONArray("item");
                        for (int i = 0; i < jaItem.length(); i++) {
                            KEntity ke = new KEntity();
                            JSONArray jaValue = jaItem.getJSONArray(i);
                            if (jaValue.length() < 6) {
                                continue;
                            }
                            ke.timestamp = jaValue.getLong(0);
                            ke.volume = jaValue.getDouble(1);
                            ke.open = jaValue.getDouble(2);
                            ke.high = jaValue.getDouble(3);
                            ke.low = jaValue.getDouble(4);
                            ke.close = jaValue.getDouble(5);
                            resultList.add(ke);
                        }
                    }

                    return resultList;
                }
            }
        } catch (Exception e) {
            LogUtils.logError(e);
        }

        return null;
    }

    // 解析港股行情数据
    private static List<HKStockRankEntity> parseHKStockRankToList(String jsonContentString) {
        try {

            if (jsonContentString != null && jsonContentString.length() != 0) {

                List<HKStockRankEntity> resultList = new ArrayList<>();

                JSONObject joTotal = new JSONObject(jsonContentString);

                if (joTotal.has("success") && joTotal.has("stocks") && joTotal.has("count")) {
                    JSONArray jaStockRankList = joTotal.getJSONArray("stocks");
                    for (int i = 0; i < jaStockRankList.length(); i++) {
                        HKStockRankEntity sre = new HKStockRankEntity();
                        JSONObject joItem = jaStockRankList.getJSONObject(i);
                        sre.symbol = joItem.getString("symbol");
                        sre.code = joItem.getString("code");
                        sre.name = joItem.getString("name");
                        sre.current = joItem.getDouble("current");
                        sre.percent = joItem.getDouble("percent");
                        sre.change = joItem.getDouble("change");
                        sre.high = joItem.getDouble("high");
                        sre.low = joItem.getDouble("low");
                        sre.high52w = joItem.getDouble("high52w");
                        sre.low52w = joItem.getDouble("low52w");
                        sre.marketcapital = joItem.getDouble("marketcapital");
                        sre.amount = joItem.getDouble("amount");
                        sre.type = joItem.getDouble("type");
                        // sre.pettm = joItem.getDouble("pettm");
                        sre.volume = joItem.getDouble("volume");
                        sre.hasexist = joItem.getBoolean("hasexist");

                        resultList.add(sre);
                    }
                    return resultList;
                }
            }
        } catch (Exception e) {
            LogUtils.logError(e);
        }

        return null;
    }

    // 解析A股行情数据
    private static List<SHStockRankEntity> parseAStockRankToList(String jsonContentString) {
        try {

            if (jsonContentString != null && jsonContentString.length() != 0) {

                List<SHStockRankEntity> resultList = new ArrayList<>();

                JSONObject joTotal = new JSONObject(jsonContentString);

                if (joTotal.has("data") && joTotal.has("count")) {
                    JSONArray jaStockRankList = joTotal.getJSONArray("data");
                    for (int i = 0; i < jaStockRankList.length(); i++) {
                        SHStockRankEntity sre = new SHStockRankEntity();
                        JSONArray jaItem = jaStockRankList.getJSONArray(i);

                        sre.symbol = jaItem.getString(0);
                        sre.name = jaItem.getString(1);
                        sre.current = jaItem.getDouble(2);
                        sre.chg = jaItem.getDouble(3);
                        sre.percent = jaItem.getDouble(4);
                        sre.last_close = jaItem.getDouble(5);
                        sre.open = jaItem.getDouble(6);
                        sre.high = jaItem.getDouble(7);
                        sre.low = jaItem.getDouble(8);
                        sre.volume = jaItem.getDouble(9);
                        sre.amount = jaItem.getDouble(10);
                        sre.market_capital = jaItem.getDouble(11);
                        sre.pe_ttm = jaItem.getDouble(12);
                        sre.high52w = jaItem.getDouble(13);
                        sre.low52w = jaItem.getDouble(14);
                        sre.hasexist = jaItem.getBoolean(15);

                        resultList.add(sre);
                    }
                    return resultList;
                }
            }
        } catch (Exception e) {
            LogUtils.logError(e);
        }

        return null;
    }

//    public static List<KEntity> convertSHStockRankEntity(List<SHStockRankEntity> shStockRankEntityList) {
//        List<KEntity> resultList = new ArrayList<>();

//        for (SHStockRankEntity sre : shStockRankEntityList) {
//            KEntity ke = new KEntity();
//
//        }
//    }

    public static void main(String[] args) {
//        requestXueqiuIndex();
//        requestStocklist();

//        testCookie1();

//        List<KEntity> l0 = requestStockDefaultKLine("SH603517", PERIOD_30_MIN);
//        l0.size();
//        requestStockDefaultKLine("08205", PERIOD_REAL_TIME);
        List<KEntity> l = requestStockDefaultKLine("00700", "1d");
        l.size();

        for (KEntity i : l) {
            LogUtils.logDebugLine(i.toOutputString());
        }

//        requestHKStockDefaultRankList(ORDERBY_PERCENT);
//        requestAStockDefaultRankList(ORDERBY_PERCENT);
    }
}
