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

import java.util.concurrent.Executor;

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
    Inicializa diferentes variables necesarias para la identificación biométrica,
    así como declara dos submétodos onAuthenticationError() y onAuthenticationSucceeded(),
    que devuelven el control al Intent CanteenMenu según si la huella es correcta o no.

    También modifica el título de la actividad, conecta la base de datos y lee desde ésta los datos
    del menú que se va a ofrecer, almacenandose en distintos atributos.

    A continuación, llama al método setCardViewMedia() y finalmente inicia la detección de
    movimiento usando el acelerómetro.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canteen_menu_creator);

        ///////////////////////////////////////////////

        // Se obtiene el sensor biométrico
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
        // Se define la autenticación, dentro se definen los métodos que se ejecutarán
        // en las distintas situaciones que puedan ocurrir durante la autenticación
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
                        "Autenticación completada!", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(getApplicationContext(), "Autenticación fallida",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación para realizar la compra")
                .setSubtitle("Verifica tu identidad mediante acceso biométrico")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Utilizar contraseña")
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
        this.setTitle("Encargo día " + date);

        // conexión con la base de datos
        mMenuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        menuToday = mMenuViewModel.getMenuOnSpecificDate(date);

        mealsNames = menuToday.getMealsNamesArray();
        mealsPrices = menuToday.getMealsPricesArray();

        setCardViewMedia();

        // iniciar la detección de movimiento
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
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

    /*
    Para cada uno de los platos, rellena una tarjeta del mazo. Los atributos inicializados
    en el método anterior contienen la información que es añadida a las tarjetas.
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
    y desplaza hacia abajo en el caso de agitar en el eje z. Añade realismo a la aplicación al
    generar la sensación de que agitar tiene un efecto real sobre el mazo.
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
    Si la tarjeta es la última de todas, llama a showOrderSummary().
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
    confirmar y cancelar. Si pulsamos en confirmar, se llama al método confirmOrder(), en el
    otro caso se llama a cancelOrder()
     */
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
        int parteEntera = (int) totalOrderPrice;
        int parteDecimal = (int) (round((totalOrderPrice - parteEntera) * 100));
        totalPrice.setText(Integer.toString(parteEntera) + "." + Integer.toString(parteDecimal));
    }

    /*
    Método que se ejecuta de forma continua para tomar datos del acelerómetro lineal
    (acelerómetro que anula el efecto de la gravedad).

    También, si se dan las condiciones adecuadas, es el responsable de ejecutar otros
    métodos que nos hacen avanzar por el mazo de tarjetas.

    Aunque se ejecute de forma continua, sólo tiene en cuenta las mediciones
    tomadas cada 100 milisegundos.

    Las señales obtenidas del acelerómetro son información en bruto que hay que procesar para
    obtener datos útiles. La información viene expresada con un valor para cada eje.
    Este valor representa la aceleración medida en ese eje. Cuando la aceleración en el eje x supera
    un umbral, rechazamos el plato mostrado en pantalla. Cuando la aceleración en el eje z supera
    otro umbral, aceptamos el plato mostrado en pantalla.

    Aún así, esta no es la única restricción que utilizamos: para afinar más a la hora de medir la
    agitación del dispositivo y prevenir falsos positivos, si al medir el valor en un eje detectamos
    movimiento en el otro eje también, descartamos la detección.

    Una vez que hemos detectado un movimiento válido, aceptamos o descartamos el plato según
    corresponda y pasamos a la siguiente tarjeta llamando al método nextCard()
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
    }

    public int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    /*
    Se devuelve a CanteenMenu un código indicando que no se ha completado el pedido.
     */
    public void cancelOrder(View view) {
        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }

    /*
    Se comprueba que el usuario tiene saldo suficiente para poder pagar el pedido.
    Si es así, se llama al módulo de autenticación biométrica.
    Si no, se devuelve a CanteenMenu un código indicando que no se ha completado el pedido.
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

    // método necesario para el funcionamiento del acelerómetro
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}