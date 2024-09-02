package com.example.myapplication;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

// Define the database and its associated entities and version
@Database(entities = {UserEntity.class}, version = 2) // Increment the version number when schema changes
public abstract class UserDatabase extends RoomDatabase {

    // Abstract method to get the DAO (Data Access Object)
    public abstract UserDao userDao();

    // Singleton instance of the database to ensure only one instance is created
    private static volatile UserDatabase INSTANCE;

    // Method to get the database instance, ensuring thread safety
    public static UserDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserDatabase.class) { // Synchronized block to handle multi-threaded access
                if (INSTANCE == null) {
                    // Create the database instance using Room's database builder
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UserDatabase.class, "user_database")
                            .fallbackToDestructiveMigration() // Handle database migrations by recreating the database
                            .build();
                }
            }
        }
        return INSTANCE; // Return the singleton instance of the database
    }
}
