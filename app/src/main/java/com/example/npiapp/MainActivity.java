package com.example.npiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    int contador = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchGuidance(View view) {
        Intent intent = new Intent(this, Guidance.class);
        startActivity(intent);
    }

    public void launchCanteenMenu(View view) {
        Intent intent = new Intent(this, CanteenMenu.class);
        startActivity(intent);
    }
}