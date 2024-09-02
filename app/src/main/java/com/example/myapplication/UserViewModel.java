package com.example.myapplication;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

// UserViewModel class provides data to the UI and acts as a bridge between the repository and UI.
// It also manages data-related operations in a background thread to keep the UI responsive.
public class UserViewModel extends AndroidViewModel {

    // Repository instance to access data
    private final UserRepository userRepository;

    // LiveData objects to hold the user list and user count
    private final MutableLiveData<List<UserData>> userListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> userCountLiveData = new MutableLiveData<>();

    // Executor service for running tasks in a background thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Handler for posting results back to the main thread
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    // Base URL for the Retrofit API requests
    private static final String BASE_URL = "https://reqres.in/";

    // Constructor for initializing the ViewModel with the application context.
    // It also initializes the UserRepository and loads users from the local database.
    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        loadUsers();
    }

    // Returns LiveData containing the list of users
    public LiveData<List<UserData>> getUserList() {
        return userListLiveData;
    }

    // Returns LiveData containing the count of users
    public LiveData<Integer> getUserCount() {
        return userCountLiveData;
    }

    // Loads users from the local database in a background thread
    private void loadUsers() {
        executorService.execute(() -> {
            List<UserData> users = userRepository.getUsers();
            mainThreadHandler.post(() -> {
                userListLiveData.setValue(users);
                userCountLiveData.setValue(users.size());
            });
        });
    }

    // Deletes all users from the local database in a background thread
    public void deleteAllUsers() {
        executorService.execute(() -> {
            userRepository.deleteAllUsers();
            loadUsers(); // Reload users to update the list
        });
    }

    // Refreshes data from the remote API using Retrofit
    public void refreshData() {
        // Create a Retrofit instance with a Gson converter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create an instance of the RequestUser API interface
        RequestUser requestUser = retrofit.create(RequestUser.class);

        // Make an asynchronous API call to fetch users
        Call<UserListResponse> call = requestUser.getUsers();
        call.enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserEntity> userEntities = new ArrayList<>();
                    List<UserData> newUsers = response.body().data;

                    // Convert UserData objects to UserEntity objects
                    for (UserData newUser : newUsers) {
                        UserEntity userEntity = new UserEntity();
                        userEntity.setFirstName(newUser.getFirstName());
                        userEntity.setLastName(newUser.getLastName());
                        userEntity.setEmail(newUser.getEmail());
                        userEntity.setAvatar(newUser.getAvatar());
                        userEntities.add(userEntity);
                    }

                    // Execute database operations in a background thread
                    executorService.execute(() -> {
                        userRepository.deleteAllUsers(); // Clear existing users
                        userRepository.insertAllUsers(userEntities); // Insert new users
                        loadUsers(); // Reload users to update the list
                    });
                } else {
                    // Handle API response error
                    Log.e("UserViewModel", "API response error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                // Handle API call failure
                Log.e("UserViewModel", "API call failed: " + t.getMessage());
            }
        });
    }

    // Adds a new user to the local database in a background thread
    public void addUser(UserData userData) {
        executorService.execute(() -> {
            userRepository.insertUser(userData);
            loadUsers(); // Reload users to update the list
        });
    }

    // Updates an existing user in the local database in a background thread
    public void updateUser(UserData userData) {
        executorService.execute(() -> {
            userRepository.updateUser(userData);
            loadUsers(); // Reload users to update the list
        });
    }

    // Deletes a user from the local database in a background thread
    public void deleteUser(UserData userData) {
        executorService.execute(() -> {
            userRepository.deleteUser(userData);
            loadUsers(); // Reload users to update the list
        });
    }

    // Adds multiple users to the local database in a background thread
    public void addUsers(List<UserData> users) {
        List<UserEntity> userEntities = new ArrayList<>();

        // Convert UserData objects to UserEntity objects
        for (UserData newUser : users) {
            UserEntity userEntity = new UserEntity();
            userEntity.setFirstName(newUser.getFirstName());
            userEntity.setLastName(newUser.getLastName());
            userEntity.setEmail(newUser.getEmail());
            userEntity.setAvatar(newUser.getAvatar());
            userEntities.add(userEntity);
        }

        // Execute database operations in a background thread
        executorService.execute(() -> {
            userRepository.insertAllUsers(userEntities);
            loadUsers(); // Reload users to update the list
        });
    }

    // Called when the ViewModel is about to be destroyed, used here to shut down the executor service
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown(); // Shutdown the executor service when ViewModel is cleared
    }

    // Interface for making Retrofit API requests to fetch users
    public interface RequestUser {
        @GET("api/users") // API endpoint to get the list of users
        Call<UserListResponse> getUsers();
    }
}
