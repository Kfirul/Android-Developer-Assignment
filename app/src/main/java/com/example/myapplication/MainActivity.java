package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

    interface RequestUser {
        @GET("/api/users")
        Call<UserListResponse> getUsers();
    }

    TextView textView;
    List<UserData> userList;
    UserDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        db = UserDatabase.getDatabase(this);

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
                }
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable throwable) {
                textView.setText(throwable.getMessage());
            }
        });
    }

    private void saveUsersToDatabase(List<UserData> users) {
        AsyncTask.execute(() -> {
            for (UserData user : users) {
                UserEntity userEntity = new UserEntity(
                        user.id,
                        user.email,
                        user.firstName,
                        user.lastName,
                        user.avatar
                );
                db.userDao().insert(userEntity);
            }
            displayUserNames();
        });
    }

    private void displayUserNames() {
        AsyncTask.execute(() -> {
            List<UserEntity> usersFromDb = db.userDao().getAllUsers();
            StringBuilder userNames = new StringBuilder();
            for (UserEntity user : usersFromDb) {
                userNames.append(user.firstName).append(" ").append(user.lastName).append("\n");
            }
            runOnUiThread(() -> textView.setText(userNames.toString()));
        });
    }
}


