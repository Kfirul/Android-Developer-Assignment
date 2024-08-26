package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity implements UserAdapter.OnSelectButtonClickListener {

    interface RequestUser {
        @GET("/api/users")
        Call<UserListResponse> getUsers();
    }

    List<UserData> userList;
    UserDatabase db;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private ArrayList<UserData> userArrayList = new ArrayList<>();
    private ArrayList<UserData> searchList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycleView);
        searchView = findViewById(R.id.searchView);

        // Initialize the database
        db = UserDatabase.getDatabase(this);
        Log.d("MainActivity", "Database initialized");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://reqres.in/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestUser requestUser = retrofit.create(RequestUser.class);

        // API Call
        requestUser.getUsers().enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MainActivity", "API call successful, data received");
                    userList = response.body().data;
                    userArrayList.addAll(userList);
                    saveUsersToDatabase(userList);
                } else {
                    Log.e("MainActivity", "API call unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable throwable) {
                Log.e("MainActivity", "API call failed: " + throwable.getMessage());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userArrayList, this);
        recyclerView.setAdapter(userAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("MainActivity", "Search query submitted: " + query);
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("MainActivity", "Search query changed: " + newText);
                performSearch(newText);
                return false;
            }
        });

        displayUserNames();
    }

    @Override
    public void onSelectButtonClick(UserData userData) {
        Log.d("MainActivity", "Select button clicked for user: " + userData.getFirstName() + " " + userData.getLastName());
        // Handle select button click here
    }

    private void saveUsersToDatabase(List<UserData> users) {
        Log.d("MainActivity", "Saving users to database");
        AsyncTask.execute(() -> {
            UserDao userDao = db.userDao();
            for (UserData user : users) {
                // Check if user with the same id already exists
                UserEntity existingUser = userDao.getUserById(user.id);
                if (existingUser == null) {
                    UserEntity userEntity = new UserEntity(
                            user.id,
                            user.email,
                            user.firstName,
                            user.lastName,
                            user.avatar
                    );
                    userDao.insert(userEntity);
                    Log.d("MainActivity", "Inserted user: " + userEntity.firstName);
                } else {
                    Log.d("MainActivity", "User with id " + user.id + " already exists.");
                }
            }
            displayUserNames();
        });
    }


    private void displayUserNames() {
        Log.d("MainActivity", "Displaying user names from database");
        AsyncTask.execute(() -> {
            List<UserEntity> usersFromDb = db.userDao().getAllUsers();
            Log.d("MainActivity", "Retrieved " + usersFromDb.size() + " users from database");
            ArrayList<UserData> userDataList = new ArrayList<>();
            for (UserEntity user : usersFromDb) {
                UserData userData = new UserData();
                userData.setId(user.id);
                userData.setEmail(user.email);
                userData.setFirstName(user.firstName);
                userData.setLastName(user.lastName);
                userData.setAvatar(user.avatar);
                userDataList.add(userData);
            }
            runOnUiThread(() -> {
                userArrayList.clear();
                userArrayList.addAll(userDataList);
                userAdapter.notifyDataSetChanged();
            });
        });
    }

    private void performSearch(String query) {
        Log.d("MainActivity", "Performing search with query: " + query);
        searchList = new ArrayList<>();
        if (query.length() > 0) {
            for (UserData userData : userArrayList) {
                if (userData.getFirstName().toUpperCase().contains(query.toUpperCase())
                        || userData.getLastName().toUpperCase().contains(query.toUpperCase())) {
                    searchList.add(userData);
                }
            }
        } else {
            searchList.addAll(userArrayList);
        }

        userAdapter = new UserAdapter(this, searchList, this);
        recyclerView.setAdapter(userAdapter);
    }
}

