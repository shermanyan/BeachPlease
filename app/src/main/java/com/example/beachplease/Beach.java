package com.example.beachplease;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Beach implements Parcelable {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private List<String> tags;
    private String description;
    private String formattedAddress;
    private List<String> hours;
    private Map<String, Integer> tagNumber;

    public Beach(String id, String name, double latitude, double longitude, List<String> tags,
                 String description, String formattedAddress, List<String> hours, Map<String, Integer> tagNumber) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tags = tags;
        this.description = description;
        this.formattedAddress = formattedAddress;
        this.hours = hours;
        this.tagNumber = tagNumber;
    }

    public Beach() {
        // Default constructor
        tagNumber = new HashMap<>(); // Initialize the map to avoid null issues
    }

    // Parcelable constructor
    protected Beach(Parcel in) {
        id = in.readString();
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        tags = in.createStringArrayList();
        description = in.readString();
        formattedAddress = in.readString();
        hours = in.createStringArrayList();

        // Initialize the map
        tagNumber = new HashMap<>();
        // Read the map from the Parcel
        in.readMap(tagNumber, Integer.class.getClassLoader()); // Explicitly state the value type
    }

    public static final Creator<Beach> CREATOR = new Creator<Beach>() {
        @Override
        public Beach createFromParcel(Parcel in) {
            return new Beach(in);
        }

        @Override
        public Beach[] newArray(int size) {
            return new Beach[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(tags);
        dest.writeString(description);
        dest.writeString(formattedAddress);
        dest.writeStringList(hours);
        dest.writeMap(tagNumber); // Write the map to the Parcel
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFormattedAddress() { return formattedAddress; }
    public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }

    public List<String> getHours() { return hours; }
    public void setHours(List<String> hours) { this.hours = hours; }

    public Map<String, Integer> getTagNumber() { return tagNumber; }
    public void setTagNumber(Map<String, Integer> tagNumber) { this.tagNumber = tagNumber; }
}