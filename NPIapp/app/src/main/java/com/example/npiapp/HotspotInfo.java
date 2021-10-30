package com.example.npiapp;

import android.graphics.Point;

public class HotspotInfo extends Hotspot{
    private String title, descrip;

    public HotspotInfo(int x, int y, int radio, String title, String descrip, int icono) {
        super(x, y, radio, icono);
        this.title = title;
        this.descrip = descrip;
    }

    public String getTitle() { return title; }

    public String getDescrip() { return descrip; }
}
