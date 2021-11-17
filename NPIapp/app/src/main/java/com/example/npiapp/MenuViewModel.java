package com.example.npiapp;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

/*
Actúa de interfaz entre el backend y la UI, proporcionando métodos de consulta y
de escritura en la base de datos.
 */
public class MenuViewModel extends AndroidViewModel {

    private MenuDao mMenuDao;

    public MenuViewModel(Application application) {
        super(application);
        MenuRoomDatabase db = MenuRoomDatabase.getDatabase(application);
        mMenuDao = db.menuDao();
    }

    Menu getMenuOnSpecificDate(String date_) {
        return mMenuDao.getMenuOnSpecificDate(date_);
    }

    void setOrderOnSpecificDate(String date_, int ordered_first1, int ordered_first2, int ordered_main1,
                                int ordered_main2, int ordered_dessert1, int ordered_dessert2, int day_with_order) {
        mMenuDao.setOrderOnSpecificDate(date_, ordered_first1, ordered_first2, ordered_main1,
                ordered_main2, ordered_dessert1, ordered_dessert2, day_with_order);
    }

    void setOrderedOnSpecificDate(String date_, int day_with_order) {
        mMenuDao.setOrderedOnSpecificDate(date_, day_with_order);
    }
}