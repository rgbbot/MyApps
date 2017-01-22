package com.liondevhq.weathertomorrow;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by pc on 21.01.2017.
 */
public class WeatherLoader extends AsyncTaskLoader<List<Weather>> {

    /** Tag for log messages */
    private static final String LOG_TAG = WeatherLoader.class.getName();

    /** Query URL */
    private List<String> mUrl;

    /**
     * Constructs a new {@link WeatherLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public WeatherLoader(Context context, List<String> url) {
        super(context);
        mUrl = url;
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
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of forecasts.
        List<Weather> forecasts = QueryUtils.fetchForecastData(mUrl);
        return forecasts;
    }

}
