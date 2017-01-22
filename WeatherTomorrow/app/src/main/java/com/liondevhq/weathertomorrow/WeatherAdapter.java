package com.liondevhq.weathertomorrow;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pc on 21.01.2017.
 */
public class WeatherAdapter extends ArrayAdapter<Weather> {

    //Separator that are used to split our min and max temperature
    //that comes from a HTTP request
    public static final String TEMPERATURE_SEPARATOR = "/";

    /**
     * Constructs a new {@link WeatherAdapter}.
     *
     * @param context of the app
     * @param weatherList is the list of weather, which is the data source of the adapter
     */
    public WeatherAdapter(Context context, List<Weather> weatherList) {
        super(context, 0, weatherList);
    }

    /**
     * Returns a list item view that displays information about the weather at the given position
     * in the list of weather.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.weather_list_item, parent, false);
        }

        // Find the forecast at the given position in the list of forecasts
        Weather currentWeather = getItem(position);

        // Find the TextView with view ID city
        TextView cityView = (TextView) listItemView.findViewById(R.id.city);
        // Display the city of the current weather in that TextView
        cityView.setText(currentWeather.getCity());

        // Get the original temperature string from the Weather object
        String originalTemperature = currentWeather.getTemperature();

        /**Temperature that comes from an HTTP request are separated by
        @link TEMPERATURE_SEPARATOR
         all we need is to separate it properly and put in different
         TextViews
        **/
        double minimumTempValue;
        double maximumTempValue;

        // Split the string into different parts (as an array of Strings)
        String[] parts = originalTemperature.split(TEMPERATURE_SEPARATOR);
        minimumTempValue = Double.parseDouble(parts[0]);
        maximumTempValue = Double.parseDouble(parts[1]);

        //Round values to int
        int formattedMinTempValueInt = (int) Math.round(minimumTempValue);
        int formattedMaxTempValueInt = (int) Math.round(maximumTempValue);


        // Find the TextView with view ID min temperature
        TextView temperatureMinView = (TextView) listItemView.findViewById(R.id.temperature_min);
        //Format min temperature
        String formattedMinTempValueString = formatMinTemp(formattedMinTempValueInt);
        // Get the appropriate text color based on the current sign
        int minTempTextColor = getTextColor(formattedMinTempValueInt);
        // Display the temperature of the current weather in that TextView
        temperatureMinView.setText(formattedMinTempValueString);
        //Set the color to min temperature TextView
        temperatureMinView.setTextColor(ContextCompat.getColor(temperatureMinView.getContext(), minTempTextColor));

        // Find the TextView with view ID max temperature
        TextView temperatureMaxView = (TextView) listItemView.findViewById(R.id.temperature_max);
        //Format max temperature
        String formattedMaxTempValueString = formatMaxTemp(formattedMaxTempValueInt);
        // Get the appropriate text color based on the current sign
        int maxTempTextColor = getTextColor(formattedMaxTempValueInt);
        // Display the temperature of the current weather in that TextView
        temperatureMaxView.setText(formattedMaxTempValueString);
        //Set the color to min temperature TextView
        temperatureMaxView.setTextColor(ContextCompat.getColor(temperatureMaxView.getContext(), maxTempTextColor));

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }

    /**
     * Return the right color that depends of integer sign
     */
    private int getTextColor(int formattedTempValueInt) {
        int temperatureColorResourceId;
        if (formattedTempValueInt < 0) {
            temperatureColorResourceId = R.color.colorAccent;
        } else if (formattedTempValueInt == 0) {
            temperatureColorResourceId = R.color.darkGrey;
        } else {
            temperatureColorResourceId = R.color.deepOrange;
        }
        return temperatureColorResourceId;
    }

    /**
     * Return the formatted minimal temperature from extracted from JSON double value
     */
    private String formatMinTemp(int formattedMinTempValueInt) {
        String formattedMinTempValueString;
        if (formattedMinTempValueInt <= 0) {
            formattedMinTempValueString = Integer.toString(formattedMinTempValueInt);
        } else {
            formattedMinTempValueString = "+" + Integer.toString(formattedMinTempValueInt);
        }

        return formattedMinTempValueString;
    }

    /**
     * Return the formatted minimal temperature from extracted from JSON double value
     */
    private String formatMaxTemp(int formattedMaxTempValueInt) {
        String formattedMaxTempValueString;
        if (formattedMaxTempValueInt <= 0) {
            formattedMaxTempValueString = Integer.toString(formattedMaxTempValueInt);
        } else {
            formattedMaxTempValueString = "+" + Integer.toString(formattedMaxTempValueInt);
        }

        return formattedMaxTempValueString;
    }
}
