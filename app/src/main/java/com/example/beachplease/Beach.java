package com.example.beachplease;

import java.util.List;

//Sample beach class
public class Beach {
    private String id;
    private String name;
    private String accessHours;
    private String weather;
    private double temperature;
    private double waveHeight;
    private double latitude;
    private double longitude;
    private List<String> tags;

    public Beach(String id, String name, String accessHours, String weather, double temperature,
                 double waveHeight, double latitude, double longitude, List<String> tags) {
        this.id = id;
        this.name = name;
        this.accessHours = accessHours;
        this.weather = weather;
        this.temperature = temperature;
        this.waveHeight = waveHeight;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessHours() {
        return accessHours;
    }

    public void setAccessHours(String accessHours) {
        this.accessHours = accessHours;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getWaveHeight() {
        return waveHeight;
    }

    public void setWaveHeight(double waveHeight) {
        this.waveHeight = waveHeight;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
