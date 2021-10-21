package com.example.npiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CanteenMenu extends AppCompatActivity {

//    private SensorManager senSensorManager;
//    private Sensor senAccelerometer;
//    private long lastUpdate = 0;
//    private float last_x, last_y, last_z;
//    private static final int SHAKE_THRESHOLD = 3000;
//    private int count = 0;

    public static final String DATE = "npiapp.CanteenMenu.DATE";
    private String[] spinnerDates;
    private String todayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canteen_menu);

        generateDates();
        loadSpinnerOptions();

    }

    public void generateDates () {
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

        spinnerDates = new String[]{
                todayPlusOne,
                todayPlusTwo,
                todayPlusThree
        };

        todayDate = today;
    }

    public void loadSpinnerOptions(){
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
        intent.putExtra(DATE, spinner.getSelectedItem().toString());
        startActivity(intent);


//        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }

//    @Override
//    protected void onPause(){
//        super.onPause();
//        senSensorManager.unregisterListener(this);
//    }
//
//    @Override
//    protected void onResume(){
//        super.onResume();
//        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//    }
//
//
//    private void getRandomNumber() {
//        ArrayList numbersGenerated = new ArrayList();
//
//        for (int i = 0; i < 6; i++) {
//            Random randNumber = new Random();
//            int iNumber = randNumber.nextInt(48) + 1;
//
//            if(!numbersGenerated.contains(iNumber)) {
//                numbersGenerated.add(iNumber);
//            } else {
//                i--;
//            }
//        }
//
//        TextView text = (TextView)findViewById(R.id.number_1);
//        text.setText(""+numbersGenerated.get(0));
//
//        text = (TextView)findViewById(R.id.number_2);
//        text.setText(""+numbersGenerated.get(1));
//
//        text = (TextView)findViewById(R.id.number_3);
//        text.setText(""+numbersGenerated.get(2));
//
//        text = (TextView)findViewById(R.id.number_4);
//        text.setText(""+numbersGenerated.get(3));
//
//        text = (TextView)findViewById(R.id.number_5);
//        text.setText(""+numbersGenerated.get(4));
//
//        text = (TextView)findViewById(R.id.number_6);
//        text.setText(""+numbersGenerated.get(5));
//
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        Sensor mySensor = sensorEvent.sensor;
//
//        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            float x = sensorEvent.values[0];
//            float y = sensorEvent.values[1];
//            float z = sensorEvent.values[2];
//
//            long curTime = System.currentTimeMillis();
//
//            if ((curTime - lastUpdate) > 100) {
//                long diffTime = (curTime - lastUpdate);
//                lastUpdate = curTime;
//
//                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
//
//                if (speed > SHAKE_THRESHOLD) {
//                    getRandomNumber();
//                    Log.d("sensores", "Se accedio al sensor! " + Integer.toString(count));
//                    count ++;
//                }
//
//                last_x = x;
//                last_y = y;
//                last_z = z;
//            }
//        }
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int i) {
//
//    }

}