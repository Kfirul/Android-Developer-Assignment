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

public class UserViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<List<UserData>> userListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> userCountLiveData = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private static final String BASE_URL = "https://reqres.in/";

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        loadUsers();
    }

    public LiveData<List<UserData>> getUserList() {
        return userListLiveData;
    }

    public LiveData<Integer> getUserCount() {
        return userCountLiveData;
    }

    private void loadUsers() {
        executorService.execute(() -> {
            List<UserData> users = userRepository.getUsers();
            mainThreadHandler.post(() -> {
                userListLiveData.setValue(users);
                userCountLiveData.setValue(users.size());
            });
        });
    }

    public void deleteAllUsers() {
        executorService.execute(() -> {
            userRepository.deleteAllUsers();
            loadUsers(); // Reload users to update the list
        });
    }

    public void refreshData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestUser requestUser = retrofit.create(RequestUser.class);
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

    public void addUser(UserData userData) {
        executorService.execute(() -> {
            userRepository.insertUser(userData);
            loadUsers(); // Reload users to update the list
        });
    }

    public void updateUser(UserData userData) {
        executorService.execute(() -> {
            userRepository.updateUser(userData);
            loadUsers(); // Reload users to update the list
        });
    }

    public void deleteUser(UserData userData) {
        executorService.execute(() -> {
            userRepository.deleteUser(userData);
            loadUsers(); // Reload users to update the list
        });
    }

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

        executorService.execute(() -> {
            userRepository.insertAllUsers(userEntities);
            loadUsers(); // Reload users to update the list
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown(); // Shutdown the executor service when ViewModel is cleared
    }

    public interface RequestUser {
        @GET("api/users") // Adjusted path to match your API
        Call<UserListResponse> getUsers();
    }
}
