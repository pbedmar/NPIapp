package com.example.npiapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MenuDao {
//    @Query("SELECT * FROM menu WHERE date == :date_")
//    LiveData<Menu> getMenuOnSpecificDate(String date_);
    @Query("SELECT * FROM menu WHERE date == :date_")
    Menu getMenuOnSpecificDate(String date_);

    @Query("UPDATE menu SET ordered_first1 = :ordered_first1, ordered_first2 = :ordered_first2," +
            " ordered_main1 = :ordered_main1, ordered_main2 = :ordered_main2, ordered_dessert1 = :ordered_dessert1," +
            " ordered_dessert2 = :ordered_dessert2 WHERE date == :date_")
    void setOrderOnSpecificDate(String date_, int ordered_first1, int ordered_first2, int ordered_main1,
                                int ordered_main2, int ordered_dessert1, int ordered_dessert2);
}
