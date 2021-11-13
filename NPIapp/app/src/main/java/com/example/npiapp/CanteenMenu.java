package com.example.npiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CanteenMenu extends AppCompatActivity {

    public static final String DATE = "npiapp.CanteenMenu.DATE";
    public static final String INFO_NFC = "npiapp.CanteenMenu.INFO_NFC";
    private final int numberOfDatesToShow = 3;
    private String[] spinnerDates = new String[numberOfDatesToShow];
    private String todayDate;
    private final int numMeals = 6;

    protected static MenuViewModel mMenuViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canteen_menu);

        mMenuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        generateDates();
        loadSpinnerOptions();
        loadOrders();

        Log.d("CanteenMenu", "OnCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    protected void generateDates() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        Date today_timestamp = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date todayPlusOne_timestamp = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date todayPlusTwo_timestamp = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date todayPlusThree_timestamp = calendar.getTime();

        String today = dateFormat.format(today_timestamp);
        String todayPlusOne = dateFormat.format(todayPlusOne_timestamp);
        String todayPlusTwo = dateFormat.format(todayPlusTwo_timestamp);
        String todayPlusThree = dateFormat.format(todayPlusThree_timestamp);

        spinnerDates[0] = todayPlusOne;
        spinnerDates[1] = todayPlusTwo;
        spinnerDates[2] = todayPlusThree;

        todayDate = today;
    }

    protected void loadSpinnerOptions() {
        List<String> datesList = Arrays.asList(spinnerDates);

        Log.d("pedro", spinnerDates[0]);
        Log.d("pedro", spinnerDates[1]);
        Log.d("pedro", spinnerDates[2]);

        Spinner spinner = (Spinner) findViewById(R.id.date_spinner);
        ArrayAdapter<String> datesArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_element, datesList);
        datesArrayAdapter.setDropDownViewResource(R.layout.spinner_element);
        spinner.setAdapter(datesArrayAdapter);
    }

    public void launchCanteenMenuCreator(View view) {
        Intent intent = new Intent(this, CanteenMenuCreator.class);

        Spinner spinner = (Spinner) findViewById(R.id.date_spinner);
        String fecha = spinner.getSelectedItem().toString();

        Menu menu = mMenuViewModel.getMenuOnSpecificDate(fecha);
        if(menu.getDay_with_order() >= 1){
            Toast.makeText(this, "Ya hay un pedido realizado en esa fecha", Toast.LENGTH_LONG).show();
        }
        else {
            intent.putExtra(DATE, fecha);
            startActivity(intent);
        }
    }

    @SuppressLint("ResourceAsColor")
    protected void loadOrders() {

        String[] dates = new String[numberOfDatesToShow + 1];
        dates[0] = todayDate;
        for (int i = 1; i < numberOfDatesToShow + 1; i++) {
            dates[i] = spinnerDates[i - 1];
        }

        LinearLayout linearLayout = findViewById(R.id.canteen_orders);
        linearLayout.removeAllViews();

        for (int i = 0; i < numberOfDatesToShow + 1; i++) {
            Menu menu = mMenuViewModel.getMenuOnSpecificDate(dates[i]);

            if (menu.getDay_with_order() >= 1) {
                String[] mealsNames = menu.getMealsNamesArray();
                float[] mealsPrices = menu.getMealsPricesArray();
                int[] mealsOrdered = menu.getMealsOrderedArray();

                float total_price = 0;
                for (int j = 0; j < numMeals; j++) {
                    if (mealsOrdered[j] == 1)
                        total_price += mealsPrices[j];
                }

                RelativeLayout newCard = (RelativeLayout) getLayoutInflater().inflate(
                        R.layout.activity_canteen_menu_ordered_menu, null);

                TextView date = newCard.findViewById(R.id.order_date);
                date.setText(dates[i]);

                TextView totalPrice = newCard.findViewById(R.id.price_ordered_menu_text);
                int parteEntera = (int)total_price;
                int parteDecimal = (int)((total_price - parteEntera)*100);
                totalPrice.setText(Integer.toString(parteEntera) + "." + Integer.toString(parteDecimal));

                LinearLayout layoutOrderedMeals = (LinearLayout) newCard.findViewById(R.id.meals_layout);

                for (int j = 0; j < numMeals; j++) {
                    if (mealsOrdered[j] == 1) {
                        TextView newText = (TextView) getLayoutInflater().inflate(
                                R.layout.activity_canteen_menu_ordered_menu_item, null);
                        newText.setText("- " + mealsNames[j]);

                        layoutOrderedMeals.addView(newText);
                    }
                }

                String date_ = dates[i];
                float finalTotal_price = total_price;
                newCard.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String mealsNamesString = String.join("\n",mealsNames);
                        String infoToNFC = "FECHA;"+date_+";TUI;"+MainActivity.TUI+";PLATOS;"+mealsNamesString+
                                ";PRECIO;"+Float.toString(finalTotal_price);

                        onClickCard(v, infoToNFC);
                    }
                });

                TextView textEstado = newCard.findViewById(R.id.estadoText);
                if (menu.getDay_with_order() == 2) {
                    textEstado.setText("Pedido recogido");
                }
                else {
                    textEstado.setText("Pedido sin recoger");
                }

                linearLayout.addView(newCard);
            }
        }
    }

    protected void onClickCard(View v, String info) {
        String[] campos = info.split(";");
        Menu menu = mMenuViewModel.getMenuOnSpecificDate(campos[1]);
        if(menu.getDay_with_order() != 2){
            Intent intent = new Intent(this, SenderActivity.class);
            intent.putExtra(INFO_NFC, info);
            startActivityForResult(intent, 1);
        }
        else {
            Toast.makeText(this, "Pedido ya registrado", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ReceiverActivity.class);
                startActivityForResult(intent, 2);
            }
        }
        else if(requestCode == 2) {
            String fecha = data.getStringExtra(ReceiverActivity.RESPO_NFC);
            if(fecha != null) {
                if(resultCode == RESULT_OK) {
                    mMenuViewModel.setOrderedOnSpecificDate(fecha, 2);
                }
                else if(resultCode == RESULT_CANCELED) {
                    mMenuViewModel.setOrderedOnSpecificDate(fecha, 1);
                }
            }
        }
    }
}