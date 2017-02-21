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

    /** Humidity of the Weather */
    private int mHumidity;

    /** Id of the Weather */
    private String mId;

    /** Description of the Weather */
    private String mDescription;

    /** Wind speed of the Weather */
    private double mWindSpeed;

    /**
     * Creates new {@link Weather} object
     * @param city
     * @param temperature
     * @param url
     * @param humidity
     * @param id
     * @param description
     * @param windSpeed
     */
    public Weather(String city, String temperature, String url, int humidity, String id, String description, double windSpeed) {
        mCity = city;
        mTemp = temperature;
        mUrl = url;
        mHumidity = humidity;
        mId = id;
        mDescription = description;
        mWindSpeed = windSpeed;
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

    /**
     * Returns the humidity of current forecast.
     */
    public int getHumidity() {
        return mHumidity;
    }

    /**
     * Returns the id of current forecast.
     */
    public String getIdWeather() {
        return mId;
    }

    /**
     * Returns the description of current forecast.
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Returns the wind speed of current forecast.
     */
    public double getWindSpeed() {
        return mWindSpeed;
    }
}
