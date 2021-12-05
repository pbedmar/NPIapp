package com.example.npiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;

public class Guidance extends AppCompatActivity implements SensorEventListener {

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

    public static final String EXTRA_MESSAGE = "com.example.beta360.MESSAGE";

    private IntentIntegrator qrScan;

    private Map<String, Map<String, ArrayList<Integer>>> rutas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inicio = 0;
        fin = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidance);

        crearRutaPrueba();

        Spinner inicio_ruta = (Spinner) findViewById(R.id.spinner_inicio_ruta);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.rutas_inicio, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inicio_ruta.setAdapter(adapter1);

        Spinner fin_ruta = (Spinner) findViewById(R.id.spinner_fin_ruta);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.rutas_fin, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fin_ruta.setAdapter(adapter2);

        // Se obtiene el lector QR
        qrScan = new IntentIntegrator(this);
        qrScan.setPrompt("Escanear un QR");
        // Se fija que el lector esté siempre en orientación vertical
        qrScan.setOrientationLocked(true);

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

    boolean reconocerIrAsistente(String datos) {
        Pattern pattern = Pattern.compile("(.)*(abr(.)*|entr(.)*)(.)*asistente(.)*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(datos);
        boolean reconocido = matcher.find();
        if(reconocido){
            Intent intent = new Intent(Guidance.this, Asistente.class);
            startActivity(intent);
        }

        return reconocido;
    }

    /**
     * Método que se ejecuta al pulsar el botón del QR
     *
     * @param view
     */
    public void leerQR(View view) {
        qrScan.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaner fallido", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Escaneado QR", Toast.LENGTH_LONG).show();
                Log.i("INFO_escaner", result.getContents());
                String lectura = result.getContents();
                Spinner inicio = findViewById(R.id.spinner_inicio_ruta);
                ArrayAdapter adapter = (ArrayAdapter) inicio.getAdapter();
                int pos = adapter.getPosition(lectura);
                Log.i("INFO_pos", Integer.toString(pos));
                inicio.setSelection(pos);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void iniciarRuta(View view) {
        Spinner inicio = findViewById(R.id.spinner_inicio_ruta);
        String elemInicio = (String) inicio.getSelectedItem();
        Spinner fin = findViewById(R.id.spinner_fin_ruta);
        String elemFin = (String) fin.getSelectedItem();
        if (elemInicio.equals("")) {
            String text = "Rellenar el campo inicio";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        } else if (elemFin.equals("")) {
            String text = "Rellenar el campo fin";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        } else {
            Map<String, ArrayList<Integer>> ruta1 = rutas.get(elemInicio);
            ArrayList<Integer> ruta2;
            if (ruta1 != null) {
                ruta2 = ruta1.get(elemFin);
            } else {
                ruta2 = null;
            }
            if (ruta2 == null) {
                String text = "Ruta no disponible";
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, PanoramaController.class);
                intent.putExtra(EXTRA_MESSAGE, ruta2);
                startActivity(intent);
            }
        }
    }

    // Método para generar las rutas de prueba a usar en la APP
    private void crearRutaPrueba() {
        rutas = new HashMap<String, Map<String, ArrayList<Integer>>>();
        ArrayList<Integer> ruta1 = new ArrayList<Integer>();
        ruta1.add(0);
        ruta1.add(1);
        ruta1.add(2);
        ruta1.add(3);
        ruta1.add(4);
        ruta1.add(5);
        ruta1.add(6);
        ruta1.add(7);
        ruta1.add(8);
        Map<String, ArrayList<Integer>> finRuta1 = new HashMap<String, ArrayList<Integer>>();
        finRuta1.put("Aula 1.5", ruta1);
        rutas.put("Entrada ETSIIT", finRuta1);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Gestión del sensor de proximidad
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distancia = sensorEvent.values[0];
            if (distancia < 1) {
                if(speaker.isSpeaking()) {
                    speaker.stop();
                }
                speechRecognizer.startListening(speechRecognizerIntent);
                inicio = System.nanoTime();
                Toast.makeText(this,
                        "Escuchando...", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, proximitySensor);
    }
}