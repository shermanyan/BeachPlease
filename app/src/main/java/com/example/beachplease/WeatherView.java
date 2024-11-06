package com.example.beachplease;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
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

    public static class WeatherData {
        public double wave_height;
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

    private TextView temperatureTab;
    private TextView waveHeightTab;
    private LineChart tempChart;
    private LineChart waveChart;

    private LottieAnimationView weatherIcon;
    private TextView currentTemperature;
    private TextView currentWeatherDescription;
    private TextView waveHeightText;
    private TextView precipitationText;
    private TextView uvIndexText;
    private TextView sunsetTimeText;
    private TextView dayOfWeekText;

    private final Beach beach;
    private final List<ForecastItem> forecastList;


    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        forecastList = new ArrayList<>();
        beach = null; // or initialize appropriately
        displayed_weatherData = new WeatherData();
        displayed_weatherData.date = Calendar.getInstance().getTime();
        createView(context);
        getCurrentWeather();
        getForecastWeather();

        init(context);
    }

    public WeatherView(Context context, Beach beach) {
        super(context);

        forecastList = new ArrayList<>();
        this.beach = beach;

        init(context);
    }

    private void init(Context context) {

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
        waveHeightText = findViewById(R.id.wave_height);
        uvIndexText = findViewById(R.id.uv_index);
        sunsetTimeText = findViewById(R.id.sunset_time);
        dayOfWeekText = findViewById(R.id.day_of_week);

        tempChart = findViewById(R.id.temperature_chart);
        waveChart = findViewById(R.id.wave_height_chart);

        setupChart(tempChart);
        setupChart(waveChart);

        updateTables();
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
        waveHeightText.setText(String.format(Locale.getDefault(), "%.2f ft", displayed_weatherData.wave_height));
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
                tempChart.setVisibility(View.VISIBLE);
                waveChart.setVisibility(View.GONE);
                temperatureTab.setTextColor(ContextCompat.getColor(this.getContext(), R.color.oceanblue));
                waveHeightTab.setTextColor(ContextCompat.getColor(this.getContext(), R.color.dark_gray));
                break;
            case WAVE_HEIGHT:
                tempChart.setVisibility(View.GONE);
                waveChart.setVisibility(View.VISIBLE);
                temperatureTab.setTextColor(ContextCompat.getColor(this.getContext(), R.color.dark_gray));
                waveHeightTab.setTextColor(ContextCompat.getColor(this.getContext(), R.color.oceanblue));
                break;
        }

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
        updateTables(displayed_weatherData.date);
        updateView();
    }


    private void getCurrentWeather() {
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + beach.getLatitude() + "&longitude=" + beach.getLongitude() + "&current_weather=true&hourly=precipitation_probability,uv_index&daily=sunset,weathercode&timezone=auto";
        String urlString2 = "https://marine-api.open-meteo.com/v1/marine?latitude=" + beach.getLatitude() + "&longitude=" + beach.getLongitude() + "&hourly=wave_height&timezone=auto&forecast_days=1";

        executor.execute(() -> {
            JSONObject weather = fetchWeatherFromUrl(urlString);
            JSONObject marine = fetchWeatherFromUrl(urlString2);

            mainThreadHandler.post(() -> {
                try {
                    JSONObject currentWeather = weather.getJSONObject("current_weather");
                    displayed_weatherData.high = WeatherUtil.toFahrenheit(currentWeather.getInt("temperature"));  // Convert to Fahrenheit
                    displayed_weatherData.weatherCode = currentWeather.getInt("weathercode");

                    JSONArray hourlyPrecipitation = weather.getJSONObject("hourly").getJSONArray("precipitation_probability");
                    JSONArray hourlyUvIndex = weather.getJSONObject("hourly").getJSONArray("uv_index");

                    displayed_weatherData.precipitation = hourlyPrecipitation.getInt(0);
                    displayed_weatherData.uvIndex = hourlyUvIndex.getInt(0);

                    JSONArray sunsetArray = weather.getJSONObject("daily").getJSONArray("sunset");
                    String sunsetTime = sunsetArray.getString(0);
                    displayed_weatherData.sunset = parseTime(sunsetTime);


                    JSONArray waveHeight = marine.getJSONObject("hourly").getJSONArray("wave_height");
                    displayed_weatherData.wave_height = waveHeight.getDouble(0);

                    updateView(true);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        });

    }

    private void getForecastWeather() {
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + beach.getLatitude() + "&longitude=" + beach.getLongitude() + "&daily=temperature_2m_max,temperature_2m_min,weathercode,precipitation_sum,uv_index_max,sunset&timezone=auto";
        String urlString2 = "https://marine-api.open-meteo.com/v1/marine?latitude=" + beach.getLatitude() + "&longitude=" + beach.getLongitude() + "&daily=wave_height_max&timezone=auto";

        executor.execute(() -> {
            JSONObject weather = fetchWeatherFromUrl(urlString);
            JSONObject marine = fetchWeatherFromUrl(urlString2);

            mainThreadHandler.post(() -> {
                try {
                    Calendar cal = Calendar.getInstance();

                    JSONArray dailyTemperatures = weather.getJSONObject("daily").getJSONArray("temperature_2m_max");
                    JSONArray dailyLowTemperatures = weather.getJSONObject("daily").getJSONArray("temperature_2m_min");
                    JSONArray weatherCodes = weather.getJSONObject("daily").getJSONArray("weathercode");
                    JSONArray dailyPrecipitation = weather.getJSONObject("daily").getJSONArray("precipitation_sum");
                    JSONArray dailyUvIndex = weather.getJSONObject("daily").getJSONArray("uv_index_max");
                    JSONArray dailySunset = weather.getJSONObject("daily").getJSONArray("sunset");

                    JSONArray waveHeight = marine.getJSONObject("daily").getJSONArray("wave_height_max");

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
                        weatherData.wave_height = waveHeight.getDouble(i);

                        forecastList.add(new ForecastItem(this.getContext(), weatherData));

                        cal.add(Calendar.DAY_OF_YEAR, 1);
                    }

                    LinearLayout forecastContainer = findViewById(R.id.forecast_container);

                    for (ForecastItem forecastItem : forecastList) {
                        forecastItem.setOnClickListener(v -> dayForecastHandler(v, forecastItem));

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 0, 15, 0);
                        forecastItem.setLayoutParams(params);

                        forecastContainer.addView(forecastItem);
                    }
                } catch (Exception e) {
                    Log.e("WeatherView", "Error parsing forecast data", e);
                }
            });
        });
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

    private JSONObject fetchWeatherFromUrl(String urlString) {
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
        try {
            return new JSONObject(result.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDragEnabled(true);
        chart.setVisibleXRangeMaximum(2);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(1f);

        leftAxis.setDrawLabels(true);
        leftAxis.setLabelCount(5, true);
    }

    private void updateTables() {
        Calendar cal = Calendar.getInstance();
        updateTables(cal.getTime());
    }

    private void updateTables(Date date) {
        populateTemperatureChart(date);
        populateWaveHeightChart(date);
    }

    private void populateTemperatureChart(Date date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fDate = dateFormat.format(date);

        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + beach.getLatitude()
                + "&longitude=" + beach.getLongitude()
                + "&current=temperature_2m&hourly=temperature_2m&temperature_unit=fahrenheit&timezone=auto&"
                + "&start_date=" + fDate + "&end_date=" + fDate;

        executor.execute(() -> {
            try {
                JSONObject weatherData = fetchWeatherFromUrl(urlString);
                JSONArray temperatureData = weatherData.getJSONObject("hourly").getJSONArray("temperature_2m");
                JSONArray timeData = weatherData.getJSONObject("hourly").getJSONArray("time");

                List<String> dates = new ArrayList<>();
                List<Entry> entries = new ArrayList<>();

                for (int i = 0; i < temperatureData.length(); i++) {
                    String time = timeData.getString(i);
                    dates.add(formatTimeToHour(time));
                    entries.add(new Entry((float) i, (float) temperatureData.getDouble(i)));
                }

                post(() -> {
                    LineDataSet dataSet = new LineDataSet(entries, "Temperature (°F)");
                    dataSet.setColor(ContextCompat.getColor(getContext(), R.color.oceanblue));
                    dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.oceanblue));

                    dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.black));
                    dataSet.setValueTextSize(15f);
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(1f);
                    dataSet.setDrawCircleHole(false);
                    dataSet.setDrawValues(false);

                    LineData lineData = new LineData(dataSet);

                    tempChart.setData(lineData);
                    tempChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));

                    tempChart.getAxisLeft().resetAxisMinimum();
                    tempChart.getAxisLeft().resetAxisMaximum();

                    tempChart.invalidate();
                });
            } catch (Exception e) {
                Log.e("WeatherView", "Error populating temperature chart", e);
            }
        });
    }

    private void populateWaveHeightChart(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fDate = dateFormat.format(date);

        String urlString = "https://marine-api.open-meteo.com/v1/marine?latitude=" + beach.getLatitude()
                + "&longitude=" + beach.getLongitude()
                + "&hourly=wave_height&length_unit=imperial&"
                + "&start_date=" + fDate + "&end_date=" + fDate;

        executor.execute(() -> {
            try {
                JSONObject weatherData = fetchWeatherFromUrl(urlString);
                JSONArray waveHeightData = weatherData.getJSONObject("hourly").getJSONArray("wave_height");
                JSONArray timeData = weatherData.getJSONObject("hourly").getJSONArray("time");

                List<String> dates = new ArrayList<>();
                List<Entry> entries = new ArrayList<>();

                for (int i = 0; i < waveHeightData.length(); i++) {
                    String time = timeData.getString(i);
                    dates.add(formatTimeToHour(time));
                    entries.add(new Entry(i, (float) waveHeightData.getDouble(i)));
                }

                post(() -> {
                    LineDataSet dataSet = new LineDataSet(entries, "Wave Height (ft)");
                    dataSet.setColor(ContextCompat.getColor(getContext(), R.color.oceanblue));
                    dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.oceanblue));

                    dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.black));
                    dataSet.setValueTextSize(15f);

                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(1f);
                    dataSet.setDrawCircleHole(false);
                    dataSet.setDrawValues(false);

                    LineData lineData = new LineData(dataSet);
                    waveChart.setData(lineData);
                    waveChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));

                    waveChart.getAxisLeft().resetAxisMinimum();
                    waveChart.getAxisLeft().resetAxisMaximum();

                    waveChart.invalidate();
                });
            } catch (Exception e) {
                Log.e("WeatherView", "Error populating wave height chart", e);
            }
        });
    }

    private String formatTimeToHour(String isoTime) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
        SimpleDateFormat hourFormat = new SimpleDateFormat("h a", Locale.getDefault());
        try {
            Date date = isoFormat.parse(isoTime);
            assert date != null;
            return hourFormat.format(date);
        } catch (ParseException e) {
            Log.e("WeatherView", "Error formatting time to hour", e);
            return "";
        }
    }


}
