package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity implements UserAdapter.OnEditButtonClickListener, UserAdapter.OnRemoveButtonClickListener {

    interface RequestUser {
        @GET("/api/users")
        Call<UserListResponse> getUsers();
    }

    private static final int REQUEST_READ_STORAGE_PERMISSION = 1;

    private List<UserData> userList;
    private UserDatabase db;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private ArrayList<UserData> userArrayList = new ArrayList<>();
    private ArrayList<UserData> searchList = new ArrayList<>();
    private UserAdapter userAdapter;
    private Uri selectedImageUri;
    private ImageView avatarImageView;
    private Button addButton;
    private Button restartButton;
    private Button sortButton;
    private TextView numberOfUsersTextView;


    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_API_CALLED = "api_called";

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            selectedImageUri = result.getData().getData();
            if (selectedImageUri != null) {
                Picasso.get().load(selectedImageUri).into(avatarImageView);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check and request storage permission
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }

        recyclerView = findViewById(R.id.recycleView);
        searchView = findViewById(R.id.searchView);
        addButton = findViewById(R.id.add_user_button);
        restartButton = findViewById(R.id.restart_button);
        numberOfUsersTextView = findViewById(R.id.number_of_users);


        // Initialize the database
        db = UserDatabase.getDatabase(this);
        Log.d("MainActivity", "Database initialized");

        // Check if the API has been called before
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean apiCalled = preferences.getBoolean(KEY_API_CALLED, false);

        if (!apiCalled) {
            // API has not been called before, make the API call
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

                        // Save flag in SharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(KEY_API_CALLED, true);
                        editor.apply(); // Apply changes
                    } else {
                        Log.e("MainActivity", "API call unsuccessful or body is null");
                    }
                }

                @Override
                public void onFailure(Call<UserListResponse> call, Throwable throwable) {
                    Log.e("MainActivity", "API call failed: " + throwable.getMessage());
                }
            });
        } else {
            // API has been called before, just load users from the database
            displayUserNames();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userArrayList, this::onEditButtonClick, this::onRemoveButtonClick);
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


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);

                EditText firstName = dialogView.findViewById(R.id.edit_first_name);
                EditText lastName = dialogView.findViewById(R.id.edit_last_name);
                EditText email = dialogView.findViewById(R.id.edit_email);
                avatarImageView = dialogView.findViewById(R.id.edit_avatar);

                selectedImageUri = null; // Reset selected image URI for each dialog

                avatarImageView.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    pickImageLauncher.launch(intent);
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(dialogView)
                        .setTitle("Add User")
                        .setPositiveButton("Add", (dialog, which) -> {
                            String fName = firstName.getText().toString();
                            String lName = lastName.getText().toString();
                            String emailText = email.getText().toString();

                            // Validate user input
                            if (!validateUserInput(fName, lName, emailText)) {
                                // Show error message
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Validation Error")
                                        .setMessage("Please enter valid first name, last name, and email.")
                                        .setPositiveButton("OK", null)
                                        .show();
                                return;
                            }

                            // Create new UserData object
                            UserData newUser = new UserData();
                            newUser.setFirstName(fName);
                            newUser.setLastName(lName);
                            newUser.setEmail(emailText);

                            // Generate a unique ID
                            long newUserId = generateUniqueId();
                            newUser.setId(newUserId);

                            // If an image was selected, set the avatar
                            if (selectedImageUri != null) {
                                newUser.setAvatar(selectedImageUri.toString());
                            } else {
                                newUser.setAvatar(""); // Ensure it's not null
                            }

                            // Save new user to the database
                            AsyncTask.execute(() -> {
                                try {
                                    UserDao userDao = db.userDao();
                                    UserEntity userEntity = new UserEntity(
                                            newUser.getEmail(),
                                            newUser.getFirstName(),
                                            newUser.getLastName(),
                                            newUser.getAvatar()
                                    );
                                    userDao.insert(userEntity);

                                    runOnUiThread(() -> {
                                        userArrayList.add(newUser);
                                        userAdapter.notifyDataSetChanged();
                                        updateNumberOfUsers();
                                    });
                                } catch (Exception e) {
                                    Log.e("MainActivity", "Error saving user: " + e.getMessage(), e);
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
                refreshUserList();
                updateNumberOfUsers();
            }

        });
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Confirm Restart")
                        .setMessage("Are you sure you wants to remove all users and call the API again?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            eraseAndRefreshUsers(); // Call the function on button click
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        sortButton = findViewById(R.id.sort_button);

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortOptions();
            }
        });



        displayUserNames();
        logAllUsers();
        updateNumberOfUsers();
    }
    private void updateNumberOfUsers() {
        Log.d("MainActivity", "Updating number of users");
        AsyncTask.execute(() -> {
            // Access UserDao to get all users
            List<UserEntity> usersFromDb = db.userDao().getAllUsers();
            int userCount = usersFromDb.size();
            Log.d("MainActivity", "User count: " + userCount);

            // Ensure updating the UI is done on the main thread
            runOnUiThread(() -> {
                if (numberOfUsersTextView != null) {
                    numberOfUsersTextView.setText("Number of users: " + userCount);
                } else {
                    Log.e("MainActivity", "numberOfUsersTextView is null");
                }
            });
        });
    }




    private void showSortOptions() {
        String[] sortOptions = {"Sort by First Name", "Sort by Last Name"};

        new AlertDialog.Builder(this)
                .setTitle("Select Sorting Option")
                .setItems(sortOptions, (dialog, which) -> {
                    if (which == 0) {
                        sortByFirstName();
                    } else if (which == 1) {
                        sortByLastName();
                    }
                })
                .show();
    }

    private void sortByFirstName() {
        Log.d("MainActivity", "Sorting by first name, user list size: " + (userArrayList != null ? userArrayList.size() : "null"));
        if (userArrayList != null && !userArrayList.isEmpty()) {
            Collections.sort(userArrayList, (u1, u2) -> u1.getFirstName().compareTo(u2.getFirstName()));
            userAdapter.notifyDataSetChanged();
        } else {
            Log.e("MainActivity", "User list is null or empty, cannot sort.");
        }
    }

    private void sortByLastName() {
        Log.d("MainActivity", "Sorting by last name, user list size: " + (userArrayList != null ? userArrayList.size() : "null"));
        if (userArrayList != null && !userArrayList.isEmpty()) {
            Collections.sort(userArrayList, (u1, u2) -> u1.getLastName().compareTo(u2.getLastName()));
            userAdapter.notifyDataSetChanged();
        } else {
            Log.e("MainActivity", "User list is null or empty, cannot sort.");
        }
    }



    private void eraseAndRefreshUsers() {
        // Erase all users from the database
        AsyncTask.execute(() -> {
            UserDao userDao = db.userDao();
            userDao.deleteAllUsers();  // Assuming deleteAllUsers() is implemented in UserDao to delete all records

            runOnUiThread(() -> {
                userArrayList.clear(); // Clear the list in memory
                userAdapter.notifyDataSetChanged(); // Refresh the UI
                callApiAndSaveUsers(); // Call API to retrieve and save users
                updateNumberOfUsers();
            });
        });
    }

    private void callApiAndSaveUsers() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://reqres.in/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestUser requestUser = retrofit.create(RequestUser.class);

        requestUser.getUsers().enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList = response.body().data;
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
    }




    @Override
    public void onEditButtonClick(UserData userData) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);

        EditText firstName = dialogView.findViewById(R.id.edit_first_name);
        EditText lastName = dialogView.findViewById(R.id.edit_last_name);
        EditText email = dialogView.findViewById(R.id.edit_email);
        avatarImageView = dialogView.findViewById(R.id.edit_avatar);

        // Set initial values
        firstName.setText(userData.getFirstName());
        lastName.setText(userData.getLastName());
        email.setText(userData.getEmail());

        if (userData.getAvatar() != null && !userData.getAvatar().isEmpty()) {
            Picasso.get().load(userData.getAvatar()).placeholder(R.drawable.anonymous).into(avatarImageView);
        }

        avatarImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Edit User")
                .setPositiveButton("Save", (dialog, which) -> {
                    String fName = firstName.getText().toString();
                    String lName = lastName.getText().toString();
                    String emailText = email.getText().toString();

                    // Validate user input
                    if (!validateUserInput(fName, lName, emailText)) {
                        // Show error message
                        new AlertDialog.Builder(this)
                                .setTitle("Validation Error")
                                .setMessage("Please enter valid first name, last name, and email.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    // Update user details
                    userData.setFirstName(fName);
                    userData.setLastName(lName);
                    userData.setEmail(emailText);

                    // If a new image was selected, update avatar
                    if (selectedImageUri != null) {
                        userData.setAvatar(selectedImageUri.toString());
                    }

                    // Save changes to database
                    AsyncTask.execute(() -> {
                        UserDao userDao = db.userDao();
                        UserEntity userEntity = userDao.getUserByIdy(userData.getId());
                        if (userEntity != null) {
                            userEntity.setFirstName(userData.getFirstName());
                            userEntity.setLastName(userData.getLastName());
                            userEntity.setEmail(userData.getEmail());
                            if (selectedImageUri != null) {
                                userEntity.setAvatar(userData.getAvatar());  // Update avatar
                            }
                            userDao.update(userEntity);
                            Log.d("MainActivity", "User saved to database: " + userEntity.getFirstName());
                        }
                    });

                    // Refresh UI
                    runOnUiThread(() -> {
                        userAdapter.notifyDataSetChanged();
                    });
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRemoveButtonClick(UserData userData) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Remove")
                .setMessage("Are you sure you want to remove this user?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Remove the user from the database
                    AsyncTask.execute(() -> {
                        UserDao userDao = db.userDao();
                        UserEntity userEntity = userDao.getUserByIdy(userData.getId());
                        if (userEntity != null) {
                            userDao.delete(userEntity);
                            Log.d("MainActivity", "Removed user: " + userEntity.firstName);

                            // Update the UI after removing the user
                            runOnUiThread(() -> {
                                userArrayList.remove(userData);
                                userAdapter.notifyDataSetChanged();
                                updateNumberOfUsers();
                            });
                        }
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void saveUsersToDatabase(List<UserData> users) {
        Log.d("MainActivity", "Saving users to database");
        AsyncTask.execute(() -> {
            UserDao userDao = db.userDao();
            for (UserData user : users) {
                // Check if user with the same id already exists
                UserEntity existingUser = userDao.getUserByIdy(user.id);
                if (existingUser == null) {
                    UserEntity userEntity = new UserEntity(
                            user.email,
                            user.firstName,
                            user.lastName,
                            user.avatar
                    );
                    userEntity.setId(user.id);
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
                userData.setAvatar(user.avatar);  // Make sure avatar URI is set
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
        searchList.clear();
        for (UserData user : userArrayList) {
            if (user.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getLastName().toLowerCase().contains(query.toLowerCase())) {
                searchList.add(user);
            }
        }
        userAdapter.updateList(searchList);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                // Handle this situation
            }
        }
    }

    private void logAllUsers() {
        AsyncTask.execute(() -> {
            List<UserEntity> users = db.userDao().getAllUsers();
            for (UserEntity user : users) {
                Log.d("MainActivity", "User: " + user.firstName + ", Avatar: " + user.avatar);
            }
        });
    }
    private boolean validateUserInput(String firstName, String lastName, String email) {
        // Check if first name and last name are not empty
        if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            return false;
        }

        // Check if email is not empty and has a valid format
        if (email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }

        return true;
    }

    private synchronized long generateUniqueId() {
        long maxId = 0;
        for (UserData user : userArrayList) {
            if (user.getId() > maxId) {
                maxId = user.getId();
            }
        }
        return maxId + 1;
    }

    private void refreshUserList() {
        AsyncTask.execute(() -> {
            List<UserEntity> usersFromDb = db.userDao().getAllUsers();
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

}
