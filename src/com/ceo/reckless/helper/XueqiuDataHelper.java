package com.ceo.reckless.helper;

import com.ceo.reckless.utils.HttpClientUtil;
import com.ceo.reckless.utils.HttpRequest;
import com.ceo.reckless.utils.HttpUrlUtils;
import com.ceo.reckless.utils.LogUtils;

import java.io.IOException;

public class XueqiuDataHelper {

    private static boolean XUEQIU_DEBUG = true;

    public static String INDEX_URL = "https://www.xueqiu.com";

    public static String STOCKLIST_URL = "https://xueqiu.com/stock/cata/stocklist.json";

    public static final String PARAM_KEY_PAGE = "page";
    public static final String PARAM_KEY_SIZE = "size";         // 雪球最大会返回100个记录
    public static final String PARAM_KEY_ORDER = "order";
    public static final String PARAM_KEY_ORDERBY = "orderby";
    public static final String PARAM_KEY_TYPE = "type";
    public static final String PARAM_KEY_ISDELAY = "isdelay";
    public static final String PARAM_KEY_TIMESTAMP = "_";

    public static final String ORDER_DESC = "desc";
    public static final String ORDER_ASC = "asc";
    public static final String ORDERBY_PERCENT = "percent";
    public static final String ISDELAY_1 = "1";
    public static final String TYPE_30 = "30";

    public static void requestXueqiuIndex() {
        String res = HttpRequest.sendGet(INDEX_URL, null);
        if (XUEQIU_DEBUG) {
            LogUtils.logDebugLine(res);
        }
    }

    public static void requestStocklist() {
        HttpUrlUtils ut = new HttpUrlUtils();
        ut.appendParam(PARAM_KEY_PAGE, "1");
        ut.appendParam(PARAM_KEY_SIZE, "60");
        ut.appendParam(PARAM_KEY_ORDER, ORDER_DESC);
        ut.appendParam(PARAM_KEY_ORDERBY, ORDERBY_PERCENT);
        ut.appendParam(PARAM_KEY_TYPE, TYPE_30);
        ut.appendParam(PARAM_KEY_ISDELAY, ISDELAY_1);
        ut.appendParam(PARAM_KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        String urlParamString = ut.formatUrlParamString();
        if (XUEQIU_DEBUG) {
            LogUtils.logDebugLine(urlParamString);
        }

        String res = HttpRequest.sendGet(STOCKLIST_URL, urlParamString);
        if (XUEQIU_DEBUG) {
            LogUtils.logDebugLine(res);
        }
    }

    public static void testCookie() {
        HttpClientUtil u = new HttpClientUtil();
        try {
            u.doGet(INDEX_URL, true);

            HttpUrlUtils ut = new HttpUrlUtils();
            ut.appendParam(PARAM_KEY_PAGE, "1");
            ut.appendParam(PARAM_KEY_SIZE, "100");
            ut.appendParam(PARAM_KEY_ORDER, ORDER_DESC);
            ut.appendParam(PARAM_KEY_ORDERBY, ORDERBY_PERCENT);
            ut.appendParam(PARAM_KEY_TYPE, TYPE_30);
            ut.appendParam(PARAM_KEY_ISDELAY, ISDELAY_1);
            ut.appendParam(PARAM_KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

            String urlParamString = ut.formatUrlParamString();
            if (XUEQIU_DEBUG) {
                LogUtils.logDebugLine(urlParamString);
            }
            String realUrlString = STOCKLIST_URL + "?" + urlParamString;

            u.doGet(realUrlString, false);

            u.doGet(realUrlString, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testCookie1() {

        try {
            HttpUrlUtils ut = new HttpUrlUtils();
            ut.appendParam(PARAM_KEY_PAGE, "1");
            ut.appendParam(PARAM_KEY_SIZE, "100");
            ut.appendParam(PARAM_KEY_ORDER, ORDER_DESC);
            ut.appendParam(PARAM_KEY_ORDERBY, ORDERBY_PERCENT);
            ut.appendParam(PARAM_KEY_TYPE, TYPE_30);
            ut.appendParam(PARAM_KEY_ISDELAY, ISDELAY_1);
            ut.appendParam(PARAM_KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

            String urlParamString = ut.formatUrlParamString();
            if (XUEQIU_DEBUG) {
                LogUtils.logDebugLine(urlParamString);
            }

            String res = HttpRequest.sendGetWithCookie(STOCKLIST_URL, urlParamString, INDEX_URL, null);
            if (XUEQIU_DEBUG) {
                LogUtils.logDebugLine(res);
            }

            String res1 = HttpRequest.sendGetWithCookie(STOCKLIST_URL, urlParamString, INDEX_URL, null);
            if (XUEQIU_DEBUG) {
                LogUtils.logDebugLine(res1);
            }

            String res2 = HttpRequest.sendGetWithCookie(STOCKLIST_URL, urlParamString, INDEX_URL, null);
            if (XUEQIU_DEBUG) {
                LogUtils.logDebugLine(res2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        requestXueqiuIndex();
//        requestStocklist();

        testCookie1();
    }
}
