package com.example.beachplease;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherView extends LinearLayout {

    protected static class WeatherUtil {
        public static String getWeatherDescription(Integer weatherCode) {
            switch (weatherCode) {
                case 0:
                    return "Clear";
                case 1:
                    return "Mainly Clear";
                case 2:
                    return "Partly Cloudy";
                case 3:
                    return "Cloudy";
                case 45:
                case 48:
                    return "Fog";
                case 51:
                case 53:
                case 55:
                    return "Drizzle";
                case 61:
                case 63:
                case 65:
                    return "Rain";
                case 80:
                case 81:
                case 82:
                    return "Rain Showers";
                case 95:
                    return "Thunderstorm";
                default:
                    return "Unknown";
            }
        }

        public static String getWeatherAnimationFile(int weatherCode) {
            switch (weatherCode) {
                case 0:
                    return "clear-day.json";
                case 1:
                case 2:
                case 3:
                    return "cloudy.json";
                case 45:
                case 48:
                    return "fog.json";
                case 51:
                case 53:
                case 55:
                case 56:
                case 57:
                case 61:
                case 63:
                case 65:
                case 66:
                case 67:
                case 80:
                case 81:
                case 82:
                    return "rain.json";
                case 71:
                case 73:
                case 75:
                case 77:
                case 85:
                case 86:
                    return "snow.json";
                case 95:
                case 96:
                case 99:
                    return "thunderstorms.json";
                default:
                    return "not-available.json";
            }

        }

        public static int toFahrenheit(int celsius) {

            return (celsius * 9 / 5 + 32);
        }
    }

    private static class ForecastItem extends LinearLayout {
        private TextView weatherDay;
        private TextView weatherHigh;
        private TextView weatherLow;
        private LottieAnimationView weatherIcon;

        public WeatherData weatherData = new WeatherData();


        public ForecastItem(Context context, WeatherData weatherData) {
            super(context);
            init(context);
            this.weatherData = weatherData;

            weatherDay.setText(new SimpleDateFormat("EEE", Locale.getDefault()).format(weatherData.date));
            weatherHigh.setText(String.format(Locale.getDefault(), "%d°", weatherData.high));
            weatherLow.setText(String.format(Locale.getDefault(), "%d°", weatherData.low));

            weatherIcon.setAnimation(WeatherUtil.getWeatherAnimationFile(weatherData.weatherCode)); // Set actual weather icon based on data
        }


        private void init(Context context) {
            LayoutInflater.from(context).inflate(R.layout.forecast_item, this, true);
            weatherDay = findViewById(R.id.weather_day);
            weatherHigh = findViewById(R.id.weather_high);
            weatherLow = findViewById(R.id.weather_low);
            weatherIcon = findViewById(R.id.weather_icon);
        }


    }

    private static class WeatherData {
        public Integer weatherCode;
        public Integer high;
        public Integer low;
        public Integer precipitation;
        public Integer uvIndex;
        public String sunset;
        public String description;
        public Date date;
    }

    private enum Tab {
        TEMPERATURE, WAVE_HEIGHT,
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private WeatherData displayed_weatherData;

    private final TextView temperatureTab;
    private final TextView waveHeightTab;
    private View graph;

    private final LottieAnimationView weatherIcon;
    private final TextView currentTemperature;
    private final TextView currentWeatherDescription;
    private final TextView precipitationText;
    private final TextView uvIndexText;
    private final TextView sunsetTimeText;
    private final TextView dayOfWeekText;

    private Tab activeTab = Tab.TEMPERATURE;

    private final Beach beach;
    private final List<ForecastItem> forecastList;


    public WeatherView(Context context, Beach beach) {
        super(context);

        forecastList = new ArrayList<>();

        this.beach = beach;

        displayed_weatherData = new WeatherData();
        displayed_weatherData.date = Calendar.getInstance().getTime();


        createView(context);
        getCurrentWeather();
        getForecastWeather();


        temperatureTab = findViewById(R.id.tab_temperature);
        waveHeightTab = findViewById(R.id.tab_waveheight);

        temperatureTab.setOnClickListener(this::switchTabs);
        waveHeightTab.setOnClickListener(this::switchTabs);

        weatherIcon = findViewById(R.id.weather_icon);
        currentTemperature = findViewById(R.id.current_temperature);
        currentWeatherDescription = findViewById(R.id.current_weather_description);
        precipitationText = findViewById(R.id.precipitation);
        uvIndexText = findViewById(R.id.uv_index);
        sunsetTimeText = findViewById(R.id.sunset_time);
        dayOfWeekText = findViewById(R.id.day_of_week);

    }


    private void createView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.weather_view, this, true);
        this.setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);

    }

    private void updateView() {
        updateView(false);
    }

    private void updateView(boolean current) {

        if (current)
            dayOfWeekText.setText(new SimpleDateFormat("EEEE h:mm a", Locale.getDefault()).format(displayed_weatherData.date));
        else
            dayOfWeekText.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(displayed_weatherData.date));
        currentTemperature.setText(String.format(Locale.getDefault(), "%d°F", displayed_weatherData.high));
        currentWeatherDescription.setText(WeatherUtil.getWeatherDescription(displayed_weatherData.weatherCode));
        precipitationText.setText(String.format(Locale.getDefault(), "%d%%", displayed_weatherData.precipitation));
        uvIndexText.setText(String.valueOf(displayed_weatherData.uvIndex));
        sunsetTimeText.setText(displayed_weatherData.sunset);

        weatherIcon.cancelAnimation();
        weatherIcon.setAnimation(WeatherUtil.getWeatherAnimationFile(displayed_weatherData.weatherCode));
        weatherIcon.playAnimation();

    }

    private void switchTabs(View view) {
        Tab tab;
        if (view.getId() == R.id.tab_temperature) {
            tab = Tab.TEMPERATURE;
        } else {
            tab = Tab.WAVE_HEIGHT;
        }

        switch (tab) {
            case TEMPERATURE:
                temperatureTab.setTextColor(ContextCompat.getColor(this.getContext(), R.color.oceanblue));
                waveHeightTab.setTextColor(ContextCompat.getColor(this.getContext(), R.color.dark_gray));
                break;
            case WAVE_HEIGHT:
                temperatureTab.setTextColor(ContextCompat.getColor(this.getContext(), R.color.dark_gray));
                waveHeightTab.setTextColor(ContextCompat.getColor(this.getContext(), R.color.oceanblue));
                break;
        }

        activeTab = tab;
        updateTables();
    }


    private void dayForecastHandler(View v, ForecastItem forecastItem) {

        for (ForecastItem f : forecastList) {
            if (f == v) {
                f.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.weather_roundedbox_selected));
            } else {
                f.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.weather_roundedbox));
            }
        }

        displayed_weatherData = forecastItem.weatherData;
        updateTables();
        updateView();
    }


    private void updateTables() {
        // Placeholder for updating tables

    }

    private void getCurrentWeather() {
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + beach.getLatitude() + "&longitude=" + beach.getLongitude() + "&current_weather=true&hourly=precipitation_probability,uv_index&daily=sunset,weathercode&timezone=auto";

        executor.execute(() -> {
            String result = fetchWeatherFromUrl(urlString);
            mainThreadHandler.post(() -> {
                parseCurrentWeatherData(result, displayed_weatherData);
                updateView(true);
            });
        });
    }


    private void parseCurrentWeatherData(String json, WeatherData data) {
        try {
            JSONObject jsonObject = new JSONObject(json);

            JSONObject currentWeather = jsonObject.getJSONObject("current_weather");
            data.high = WeatherUtil.toFahrenheit(currentWeather.getInt("temperature"));  // Convert to Fahrenheit
            data.weatherCode = currentWeather.getInt("weathercode");

            JSONArray hourlyPrecipitation = jsonObject.getJSONObject("hourly").getJSONArray("precipitation_probability");
            JSONArray hourlyUvIndex = jsonObject.getJSONObject("hourly").getJSONArray("uv_index");

            data.precipitation = hourlyPrecipitation.getInt(0);
            data.uvIndex = hourlyUvIndex.getInt(0);

            JSONArray sunsetArray = jsonObject.getJSONObject("daily").getJSONArray("sunset");
            String sunsetTime = sunsetArray.getString(0);
            data.sunset = parseTime(sunsetTime);

        } catch (Exception e) {
            Log.e("WeatherView", "Error parsing current weather data", e);
        }
    }

    private void getForecastWeather() {
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + beach.getLatitude() + "&longitude=" + beach.getLongitude() + "&daily=temperature_2m_max,temperature_2m_min,weathercode,precipitation_sum,uv_index_max,sunset&timezone=auto";

        executor.execute(() -> {
            String result = fetchWeatherFromUrl(urlString);
            mainThreadHandler.post(() -> {
                populateForecast(this.getContext(), result);
            });
        });
    }

    private void populateForecast(Context context, String forecastJsonData) {
        try {
            Calendar cal = Calendar.getInstance();

            JSONObject jsonObject = new JSONObject(forecastJsonData);
            JSONArray dailyTemperatures = jsonObject.getJSONObject("daily").getJSONArray("temperature_2m_max");
            JSONArray dailyLowTemperatures = jsonObject.getJSONObject("daily").getJSONArray("temperature_2m_min");
            JSONArray weatherCodes = jsonObject.getJSONObject("daily").getJSONArray("weathercode");
            JSONArray dailyPrecipitation = jsonObject.getJSONObject("daily").getJSONArray("precipitation_sum");
            JSONArray dailyUvIndex = jsonObject.getJSONObject("daily").getJSONArray("uv_index_max");
            JSONArray dailySunset = jsonObject.getJSONObject("daily").getJSONArray("sunset");

            forecastList.clear();

            for (int i = 0; i < dailyTemperatures.length(); i++) {
                WeatherData weatherData = new WeatherData();
                weatherData.date = cal.getTime();
                weatherData.high = WeatherUtil.toFahrenheit(dailyTemperatures.getInt(i));
                weatherData.low = WeatherUtil.toFahrenheit(dailyLowTemperatures.getInt(i));
                weatherData.weatherCode = weatherCodes.getInt(i);
                weatherData.precipitation = dailyPrecipitation.getInt(i);
                weatherData.uvIndex = dailyUvIndex.getInt(i);
                weatherData.sunset = parseTime(dailySunset.getString(i));
                weatherData.description = WeatherUtil.getWeatherDescription(weatherData.weatherCode);

                forecastList.add(new ForecastItem(context, weatherData));

                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

            LinearLayout forecastContainer = findViewById(R.id.forecast_container);

            for (ForecastItem forecastItem : forecastList) {
                forecastItem.setOnClickListener(v -> dayForecastHandler(v, forecastItem));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 20, 0);
                forecastItem.setLayoutParams(params);

                forecastContainer.addView(forecastItem);
            }
        } catch (Exception e) {
            Log.e("WeatherView", "Error parsing forecast data", e);
        }
    }


    public static String parseTime(String iso8601Time) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
        SimpleDateFormat amPmFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        try {
            Date date = isoFormat.parse(iso8601Time);
            assert date != null;
            return amPmFormat.format(date);
        } catch (ParseException e) {
            Log.e("WeatherView", "Error parsing time", e);
            return null;
        }
    }

    private String fetchWeatherFromUrl(String urlString) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            Log.e("WeatherView", "Error fetching weather data", e);
        }
        return result.toString();
    }


}
