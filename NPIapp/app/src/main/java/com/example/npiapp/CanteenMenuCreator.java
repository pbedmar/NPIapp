package com.example.npiapp;

import static java.lang.Math.abs;
import static java.lang.Math.round;

import android.animation.ObjectAnimator;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CanteenMenuCreator extends AppCompatActivity implements SensorEventListener {

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private TextToSpeech speaker;
    // Sensor de proximidad
    private Sensor proximitySensor;
    long inicio;
    long fin;
    private MenuViewModel mMenuViewModel;
    private Menu menuToday;
    private int numberOfPossibleMeals = 6;
    private String date = "00/00/0000";
    private int currentCard = 0;
    private boolean[] orderedMeals = new boolean[numberOfPossibleMeals];
    private float[] mealsPrices = new float[numberOfPossibleMeals];
    private String[] mealsNames = new String[numberOfPossibleMeals];
    private String[] mealsAllergens = new String[numberOfPossibleMeals];
    private String[] mealsTypes = {"Primero", "Primero", "Segundo", "Segundo", "Postre", "Postre"};
    private int[] cardsIds = new int[numberOfPossibleMeals];
    float totalOrderPrice = 0;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private long lastUpdate = 0;
    private long lastDetected = 0;
    private float umbralAceptar = 1.0f;
    private float umbralRechazar = 1.0f;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    /*
    Inicializa diferentes variables necesarias para la identificaci??n biom??trica,
    as?? como declara dos subm??todos onAuthenticationError() y onAuthenticationSucceeded(),
    que devuelven el control al Intent CanteenMenu seg??n si la huella es correcta o no.

    Tambi??n modifica el t??tulo de la actividad, conecta la base de datos y lee desde ??sta los datos
    del men?? que se va a ofrecer, almacenandose en distintos atributos.

    A continuaci??n, llama al m??todo setCardViewMedia() y finalmente inicia la detecci??n de
    movimiento usando el aceler??metro.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inicio = 0;
        fin = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canteen_menu_creator);

        ///////////////////////////////////////////////

        // Se obtiene el sensor biom??trico
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                Log.e("MY_APP_TAG", "Biometric features are none enrolled.");
                break;
        }

        executor = ContextCompat.getMainExecutor(this);
        // Se define la autenticaci??n, dentro se definen los m??todos que se ejecutar??n
        // en las distintas situaciones que puedan ocurrir durante la autenticaci??n
        biometricPrompt = new BiometricPrompt(CanteenMenuCreator.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();

                Intent replyIntent = new Intent();
                setResult(RESULT_CANCELED, replyIntent);
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Autenticaci??n completada!", Toast.LENGTH_SHORT).show();

                mMenuViewModel.setOrderOnSpecificDate(date, boolToInt(orderedMeals[0]),
                        boolToInt(orderedMeals[1]), boolToInt(orderedMeals[2]), boolToInt(orderedMeals[3]),
                        boolToInt(orderedMeals[4]), boolToInt(orderedMeals[5]), 1);

                MainActivity.saldo = MainActivity.saldo - totalOrderPrice;

                Intent replyIntent = new Intent();
                setResult(RESULT_OK, replyIntent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Autenticaci??n fallida",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticaci??n para realizar la compra")
                .setSubtitle("Verifica tu identidad mediante acceso biom??trico")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Utilizar contrase??a")
                .build();

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.

        ///////////////////////////////////////////////

        // descarga de extras recibidos
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            date = extras.getString(CanteenMenu.DATE);
        }

        // titulo de la actividad
        this.setTitle("Encargo d??a " + date);

        // conexi??n con la base de datos
        mMenuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        menuToday = mMenuViewModel.getMenuOnSpecificDate(date);

        mealsNames = menuToday.getMealsNamesArray();
        mealsPrices = menuToday.getMealsPricesArray();
        mealsAllergens = menuToday.getMealsAllergensArray();

        setCardViewMedia();

        // iniciar la detecci??n de movimiento
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
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
            Intent intent = new Intent(CanteenMenuCreator.this, Asistente.class);
            startActivity(intent);
        }

        return reconocido;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, proximitySensor);
        sensorManager.unregisterListener(this, sensorAccelerometer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    /*
    Para cada uno de los platos, rellena una tarjeta del mazo. Los atributos inicializados
    en el m??todo anterior contienen la informaci??n que es a??adida a las tarjetas.
     */
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

    /*
    Desplaza la tarjeta desde el centro de la pantalla hacia la izda en caso de agitar en el eje x,
    y desplaza hacia abajo en el caso de agitar en el eje z. A??ade realismo a la aplicaci??n al
    generar la sensaci??n de que agitar tiene un efecto real sobre el mazo.
     */
    protected void cardAnimation(RelativeLayout relativeLayout, boolean ordered, String axis) {
        float magnitude = -10000f;
        if (ordered) {
            magnitude = 10000f;
        }

        ObjectAnimator animation = ObjectAnimator.ofFloat(relativeLayout, axis, magnitude);
        animation.setDuration(1500);
        animation.start();

    }

    /*
    Avanza a la siguiente tarjeta del mazo llamando a cardAnimation().
    Si la tarjeta es la ??ltima de todas, llama a showOrderSummary().
     */
    protected void nextCard(String animationAxis) {

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(cardsIds[currentCard]);
        cardAnimation(relativeLayout, orderedMeals[currentCard], animationAxis);

        if (currentCard == 5) {
            showOrderSummary();
        }

        currentCard++;
    }

    /*
    Muestra un resumen del pedido, donde se mencionan los platos adquiridos, su precio y
    el precio total. En la misma tarjeta donde se muestra este resumen, aparecen los botones
    confirmar y cancelar. Si pulsamos en confirmar, se llama al m??todo confirmOrder(), en el
    otro caso se llama a cancelOrder()
     */
    protected void showOrderSummary() {
        LinearLayout linearLayout_order = (LinearLayout) findViewById(R.id.order_layout);
        linearLayout_order.setVisibility(View.VISIBLE);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.chosen_meals_layout);

        totalOrderPrice = 0;
        for (int i = 0; i < numberOfPossibleMeals; i++) {
            if (orderedMeals[i] == true) {
                Log.d("pedro", "a??adiendo plato a la cuenta");
                RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(
                        R.layout.activity_canteen_menu_creator_summary_entry, null);

                TextView mealName = (TextView) relativeLayout.getChildAt(0);
                mealName.setText(mealsNames[i]);
                TextView mealPrice = (TextView) relativeLayout.getChildAt(1);
                mealPrice.setText(Float.toString(mealsPrices[i]));
                linearLayout.addView(relativeLayout);

                totalOrderPrice += mealsPrices[i];
                Log.d("pedro", "plato a??adido a la cuenta");
            }
        }

        TextView totalPrice = (TextView) findViewById(R.id.total_price_text);
        int parteEntera = (int) totalOrderPrice;
        int parteDecimal = (int) (round((totalOrderPrice - parteEntera) * 100));
        totalPrice.setText(Integer.toString(parteEntera) + "." + Integer.toString(parteDecimal));
    }

    /*
    M??todo que se ejecuta de forma continua para tomar datos del aceler??metro lineal
    (aceler??metro que anula el efecto de la gravedad).

    Tambi??n, si se dan las condiciones adecuadas, es el responsable de ejecutar otros
    m??todos que nos hacen avanzar por el mazo de tarjetas.

    Aunque se ejecute de forma continua, s??lo tiene en cuenta las mediciones
    tomadas cada 100 milisegundos.

    Las se??ales obtenidas del aceler??metro son informaci??n en bruto que hay que procesar para
    obtener datos ??tiles. La informaci??n viene expresada con un valor para cada eje.
    Este valor representa la aceleraci??n medida en ese eje. Cuando la aceleraci??n en el eje x supera
    un umbral, rechazamos el plato mostrado en pantalla. Cuando la aceleraci??n en el eje z supera
    otro umbral, aceptamos el plato mostrado en pantalla.

    A??n as??, esta no es la ??nica restricci??n que utilizamos: para afinar m??s a la hora de medir la
    agitaci??n del dispositivo y prevenir falsos positivos, si al medir el valor en un eje detectamos
    movimiento en el otro eje tambi??n, descartamos la detecci??n.

    Una vez que hemos detectado un movimiento v??lido, aceptamos o descartamos el plato seg??n
    corresponda y pasamos a la siguiente tarjeta llamando al m??todo nextCard()
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor usedSensor = sensorEvent.sensor;

        if (currentCard < 6) {
            if (usedSensor == sensorAccelerometer) {
                long currentTime = System.currentTimeMillis();

                if ((currentTime - lastDetected) > 1000) {

                    if ((currentTime - lastUpdate) > 100) {
                        lastUpdate = currentTime;

                        float x = sensorEvent.values[0];
                        float z = sensorEvent.values[2];
                        Log.i("Info_acce_X", Float.toString(x));
                        Log.i("Info_acce_Z", Float.toString(z));

                        if (x > 2.0f && abs(z) < umbralRechazar) {
                            Log.d("pedro", "x shake detected");
                            orderedMeals[currentCard] = false;
                            lastDetected = System.currentTimeMillis();
                            nextCard("translationX");
                        } else if (z > 3.5f && abs(x) < umbralAceptar) {
                            Log.d("pedro", "z shake detected");
                            orderedMeals[currentCard] = true;
                            lastDetected = System.currentTimeMillis();
                            nextCard("translationY");
                        }
                    }
                }
            }
        }
        else {
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
    }

    public int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    /*
    Se devuelve a CanteenMenu un c??digo indicando que no se ha completado el pedido.
     */
    public void cancelOrder(View view) {
        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }

    public void onClickCard(View view) {
        Log.d("speaker", mealsAllergens[currentCard]);
        speaker.speak(mealsAllergens[currentCard], TextToSpeech.QUEUE_FLUSH, null);
    }

    /*
    Se comprueba que el usuario tiene saldo suficiente para poder pagar el pedido.
    Si es as??, se llama al m??dulo de autenticaci??n biom??trica.
    Si no, se devuelve a CanteenMenu un c??digo indicando que no se ha completado el pedido.
     */
    public void confirmOrder(View view) {
        if (MainActivity.saldo < totalOrderPrice) {
            Toast.makeText(getApplicationContext(),
                    "Saldo insuficiente", Toast.LENGTH_LONG)
                    .show();

            Intent replyIntent = new Intent();
            setResult(RESULT_CANCELED, replyIntent);
            finish();
        } else {
            biometricPrompt.authenticate(promptInfo);
        }
    }

    // m??todo necesario para el funcionamiento del aceler??metro
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}