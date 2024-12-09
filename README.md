# BeachPlease


### Summary
BeachPlease is an Android application that allows users to view and interact with beach-related information across Southern California. The app provides real-time beach conditions, activities, user reviews, and tagging features, helping users choose beaches that fit their interests.


### Features
- **User Account**: Register, log in, log out, and view/edit profiles.
- **Beach Map**: View Southern California beaches on an interactive map.
- **Filtering**: Filter beaches by activity tags.
- **Beach Information**: Access detailed beach info including hours, weather, and real-time conditions provided by [open-meteo](https://open-meteo.com/).
- **Forecasts**: 7-day forecasts with high/low temperatures, UV index, and wave height.
- **Beach Reviews**: View, add, edit, and delete reviews, including star ratings, comments, and photos.
- **Beach Tags**: Users can tag beaches by specific activities (e.g., swimming, sunbathing, surfing), enabling others to filter and find beaches that suit their preferences.

### Improvements (12/8/2024)
- **Beach Overview Pictures**: Add a picture for each beach in the overview screen. This will provide users with a visual representation of the beach, enhancing the overall aesthetic of the app.
- **Reviewer Profile Pictures**: Add profile pictures to the reviews for each beach. This will help users identify reviewers and personalize the experience, making it more engaging and trustworthy.
- **User Profile Picture Management**: Implement functionality that allows users to add and edit their profile pictures. This feature will enhance personalization and provide users with the ability to tailor their accounts to their preferences.
- **Mini Popup for Beach Markers**: Implement a mini popup view that displays basic information about each beach when users click on a red marker on the map. This will improve user navigation and make it easier to access key details about each location.



## Getting Started

### How to run
1. Build the project in Android Studio
2. Create a new emulator with API 35 and run the project
3. Allow the app to access the device location when prompt
4. Wait for the map to load and you are all set

#### Sample User Login
Username: mj@gmail.com<br>
Password: heehee

## Requirements

### Development Requirements
- **Android Studio** (Koala or newer)
- **Android SDK** (API level 35+)
- **Java** (for Android development)
- **Firebase** (for database and authentication)
- **Google Cloud Storage** (for image storage)

### Run-time Requirements
- **Internet Connection** (required for fetching map, syncing with the Firebase database, and uploading images to Google Cloud Storage)
- **Google Play Services** (for map display)
- **Location Data** (to display nearby beaches and enable map-related features)
- **Android OS 12+** (requires Android 12 (API level 35) or newer)

## Tested Devices
- **Google Pixel 8 Pro**
- **Google Pixel 8 Fold**
  
## Authors
* Steven Wang | [bwang442@usc.edu](mailto:shermany@usc.edu) | [iotsak1r](https://github.com/iotsak1r)
* Sherman Yan | [shermany@usc.edu](mailto:shermany@usc.edu) | [shermanyan](https://github.com/shermanyan)
* Mikael Yikum | [yikum@usc.edu](mailto:yikum@usc.edu) | [YikumMikael](https://github.com/YikumMikael)

