package com.ceo.reckless.helper;

import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.utils.HttpRequest;
import com.ceo.reckless.utils.HttpUrlParamBuilder;
import com.ceo.reckless.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HexunDataHelper {

    private static boolean HEXUN_DEBUG = true;

    public static String URL_KLINE = "http://webftcn.hermes.hexun.com/shf/kline";

    public static int TYPE_LEVEL_1_MIN = 0;// 1分
    public static int TYPE_LEVEL_5_MIN = 1;// 5分
    public static int TYPE_LEVEL_15_MIN = 2;// 15分
    public static int TYPE_LEVEL_30_MIN = 3;// 30分
    public static int TYPE_LEVEL_1_HOUR = 4;// 1小时
    public static int TYPE_LEVEL_1_DAY = 5;// 1天
    public static int TYPE_LEVEL_1_WEEK = 6;// 1周
    public static int TYPE_LEVEL_1_MONTH = 9;// 1月
    public static int TYPE_LEVEL_1_SEASON = 12;// 1季
    public static int TYPE_LEVEL_1_YEAR = 15;// 1年

    public static Map<String, Integer> periodTypeMap = new HashMap<>();

    public static Map<String, String> codeDescMap = new HashMap<>();

    static {
        periodTypeMap.put("1m", TYPE_LEVEL_1_MIN);// 1分
        periodTypeMap.put("5m", TYPE_LEVEL_5_MIN);// 5分
        periodTypeMap.put("15m", TYPE_LEVEL_15_MIN);// 15分
        periodTypeMap.put("30m", TYPE_LEVEL_30_MIN);// 30分
        periodTypeMap.put("1h", TYPE_LEVEL_1_HOUR);// 1小时
        periodTypeMap.put("1d", TYPE_LEVEL_1_DAY);// 1天
        periodTypeMap.put("1w", TYPE_LEVEL_1_WEEK);// 1周
        periodTypeMap.put("1mon", TYPE_LEVEL_1_MONTH);// 1月
        periodTypeMap.put("1sea", TYPE_LEVEL_1_SEASON);// 1季
        periodTypeMap.put("1year", TYPE_LEVEL_1_YEAR);// 1年

        codeDescMap.put("DCEI", "铁矿石");
        codeDescMap.put("DCEjd", "鲜鸡蛋");
        codeDescMap.put("DCEj", "焦炭");
        codeDescMap.put("DCEjm", "焦煤");
        codeDescMap.put("DCEv", "PVC");
        codeDescMap.put("CZCEma", "新甲醇");
        codeDescMap.put("CZCEta", "PTA");
        codeDescMap.put("SHFE3rb", "螺纹钢");
        codeDescMap.put("SHFE3cu", "沪铜");
        codeDescMap.put("SHFE2ag", "白银");
        codeDescMap.put("SHFE3ni", "沪镍");

        /**
         * DCEI 铁矿石的数据会默认x10  eg:545 -> 5450
         */
    }

    public static List<KEntity> requestKLIne(String code, String periodType, String startDateString) {

        List<KEntity> resultList = new ArrayList<>();

        // 校验商品code
        if (!codeDescMap.containsKey(code)) {
            LogUtils.logDebugLine("wrong code " + code + " and supported as blew:");
            for (Map.Entry<String, String> entry : codeDescMap.entrySet()) {
                LogUtils.logDebugLine(entry.getValue() + "\t" + entry.getKey());
            }
        }

        // 校验k线时间参数
        if (!periodTypeMap.containsKey(periodType)) {
            LogUtils.logDebug("wrong period type " + periodType);
            return null;
        }

        int periodTypeNumber = periodTypeMap.get(periodType);

        // 时间参数校验
        if (startDateString == null || startDateString.equals("")) {
            LogUtils.logDebugLine("wrong start time string : " + startDateString + " it should be 20171102210000");
        }

        HttpUrlParamBuilder ub = new HttpUrlParamBuilder();
        ub.appendParam("code", code);
        ub.appendParam("start", startDateString);
        ub.appendParam("number", "-1000");
        ub.appendParam("type", String.valueOf(periodTypeNumber));

        String urlParamString = ub.formatUrlParamString();

        String responseString = HttpRequest.sendGet(URL_KLINE, urlParamString);
        try {
            if (responseString != null && !responseString.equals("")) {
                if (responseString.length() > 3) {
                    // 去掉开头的"("和结尾的");"
                    responseString = responseString.substring(1, responseString.length() - 2);
                }

                //<<>>
                LogUtils.logDebugLine(responseString);

                JSONObject joTotal = new JSONObject(responseString);
                JSONArray jaData = joTotal.optJSONArray("Data");
                if (jaData != null && jaData.length() != 0) {
                    JSONArray jaKData = jaData.optJSONArray(0);
                    if (jaKData != null && jaKData.length() != 0) {
                        for (int i = 0; i < jaKData.length(); i++) {
                            JSONArray jaItemKData = jaKData.optJSONArray(i);
                            if (jaItemKData != null && jaItemKData.length() != 0) {
                                long Time = jaItemKData.optLong(0);
                                int LastClose = jaItemKData.optInt(1);
                                int Open = jaItemKData.optInt(2);
                                int Close = jaItemKData.optInt(3);
                                int High = jaItemKData.optInt(4);
                                int Low = jaItemKData.optInt(5);
                                int Volume = jaItemKData.optInt(6);
                                int Amount = jaItemKData.optInt(7);

                                String dateFormatString = "yyyyMMddhhmmss";
                                SimpleDateFormat sdf = new SimpleDateFormat(dateFormatString);
                                Date tmpDate = sdf.parse(String.valueOf(Time));

                                KEntity keItem = new KEntity();
                                keItem.timestamp = tmpDate.getTime();
                                keItem.open = Open;
                                keItem.close = Close;
                                keItem.high = High;
                                keItem.low = Low;
                                keItem.volume = Volume;

                                resultList.add(keItem);
                            }
                        }

                        return resultList;
                    } else {
                        LogUtils.logDebugLine("json array kline data null or size 0");
                    }
                } else {
                    LogUtils.logDebugLine("json array 'Data' null or size 0");
                }
            }
        } catch (Exception e) {
            LogUtils.logError(e);
        }

        return null;
    }

    public static void main(String[] args) {

        // 测试商品品种k线
        List<KEntity> list = requestKLIne("DCEI1805", "1h", "20171030210000");
        for (KEntity item : list) {
            LogUtils.logDebugLine(item.open + " " + item.high + " " + item.low + " " + item.close + " " + item.volume);
        }
    }
}
