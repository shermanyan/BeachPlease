package com.example.beachplease;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherView extends LinearLayout {

    private static class ForecastDate extends LinearLayout {
        private TextView weatherDay;
        private TextView weatherHigh;
        private TextView weatherLow;
        private ImageView weatherIcon;

        private Date date;

        public ForecastDate(Context context, Date date, String day, int high, int low, String description) {
            super(context);
            init(context);
            setData(date, day, high, low, description);
        }

        public ForecastDate(Context context) {
            super(context);
            init(context);
        }

        private void init(Context context) {
            LayoutInflater.from(context).inflate(R.layout.forecast_item, this, true);
            weatherDay = findViewById(R.id.weather_day);
            weatherHigh = findViewById(R.id.weather_high);
            weatherLow = findViewById(R.id.weather_low);
            weatherIcon = findViewById(R.id.weather_icon);
        }

        public void setData(Date date,String day, int high, int low, String description) {
            weatherDay.setText(day);
            weatherHigh.setText(String.format(Locale.getDefault(), "%d°F", high));
            weatherLow.setText(String.format(Locale.getDefault(), "%d°F", low));

            // TODO: Set actual weather icon based on description
            weatherIcon.setImageResource(R.drawable.ic_user_icon); // Set actual weather icon based on data

            this.date = date;
        }

        public Date getDate() {
            return date;
        }
    }
    private static class WeatherData {
        public String currentDescription;
        public Integer currentTemperature;
        public Integer precipitation;
        public Integer uvIndex;
        public Time sunset;
        public List<ForecastDate> forecastList;

        public WeatherData() {
            forecastList = new ArrayList<>();
        }
    }
    private enum Tab {
        TEMPERATURE,
        WAVE_HEIGHT,
    }

    private final WeatherData weatherData;

    private TextView temperatureTab;
    private TextView waveHeightTab;
    private View graph;

    private Tab activeTab = Tab.TEMPERATURE;
    private Calendar calendar;

    private Beach beach;

    public WeatherView(Context context, Beach beach) {
        super(context);

        calendar = Calendar.getInstance();

        this.beach = beach;

        weatherData = new WeatherData();
        createView(context);
        populateForecast(this.getContext());
        getCurrentWeather();
        updateView();


        temperatureTab = findViewById(R.id.tab_temperature);
        waveHeightTab = findViewById(R.id.tab_waveheight);

        temperatureTab.setOnClickListener(this::switchTabs);
        waveHeightTab.setOnClickListener(this::switchTabs);

    }


    private void createView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.weather_view, this, true);
        this.setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);

    }

    private void populateForecast(Context context) {
        // Example forecast data
        // TODO: Replace with actual forecast data

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            Date date = calendar.getTime();
            String dayOfWeek = dayFormat.format(date).toUpperCase(Locale.getDefault());

            // TODO: Replace with actual forecast data
            int high = 75;
            int low = 60;
            String description = "Sunny";

            weatherData.forecastList.add(new ForecastDate(context, date, dayOfWeek, high, low, description));

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        LinearLayout forecastContainer = findViewById(R.id.forecast_container);

        for (ForecastDate forecastDate : weatherData.forecastList) {
            forecastDate.setOnClickListener(v->dayForecastHandler(v,forecastDate.getDate()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 15, 0); // Set bottom margin for spacing
            forecastDate.setLayoutParams(params); // Apply margin to the forecast date view

            forecastContainer.addView(forecastDate);
        }
    }

    private void getCurrentWeather() {
        // Placeholder for current weather data
        // TODO Replace with actual weather data
        weatherData.currentDescription = "Sunny";
        weatherData.currentTemperature = 69;
        weatherData.precipitation = 10;
        weatherData.uvIndex = 10;
        weatherData.sunset = new Time(20, 32, 0); // Sunset time at 8:32 PM
    }

    private void updateView() {
        ImageView weatherIcon = findViewById(R.id.weather_icon);
        TextView currentTemperature = findViewById(R.id.current_temperature);
        TextView currentWeatherDescription = findViewById(R.id.current_weather_description);
        TextView precipitationText = findViewById(R.id.precipitation);
        TextView uvIndexText = findViewById(R.id.uv_index);
        TextView sunsetTimeText = findViewById(R.id.sunset_time);

        currentTemperature.setText(String.format(Locale.getDefault(), "%d°F", weatherData.currentTemperature));
        currentWeatherDescription.setText(weatherData.currentDescription);
        precipitationText.setText(String.format(Locale.getDefault(), "%d%%", weatherData.precipitation));
        uvIndexText.setText(String.valueOf(weatherData.uvIndex));
        sunsetTimeText.setText(String.format(Locale.getDefault(), "%02d:%02d PM", weatherData.sunset.getHours(), weatherData.sunset.getMinutes()));

        // Set placeholder data
        // TODO Set actual weather icon based on description
        weatherIcon.setImageResource(R.drawable.ic_user_icon); // Set actual weather icon based on data

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
        updateTables(calendar.getTime());
    }


    private void dayForecastHandler(View v, Date date) {
        updateTables(date);

        for (ForecastDate forecastDate : weatherData.forecastList) {
            if (forecastDate == v) {
                forecastDate.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.weather_roundedbox_selected));
            } else {
                forecastDate.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.weather_roundedbox));
            }
        }
    }


    private void updateTables (Date date) {
        // Placeholder for updating tables

    }
}
