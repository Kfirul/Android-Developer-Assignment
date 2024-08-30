package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("SELECT * FROM user_table WHERE id = :id")
    UserEntity getUserByIdy(long id);

    @Query("SELECT * FROM user_table")
    List<UserEntity> getAllUsers();

    // Get the maximum ID
    @Query("SELECT MAX(id) FROM user_table")
    Integer getMaxId();

    @Query("DELETE FROM user_table") // Assuming your table is named user_table
    void deleteAllUsers();
}


