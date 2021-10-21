package com.example.npiapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Class to create the database. Uses the DAO to access it.
@Database(entities = {Menu.class}, version = 1, exportSchema = false)
public abstract class MenuRoomDatabase extends RoomDatabase {
    public abstract MenuDao menuDao();

    // Create this class as a singleton, only one instance can be running.
    private static MenuRoomDatabase INSTANCE;

    public static MenuRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MenuRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), MenuRoomDatabase.class, "menu")
                            .createFromAsset("menu.db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
