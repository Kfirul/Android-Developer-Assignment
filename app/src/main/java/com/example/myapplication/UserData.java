package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserData {
    @SerializedName("id")
    int id;

    @SerializedName("email")
    String email;

    @SerializedName("first_name")
    String firstName;

    @SerializedName("last_name")
    String lastName;

    @SerializedName("avatar")
    String avatar;
}

