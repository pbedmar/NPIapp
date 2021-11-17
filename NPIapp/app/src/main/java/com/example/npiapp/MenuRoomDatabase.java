package com.example.npiapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/*
Crea y mantiene la instancia de la base de datos a la que se accede.
Es un singleton, ya que no debe existir más de una instancia activa a la vez.
A la hora de crearla, utilizamos allowMainThreadQueries(), método que nos permite acceder a la base
de datos en el hilo de ejecución principal. Esto no es una buena práctica, ya que si el acceso a la
base de datos se bloquea, también lo hace nuestro programa, por lo que se debería implementar un
thread aparte para esto. Aún así, por la simplicidad de nuestra aplicación y por no entrar dentro
del objetivo de la práctica, vamos a mantenerlo de esta forma.
 */
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
