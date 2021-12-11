package com.example.npiapp;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class Asistente extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistente);

        String frameVideo = "<html><body><iframe width=\"100%\" height=\"100%\" allow=\"microphone;\" src=\"https://console.dialogflow.com/api-client/demo/embedded/c335863e-0d0d-4d1a-a294-97fc3e84dffd\"></iframe></body></html>";

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