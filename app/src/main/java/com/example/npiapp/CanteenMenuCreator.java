package com.example.npiapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;

public class CanteenMenuCreator extends AppCompatActivity {

    private MenuViewModel mMenuViewModel;
    private String date = "00/00/0000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canteen_menu_creator);

        // download extras received from calling activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            date = extras.getString(CanteenMenu.DATE);
        }

        // set activity title
        this.setTitle("Encargo día " + date);

        mMenuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        Menu menuOnSpecificDate = mMenuViewModel.getMenuOnSpecificDate(date);


        Log.d("pedro", menuOnSpecificDate.getDessert1());

    }


}