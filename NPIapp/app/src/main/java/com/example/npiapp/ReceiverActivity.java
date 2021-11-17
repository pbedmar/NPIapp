package com.example.npiapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ReceiverActivity extends AppCompatActivity {


    public static final String RESPO_NFC = "com.example.emisornfc.RESPONSE";
    // Tipo de la etiqueta a recibir
    public static final String MIME_TEXT_PLAIN = "text/plain";
    // Sensor NFC
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        // Se obtiene el sensor NFC
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC disabled on this device. Turn on to proceed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Método que se ejecuta al detectar un NFC
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Se gestiona la recepción del mensaje
        receiveMessageFromDevice(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Se habilita la recepción con el NFC
        enableForegroundDispatch(this, this.nfcAdapter);
        // Se gestiona la recepción del mensaje
        receiveMessageFromDevice(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch(this, this.nfcAdapter);
    }

    /**
     * Método para gestionar la recepción del mensaje
     * @param intent
     */
    private void receiveMessageFromDevice(Intent intent) {
        String action = intent.getAction();
        // Si se ha descubierto una etiqueta NFC
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // Se obtiene la información del mensaje
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage inNdefMessage = (NdefMessage) parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord ndefRecord_0 = inNdefRecords[0];

            String inMessage = new String(ndefRecord_0.getPayload());

            // Se vuelve a la actividad de los menús indicando el resultado del NFC
            String[] campos = inMessage.split(";");
            Intent replyIntent = new Intent(this, CanteenMenu.class);
            replyIntent.putExtra(RESPO_NFC, campos[0]);
            if(campos[1].equals("OK")) {
                Toast.makeText(this, "Pedido registrado correctamente", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK, replyIntent);
            }
            else if(campos[1].equals("ERROR")) {
                Toast.makeText(this, "Error al registrar pedido", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED, replyIntent);
            }
            finish();
        }
    }


    // Foreground dispatch holds the highest priority for capturing NFC intents
    // then go activities with these intent filters:
    // 1) ACTION_NDEF_DISCOVERED
    // 2) ACTION_TECH_DISCOVERED
    // 3) ACTION_TAG_DISCOVERED

    // always try to match the one with the highest priority, cause ACTION_TAG_DISCOVERED is the most
    // general case and might be intercepted by some other apps installed on your device as well

    // When several apps can match the same intent Android OS will bring up an app chooser dialog
    // which is undesirable, because user will most likely have to move his device from the tag or another
    // NFC device thus breaking a connection, as it's a short range

    public void enableForegroundDispatch(AppCompatActivity activity, NfcAdapter adapter) {

        // here we are setting up receiving activity for a foreground dispatch
        // thus if activity is already started it will take precedence over any other activity or app
        // with the same intent filters


        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException ex) {
            throw new RuntimeException("Check your MIME type");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public void disableForegroundDispatch(final AppCompatActivity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
}