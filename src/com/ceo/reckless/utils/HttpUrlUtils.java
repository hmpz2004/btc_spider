package com.ceo.reckless.utils;

import java.util.ArrayList;
import java.util.List;

public class HttpUrlUtils {

    private List<String> keyList = new ArrayList<>();
    private List<String> valueList = new ArrayList<>();

    public void appendParam(String key, String value) {
        keyList.add(key);
        valueList.add(value);
    }

    public String formatUrlParamString() {
        if (keyList.size() != valueList.size()) {
            LogUtils.logDebugLine("HttpUrlUtils param list len error");
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <  keyList.size(); i++) {
            if (i != 0) {
                sb.append("&");
            }
            sb.append(keyList.get(i)).append("=").append(valueList.get(i));
        }
        return sb.toString();
    }
}
