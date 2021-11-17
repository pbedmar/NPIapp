package com.example.npiapp;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SenderActivity extends AppCompatActivity implements OutcomingNfcManager.NfcActivity {

    // Info a enviar por el NFC
    private String info;
    // Sensor NFC
    private NfcAdapter nfcAdapter;
    // Lógica de envío
    private OutcomingNfcManager outcomingNfccallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        Log.d("NFC", "activity launched");

        // Se obtiene el sensor NFC
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC disabled on this device. Turn on to proceed", Toast.LENGTH_SHORT).show();
            }

            // Se obtiene la información a enviar a través del NFC
            Intent intent = getIntent();
            info = intent.getStringExtra(CanteenMenu.INFO_NFC);

            // Obtenemos la lógica de envío
            this.outcomingNfccallback = new OutcomingNfcManager(this);

            // Iniciamos el envío de la etiqueta NFC
            this.nfcAdapter.setOnNdefPushCompleteCallback(outcomingNfccallback, this);
            this.nfcAdapter.setNdefPushMessageCallback(outcomingNfccallback, this);
        }
    }

    /**
     * Método para obtener la información a enviar en la etiqueta
     *
     * @return información a enviar
     */
    @Override
    public String getOutcomingMessage() {
        return info;
    }

    /**
     * Método que se ejecuta al completar el envío
     */
    @Override
    public void signalResult() {
        runOnUiThread(() ->
                Toast.makeText(SenderActivity.this, "Beaming complete", Toast.LENGTH_SHORT).show());

        // Se finaliza la actividad de envío NFC
        Intent replyIntent = new Intent(SenderActivity.this, CanteenMenu.class);
        setResult(RESULT_OK, replyIntent);
        finish();
    }
}