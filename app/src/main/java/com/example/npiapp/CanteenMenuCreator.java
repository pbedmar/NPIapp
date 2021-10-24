package com.example.npiapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CanteenMenuCreator extends AppCompatActivity implements SensorEventListener {

    private MenuViewModel mMenuViewModel;
    private Menu menuToday;
    private int numberOfPossibleMeals = 6;
    private String date = "00/00/0000";
    private int currentCard = 0;
    private boolean[] orderedMeals = new boolean[numberOfPossibleMeals];
    private float[] mealsPrices = new float[numberOfPossibleMeals];
    private String[] mealsNames = new String[numberOfPossibleMeals];
    private String[] mealsTypes = {"Primero", "Primero", "Segundo", "Segundo", "Postre", "Postre"};
    private int[] cardsIds = new int[numberOfPossibleMeals];
    float totalOrderPrice = 0;
    public static final String REPLY = "npiapp.CanteenMenuCreator.REPLY";

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private long lastUpdate = 0;
    private long lastDetected = 0;


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

        // access database & retrieve current menu
        mMenuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        menuToday = mMenuViewModel.getMenuOnSpecificDate(date);

        mealsNames = menuToday.getMealsNamesArray();
        mealsPrices = menuToday.getMealsPricesArray();

        setCardViewMedia();

        // start motion detection
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, sensorAccelerometer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void setCardViewMedia() {

        RelativeLayout currentView = (RelativeLayout) findViewById(R.id.menu_options_layout);

        for (int i = numberOfPossibleMeals - 1; i >= 0; i--) {
            RelativeLayout meal = (RelativeLayout) getLayoutInflater().inflate(
                    R.layout.activity_canteen_menu_creator_cardview, null);

            meal.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            int id = View.generateViewId();
            cardsIds[i] = id;
            meal.setId(id);

            TextView mealType = (TextView) meal.findViewById(R.id.meal_type_text);
            mealType.setText(mealsTypes[i]);
            TextView mealName = (TextView) meal.findViewById(R.id.meal_text);
            mealName.setText(mealsNames[i]);
            TextView mealPrice = (TextView) meal.findViewById(R.id.price_text);
            mealPrice.setText(Float.toString(mealsPrices[i]));

            currentView.addView(meal);
        }
    }

    protected void cardAnimation(RelativeLayout relativeLayout, boolean ordered, String axis) {
        float magnitude = -10000f;
        if (ordered) {
            magnitude = 10000f;
        }

        ObjectAnimator animation = ObjectAnimator.ofFloat(relativeLayout, "translationX", magnitude);
        animation.setDuration(1500);
        animation.start();

    }

    protected void nextCard() {

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(cardsIds[currentCard]);
        cardAnimation(relativeLayout, orderedMeals[currentCard], "translationX");

        if (currentCard == 5) {
//            if (sensorManager != null) {
//                sensorManager.unregisterListener(this, sensorAccelerometer);
//                sensorManager = null;
//            }

            showOrderSummary();
        }

        currentCard++;
    }

    protected void showOrderSummary() {
        LinearLayout linearLayout_order = (LinearLayout) findViewById(R.id.order_layout);
        linearLayout_order.setVisibility(View.VISIBLE);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.chosen_meals_layout);

        totalOrderPrice = 0;
        for (int i = 0; i < numberOfPossibleMeals; i++) {
            if (orderedMeals[i] == true) {
                Log.d("pedro", "añadiendo plato a la cuenta");
                RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(
                        R.layout.activity_canteen_menu_creator_summary_entry, null);

                TextView mealName = (TextView) relativeLayout.getChildAt(0);
                mealName.setText(mealsNames[i]);
                TextView mealPrice = (TextView) relativeLayout.getChildAt(1);
                mealPrice.setText(Float.toString(mealsPrices[i]));
                linearLayout.addView(relativeLayout);

                totalOrderPrice += mealsPrices[i];
                Log.d("pedro", "plato añadido a la cuenta");
            }
        }

        TextView totalPrice = (TextView) findViewById(R.id.total_price_text);
        totalPrice.setText(Float.toString(totalOrderPrice));
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor usedSensor = sensorEvent.sensor;

        if (usedSensor == sensorAccelerometer) {
            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastDetected) > 1000) {

                if ((currentTime - lastUpdate) > 100) {
                    lastUpdate = currentTime;

                    float x = sensorEvent.values[0];

                    if (x > 10) {
                        Log.d("pedro", "X Right axis: " + x);
                        Log.d("pedro", "Right shake detected");
                        orderedMeals[currentCard] = true;
                        lastDetected = System.currentTimeMillis();
                        nextCard();
                    } else if (x < -10) {
                        Log.d("pedro", "X Left axis: " + x);
                        Log.d("pedro", "Left shake detected");
                        orderedMeals[currentCard] = false;
                        lastDetected = System.currentTimeMillis();
                        nextCard();
                    }

                }
            }
        }
    }

    public int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    public void cancelOrder(View view) {
        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }

    public void confirmOrder(View view) {

        mMenuViewModel.setOrderOnSpecificDate(date, boolToInt(orderedMeals[0]),
                boolToInt(orderedMeals[1]), boolToInt(orderedMeals[2]), boolToInt(orderedMeals[3]),
                boolToInt(orderedMeals[4]), boolToInt(orderedMeals[5]), 1);


        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}