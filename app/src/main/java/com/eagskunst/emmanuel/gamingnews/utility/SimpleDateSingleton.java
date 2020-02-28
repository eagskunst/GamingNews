package com.eagskunst.emmanuel.gamingnews.utility;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by eagskunst in 27/2/2020.
 */
public class SimpleDateSingleton {
    private static final SimpleDateSingleton ourInstance = new SimpleDateSingleton();
    private SimpleDateFormat fromSdf = new SimpleDateFormat(Constants.NEWS_DATE_RSS_FORMAT, Locale.US);
    private SimpleDateFormat toSdf = new SimpleDateFormat(Constants.OUTPUT_FORMAT, Locale.US);

    public static SimpleDateSingleton getInstance() {
        return ourInstance;
    }

    private SimpleDateSingleton() {
    }

    public SimpleDateFormat getInputSdf() {
        return fromSdf;
    }

    public SimpleDateFormat getToSdf() {
        return toSdf;
    }
}
