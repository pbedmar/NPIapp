package com.example.npiapp;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class Asistente extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistente);

        String frameVideo = "<html><body><iframe allow=\"microphone;\" width=\"350\" height=\"430\" src=\"https://console.dialogflow.com/api-client/demo/embedded/c335863e-0d0d-4d1a-a294-97fc3e84dffd\"> </iframe></body></html>";

        WebView pagina = findViewById(R.id.pagina);
        pagina.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        WebSettings webSettings = pagina.getSettings();
        webSettings.setJavaScriptEnabled(true);
        pagina.loadData(frameVideo, "text/html", "utf-8");
    }
}