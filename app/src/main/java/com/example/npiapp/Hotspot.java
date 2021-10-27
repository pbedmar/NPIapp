package com.example.npiapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public abstract class Hotspot {
    protected int x, y, radio;
    protected int icono;

    public Hotspot(int x, int y, int radio, int icono) {
        this.x = x;
        this.y = y;
        this.radio = radio;
        this.icono = icono;
    }
}
