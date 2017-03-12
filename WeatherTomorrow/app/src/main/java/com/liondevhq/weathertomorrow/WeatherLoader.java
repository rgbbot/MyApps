package com.liondevhq.weathertomorrow;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.liondevhq.weathertomorrow.data.WeatherContract;
import com.liondevhq.weathertomorrow.data.WeatherDbHelper;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 21.01.2017.
 */
public class WeatherLoader extends AsyncTaskLoader<List<Weather>> {

    /** Tag for log messages */
    private static final String LOG_TAG = WeatherLoader.class.getName();

    /** URL for forecast data from the OpenWeatherMap dataset */
    private static final String OWM_REQUEST_URL =
            "http://api.openweathermap.org/data/2.5/forecast";

    /** Query URL */
    private List<String> mUrl;

    /** Temperature unit of measure from shared preferences of {@link WeatherActivity} */
    private String mTempUnit;

    /** Database with weather cities data */
    private WeatherDbHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Constructs a new {@link WeatherLoader}.
     *
     * @param context of the activity
     * @param tmpUnit to load data from
     */
    public WeatherLoader(Context context, String tmpUnit) {
        super(context);
        mTempUnit = tmpUnit;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Weather> loadInBackground() {

        // List for storing DB ids
        List<Integer> idDbList = new LinkedList<>();
        // List for storing built URIs
        List<String> uriList = new LinkedList<>();
        // List for storing forecast cities
        List<String> cities = new LinkedList<>();

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_CITY,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_COUNTRY };

        //Cursor for getting data from DB
        mDbHelper = new WeatherDbHelper(this.getContext());
        mDb = mDbHelper.getReadableDatabase();

        Cursor forecastCitiesDataCursor = mDb.query(true, WeatherContract.WeatherEntry.TABLE_NAME, projection,
                null, null, null,
                null, null, null);

        if (forecastCitiesDataCursor.moveToFirst()){
            do{
                int idDB = forecastCitiesDataCursor.getInt(forecastCitiesDataCursor.getColumnIndex(WeatherContract.WeatherEntry._ID));
                String city = forecastCitiesDataCursor.getString(forecastCitiesDataCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_CITY));
                String country = forecastCitiesDataCursor.getString(forecastCitiesDataCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_COUNTRY));

                String full = city + "," + country;

                cities.add(full);
                idDbList.add(idDB);
            }while(forecastCitiesDataCursor.moveToNext());
        }
        forecastCitiesDataCursor.close();

        //For each city in the list generate URI and put it in the URIs list
        for (String city : cities){
            Uri baseUri = Uri.parse(OWM_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("q", city);
            uriBuilder.appendQueryParameter("cnt", "16");
            uriBuilder.appendQueryParameter("units", mTempUnit);
            uriBuilder.appendQueryParameter("appid", "031d20c5934f7a1edd29b1bcfe6c4874");

            uriList.add(uriBuilder.toString());
        }

        if (uriList == null) {
            return null;
        }

        // Map for storing final forecast data
        Map<String,Integer> forecastDataMap = new LinkedHashMap<>();

        // Insert values to the map
        for (int i = 0; i < uriList.size(); i++) {
            forecastDataMap.put(uriList.get(i), idDbList.get(i));
        }

        // Perform the network request, parse the response, and extract a list of forecasts.
        List<Weather> forecasts = QueryUtils.fetchForecastData(forecastDataMap);
        return forecasts;
    }

}
