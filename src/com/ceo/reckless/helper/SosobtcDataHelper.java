package com.ceo.reckless.helper;

import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.utils.HttpRequest;
import com.ceo.reckless.utils.LogUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SosobtcDataHelper {

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

    public static String httpQueryKData(String market, String coin, int type, long since) {

        try {

            JSONObject joPostParams = new JSONObject();
            joPostParams.put("symbol", market + "_" + coin);
            joPostParams.put("type", type);
            if (since != 0) {
                joPostParams.put("since", String.valueOf(since));
            } else {
                joPostParams.put("since", "");
            }

            String param = joPostParams.toString();

            return HttpRequest.sendPost(URL_KLINE, param);

        } catch (Exception e) {
            LogUtils.logError(e);
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
            LogUtils.logError(e);
        }

        return null;
    }
}
