package com.example.myapplication;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final UserDao userDao;

    public UserRepository(Application application) {
        UserDatabase db = UserDatabase.getDatabase(application);
        userDao = db.userDao();
    }

    public void deleteAllUsers() {
        userDao.deleteAllUsers();
    }

    public List<UserData> getUsers() {
        List<UserEntity> userEntities = userDao.getAllUsers();
        return convertToUserData(userEntities);
    }

    public void insertUser(UserData userData) {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(userData.getFirstName());
        userEntity.setLastName(userData.getLastName());
        userEntity.setEmail(userData.getEmail());
        userEntity.setAvatar(userData.getAvatar());
        userDao.insert(userEntity);
    }

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

    public void deleteUser(UserData userData) {
        UserEntity userEntity = userDao.getUserById(userData.getId());
        if (userEntity != null) {
            userDao.delete(userEntity);
        }
    }

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
    public void insertAllUsers(List<UserEntity> users) {
        // Assuming you have a DAO with a method for inserting multiple users
        userDao.insertAll(users);
    }


}

