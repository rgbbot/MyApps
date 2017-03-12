package com.liondevhq.weathertomorrow;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.liondevhq.weathertomorrow.data.WeatherContract;

import java.util.ArrayList;
import java.util.List;

public class WeatherActivity extends AppCompatActivity implements LoaderCallbacks<List<Weather>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /*
        TODO: 0) Get rid of SQL query on the main thread - DONE
        TODO: 1) Delete all - DONE
        TODO: 2) Refresh when delete all - DONE
        TODO: 3) Change color of FAB - DONE
        TODO: 4) Create menu for new sql features - DONE
        TODO: 5) Edit item on click and hold - DONE
        TODO: 6) Check DB requests for 1 item - DONE
        TODO: 7) Change Editor Activity depending on editing or adding item
        TODO: 8) Swap onClick to change and web site to long click
     */

    private static final String LOG_TAG = WeatherActivity.class.getName();

    /**
     * Constant value for the weather loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int WEATHER_LOADER_ID = 1;

    /**
     * Constants that are used in context menu, when user click on forecast
     * in the list, and represents each menu item id
     */
    public static final int EDIT = 0;
    public static final int WEBSITE = 1;

    /** Adapter for the list of forecasts */
    private WeatherAdapter mAdapter;

    /** URI for the current onClicked item */
    private Uri mCurrentUri;
    /** Position for the current onClicked item */
    private int mCurrentPosition;


    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
    }

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
        registerForContextMenu(weatherListView);

        // Obtain a reference to the SharedPreferences file for this app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected forecast.
        weatherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //TODO: provide correct id returning
                int idDb = mAdapter.getItem(position).getIdDB();
                // Save the state of current Uri for the future usage.
                mCurrentUri =  ContentUris.withAppendedId(WeatherContract.WeatherEntry.CONTENT_URI, idDb);
                // Save the state of current position for the future usage.
                mCurrentPosition = position;
                // Show context menu on clicked item
                view.showContextMenu();
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


    //Context menu creation
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            int position = info.position;
            //System.err.println("position: " + position);
            menu.setHeaderTitle(R.string.weather_activity_context_menu_title);
            menu.add(Menu.NONE, EDIT, position, R.string.weather_activity_context_edit_menu_item);
            menu.add(Menu.NONE, WEBSITE, position, R.string.weather_activity_context_website_menu_item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0:
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(WeatherActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific forecast that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link WeatherEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.com.liondevhq.weathertomorrow/weather/2"
                // if the forecast with ID 2 was clicked on.
                Uri currentPetUri = mCurrentUri;
                //System.err.println(currentPetUri.toString());

                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display the data for the current forecast.
                startActivity(intent);
                break;
            case 1:
                // Find the current forecast that was clicked on
                Weather currentWeather = mAdapter.getItem(mCurrentPosition);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri forecastUri = Uri.parse(currentWeather.getUrl());

                // Create a new intent to view the forecast URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, forecastUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
                break;
        }
        return true;
    }

    /**
     * Helper method to delete all forecasts in the database.
     */
    private void deleteAllForecasts() {
        int rowsDeleted = getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
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

        return new WeatherLoader(this, tempUnit);
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
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Settings" menu option
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllForecasts();
                // Refresh data
                getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
