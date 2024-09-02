package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
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

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_API_CALLED = "apiCalled";

    private UserViewModel userViewModel;
    private UserAdapter userAdapter;
    private ArrayList<UserData> userArrayList = new ArrayList<>();
    private ArrayList<UserData> searchList = new ArrayList<>();
    private Uri selectedImageUri;
    private ImageView avatarImageView;
    private Button addButton;
    private Button restartButton;
    private Button sortButton;
    private TextView numberOfUsersTextView;
    private SearchView searchView;
    private RecyclerView recyclerView;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            selectedImageUri = result.getData().getData();
            if (selectedImageUri != null) {
                Picasso.get().load(selectedImageUri).into(avatarImageView);
            }
        }
    });

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
                    userViewModel.updateUser(userData);

                    // Refresh UI
                    userAdapter.notifyDataSetChanged();
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
                    userViewModel.deleteUser(userData);

                    // Update both lists
                    userArrayList.remove(userData);
                    searchList.remove(userData);

                    // Refresh the UI
                    if (!searchView.getQuery().toString().isEmpty()) {
                        performSearch(searchView.getQuery().toString());
                    } else {
                        // Update the adapter with the modified user list
                        userAdapter = new UserAdapter(this, userArrayList, this::onEditButtonClick, this::onRemoveButtonClick);
                        recyclerView.setAdapter(userAdapter);
                    }
                    updateNumberOfUsers();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    public interface RequestUser {
        @GET("api/users") // Adjusted path to match your API
        Call<UserListResponse> getUsers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        recyclerView = findViewById(R.id.recycleView);
        searchView = findViewById(R.id.searchView);
        addButton = findViewById(R.id.add_user_button);
        restartButton = findViewById(R.id.restart_button);
        sortButton = findViewById(R.id.sort_button);
        numberOfUsersTextView = findViewById(R.id.number_of_users);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userArrayList, this::onEditButtonClick, this::onRemoveButtonClick);
        recyclerView.setAdapter(userAdapter);

        // Observe LiveData
        userViewModel.getUserList().observe(this, users -> {
            userArrayList.clear();
            userArrayList.addAll(users);
            userAdapter.notifyDataSetChanged();
        });

        userViewModel.getUserCount().observe(this, count -> {
            numberOfUsersTextView.setText("Number of users: " + count);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });

        addButton.setOnClickListener(view -> {
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
                        newUser.setId(generateUniqueId());

                        // If an image was selected, set the avatar
                        if (selectedImageUri != null) {
                            newUser.setAvatar(selectedImageUri.toString());
                        } else {
                            newUser.setAvatar(""); // Ensure it's not null
                        }

                        // Save new user to the database
                        userViewModel.addUser(newUser);
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        });


        restartButton.setOnClickListener(view -> {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Confirm Restart")
                    .setMessage("Are you sure you want to remove all users and call the API again?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        userViewModel.deleteAllUsers(); // Delete all users from the database
                        userViewModel.refreshData(); // Call API to refresh data

                        // Clear search query and refresh the UI
                        searchView.setQuery("", false);
                        searchView.clearFocus();
                        userAdapter.updateList(new ArrayList<>(userArrayList)); // Reset the list in the adapter
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });


        sortButton.setOnClickListener(view -> showSortOptions());

        // Check if the API has been called before
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean apiCalled = preferences.getBoolean(KEY_API_CALLED, false);

        if (!apiCalled) {
            fetchUsersFromApi();
        }
    }

    private void fetchUsersFromApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://reqres.in/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestUser requestUser = retrofit.create(RequestUser.class);
        Call<UserListResponse> call = requestUser.getUsers();

        call.enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserData> users = response.body().data;
                    userViewModel.addUsers(users);

                    // Mark API as called
                    SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(KEY_API_CALLED, true);
                    editor.apply();
                } else {
                    Log.e("MainActivity", "API response error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                Log.e("MainActivity", "API call failed: " + t.getMessage());
            }
        });
    }

    private boolean validateUserInput(String firstName, String lastName, String email) {
        return !firstName.trim().isEmpty() && !lastName.trim().isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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

    private void performSearch(String query) {
        // Create a new searchList based on the query
        ArrayList<UserData> filteredList = new ArrayList<>();
        if (query.length() > 0) {
            for (UserData user : userArrayList) {
                if (user.getFirstName().toUpperCase().contains(query.toUpperCase()) ||
                        user.getLastName().toUpperCase().contains(query.toUpperCase())) {
                    filteredList.add(user);
                }
            }
        } else {
            filteredList.addAll(userArrayList);
        }

        // Update the adapter with the new search results
        userAdapter = new UserAdapter(this, filteredList, this::onEditButtonClick, this::onRemoveButtonClick);
        recyclerView.setAdapter(userAdapter);
    }




    private void showSortOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort By")
                .setItems(new CharSequence[]{"First Name", "Last Name"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Collections.sort(userArrayList, Comparator.comparing(UserData::getFirstName));
                            break;
                        case 1:
                            Collections.sort(userArrayList, Comparator.comparing(UserData::getLastName));
                            break;
                    }
                    userAdapter.notifyDataSetChanged();
                })
                .show();
    }

    private void updateNumberOfUsers() {
        numberOfUsersTextView.setText("Number of users: " + userArrayList.size());
    }
}
