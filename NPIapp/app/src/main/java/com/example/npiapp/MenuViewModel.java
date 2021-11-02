package com.example.npiapp;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MenuViewModel extends AndroidViewModel {

    private MenuDao mMenuDao;

    private LiveData<Menu> mMenu;

    public MenuViewModel(Application application) {
        super(application);
        MenuRoomDatabase db = MenuRoomDatabase.getDatabase(application);
        mMenuDao = db.menuDao();
    }

//    LiveData<Menu> getMenuOnSpecificDate(String date_) {
//        return mRepository.getMenuOnSpecificDate(date_);
//    }

    Menu getMenuOnSpecificDate(String date_) {
        return mMenuDao.getMenuOnSpecificDate(date_);
    }

    void setOrderOnSpecificDate(String date_, int ordered_first1, int ordered_first2, int ordered_main1,
                                int ordered_main2, int ordered_dessert1, int ordered_dessert2, int day_with_order) {
        mMenuDao.setOrderOnSpecificDate(date_, ordered_first1, ordered_first2, ordered_main1,
                ordered_main2, ordered_dessert1, ordered_dessert2, day_with_order);
    }

    void setOrderedOnSpecificDate(String date_, int day_with_order){
        mMenuDao.setOrderedOnSpecificDate(date_, day_with_order);
    }

    //public void insert(Word word) { mRepository.insert(word); }
}