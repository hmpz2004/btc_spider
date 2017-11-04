package com.ceo.reckless;

public class Env {

    public static boolean DEBUG = true;

    public static int MACD_PERIOD_FAST = 12;
    public static int MACD_PERIOD_LOW = 26;
    public static int MACD_PERIOD_SIGNAL = 9;

    public static String BAR_CHART_HTML_FILE_PATH = "./column-rotated-labels.html";
    public static String Test_BASE_LINE_CHART_HTML_FILE_PATH = "./basic-line.html";
    public static String KLINE_CHART_HTML_FILE_PATH = "./candlestick-and-volume.html";
    public static String KLINE_SHRINK_CHART_HTML_FILE_PATH = "./candlestick-and-volume_double.html";

    public static String BAR_CHART_HTML_DATA_PLACE_HOLDER = "<<BAR_DATA_PLACE_HOLDER>>";
    public static String LINE_CHART_HTML_DATA_PLACE_HOLDER = "<<LINE_DATA_PLACE_HOLDER>>";
    public static String HTML_TITLE_PLACE_HOLDER = "<<HTML_TITLE_PLACE_HOLDER>>";
    public static String KLINE_DOUBLE_CHART_HTML_DATA_PLACE_HOLDER = "<<KLINE_DATA_PLACE_HOLDER>>";
    public static String KLINE_DOUBLE_CHART_HTML_DATA_1_PLACE_HOLDER = "<<KLINE_DATA_1_PLACE_HOLDER>>";
    public static String KLINE_DOUBLE_CHART_HTML_DATA_2_PLACE_HOLDER = "<<KLINE_DATA_2_PLACE_HOLDER>>";
}
