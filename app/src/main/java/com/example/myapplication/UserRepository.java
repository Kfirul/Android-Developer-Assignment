package com.example.myapplication;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.ArrayList;
import java.util.List;

// UserRepository class serves as a bridge between the data source (UserDao) and the rest of the application.
// It abstracts the logic required to access data and provides a clean API for the ViewModel to use.
public class UserRepository {

    // DAO for accessing user data from the database.
    private final UserDao userDao;

    // Constructor for initializing the UserRepository with the application context.
    // It sets up the database and the DAO.
    public UserRepository(Application application) {
        UserDatabase db = UserDatabase.getDatabase(application);
        userDao = db.userDao();
    }

    // Deletes all users from the database by calling the DAO method.
    public void deleteAllUsers() {
        userDao.deleteAllUsers();
    }

    // Retrieves all users from the database, converts them from UserEntity to UserData, and returns them as a list.
    public List<UserData> getUsers() {
        List<UserEntity> userEntities = userDao.getAllUsers();
        return convertToUserData(userEntities);
    }

    // Inserts a new user into the database by converting UserData to UserEntity.
    public void insertUser(UserData userData) {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(userData.getFirstName());
        userEntity.setLastName(userData.getLastName());
        userEntity.setEmail(userData.getEmail());
        userEntity.setAvatar(userData.getAvatar());
        userDao.insert(userEntity);
    }

    // Updates an existing user in the database.
    // The user is first retrieved by its ID, and if found, the data is updated.
    public void updateUser(UserData userData) {
        UserEntity userEntity = userDao.getUserById(userData.getId());
        if (userEntity != null) {
            userEntity.setFirstName(userData.getFirstName());
            userEntity.setLastName(userData.getLastName());
            userEntity.setEmail(userData.getEmail());
            userEntity.setAvatar(userData.getAvatar());
            userDao.update(userEntity);
        }
    }

    // Deletes a user from the database by its ID.
    public void deleteUser(UserData userData) {
        UserEntity userEntity = userDao.getUserById(userData.getId());
        if (userEntity != null) {
            userDao.delete(userEntity);
        }
    }

    // Helper method to convert a list of UserEntity objects to a list of UserData objects.
    private List<UserData> convertToUserData(List<UserEntity> userEntities) {
        List<UserData> userDataList = new ArrayList<>();
        for (UserEntity entity : userEntities) {
            UserData userData = new UserData(); // Use the empty constructor
            userData.setId(entity.getId());
            userData.setEmail(entity.getEmail());
            userData.setFirstName(entity.getFirstName());
            userData.setLastName(entity.getLastName());
            userData.setAvatar(entity.getAvatar());
            userDataList.add(userData);
        }
        return userDataList;
    }

    // Inserts multiple users into the database.
    // Converts the list of UserEntity objects to a format that the DAO can use.
    public void insertAllUsers(List<UserEntity> users) {
        userDao.insertAll(users);
    }
}
