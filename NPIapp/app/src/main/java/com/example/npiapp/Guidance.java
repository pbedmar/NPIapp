package com.example.npiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

public class Guidance extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.beta360.MESSAGE";

    private IntentIntegrator qrScan;

    private Map<String, Map<String, ArrayList<Integer>>> rutas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    }

    /**
     * Método que se ejecuta al pulsar el botón del QR
     * @param view
     */
    public void leerQR(View view) {
        qrScan.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Escaner fallido", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Escaneado QR", Toast.LENGTH_LONG).show();
                Log.i("INFO_escaner", result.getContents());
                String lectura = result.getContents();
                Spinner inicio = findViewById(R.id.spinner_inicio_ruta);
                ArrayAdapter adapter = (ArrayAdapter) inicio.getAdapter();
                int pos = adapter.getPosition(lectura);
                Log.i("INFO_pos", Integer.toString(pos));
                inicio.setSelection(pos);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void iniciarRuta(View view) {
        Spinner inicio = findViewById(R.id.spinner_inicio_ruta);
        String elemInicio = (String) inicio.getSelectedItem();
        Spinner fin = findViewById(R.id.spinner_fin_ruta);
        String elemFin = (String) fin.getSelectedItem();
        if(elemInicio.equals("")) {
            String text = "Rellenar el campo inicio";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
        else if(elemFin.equals("")) {
            String text = "Rellenar el campo fin";
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
        else {
            Map<String, ArrayList<Integer>> ruta1 = rutas.get(elemInicio);
            ArrayList<Integer> ruta2;
            if(ruta1 != null) {
                ruta2 = ruta1.get(elemFin);
            }
            else {
                ruta2 = null;
            }
            if(ruta2 == null) {
                String text = "Ruta no disponible";
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(this, PanoramaController.class);
                intent.putExtra(EXTRA_MESSAGE, ruta2);
                startActivity(intent);
            }
        }
    }

    // MÃ©todo para generar las rutas de prueba a usar en la APP
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
}