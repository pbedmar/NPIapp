package com.example.npiapp;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SenderActivity extends AppCompatActivity implements OutcomingNfcManager.NfcActivity {

    private String info;
    private NfcAdapter nfcAdapter;
    private OutcomingNfcManager outcomingNfccallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        Log.d("NFC", "activity launched");

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC disabled on this device. Turn on to proceed", Toast.LENGTH_SHORT).show();
            }

            Intent intent = getIntent();
            info = intent.getStringExtra(CanteenMenu.INFO_NFC);

            // encapsulate sending logic in a separate class
            this.outcomingNfccallback = new OutcomingNfcManager(this);
            this.nfcAdapter.setOnNdefPushCompleteCallback(outcomingNfccallback, this);
            this.nfcAdapter.setNdefPushMessageCallback(outcomingNfccallback, this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public String getOutcomingMessage() {
        return info;
    }

    @Override
    public void signalResult() {
        // this will be triggered when NFC message is sent to a device.
        // should be triggered on UI thread. We specify it explicitly
        // cause onNdefPushComplete is called from the Binder thread
        runOnUiThread(() ->
                Toast.makeText(SenderActivity.this, "Beaming complete", Toast.LENGTH_SHORT).show());
    }
}