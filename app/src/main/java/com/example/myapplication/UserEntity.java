package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Annotates this class as an entity for Room, specifying the table name
@Entity(tableName = "user_table")
public class UserEntity {

    // Annotates the field as the primary key and specifies that it should be auto-generated
    @PrimaryKey(autoGenerate = true)
    public long id;

    // Fields for storing user data
    public String email;
    public String firstName;
    public String lastName;
    public String avatar;

    // Default constructor is required by Room for entity creation
    public UserEntity() {}


    // Getter and setter methods for 'id'
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // Getter and setter methods for 'email'
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter methods for 'firstName'
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Getter and setter methods for 'lastName'
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Getter and setter methods for 'avatar'
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
