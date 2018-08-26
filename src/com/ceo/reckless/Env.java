package com.ceo.reckless;

public class Env {

    public static boolean DEBUG = true;

    public static int MACD_PERIOD_FAST = 12;
    public static int MACD_PERIOD_LOW = 26;
    public static int MACD_PERIOD_SIGNAL = 9;

    // 根据k线数据计算当前近期走势多空两方力度,计算所用k线的个数
    public static int BULL_VS_SHORT_K_ITEM_INTERVAL = 50;

    public static String BAR_CHART_HTML_FILE_PATH = "./column-rotated-labels.html";
    public static String LINE_CHART_HTML_FILE_PATH = "./spline.html";
    public static String Test_BASE_LINE_CHART_HTML_FILE_PATH = "./basic-line.html";
    public static String KLINE_CHART_HTML_FILE_PATH = "./candlestick-and-volume.html";
    public static String KLINE_SHRINK_CHART_HTML_FILE_PATH = "./candlestick-and-volume_double.html";
    public static String CANVAS_POLY_LINE_HTML_FILE_PATH = "./canvas_poly_line.htm";

    public static String CANVAS_POLY_LINE_HTML_FILE_NAME = "canvas_poly_line.htm";

    public static String BAR_CHART_HTML_DATA_PLACE_HOLDER = "<<BAR_DATA_PLACE_HOLDER>>";
    public static String LINE_CHART_HTML_DATA_PLACE_HOLDER = "<<LINE_DATA_PLACE_HOLDER>>";
    public static String HTML_TITLE_PLACE_HOLDER = "<<HTML_TITLE_PLACE_HOLDER>>";
    public static String KLINE_DOUBLE_CHART_HTML_DATA_PLACE_HOLDER = "<<KLINE_DATA_PLACE_HOLDER>>";
    public static String KLINE_DOUBLE_CHART_HTML_DATA_1_PLACE_HOLDER = "<<KLINE_DATA_1_PLACE_HOLDER>>";
    public static String KLINE_DOUBLE_CHART_HTML_DATA_2_PLACE_HOLDER = "<<KLINE_DATA_2_PLACE_HOLDER>>";

    public static String CANVAS_UP_LINE_COLOR_PLACE_HOLDER = "<<CANVAS_UP_LINE_COLOR_PLACE_HOLDER>>";
    public static String CANVAS_DOWN_LINE_COLOR_PLACE_HOLDER = "<<CANVAS_DOWN_LINE_COLOR_PLACE_HOLDER>>";


    public static String DATA_MATRIX_PLACE_HOLDER = "<<DATA_MATRIX_PLACE_HOLDER>>";

    public static String CANVAS_COLOR_GREEN = "#90EE90";
    public static String CANVAS_COLOR_RED = "#FF6A6A";
}
