package com.example.receptornfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class ReceiverActivity extends AppCompatActivity {

    public static final String RESPO_NFC = "com.example.emisornfc.RESPONSE";

    public static final String ID_NFC = "com.example.emisornfc.ID";

    public static final String MIME_TEXT_PLAIN = "text/plain";

    private Map<String,String> campos;

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

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

    @Override
    protected void onNewIntent(Intent intent) {
        // also reading NFC message from here in case this activity is already started in order
        // not to start another instance of this activity
        super.onNewIntent(intent);
        receiveMessageFromDevice(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // foreground dispatch should be enabled here, as onResume is the guaranteed place where app
        // is in the foreground
        enableForegroundDispatch(this, this.nfcAdapter);
        receiveMessageFromDevice(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch(this, this.nfcAdapter);
    }

    private void receiveMessageFromDevice(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage inNdefMessage = (NdefMessage) parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord ndefRecord_0 = inNdefRecords[0];

            String inMessage = new String(ndefRecord_0.getPayload());

            campos = procesarMensaje(inMessage);

            if(campos.get("RESULT").equals("OK")) {
                TextView TUI = findViewById(R.id.content_TUI);
                TUI.setText(campos.get("TUI"));
                TextView PLATOS = findViewById(R.id.content_menu);
                PLATOS.setText(campos.get("PLATOS"));
                TextView PRECIO = findViewById(R.id.content_precio);
                PRECIO.setText(campos.get("PRECIO"));
            }
            else if(campos.get("RESULT").equals("ERROR")) {
                Toast.makeText(this, "Formato erroneo", Toast.LENGTH_LONG).show();

                Intent replyIntent = new Intent(this, SenderActivity.class);
                replyIntent.putExtra(RESPO_NFC, campos.get("RESULT"));
                replyIntent.putExtra(ID_NFC, campos.get("FECHA"));
                startActivity(replyIntent);
            }

            Button boton = findViewById(R.id.button_responder);
            boton.setVisibility(View.VISIBLE);
        }
    }

    private Map<String,String> procesarMensaje(String inMessage) {
        String[] secciones = inMessage.split(";");
        Map<String,String> campos = new HashMap<>();
        if(secciones.length == 8) {
            if(secciones[0].equals("FECHA")) {
                campos.put("FECHA", secciones[1]);
                if(secciones[2].equals("TUI")) {
                    campos.put("TUI",secciones[3]);
                    if(secciones[4].equals("PLATOS")) {
                        campos.put("PLATOS",secciones[5]);
                        if(secciones[6].equals("PRECIO")) {
                            campos.put("PRECIO",secciones[7]);
                            campos.put("RESULT","OK");
                        }
                        else {
                            campos.put("RESULT","ERROR");
                        }
                    }
                    else {
                        campos.put("RESULT","ERROR");
                    }
                }
                else {
                    campos.put("RESULT","ERROR");
                }
            }
            else {
                campos.put("RESULT","ERROR");
            }
        }
        else {
            campos.put("RESULT","ERROR");
        }

        return campos;
    }

    public void responder(View view) {
        Intent replyIntent = new Intent(this, SenderActivity.class);
        replyIntent.putExtra(RESPO_NFC, campos.get("RESULT"));
        replyIntent.putExtra(ID_NFC, campos.get("FECHA"));
        startActivity(replyIntent);
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

        //
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