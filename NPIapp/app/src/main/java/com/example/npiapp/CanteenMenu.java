package com.example.npiapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Muestra una lista de los pedidos realizados. También, en la parte inferior de la pantalla,
muestra un spinner donde aparecen las fechas de los próximos tres días,
para pedir un menú en alguno de ellos pulsando el botón Encargar.

En el momento en el que se pide algún menú, este aparece en la lista de menús encargados.
Se muestran los platos elegidos, el precio total del menú y si ha sido recogido o no.
 */
public class CanteenMenu extends AppCompatActivity implements SensorEventListener {

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
    public static final String DATE = "npiapp.CanteenMenu.DATE";
    public static final String INFO_NFC = "npiapp.CanteenMenu.INFO_NFC";
    private final int numberOfDatesToShow = 3;
    private String[] spinnerDates = new String[numberOfDatesToShow];
    private String todayDate;
    private final int numMeals = 6;

    protected static MenuViewModel mMenuViewModel;

    /*
    Genera la actividad y conecta la base de datos.
    Además, llama a los métodos generateDates(), loadSpinnerOptions() y loadOrders()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inicio = 0;
        fin = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canteen_menu);

        mMenuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        generateDates();
        loadSpinnerOptions();
        loadOrders();

        Log.d("CanteenMenu", "OnCreate");

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

                reconocido = reconocido || reconocerElegirFecha(datos);

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

    boolean reconocerElegirFecha(String datos) {
        ArrayList<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile("(.)*(reserv(.)*|ped(.)*)([a-z]|[A-Z]| )*pasado([a-z]|[A-Z]| )*mañana(.)*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(reserv(.)*|ped(.)*)([a-z]|[A-Z]| )*mañana(.)*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(reserv(.)*|ped(.)*)([a-z]|[A-Z]| )*dentro([a-z]|[A-Z]| )*de([a-z]|[A-Z]| )*2([a-z]|[A-Z]| )*días(.)*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(reserv(.)*|ped(.)*)([a-z]|[A-Z]| )*para([a-z]|[A-Z]| )*([0-9]{2})([a-z]|[A-Z]| )+([0-9]{2})([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(reserv(.)*|ped(.)*)([a-z]|[A-Z]| )*para([a-z]|[A-Z]| )*([0-9]{2})([a-z]|[A-Z]| )+([0-9])([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(reserv(.)*|ped(.)*)([a-z]|[A-Z]| )*para([a-z]|[A-Z]| )*([0-9])([a-z]|[A-Z]| )+([0-9]{2})([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(reserv(.)*|ped(.)*)([a-z]|[A-Z]| )*para([a-z]|[A-Z]| )*([0-9])([a-z]|[A-Z]| )+([0-9])([a-z]|[A-Z]| )*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(reserv(.)*|ped(.)*)([a-z]|[A-Z]| )*para([a-z]|[A-Z]| )*([0-9]{2})([a-z]|[A-Z]| )+(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)(.)*", Pattern.CASE_INSENSITIVE));
        patterns.add(Pattern.compile("(.)*(reserv(.)*|ped(.)*)([a-z]|[A-Z]| )*para([a-z]|[A-Z]| )*([0-9])([a-z]|[A-Z]| )+(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)(.)*", Pattern.CASE_INSENSITIVE));

        boolean reconocido = false;
        int i = 0;
        boolean saltar = false;
        while(!reconocido && i < patterns.size()) {
            Matcher matcher = patterns.get(i).matcher(datos);
            Log.i("Datos", datos);
            reconocido = matcher.find();
            if(reconocido) {
                Log.i("Datos", Integer.toString(i));
                Spinner spinner = (Spinner) findViewById(R.id.date_spinner);
                int pos;
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date hoy = calendar.getTime();
                String year = dateFormat.format(hoy).split("/")[2];
                String fecha;


                switch (i) {
                    case 0:
                        pos = 1;
                        spinner.setSelection(pos);
                        saltar = true;
                        break;
                    case 1:
                        pos = 0;
                        spinner.setSelection(pos);
                        saltar = true;
                        break;
                    case 2:
                        pos = 2;
                        spinner.setSelection(pos);
                        saltar = true;
                        break;
                    case 3:
                        fecha = matcher.group(7) + "/" + matcher.group(9) + "/" + year;

                        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
                        pos = adapter.getPosition(fecha);
                        Log.i("Datos", Integer.toString(pos));

                        if(pos != -1) {
                            spinner.setSelection(pos);
                            saltar = true;
                        }
                        else {
                            speaker.speak("La fecha no está disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 4:
                        fecha = matcher.group(7) + "/" + "0" +  matcher.group(9) + "/" + year;

                        adapter = (ArrayAdapter) spinner.getAdapter();
                        pos = adapter.getPosition(fecha);
                        Log.i("Datos", Integer.toString(pos));

                        if(pos != -1) {
                            spinner.setSelection(pos);
                            saltar = true;
                        }
                        else {
                            speaker.speak("La fecha no está disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 5:
                        fecha = "0" + matcher.group(7) + "/" + matcher.group(9) + "/" + year;
                        Log.i("Datos", fecha);
                        adapter = (ArrayAdapter) spinner.getAdapter();
                        pos = adapter.getPosition(fecha);
                        Log.i("Datos", Integer.toString(pos));

                        if(pos != -1) {
                            spinner.setSelection(pos);
                            saltar = true;
                        }
                        else {
                            speaker.speak("La fecha no está disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 6:
                        fecha = "0" + matcher.group(7) + "/" + "0" + matcher.group(9) + "/" + year;

                        adapter = (ArrayAdapter) spinner.getAdapter();
                        pos = adapter.getPosition(fecha);
                        Log.i("Datos", Integer.toString(pos));

                        if(pos != -1) {
                            spinner.setSelection(pos);
                            saltar = true;
                        }
                        else {
                            speaker.speak("La fecha no está disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 7:
                        fecha = matcher.group(7);

                        switch (matcher.group(9)) {
                            case "enero":
                                fecha += "/" + "01" + "/" + year;
                                break;
                            case "febrero":
                                fecha += "/" + "02" + "/" + year;
                                break;
                            case "marzo":
                                fecha += "/" + "03" + "/" + year;
                                break;
                            case "abril":
                                fecha += "/" + "04" + "/" + year;
                                break;
                            case "mayo":
                                fecha += "/" + "05" + "/" + year;
                                break;
                            case "junio":
                                fecha += "/" + "06" + "/" + year;
                                break;
                            case "julio":
                                fecha += "/" + "07" + "/" + year;
                                break;
                            case "agosto":
                                fecha += "/" + "08" + "/" + year;
                                break;
                            case "septiembre":
                                fecha += "/" + "09" + "/" + year;
                                break;
                            case "octubre":
                                fecha += "/" + "10" + "/" + year;
                                break;
                            case "noviembre":
                                fecha += "/" + "11" + "/" + year;
                                break;
                            case "diciembre":
                                fecha += "/" + "12" + "/" + year;
                                break;
                            default:
                                fecha = "";
                        }

                        adapter = (ArrayAdapter) spinner.getAdapter();
                        pos = adapter.getPosition(fecha);

                        if(pos != -1) {
                            spinner.setSelection(pos);
                            saltar = true;
                        }
                        else {
                            speaker.speak("La fecha no está disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                    case 8:
                        fecha = "0" + matcher.group(7);

                        switch (matcher.group(9)) {
                            case "enero":
                                fecha += "/" + "01" + "/" + year;
                                break;
                            case "febrero":
                                fecha += "/" + "02" + "/" + year;
                                break;
                            case "marzo":
                                fecha += "/" + "03" + "/" + year;
                                break;
                            case "abril":
                                fecha += "/" + "04" + "/" + year;
                                break;
                            case "mayo":
                                fecha += "/" + "05" + "/" + year;
                                break;
                            case "junio":
                                fecha += "/" + "06" + "/" + year;
                                break;
                            case "julio":
                                fecha += "/" + "07" + "/" + year;
                                break;
                            case "agosto":
                                fecha += "/" + "08" + "/" + year;
                                break;
                            case "septiembre":
                                fecha += "/" + "09" + "/" + year;
                                break;
                            case "octubre":
                                fecha += "/" + "10" + "/" + year;
                                break;
                            case "noviembre":
                                fecha += "/" + "11" + "/" + year;
                                break;
                            case "diciembre":
                                fecha += "/" + "12" + "/" + year;
                                break;
                            default:
                                fecha = "";
                        }

                        adapter = (ArrayAdapter) spinner.getAdapter();
                        pos = adapter.getPosition(fecha);

                        if(pos != -1) {
                            spinner.setSelection(pos);
                            saltar = true;
                        }
                        else {
                            speaker.speak("La fecha no está disponible", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        break;
                }

                if(saltar) {
                    Intent intent = new Intent(this, CanteenMenuCreator.class);

                    spinner = findViewById(R.id.date_spinner);
                    fecha = spinner.getSelectedItem().toString();

                    Menu menu = mMenuViewModel.getMenuOnSpecificDate(fecha);
                    if (menu.getDay_with_order() >= 1) {
                        Toast.makeText(this, "Ya hay un pedido realizado en esa fecha", Toast.LENGTH_LONG).show();
                    } else {
                        intent.putExtra(DATE, fecha);
                        startActivity(intent);
                    }
                }
            }

            i += 1;
        }

        return reconocido;
    }

    boolean reconocerIrAsistente(String datos) {
        Pattern pattern = Pattern.compile("(.)*(abr(.)*|entr(.)*)(.)*asistente(.)*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(datos);
        boolean reconocido = matcher.find();
        if(reconocido){
            Intent intent = new Intent(CanteenMenu.this, Asistente.class);
            startActivity(intent);
        }

        return reconocido;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, proximitySensor);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /*
    Genera las fechas del día de hoy y para los tres días siguientes y las almacena como atributo
     */
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

    /*
    Carga las fechas generadas en el método anterior como opciones del spinner
    que permite elegir en qué día realizar el pedido
     */
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

    /*
    Listener del botón Encargar. Cuando el botón es pulsado, el método lanza un Intent a la clase
    CanteenMenuCreator(). Esta clase permite realizar un pedido para un día en concreto
    (este día es enviado como parámetro del Intent, y proviene del valor seleccionado en el spinner)
     */
    public void launchCanteenMenuCreator(View view) {
        Intent intent = new Intent(this, CanteenMenuCreator.class);

        Spinner spinner = (Spinner) findViewById(R.id.date_spinner);
        String fecha = spinner.getSelectedItem().toString();

        Menu menu = mMenuViewModel.getMenuOnSpecificDate(fecha);
        if (menu.getDay_with_order() >= 1) {
            Toast.makeText(this, "Ya hay un pedido realizado en esa fecha", Toast.LENGTH_LONG).show();
        } else {
            intent.putExtra(DATE, fecha);
            startActivity(intent);
        }
    }

    /*
    Para el día actual y los tres días siguientes, muestra los menús que se han solicitado leyendo
    desde la base de datos. Se muestran los platos elegidos, el precio total del menú y si ha sido
    recogido o no. Para programar esto, creamos un ScrollView que contiene un LinearLayout al que
    se añaden RelativeLayout programáticamente (cada uno de ellos representa un encargo).

    De la misma forma, a cada RelativeLayout se le añaden varios LinearLayout conteniendo los platos.
    El realizar  esto de forma programática (y no directamente en el xml) permite que las vistas
    sean escalables y puedan cambiar dinámicamente.

    Para cada uno de los RelativeLayout se añade un onClickListener, para que en el caso de que
    se haga click sobre ellos, se inicie la actividad de recogida del pedido SenderActivity.
     */
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
                int parteEntera = (int) total_price;
                int parteDecimal = (int) ((total_price - parteEntera) * 100);
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
                        String mealsNamesString = String.join("\n", mealsNames);
                        String infoToNFC = "FECHA;" + date_ + ";TUI;" + MainActivity.TUI + ";PLATOS;" + mealsNamesString +
                                ";PRECIO;" + Float.toString(finalTotal_price);

                        onClickCard(v, infoToNFC);
                    }
                });

                TextView textEstado = newCard.findViewById(R.id.estadoText);
                if (menu.getDay_with_order() == 2) {
                    textEstado.setText("Pedido recogido");
                } else {
                    textEstado.setText("Pedido sin recoger");
                }

                linearLayout.addView(newCard);
            }
        }
    }

    /*
    Extensión del listener onClick() del método anterior
     */
    protected void onClickCard(View v, String info) {
        String[] campos = info.split(";");
        Menu menu = mMenuViewModel.getMenuOnSpecificDate(campos[1]);
        if (menu.getDay_with_order() != 2) {
            Intent intent = new Intent(this, SenderActivity.class);
            intent.putExtra(INFO_NFC, info);
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(this, "Pedido ya registrado", Toast.LENGTH_LONG).show();
        }
    }

    /*
    Recibe la respuesta de la actividad de recogida del pedido ReceiverActivity.
    Si el pedido se ha recogido correctamente, se actualiza la base de datos para reflejar esto.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, ReceiverActivity.class);
                startActivityForResult(intent, 2);
            }
        } else if (requestCode == 2) {
            String fecha = data.getStringExtra(ReceiverActivity.RESPO_NFC);
            if (fecha != null) {
                if (resultCode == RESULT_OK) {
                    mMenuViewModel.setOrderedOnSpecificDate(fecha, 2);
                } else if (resultCode == RESULT_CANCELED) {
                    mMenuViewModel.setOrderedOnSpecificDate(fecha, 1);
                }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}