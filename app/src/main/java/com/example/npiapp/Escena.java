package com.example.npiapp;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Map;

public class Escena {
    public final int imagen360;
    public final Map<Integer,HotspotJump> listaHotspotJump;
    public final ArrayList<HotspotInfo> listaHotspotInfo;
    public float zoom;
    public int X, Y;
    public int norte;
    public String titulo;

    public Escena(int imagen360, String titulo, float zoom, int norte ,Map<Integer,HotspotJump> listaHotspotJump,
                  ArrayList<HotspotInfo> listaHotspotInfo) {
        this.imagen360 = imagen360;
        this.titulo = titulo;
        this.zoom = zoom;
        this.norte = norte;
        this.listaHotspotJump = listaHotspotJump;
        this.listaHotspotInfo = listaHotspotInfo;
    }
}
