package com.ceo.reckless;

import com.ceo.reckless.chart.KLineChart;
import com.ceo.reckless.entity.KEntity;
import com.ceo.reckless.entity.LinkEntity;
import com.ceo.reckless.helper.AicoinDataHelper;
import com.ceo.reckless.helper.SosobtcDataHelper;
import com.ceo.reckless.utils.FileUtils;
import com.ceo.reckless.utils.LogUtils;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.SharedInputStream;
import com.sun.xml.internal.ws.util.pipe.StandaloneTubeAssembler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KinkScanner {

    static int MIN_DISTANCE = 4;

    static int TYPE_EMPTY = -1;
    static int TYPE_TOP = 1;
    static int TYPE_BTM = 2;

    public static List<KEntity> shrinkKLine(List<KEntity> kEntityList) {
        List<KEntity> shrinkedKEntityList = new ArrayList<>();

        KEntity keItem = new KEntity();
        KEntity keNext = new KEntity();
        boolean isLastUp = true;
        shrinkedKEntityList.add(kEntityList.get(0));
        for (int n = 1; n < kEntityList.size(); n++) {
            keItem = shrinkedKEntityList.get(shrinkedKEntityList.size() - 1);
            keNext = kEntityList.get(n);

            if ((keNext.high > keItem.high && keNext.low > keItem.low) || (keNext.high < keItem.high && keNext.low < keItem.low)) {
                // 下一跟k比当前高 or 下一跟k比当前低
                // 不需要与下一根合并,进入list
                shrinkedKEntityList.add(keNext);

                isLastUp = keNext.high > keItem.high && keNext.low > keItem.low;

            } else if ((keNext.high > keItem.high && keNext.low < keItem.low) || (keNext.high < keItem.high && keNext.low > keItem.low)) {
                // 下一根k比当前长 or 下一根k比当前短 -> 包含关系
                KEntity k = new KEntity();
                k.timestamp = keNext.timestamp;
                if (isLastUp) {
                    // 上升处理
                    k.high = keNext.high > keItem.high ? keNext.high : keItem.high; // 高点取高
                    k.low = keNext.low < keItem.low ? keItem.low : keNext.low;      // 低点取高
                } else {
                    // 下降处理
                    k.low = keNext.low < keItem.low ? keNext.low : keItem.low;      // 低点取低
                    k.high = keNext.high > keItem.high ? keItem.high : keNext.high; // 高点取低
                }
                k.volume = keNext.volume + keItem.volume;
                k.open = (k.high + k.low) / 2;
                k.close = (k.high + k.low) / 2;

                shrinkedKEntityList.remove(shrinkedKEntityList.size() - 1);
                shrinkedKEntityList.add(k);
            }
        }

        return shrinkedKEntityList;
    }

    /**
     * 标记顶分型底分型的顶和底
     * 输入:已经合并够的K list
     */
    public static int[] markTopBottomShape(List<KEntity> shrinkedKEntityList) {

        int[] markTypeArray = new int[shrinkedKEntityList.size()];

        int lastTopIdx = -1;
        int lastBtmIdx = -1;

        KEntity keTemp = new KEntity();
        KEntity keLast = new KEntity();
        KEntity keItem = new KEntity();
        KEntity keNext = new KEntity();
        for (int n = 2; n < shrinkedKEntityList.size(); n++) {
            int curIdx = n - 1;
            keLast = shrinkedKEntityList.get(curIdx - 1);
            keItem = shrinkedKEntityList.get(curIdx);
            keNext = shrinkedKEntityList.get(curIdx + 1);

            // 顶分型判断
            // 高点最高 低点最高
            if ((keItem.high > keLast.high && keItem.high > keNext.high) &&
                    (keItem.low > keLast.low && keItem.low > keNext.low)) {

                // 修改形状为上丁字形
//                keItem.open = keItem.high;
//                keItem.close = keItem.high;

                if (lastTopIdx == TYPE_EMPTY) {
                    // 当前没有临时top
                    lastTopIdx = curIdx;
                } else {
                    // 顶顶相邻取高顶
                    keTemp = shrinkedKEntityList.get(lastTopIdx);
                    if (keTemp.high <= keItem.high) {
                        // 上一个顶较低
                        // 处理:更新临时顶
                        lastTopIdx = curIdx;
                    } else {
                        // 当前顶较低
                        // 处理:保留上个顶
                    }
                }

                // 碰到顶,确认上个底
                if (lastBtmIdx != TYPE_EMPTY) {
                    markTypeArray[lastBtmIdx] = TYPE_BTM;
                    lastBtmIdx = TYPE_EMPTY;
                }

                n += 2; // 总共后移3个位置,离开当前划分为顶分型的几根k
                continue;
            }

            // 底分型判断
            // 低点最低 高点最低
            if ((keItem.low < keLast.low && keItem.low < keNext.low) &&
                    (keItem.high < keLast.high && keItem.high < keNext.high)) {

                // 修改形状为下丁字形
//                keItem.open = keItem.low;
//                keItem.close = keItem.low;

                if (lastBtmIdx == TYPE_EMPTY) {
                    // 当前没有临时底
                    lastBtmIdx = curIdx;
                } else {
                    // 底底相邻取较低
                    keTemp = shrinkedKEntityList.get(lastBtmIdx);
                    if (keTemp.low >= keItem.low) {
                        // 当前底较低
                        // 处理:更新临时底
                        lastBtmIdx = curIdx;
                    } else {
                        // 上一个底较低
                        // 处理:保留上个底
                    }
                }

                if (lastTopIdx != TYPE_EMPTY) {
                    markTypeArray[lastTopIdx] = TYPE_TOP;
                    lastTopIdx = TYPE_EMPTY;
                }

                n += 2; // 总共后移3个位置,离开当前划分为底分型的几根k
                continue;
            }
        }

        // 循环结束,把最后一个临时顶和临时底都算进去
        if (lastBtmIdx != TYPE_EMPTY) {
            markTypeArray[lastBtmIdx] = TYPE_BTM;
        }
        if (lastTopIdx != TYPE_EMPTY) {
            markTypeArray[lastTopIdx] = TYPE_TOP;
        }

        return markTypeArray;
    }

    public static void processMarkTypeArray(List<KEntity> shrinkedKEntityList, int[] markTypeArray) {
        // 根据markTypeArray的顶底结果,尝试link(目前只有顶底相连,没有顶顶或底底了)
        List<LinkEntity> linkList = new ArrayList<>();
        int lastTopIdx = TYPE_EMPTY;
        int lastBtmIdx = TYPE_EMPTY;
        LinkEntity lastLinkEntity = null;
        LinkEntity tmpLinkEntity = null;
        for (int i = 0; i < markTypeArray.length; i++) {
            int curIdx = i;
            int itemType = markTypeArray[i];
            if (itemType == 0) {
                continue;
            }
            boolean needOperLink = false;
            if (itemType == TYPE_TOP) {
                // 碰到一个顶
                if (lastBtmIdx != TYPE_EMPTY) {
                    // 前面有个底
                    if (curIdx - lastBtmIdx >= MIN_DISTANCE) {
                        // 当前顶与前底能link
                        needOperLink = true;
                        lastTopIdx = curIdx;
                    } else {
                        // 当前顶与前底不能link
                        // 处理:二顶取一
                        if (lastTopIdx == TYPE_EMPTY) {
                            // 当前没有待link的顶
                            lastTopIdx = curIdx;
                        } else if (lastTopIdx == TYPE_TOP) {
                            // 有相邻待link的顶,取高顶
                            KEntity cur = shrinkedKEntityList.get(curIdx);
                            KEntity lst = shrinkedKEntityList.get(lastTopIdx);
                            if (cur.high > lst.high) {
                                // 当前顶较高
                                // 处理:更新待link顶
                                lastTopIdx = curIdx;
                            } else {
                                // 前顶较高
                                // 处理:不更新
                            }
                        }
                    }
                } else {
                    // 前面无底
                    lastTopIdx = curIdx;
                }
            } else if (itemType == TYPE_BTM) {
                // 碰到一个底
                if (lastTopIdx != TYPE_EMPTY) {
                    // 前面有个顶
                    if (curIdx - lastTopIdx >= MIN_DISTANCE) {
                        // 当前底与前顶能link
                        needOperLink = true;
                        lastBtmIdx = curIdx;
                    } else {
                        // 当前底与前顶不能link
                        // 处理:二底取一
                        if (lastBtmIdx == TYPE_EMPTY) {
                            // 当前没有待link的底
                            lastBtmIdx = curIdx;
                        } else if (lastTopIdx == TYPE_TOP) {
                            // 有相邻待link的底,取较低
                            KEntity cur = shrinkedKEntityList.get(curIdx);
                            KEntity lst = shrinkedKEntityList.get(lastBtmIdx);
                            if (cur.low < lst.low) {
                                // 当前底较低
                                // 处理:更新待link底
                                lastBtmIdx = curIdx;
                            } else {
                                // 前底较低
                                // 处理:不更新
                            }
                        }
                    }
                } else {
                    // 前面无顶
                    lastBtmIdx = curIdx;
                }

                // 当前有待link的顶,走一下link处理
//                if (lastTopIdx != TYPE_EMPTY) {
//                    needOperLink = true;
//                }
            }

            if (needOperLink) {

                KEntity topEntity = shrinkedKEntityList.get(lastTopIdx);
                KEntity btmEntity = shrinkedKEntityList.get(lastBtmIdx);
                if (btmEntity.low >= topEntity.high) {
                    // 底比顶高
                    continue;
                }

                KEntity first = null;
                KEntity second = null;
                first = lastTopIdx > lastBtmIdx ? btmEntity : topEntity;
                second = lastTopIdx > lastBtmIdx ? topEntity : btmEntity;
                tmpLinkEntity = new LinkEntity(first, second);

                if (lastLinkEntity == null) {
                    // 没有待确认的link
                    lastLinkEntity = tmpLinkEntity;
                } else {
                    // 更新link信息
                    // 只需要更新second
                    if (lastLinkEntity.isSameStart(tmpLinkEntity)) {
                        // 起点相同
                        // 处理:延伸,更新第二个点
                        lastLinkEntity.second = tmpLinkEntity.second;
                    } else {
                        // 起点不同
                        // 处理:前面一笔定型,加入link list
                        linkList.add(lastLinkEntity);
                        lastLinkEntity = tmpLinkEntity;
                    }
                }
            }
        }

        // 循环结束后处理一下最后一个link
        linkList.add(lastLinkEntity);

        debugPrintLinkList(linkList);
    }

    public static void debugPrintLinkList(List<LinkEntity> list) {
        for (LinkEntity item : list) {
            double start = 0.0;
            double end = 0.0;
            if (item.type == LinkEntity.TYPE_UP) {
                start = item.first.low;
                end = item.second.high;
            } else if (item.type == LinkEntity.TYPE_DOWN) {
                start = item.first.high;
                end = item.second.low;
            }
            LogUtils.logDebug(start + "-" + end + " ");
        }
    }

    public static void testMarkTopBtm() {
        double[] t1 = {
                //1
                39, 40, 39,

                //2
                13, 12, 13,

                15, 16, 17,

                //3
                49, 50, 49,

                //4
                33, 32, 33,

                //5
                59, 60, 59,

                43, 35, 28,

                //6
                23, 22, 23,

                //7
                29, 30, 29,

                //8
                26, 25, 26,

                //9
                27, 28, 27,

                22, 18, 10,

                //10
                6, 5, 6
        };
        int[] mark1 = {
                //1
                0, TYPE_TOP, 0,
                //2
                0, TYPE_BTM, 0,
                0, 0, 0,
                //3
                0, TYPE_TOP, 0,
                //4
                0, TYPE_BTM, 0,
                //5
                0, TYPE_TOP, 0,
                0, 0, 0,
                //6
                0, TYPE_BTM, 0,
                //7
                0, TYPE_TOP, 0,
                //8
                0, TYPE_BTM, 0,
                //9
                0, TYPE_TOP, 0,
                0, 0, 0,
                //10
                0, TYPE_BTM, 0
        };

        List<KEntity> list = new ArrayList<>();
        for (double v : t1) {
            KEntity k = testGenUpEntity(v);
            list.add(k);
        }

        processMarkTypeArray(list, mark1);
    }

    private static KEntity testGenUpEntity(double value) {
        KEntity ke = new KEntity();
        ke.high = value;
        ke.open = value - 0.5;
        ke.close = value - 1;
        ke.low = value - 2;
        return ke;
    }

//    private static KEntity testGenDownEntity(double value) {
//        KEntity ke = new KEntity();
//        ke.high = value + 2;
//        ke.open = value + 0.5;
//        ke.close = value + 1;
//        ke.low = value;
//        return ke;
//    }

    private static void testShape() {
        double[] t1 = {
                18,
                //1
                19, 20, 19,
                //2
                18, 12, 18,
                25,
                //3
                34, 35, 34,
                32, 34, 35,
                //4
                39, 40, 39,
                //5
                36, 37, 36,
                35, 36
        };

        List<KEntity> list = new ArrayList<>();
        for (double v : t1) {
            KEntity k = testGenUpEntity(v);
            list.add(k);
        }
        int[] markTypeArray = markTopBottomShape(list);
        for (int i = 0; i < markTypeArray.length; i++) {
            LogUtils.logDebug(" " + markTypeArray[i]);
        }
    }

    public static void test() {

        testShape();
//        testMarkTopBtm();
    }

    public static void main(String[] args) {

        try {

            test();

//            String jsonResult = SosobtcDataHelper.httpQueryKData("huobi", "btc", SosobtcDataHelper.TYPE_LEVEL_1_HOUR, 0);
//            List<KEntity> list = SosobtcDataHelper.parseKlineToList(jsonResult);
//            List<KEntity> slist = shrinkKLine(list);
//
//            LogUtils.logDebugLine("list size " + list.size() + " slist size " + slist.size());
//
//            KLineChart.outputKLineShrinkChart("im title", list, slist, "test_double_kline_chart.html");


//            String market = "okex";
//            String targetCoin = "ethquarter";
//            String srcCoin = "usd";
//            String periodType = "1h";

            /*
            String market = "okcoinfutures";
            String targetCoin = "btcquarter";
            String srcCoin = "usd";
            String periodType = "30m";
            long since = 0;
            List<KEntity> list = AicoinDataHelper.requestKLine(market, targetCoin, srcCoin, periodType, 0);
            List<KEntity> slist = shrinkKLine(list);

            LogUtils.logDebugLine("list size " + list.size() + " slist size " + slist.size());

            KLineChart.outputKLineShrinkChart("im title", list, slist, "btcquarter_shrink_kline_chart.html");
            */


        } catch (Exception e) {
            LogUtils.logError(e);
        }
    }
}
