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
import android.speech.tts.TextToSpeech;
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

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static float saldo = 20.0f;
    public static String TUI = "0001246933";
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private TextToSpeech speaker;
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

                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String datos = data.get(0);

                boolean reconocido = false;

                reconocido = reconocido || reconocerIrGuidance(datos);

                reconocido = reconocido || reconocerIrCanteenMenu(datos);

                reconocido = reconocido || reconocerIrAsistente(datos);

                if(!reconocido) {
                    speaker.speak("Reconocimiento fallido. Vuelve a intentarlo", TextToSpeech.QUEUE_FLUSH, null);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        speaker = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        speaker.setLanguage(Locale.getDefault());
    }

    boolean reconocerIrGuidance(String datos) {
        Pattern pattern = Pattern.compile("(.)*(abr(.)*|entr(.)*)(.)*guía(.)*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(datos);
        boolean reconocido = matcher.find();
        if(reconocido){
            Intent intent = new Intent(MainActivity.this, Guidance.class);
            startActivity(intent);
        }

        return reconocido;
    }

    boolean reconocerIrCanteenMenu(String datos) {
        Pattern pattern = Pattern.compile("(.)*(abr(.)*|entr(.)*)(.)*menú(.)*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(datos);
        boolean reconocido = matcher.find();
        if(reconocido){
            Intent intent = new Intent(MainActivity.this, CanteenMenu.class);
            startActivity(intent);
        }

        return reconocido;
    }

    boolean reconocerIrAsistente(String datos) {
        Pattern pattern = Pattern.compile("(.)*(abr(.)*|entr(.)*)(.)*asistente(.)*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(datos);
        boolean reconocido = matcher.find();
        if(reconocido){
            Intent intent = new Intent(MainActivity.this, Asistente.class);
            startActivity(intent);
        }

        return reconocido;
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
            if (distancia < 1) {
                speechRecognizer.startListening(speechRecognizerIntent);
                inicio = System.nanoTime();
                Toast.makeText(MainActivity.this,
                        "Escuchando...", Toast.LENGTH_LONG)
                        .show();
            }
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


    public void launchGuidance(View view) {
        Intent intent = new Intent(this, Guidance.class);
        startActivity(intent);
    }

    public void launchCanteenMenu(View view) {
        Intent intent = new Intent(this, CanteenMenu.class);
        startActivity(intent);
    }

    public void launchAsistente(View view) {
        Intent intent = new Intent(this, Asistente.class);
        startActivity(intent);
    }
}