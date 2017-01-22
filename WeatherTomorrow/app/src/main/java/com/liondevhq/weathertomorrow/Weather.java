package com.liondevhq.weathertomorrow;

/**
 * Created by pc on 21.01.2017.
 */
public class Weather {
    /** City for the forecast */
    private String mCity;

    /** Forecast temperature */
    private String mTemp;

    /** Website URL of the Weather */
    private String mUrl;

    /**
     * Constructs a new {@link Weather} object.
     *
     * @param city is the city for forecast
     * @param temperature is the temperature for city
     * @param url is the website URL to find more details about the weather forecast
     */
    public Weather(String city, String temperature, String url) {
        mCity = city;
        mTemp = temperature;
        mUrl = url;
    }

    /**
     * Returns the city.
     */
    public String getCity() {
        return mCity;
    }

    /**
     * Returns the temperature for this city.
     */
    public String getTemperature() {
        return mTemp;
    }

    /**
     * Returns the website URL to find more information about the current forecast.
     */
    public String getUrl() {
        return mUrl;
    }
}
