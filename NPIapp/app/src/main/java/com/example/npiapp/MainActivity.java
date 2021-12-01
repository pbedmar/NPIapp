package com.example.npiapp;

import static java.lang.Math.round;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {

    public static float saldo = 20.0f;
    public static String TUI = "0001246933";
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    // Manejador de sensores
    private SensorManager sensorManager;
    // Sensor de proximidad
    private Sensor proximitySensor;
    long inicio;
    long fin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inicio = 0;
        fin = 0;
        Log.i("INFO_SPEECH","ON create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        // Creamos el manejador de sensores
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Obtenemos el sensor de proximidad
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);


        //speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {
                fin = System.nanoTime();
                Log.i("INFO_SPEECH","El sonido es "+v);
                if(v < 0 && (fin - inicio)/1000000000 > 3)
                    speechRecognizer.stopListening();
            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {

                //micButton.setImageResource(R.drawable.ic_mic_black_off);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String datos = data.get(0);
                Log.i("INFO_SPEECH",data.get(0));
                Pattern pattern = Pattern.compile("(.)*(abr(.)*|entr(.)*)(.)*guía(.)*", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(datos);
                boolean matchFound = matcher.find();
                if(matchFound){
                    Intent intent = new Intent(MainActivity.this, Guidance.class);
                    startActivity(intent);
                }
                pattern = Pattern.compile("(.)*(abr(.)*|entr(.)*)(.)*menú(.)*", Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(datos);
                matchFound = matcher.find();
                if(matchFound){
                    Intent intent = new Intent(MainActivity.this, CanteenMenu.class);
                    startActivity(intent);
                }
                //editText.setText(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        //speechRecognizer.startListening(speechRecognizerIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Método que se ejecuta cuando la actividad no está activa (segundo plano)
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, proximitySensor);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView textView = findViewById(R.id.saldo_value_text);
        int parteEntera = (int) saldo;
        int parteDecimal = (int) (round((saldo - parteEntera) * 100));
        textView.setText(Integer.toString(parteEntera) + "." + Integer.toString(parteDecimal));
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Gestión del sensor de proximidad
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distancia = sensorEvent.values[0];
            Log.i("INFO_SPEECH","la distancia es " + distancia);
            if (distancia < 1) {
                speechRecognizer.startListening(speechRecognizerIntent);
                inicio = System.nanoTime();
                Toast.makeText(MainActivity.this,
                        "Escuchando...", Toast.LENGTH_LONG)
                        .show();
                Log.i("INFO_SPEECH","tamos escuchando");
                //startActivityForResult(speechRecognizerIntent,10);
                Log.i("INFO_SPEECH","Proximidad activada");
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case 10:
                    String datos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                    Log.i("INFO_SPEECH",datos);
                    Toast.makeText(this,
                            datos, Toast.LENGTH_LONG)
                            .show();

                    Pattern pattern = Pattern.compile("(.)*(abr(.)*|entr(.)*)(.)*guía(.)*", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(datos);
                    boolean matchFound = matcher.find();
                    if(matchFound){
                        Intent intent = new Intent(this, Guidance.class);
                        startActivity(intent);
                    }
                    pattern = Pattern.compile("(.)*(abr(.)*|entr(.)*)(.)*menú(.)*", Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(datos);
                    matchFound = matcher.find();
                    if(matchFound){
                        Intent intent = new Intent(this, CanteenMenu.class);
                        startActivity(intent);
                    }

                    /*int intFound = getNumberFromResult(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
                    if (intFound != -1) {
                        FIRST_NUMBER = intFound;
                        firstNumTextView.setText(intFound);
                    } else {
                        Toast.makeText(getApplicationContext(), "Sorry, I didn't catch that! Please try again", Toast.LENGTH_LONG).show();
                    }*/
                    break;

            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to recognize speech!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
        if (requestCode == RecordAudioRequestCode &&
                grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }*/


    public void launchGuidance(View view) {
        Intent intent = new Intent(this, Guidance.class);
        startActivity(intent);
    }

    public void launchCanteenMenu(View view) {
        Intent intent = new Intent(this, CanteenMenu.class);
        startActivity(intent);
    }
}