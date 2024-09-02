package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface UserDao {

    // Insert a new user into the user_table
    @Insert
    void insert(UserEntity user);

    // Update an existing user's information in the user_table
    @Update
    void update(UserEntity user);

    // Delete a specific user from the user_table
    @Delete
    void delete(UserEntity user);

    // Retrieve a user by their unique ID from the user_table
    @Query("SELECT * FROM user_table WHERE id = :id")
    UserEntity getUserById(long id);

    // Retrieve all users from the user_table
    @Query("SELECT * FROM user_table")
    List<UserEntity> getAllUsers();

    // Get the maximum ID value from the user_table, useful for generating new IDs
    @Query("SELECT MAX(id) FROM user_table")
    Integer getMaxId();

    // Delete all records from the user_table
    @Query("DELETE FROM user_table")
    void deleteAllUsers();

    // Insert a list of users into the user_table
    @Insert
    void insertAll(List<UserEntity> users); // Fixed to accept List<UserEntity>
}
