package com.liondevhq.weathertomorrow;

/**
 * Created by pc on 21.01.2017.
 */

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper methods related to requesting and receiving forecasts data from OpenWeatherMap.
 */
public final class QueryUtils {

    /** Tag for the log messages */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /** Base URL for site access that are needed to fetch data for certain city */
    private static final String BASE_URL =
            "https://openweathermap.org/city/";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the OpenWeatherMap dataset and return a list of {@link Weather} objects.
     */
    public static List<Weather> fetchForecastData(List<String> requestUrlList) {

        //Create list for holding elements that come back from HTTP request
        List<Weather> weatherList = new ArrayList<>();

        //For every income url create URL object, perform HTTP request and receive a JSON response back
        for (String requestUrl : requestUrlList) {
            // Create URL object
            URL url = createUrl(requestUrl);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = null;
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }

            // Extract relevant fields from the JSON response and create a {@link Weather} object
            Weather weather = extractFeatureFromJson(jsonResponse);

            //Add received weather object to list
            weatherList.add(weather);
        }

        // Return the list of {@link Weather} objects
        return weatherList;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the forecasts JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies that an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /***
     * Round double value to a number of places
     * @param value - double number
     * @param places - places to be left
     * @return - rounded value
     */
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Return a list of {@link Weather} objects that has been built up from
     * parsing the given JSON response.
     */
    private static Weather extractFeatureFromJson(String weatherJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(weatherJSON)) {
            return null;
        }

        Weather weatherObject = null;

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(weatherJSON);

            //Get the city object from JSON
            JSONObject cityObject = baseJsonResponse.getJSONObject("city");

            // Extract the value for the key called "id"
            int cityId = cityObject.getInt("id");

            // Extract the value for the key called "name"
            String cityName = cityObject.getString("name");

            // Extract the value for the key called "country"
            String cityCountry = cityObject.getString("country");

            // Extract the JSONArray associated with the key called "list",
            // which represents a list of weather 3-hours interval.
            JSONArray weatherArray = baseJsonResponse.getJSONArray("list");

            //To perfectly fetch the data from a server (see in documentation why)
            //I need to compare quoters of the current day.
            //Get current datetime
            Calendar rightNow = Calendar.getInstance();
            int hourOfDay = rightNow.get(Calendar.HOUR_OF_DAY);

            //Set the counters for the future for cycle
            int counterFrom = 0;
            int counterTo = 0;

            //Set switch that depends from the current quoter of the current day
            //to set for cycle read only tomorrow data
            switch(hourOfDay) {
                case 0:
                case 1:
                case 2:
                    counterFrom = 8;
                    counterTo = 15;
                    break;
                case 3:
                case 4:
                case 5:
                    counterFrom = 7;
                    counterTo = 14;
                    break;
                case 6:
                case 7:
                case 8:
                    counterFrom = 6;
                    counterTo = 13;
                    break;
                case 9:
                case 10:
                case 11:
                    counterFrom = 5;
                    counterTo = 12;
                    break;
                case 12:
                case 13:
                case 14:
                    counterFrom = 4;
                    counterTo = 11;
                    break;
                case 15:
                case 16:
                case 17:
                    counterFrom = 3;
                    counterTo = 10;
                    break;
                case 18:
                case 19:
                case 20:
                    counterFrom = 2;
                    counterTo = 9;
                    break;
                case 21:
                case 22:
                case 23:
                    counterFrom = 1;
                    counterTo = 8;
                    break;
            }

            //Get every quoter of tomorrow's day to fetch it's min and max temperature
            double minTemp = 1000.0;
            double maxTemp = -1000.0;

            //Humidity sum holder
            double humiditySum = 0;

            //Wind speed sum holder
            double windSpeedSum = 0;

            //Id mapping
            HashMap<String, Integer> idMap = new HashMap<String, Integer>();

            //Description mapping
            HashMap<String, Integer> descriptionMap = new HashMap<String, Integer>();

            for (int i = counterFrom; i <= counterTo; i++) {
                // Get a single weather forecast of current quoter
                JSONObject weatherArrayJSONObject = weatherArray.getJSONObject(i);

                //Extract object from list called "main"
                JSONObject mainInfoFromListObject = weatherArrayJSONObject.getJSONObject("main");

                // Extract the value for the key called "temp_min"
                double min = mainInfoFromListObject.getDouble("temp_min");

                //Compare initial min value and the min value from JSON
                //Then assign min value to a minTemp variable
                minTemp = Math.min(min, minTemp);

                // Extract the value for the key called "temp_max"
                double max = mainInfoFromListObject.getDouble("temp_max");

                //Compare initial max value and the max value from JSON
                //Then assign max value to a maxTemp variable
                maxTemp = Math.max(max, maxTemp);

                //Humidity addition
                double humidityObject = mainInfoFromListObject.getDouble("humidity");
                humiditySum += humidityObject;



                //Extract array from list called "weather"
                JSONArray weatherArrayInfoFromListObject = weatherArrayJSONObject.getJSONArray("weather");
                //Zero object
                JSONObject zeroObject = weatherArrayInfoFromListObject.getJSONObject(0);
                //Get id of current weather
                String IdObject = zeroObject.getString("id");

                //Insert id value to the map if it exists
                Integer countId = 0;
                if (idMap.containsKey(IdObject)) {
                    idMap.put(IdObject, countId + 1);
                } else {
                    idMap.put(IdObject, 1);
                }
                //Get description of current weather
                String descriptionObject = zeroObject.getString("description");

                //Insert description value to the map if it exists
                Integer countDescription = 0;
                if (descriptionMap.containsKey(descriptionObject)) {
                    descriptionMap.put(descriptionObject, countDescription + 1);
                } else {
                    descriptionMap.put(descriptionObject, 1);
                }


                //Extract object from list called "wind"
                JSONObject windInfoFromListObject = weatherArrayJSONObject.getJSONObject("wind");

                //Wind speed addition
                double windSpeedObject = windInfoFromListObject.getDouble("speed");
                windSpeedSum += windSpeedObject;

            }

            //Final humidity average value
            double humidityValue = humiditySum/counterTo;

            //Final id extraction based on most repeated value
            Map.Entry<String, Integer> maxIdEntry = null;
            for (Map.Entry<String, Integer> entry1 : idMap.entrySet()) {
                if (maxIdEntry == null || (entry1.getValue() > maxIdEntry.getValue())) {
                    maxIdEntry = entry1;
                }
            }

            //Final description extraction based on most repeated value
            Map.Entry<String, Integer> maxDescEntry = null;
            for (Map.Entry<String, Integer> entry2 : descriptionMap.entrySet()) {
                if (maxDescEntry == null || (entry2.getValue() > maxDescEntry.getValue())) {
                    maxDescEntry = entry2;
                }
            }

            //Final wind speed average value
            double windSpeedValue = windSpeedSum/counterTo;



            /** Here I generate final data to create Weather object **/

            //Form forecast city for an Weather future object creation
            String forecastCity = cityName + ", " + cityCountry;

            //Form forecast temperature for an Weather future object creation
            String forecastTemperature = minTemp + "/" + maxTemp;

            //Form url for an Weather future object creation
            String url = BASE_URL + cityId;

            //Round humidity for an Weather future object creation
            int humidity = (int) Math.round(humidityValue);

            //Extract id from map
            String id = maxIdEntry.getKey();

            //Extract description from map
            String description = maxDescEntry.getKey();

            double windSpeed = round(windSpeedValue, 2);

            //TODO: Provide icon fetching based on id received

            // Create a new {@link Weather} object with the forecastCity, forecastTemperature,
            // and url from the JSON response.
            Weather weather = new Weather(forecastCity, forecastTemperature, url, humidity, id, description, windSpeed);

            // Assign the new {@link Weather}.
            weatherObject = weather;


        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the forecasts JSON results", e);
        }

        // Return the forecast
        return weatherObject;
    }


}