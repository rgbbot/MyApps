package com.liondevhq.weathertomorrow.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by pc on 31.01.2017.
 */
public class WeatherContract {
    private WeatherContract() {}

    public static final String CONTENT_AUTHORITY = "com.liondevhq.weathertomorrow";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";

    public static final class WeatherEntry implements BaseColumns {

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of forecasts.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single forecast.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_WEATHER);
        public static final String TABLE_NAME = "weather";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_WEATHER_CITY = "city";
        public static final String COLUMN_WEATHER_COUNTRY = "country";

    }
}
