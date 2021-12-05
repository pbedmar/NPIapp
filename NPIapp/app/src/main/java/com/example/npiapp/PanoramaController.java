package com.example.npiapp;

import static com.example.npiapp.R.id.panoramaView;

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
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PanoramaController extends AppCompatActivity implements SensorEventListener {

    /**
     * Clase con las distintas direcciones de la flecha de guiado
     */
    public static class Direcciones {
        public static final Integer SIN_DIRE = -1;
        public static final Integer FLECHA_IZQ = 0;
        public static final Integer FLECHA_DER = 1;
        public static final Integer FLECHA_ARRIBA = 2;
        public static final Integer FLECHA_ABAJO = 3;
        public static final Integer FLECHA_ARRIBA_IZQ = 4;
        public static final Integer FLECHA_ARRIBA_DER = 5;
        public static final Integer FLECHA_ABAJO_IZQ = 6;
        public static final Integer FLECHA_ABAJO_DER = 7;
    }

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private TextToSpeech speaker;
    long inicio;
    long fin;

    // Límites del zoom
    private final float MAX_ZOOM = 1.0f;
    private final float MIN_ZOOM = 0.3f;

    // Elemento de tipo panoramaView que mostrará la imagen
    private PanoramaView panorama;
    // Elemento que servirá de copia de estado de la imagen al mostrar la info de un hotspot
    private PanoramaView panoramaCopy;

    // Manejador de sensores
    private SensorManager sensorManager;
    // Sensor de rotación del vector (es un sensor software, es decir, es la combinación de varios
    // sensores hardware (giroscopio, acelerómetro y magnetómetro))
    private Sensor rotationVectorSensor;
    // Detector de gestos
    private ScaleGestureDetector scaleGestureDetector;
    // Sensor de proximidad
    private Sensor proximitySensor;

    // Variable que indica si se está mostrando info para dejar de muestrear los sensores
    private Boolean mostrandoInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inicio = 0;
        fin = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panorama_controller);

        // Recibimos la ruta a mostrar a través del Intent
        Intent intent = getIntent();
        ArrayList<Integer> ruta = intent.getIntegerArrayListExtra(Guidance.EXTRA_MESSAGE);

        // Obtenemos el objeto PanoramaView
        panorama = findViewById(panoramaView);
        // Lo inicializamos
        panorama.initialize(ruta);

        // Creamos el manejador de sensores
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Obtenemos el sensor de rotación del vector
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVectorSensor == null) {
            Log.e("ERROR", "Proximity sensor not available");
            finish(); // Close app
        }

        // Obtenemos el detector de gestos
        scaleGestureDetector = new ScaleGestureDetector(this, new OnPinchListener());

        // Obtenemos el sensor de proximidad
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Se indica que no se está mostrando info
        mostrandoInfo = false;

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

                reconocido = reconocido || reconocerAvanzar(datos);

                reconocido = reconocido || reconocerRetroceder(datos);

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
            Intent intent = new Intent(PanoramaController.this, Asistente.class);
            startActivity(intent);
        }

        return reconocido;
    }

    boolean reconocerAvanzar(String datos) {
        ArrayList<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile("(.)*(avanz(.)*)([a-z]|[A-Z]| )*siguiente([a-z]|[A-Z]| )*escena([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(avanz(.)*)([a-z]|[A-Z]| )*([1-3])([a-z]|[A-Z]| )*escena([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(avanz(.)*)([a-z]|[A-Z]| )*(una|dos|tres)([a-z]|[A-Z]| )*escena([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));

        boolean reconocido = false;
        int i = 0;
        while (!reconocido && i < patterns.size()) {
            Matcher matcher = patterns.get(i).matcher(datos);
            Log.i("Datos", datos);
            reconocido = matcher.find();
            if (reconocido) {
                Log.i("Datos", Integer.toString(i));

                switch (i) {
                    case 0:
                        if(!panorama.moverEscena(1)) {
                            speaker.speak("Escena no disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 1:
                        int num = Integer.parseInt(matcher.group(5));

                        Log.i("Datos", matcher.group(5));
                        if(!panorama.moverEscena(num)) {
                            speaker.speak("Escena no disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 2:
                        num = 0;

                        switch (matcher.group(5)) {
                            case "una":
                                num = 1;
                                break;
                            case "dos":
                                num = 2;
                                break;
                            case "tres":
                                num = 3;
                                break;
                        }

                        Log.i("Datos", matcher.group(5));
                        if(!panorama.moverEscena(num)) {
                            speaker.speak("Escena no disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                }
            }

            i += 1;
        }

        return reconocido;
    }

    boolean reconocerRetroceder(String datos) {
        ArrayList<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile("(.)*(retroced(.)*)([a-z]|[A-Z]| )*anterior([a-z]|[A-Z]| )*escena([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(retroced(.)*)([a-z]|[A-Z]| )*([1-3])([a-z]|[A-Z]| )*escena([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(retroced(.)*)([a-z]|[A-Z]| )*(una|dos|tres)([a-z]|[A-Z]| )*escena([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));

        boolean reconocido = false;
        int i = 0;
        while (!reconocido && i < patterns.size()) {
            Matcher matcher = patterns.get(i).matcher(datos);
            Log.i("Datos", datos);
            reconocido = matcher.find();
            if (reconocido) {
                Log.i("Datos", Integer.toString(i));

                switch (i) {
                    case 0:
                        if(!panorama.moverEscena(-1)) {
                            speaker.speak("Escena no disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 1:
                        int num = Integer.parseInt(matcher.group(5));

                        Log.i("Datos", matcher.group(5));
                        if(!panorama.moverEscena(-num)) {
                            speaker.speak("Escena no disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 2:
                        num = 0;

                        switch (matcher.group(5)) {
                            case "una":
                                num = 1;
                                break;
                            case "dos":
                                num = 2;
                                break;
                            case "tres":
                                num = 3;
                                break;
                        }

                        Log.i("Datos", matcher.group(5));
                        if(!panorama.moverEscena(-num)) {
                            speaker.speak("Escena no disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                }
            }

            i += 1;
        }

        return reconocido;
    }

    /**
     * Método que se ejecuta cuando la actividad está activa
     */
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotationVectorSensor, 0);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Método que se ejecuta cuando la actividad no está activa (segundo plano)
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, rotationVectorSensor);
        sensorManager.unregisterListener(this, proximitySensor);
    }

    /**
     * Método que se ejecuta al iniciar la actividad
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Se fija el texto inferior de la escena
        TextView t = findViewById(R.id.title_escena);
        t.setText(panorama.getTituloEscena());

        // Se carga la imagen seleccionada
        panorama.cargarImagen();
        if (panorama.getPosRuta() == panorama.getLenRuta() - 1) {
            Toast.makeText(getApplicationContext(),
                    "Has llegado al destino", Toast.LENGTH_LONG)
                    .show();
        }

        // Redibuja el lienzo
        panorama.invalidate();
    }

    /**
     * Método que se ejecuta al percibir un cambio en algún sensor
     *
     * @param sensorEvent evento de cambio de algún sensor
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (!mostrandoInfo) {
            // Gestión del sensor de proximidad
            if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                float distancia = sensorEvent.values[0];
                if (distancia < 1) {
                    speechRecognizer.startListening(speechRecognizerIntent);
                    inicio = System.nanoTime();
                    Toast.makeText(this,
                            "Escuchando...", Toast.LENGTH_LONG)
                            .show();
                }
            }
            // Gestión del sensor de rotación del vector
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // Matriz que contendrá la rotación del móvil
                float[] rotationMatrix = new float[16];

                // Obtener la rotación del móvil
                SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);

                // Volvemos a mapear la rotación pero colocando en la misma posición el eje Z y el eje Y,
                // para poder obtener la orientación del móvil
                float[] remappedRotationMatrix = new float[16];
                SensorManager.remapCoordinateSystem(rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedRotationMatrix);

                // Obtenemos la orientación del móvil
                // La posición cero contendrá la orientación del eje X (bŕujula)
                // La posición uno contendrá la orientación del eje Y (inclinación)
                float[] orientations = new float[3];
                SensorManager.getOrientation(remappedRotationMatrix, orientations);

                // Pasamos de radianes a grados
                for (int i = 0; i < 3; i++) {
                    orientations[i] = (float) (Math.toDegrees(orientations[i]));
                }

                // Aplicamos la nueva orientación en la imagen
                panorama.aplicarOrientacion(orientations[0]);
                // Aplicamos la nueva inclinación en la imagen
                panorama.aplicarInclinacion(orientations[1]);

                // Obtenemos que flecha mostrar dependiendo de la región que se está mostrando
                int result = panorama.fijarFlecha();

                // Inicializamos todas las flechas a invisible
                ArrayList<Integer> estados = new ArrayList<>();
                for (int i = 0; i < 8; ++i) {
                    estados.add(View.INVISIBLE);
                }
                // Si es necesario mostrar alguna flecha se pasa a visible la flecha indicada
                if (result >= 0) {
                    estados.set(result, View.VISIBLE);
                }

                // Fijar la visibilidad de todas las flechas
                findViewById(R.id.flecha_izq).setVisibility(estados.get(Direcciones.FLECHA_IZQ));
                findViewById(R.id.flecha_der).setVisibility(estados.get(Direcciones.FLECHA_DER));
                findViewById(R.id.flecha_arriba).setVisibility(estados.get(Direcciones.FLECHA_ARRIBA));
                findViewById(R.id.flecha_abajo).setVisibility(estados.get(Direcciones.FLECHA_ABAJO));
                findViewById(R.id.flecha_arriba_der).setVisibility(estados.get(Direcciones.FLECHA_ARRIBA_DER));
                findViewById(R.id.flecha_arriba_izq).setVisibility(estados.get(Direcciones.FLECHA_ARRIBA_IZQ));
                findViewById(R.id.flecha_abajo_der).setVisibility(estados.get(Direcciones.FLECHA_ABAJO_DER));
                findViewById(R.id.flecha_abajo_izq).setVisibility(estados.get(Direcciones.FLECHA_ABAJO_IZQ));

                // Redibujamos la imagen
                panorama.invalidate();
            }
        }
    }

    /**
     * Método que se ejecuta al percibir una pulsación en la pantalla
     *
     * @param event evento de multitouch
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mostrandoInfo) {
            scaleGestureDetector.onTouchEvent(event);
            // Si se detecta sólo una pulsación a la vez
            if (event.getPointerCount() == 1) {
                // Obtenemos el hotspot pulsado
                Hotspot hotspot = panorama.pulsacion(event.getX(0), event.getY(0));

                // Si se pulsa algún hotspot
                if (hotspot != null) {
                    // Si es un hotspot de salto
                    if (hotspot.getClass() == HotspotJump.class) {
                        // Cambiamos de escena
                        panorama.cambiarEscena((HotspotJump) hotspot);
                        if (panorama.getPosRuta() == panorama.getLenRuta() - 1) {
                            Toast.makeText(getApplicationContext(),
                                    "Has llegado al destino", Toast.LENGTH_LONG)
                                    .show();
                        }

                        // Fijamos el nuevo texto de la escena
                        TextView p = findViewById(R.id.title_escena);
                        p.setText(panorama.getTituloEscena());

                        // Redibujamos el lienzo
                        panorama.invalidate();
                    }
                    // Si es un hotspot de info
                    else if (hotspot.getClass() == HotspotInfo.class) {
                        // Indicamos que estamos mostrando información
                        mostrandoInfo = true;

                        // Salvamos el estado de la imagen
                        panoramaCopy = panorama;

                        // Fijamos el layout de mostrar información
                        setContentView(R.layout.activity_mostrar_info);

                        // Fijamos el titulo de la información
                        TextView t = findViewById(R.id.title_info);
                        t.setText(((HotspotInfo) hotspot).getTitle());

                        // Fijamos la descripción de la información
                        TextView d = findViewById(R.id.descrip_info);
                        d.setText(((HotspotInfo) hotspot).getDescrip());

                        TextView descripInfo = findViewById(R.id.descrip_info);
                        descripInfo.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                    if(speaker.isSpeaking()) {
                                        speaker.stop();
                                    }
                                    else {
                                        speaker.speak((String) descripInfo.getText(), TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                }
                                return false;
                            }
                        });
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Método para cerrar la ventana de información
     *
     * @param view
     */
    public void cerrarVentana(View view) {
        // Se vuelve a fijar el layout de la imagen
        setContentView(R.layout.activity_panorama_controller);

        // Se fija el texto inferior de la escena
        TextView t = findViewById(R.id.title_escena);
        t.setText(panorama.getTituloEscena());

        // Se inicializa la imagen con el estado guardado
        panorama = findViewById(panoramaView);
        panorama.initialize(panoramaCopy);

        // Se indica que ya no se está mostrando información
        mostrandoInfo = false;
    }

    private class OnPinchListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        /**
         * Método para reescalar la imagen
         *
         * @param detector
         * @return
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!mostrandoInfo) {
                // Obtenemos la nueva escala
                float scaleFactor = (float) panorama.getZoom() / detector.getScaleFactor();

                // Se fija el zoom dentro de los límites
                if (scaleFactor > MAX_ZOOM)
                    scaleFactor = MAX_ZOOM;
                if (scaleFactor < MIN_ZOOM)
                    scaleFactor = MIN_ZOOM;

                // Aplicamos el zoom en la imagen
                panorama.aplicarZoom(scaleFactor);
            }

            return true;
        }
    }
}
