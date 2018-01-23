package com.ceo.reckless;

import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.entity.MACDEntity;
import com.ceo.reckless.entity.MAEntity;
import com.ceo.reckless.helper.AicoinDataHelper;
import com.ceo.reckless.helper.TalibHelper;
import com.ceo.reckless.utils.LogUtils;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VolumeScanner {

    private static boolean VOLUME_DEBUG = true;

    private static final double TOP_VOLUME_THRESHOLD = 0.0375;  // 成交量排在前3.75%才算作异常数据

    private static final double VOLUME_MA5_THRESHOLD = 1.8;     // 成交量排至少是成家量MA5的160%

    public static void scanRecentlyBigVolume(List<KEntity> kEntityList) {

        // 最后入选的近期成交数据
        List<KEntity> filteredListStrong = new ArrayList<>();
        List<KEntity> filteredListWeak = new ArrayList<>();


        MAEntity maEntity = TalibHelper.genMa(kEntityList, 5);
        if (maEntity == null) {
            LogUtils.logDebugLine("calculate ma 5 error");
            return;
        }

        // 先剔除最后20个记录不参与挑选
        List<KEntity> sampleList = new ArrayList<>();
        List<KEntity> targetList = new ArrayList<>();
        int i = kEntityList.size();
        for (KEntity kitem : kEntityList) {
            if (i-- <= 20) {
                targetList.add(kitem);
            } else {
                sampleList.add(kitem);
            }
        }


        if (VOLUME_DEBUG) {
            LogUtils.logDebugLine("orig k list");
            for (KEntity kitem : kEntityList) {
                LogUtils.logDebugLine(kitem.toString());
            }
            LogUtils.logDebugLine("");

            LogUtils.logDebugLine("sample k list");
            for (KEntity kitem : sampleList) {
                LogUtils.logDebugLine(kitem.toString());
            }
            LogUtils.logDebugLine("");

            LogUtils.logDebugLine("target k list");
            for (KEntity kitem : targetList) {
                LogUtils.logDebugLine(kitem.toString());
            }
            LogUtils.logDebugLine("");

        }

        Collections.sort(sampleList, new Comparator<KEntity>() {
            @Override
            public int compare(KEntity o1, KEntity o2) {
                return o1.volume > o2.volume ? -1 : 1;
            }
        });

        if (VOLUME_DEBUG) {
            LogUtils.logDebugLine("sorted sample k list");
            for (KEntity kitem : sampleList) {
                LogUtils.logDebugLine(kitem.toString());
            }
        }

        // 粗略筛选条件(只按top成交个数)
        // 已按交易量排好序前大后小,取第cnt+1个元素
        int cnt = (int) (sampleList.size() * TOP_VOLUME_THRESHOLD + 1);
        int idxCnt = cnt + 1 < sampleList.size() ? cnt : sampleList.size() - 1;        // 防止越界
        KEntity baseLineEntityWeak = sampleList.get(idxCnt);
        for (KEntity kitem : targetList) {
            Double tmpMa5Value = maEntity.timeValueMap.get(kitem.timestamp);
            if (tmpMa5Value != null) {
                if (kitem.volume > baseLineEntityWeak.volume && kitem.volume > tmpMa5Value * VOLUME_MA5_THRESHOLD) {
                    filteredListWeak.add(kitem);
                }
            }
        }

        // 严格筛选条件(按照top成交个数和ma比对结果)
        // 把符合成交量比ma5高的判断标准的挑出来
        // idxTgt定位的是最后一个符合volume > ma5 * 1.6条件的最后一个元素
        int idxTgt = 0;
        for (i = 0; i < idxCnt; i++) {
            KEntity kitem = sampleList.get(i);
            Double itemMa5Value = maEntity.timeValueMap.get(kitem.timestamp);
            if (itemMa5Value != null) {
                idxTgt = i;
                if (kitem.volume <= itemMa5Value * VOLUME_MA5_THRESHOLD){
                    break;
                }
            }
        }

        int finalIdx = idxTgt < idxCnt ? idxTgt : idxCnt;

        KEntity baseLineEntityStrong = sampleList.get(finalIdx);

        // 与基准"高成交量"的数据比对
        for (KEntity kitem : targetList) {
            Double tmpMa5Value = maEntity.timeValueMap.get(kitem.timestamp);
            if (tmpMa5Value != null) {
                if (kitem.volume > baseLineEntityStrong.volume && kitem.volume > tmpMa5Value * VOLUME_MA5_THRESHOLD) {
                    filteredListStrong.add(kitem);
                }
            }
        }

        if (VOLUME_DEBUG) {
            LogUtils.logDebugLine("strong filtered big volume list");
            for (KEntity kitem : filteredListStrong) {
                LogUtils.logDebugLine(kitem.toString());
            }
            LogUtils.logDebugLine("");

            LogUtils.logDebugLine("weak   filtered big volume list");
            for (KEntity kitem : filteredListWeak) {
                LogUtils.logDebugLine(kitem.toString());
            }
        }
    }

    public static void main(String[] args) {

        String market = "gate";
        String targetCoin = "ven";
        String srcCoin = "usdt";
        String periodType = "1h";
        long since = 1515312000;    // 1.7 22:00
//        long since = 0;
        long end = 1516215600;

        List<KEntity> list = AicoinDataHelper.requestKLine(market, targetCoin, srcCoin, periodType, since);

        List<KEntity> list1 = new ArrayList<>();
        for (KEntity kitem : list) {
            if (kitem.timestamp > end) {
                continue;
            }
            list1.add(kitem);
        }

        scanRecentlyBigVolume(list1);

        LogUtils.logDebugLine(" " + (int) (10 * 0.26));

        // 测试macd
//        MACDEntity macdEntity = TalibHelper.genMacd(list1);
//        for (int i = 0; i < macdEntity.timeArray.length; i++) {
//            LogUtils.logDebugLine(macdEntity.timeArray[i] + " " + macdEntity.barArray[i]);
//        }

        // // 测试ma
//        MAEntity maEntity = TalibHelper.genMa(list1, 5);
//        for (int i = 0; i < maEntity.timeArray.length; i++) {
//            LogUtils.logDebugLine(maEntity.timeArray[i] + " " + maEntity.valueArray[i]);
//        }
    }
}
