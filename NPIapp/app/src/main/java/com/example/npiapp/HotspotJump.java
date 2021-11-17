package com.example.npiapp;

public class HotspotJump extends Hotspot {
    private int indiceEscena;

    public HotspotJump(int x, int y, int radio, int indiceEscena, int icono) {
        super(x, y, radio, icono);
        this.indiceEscena = indiceEscena;
    }

    public int getEscena() {
        return indiceEscena;
    }
}
