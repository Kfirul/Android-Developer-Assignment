package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class UserData {

    // Unique identifier for the user
    @SerializedName("id")
    long id;

    // User's email address
    @SerializedName("email")
    String email;

    // User's first name
    @SerializedName("first_name")
    String firstName;

    // User's last name
    @SerializedName("last_name")
    String lastName;

    // URL to the user's avatar image
    @SerializedName("avatar")
    String avatar;

    // Getter method for user's ID
    public long getId() {
        return id;
    }

    // Setter method for user's ID
    public void setId(long id) {
        this.id = id;
    }

    // Getter method for user's email
    public String getEmail() {
        return email;
    }

    // Setter method for user's email
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter method for user's first name
    public String getFirstName() {
        return firstName;
    }

    // Setter method for user's first name
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Getter method for user's last name
    public String getLastName() {
        return lastName;
    }

    // Setter method for user's last name
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Getter method for user's avatar URL
    public String getAvatar() {
        return avatar;
    }

    // Setter method for user's avatar URL
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
