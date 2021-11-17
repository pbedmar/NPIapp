package com.example.npiapp;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;

public class OutcomingNfcManager implements NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback {

    // Tipo de la etiqueta a enviar
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private NfcActivity activity;

    public OutcomingNfcManager(NfcActivity activity) {
        this.activity = activity;
    }

    /**
     * Método para crear la etiqueta NFC
     * @param event
     * @return
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String outString = activity.getOutcomingMessage();
        byte[] outBytes = outString.getBytes();
        NdefRecord outRecord = NdefRecord.createMime(MIME_TEXT_PLAIN, outBytes);

        return new NdefMessage(outRecord);
    }

    /**
     * Método que se ejecuta al completar el envío
     * @param event
     */
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        activity.signalResult();
    }


    /*
     * Callback to be implemented by a Sender activity
     * */
    public interface NfcActivity {
        String getOutcomingMessage();
        void signalResult();
    }
}
