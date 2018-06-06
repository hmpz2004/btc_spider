package com.ceo.reckless.entity;

import com.ceo.reckless.strategy.Deviation;
import com.ceo.reckless.utils.TimeUtils;

public class DeviationEntity {

    // 币种symbol
    public String symbol = "";
    // 币种
    public String coin = "";
    // 交易所
    public String exchange = "";
    // 交易对儿(购买币种usdt usd btc qc bnb等)
    public String exchangeFrom = "";

    // isPreDeviation true : 查看已经出现拐点的背离情况 false : 当前走势的最后一个点也算进去
    public boolean isPreDevition = false;

    public int deviationTypeTopBtm = 0;

    public int deviationTypeLineBar = 0;

    public long timeDiffFromCur = 0;

    public String toOutputString() {
        // return coin + " " + exchange + " " + " " + exchangeFrom + " " + (isPreDevition ? "PRE" : "NML");
        String topBtm = deviationTypeTopBtm == Deviation.TYPE_TOP ? "TOP" : "BTM";
        String lineBar = deviationTypeLineBar == Deviation.TYPE_LINE ? "LINE" : "BAR";
        return coin + "\t" + exchange + "\t" + exchangeFrom + "\t" + TimeUtils.calculateTimeDistance(timeDiffFromCur) + "\t" + (isPreDevition ? "PRE" : "NML") + "\t" + topBtm + "\t" + lineBar;
    }
}
