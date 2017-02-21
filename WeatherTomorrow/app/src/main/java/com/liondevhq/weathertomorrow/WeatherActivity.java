package com.liondevhq.weathertomorrow;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WeatherActivity extends AppCompatActivity implements LoaderCallbacks<List<Weather>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    //TODO: 1) Change List of cities to SQL DB
    //TODO: 2) Refresh view when city forecast adds from editor

    private static final String LOG_TAG = WeatherActivity.class.getName();

    /** URL for forecast data from the OpenWeatherMap dataset */
    private static final String OWM_REQUEST_URL =
            "http://api.openweathermap.org/data/2.5/forecast";

    /**
     * Constant value for the weather loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int WEATHER_LOADER_ID = 1;

    /** Adapter for the list of forecasts */
    private WeatherAdapter mAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find a reference to the {@link ListView} in the layout
        ListView weatherListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        weatherListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of weather as input
        mAdapter = new WeatherAdapter(this, new ArrayList<Weather>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        weatherListView.setAdapter(mAdapter);

        // Obtain a reference to the SharedPreferences file for this app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected forecast.
        weatherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current forecast that was clicked on
                Weather currentWeather = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri forecastUri = Uri.parse(currentWeather.getUrl());

                // Create a new intent to view the forecast URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, forecastUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(WEATHER_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_temp_unit_key))){
            // Clear the ListView as a new query will be kicked off
            mAdapter.clear();

            // Hide the empty state text view as the loading indicator will be displayed
            mEmptyStateTextView.setVisibility(View.GONE);

            // Show the loading indicator while new data is being fetched
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Restart the loader to requery the OWM as the query settings have been updated
            getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<List<Weather>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String tempUnit = sharedPrefs.getString(
                getString(R.string.settings_temp_unit_key),
                getString(R.string.settings_temp_unit_default));

        //List for storing built URIs
        List<String> uriList = new ArrayList<>();

        /*** ATTENTION
         *
         * Here we input cities for which we want to see the forecast
         *
         * There are no limits to the amount of cities
         * You have only add the to list "cities" value with appropriate city
         *
         * The full list of cities you can get from http://bulk.openweathermap.org/sample/ (city.list.json.gz)
         * Or just visit site, find your city using search results and get info from there
         *
         * ***/

        List<String> cities = new ArrayList<>();
        cities.add("London,uk");
        cities.add("Kiev,ua");
        cities.add("Berlin,de");
        cities.add("Dubai,ae");

        //For each city in the list generate URI and put it in the URIs list
        for (String city : cities){
            Uri baseUri = Uri.parse(OWM_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("q", city);
            uriBuilder.appendQueryParameter("cnt", "16");
            uriBuilder.appendQueryParameter("units", tempUnit);
            uriBuilder.appendQueryParameter("appid", "031d20c5934f7a1edd29b1bcfe6c4874");

            uriList.add(uriBuilder.toString());
        }

        return new WeatherLoader(this, uriList);
    }

    @Override
    public void onLoadFinished(Loader<List<Weather>> loader, List<Weather> weatherList) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No forecasts found."
        mEmptyStateTextView.setText(R.string.no_forecasts);

        // Clear the adapter of previous forecasts data
        mAdapter.clear();

        // If there is a valid list of forecasts, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (weatherList != null && !weatherList.isEmpty()) {
            mAdapter.addAll(weatherList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Weather>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
