# User Management Android Application
## Overview
This Android application allows users to manage a list of users by adding, editing, and removing them. The application also supports fetching user data from a remote API, sorting, searching, and updating user avatars.

# Features
* Add Users: Users can add new entries to the list with first name, last name, email, and avatar.
* Edit Users: Users can edit existing entries, including updating the avatar by selecting an image from the gallery.
* Remove Users: Users can remove any entry from the list after confirmation.
* Search: The list of users can be searched by first or last name using the search functionality.
* Sort: Users can sort the list by first or last name.
* Restart: All users can be removed, and the API can be called again to refresh the user list.
* Persisted Storage: User data is stored locally in a Room database for offline access.
* API Integration: Users are fetched from a remote API and saved locally.
# Architecture
MVVM (Model-View-ViewModel): The app uses the MVVM architecture, which promotes a clean separation of concerns.
* Model: Represents the data (e.g., UserData).
* View: Activities and layouts that display the data.
* ViewModel: Manages UI-related data and business logic (e.g., UserViewModel).
# API Integration
* Library: The app uses the Retrofit library for API calls.
* Endpoint: User data is fetched from the API at https://reqres.in/api/users.
# Local Database
* Room Persistence Library: The app uses Room to store user data locally. This allows for offline access and persistent storage of user information.
# Installation
To install the application:

* Download the APK: Obtain the APK file from the release section or another source.
* Enable Unknown Sources: Go to Settings > Security, then enable Unknown Sources to allow installation from sources other than the Play Store.
* Install the APK: Locate the downloaded APK file and tap on it to start the installation.
* Open the App: Once installed, open the app and start managing users.
# How It Works
* Adding a User: Tap the "Add User" button, fill in the required fields, optionally select an avatar, and press "Add".
* Editing a User: Tap the "Edit" button next to a user, modify the details, and press "Save".
* Removing a User: Tap the "Remove" button next to a user, confirm the action, and the user will be deleted.
* Fetching Users from API: On first launch, the app will fetch users from the API and save them to the local database. Users can trigger this manually by using the "Restart" button.
* Sorting Users: Use the "Sort" button to sort users by first or last name.
* Searching Users: Use the search bar to filter users by first or last name.

# Dependencies
Retrofit: For making network calls.
Room: For local database management.
Picasso: For loading and displaying images.
LiveData and ViewModel: For managing UI-related data lifecycle-aware.
