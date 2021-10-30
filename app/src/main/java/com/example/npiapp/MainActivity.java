package com.example.npiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static float saldo = 20.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.saldo_value_text);
        textView.setText(Float.toString(saldo));
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView textView = findViewById(R.id.saldo_value_text);
        textView.setText(Float.toString(saldo));
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