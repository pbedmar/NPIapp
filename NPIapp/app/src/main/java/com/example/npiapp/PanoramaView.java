package com.example.npiapp;


import static android.graphics.Bitmap.createBitmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Math;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PanoramaView extends View {

    private ArrayList<Integer> ruta;

    private int posRuta;

    // Lista que contiene las escenas
    private ArrayList<Escena> listaEscenas;

    // Indice que indica la escena que se está mostrando
    private int indiceEscena;

    // Mapa de bits de la escena mostrada
    private Bitmap mapa;

    // Proporcion alto por ancho de la pantalla
    private float proporcion;

    // Resolución en píxeles de la pantalla a usar
    private final int pixelX = 1080;
    private final int pixelY = 2148;


    /*********************/
    /*    Dimensiones    */
    /*********************/

    // Dimensiones de la imagen
    private int widthImagen;
    private int heightImagen;

    // Dimensiones de la pantalla
    private int heightScreen;
    private int widthScreen;

    // Dimensiones de la imagen escala por el zoom
    private int widthScale;
    private int heightScale;

    // Escalas a aplicar a las dimensiones para adaptar el Bitmap a la pantalla
    private float scaleX;
    private float scaleY;

    private final int maxPantalla = 2400;


    // Matriz para guardar los valores del sensor de rotación del vector
    private Matrix matrix;

    private float desplaPorGrado;
    private float desplaPorGradoVertical;

    private float orientacionNorma;

    private float inclinacionNorma;

    private Boolean hayHotSpot;


    /***********************/
    /*    Constructores    */

    /***********************/

    public PanoramaView(Context context) {
        super(context);
        crearRutaPrueba();
    }

    public PanoramaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        crearRutaPrueba();
    }

    public PanoramaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        crearRutaPrueba();
    }

    // Inicialización de las variables
    public void initialize(ArrayList<Integer> ruta) {
        Log.i("Inicio", "pixelX " + getResources().getDisplayMetrics().widthPixels + "pixelY " + getResources().getDisplayMetrics().heightPixels);

        // Se selecciona la primera escena de la lista
        posRuta = 0;
        this.ruta = ruta;
        indiceEscena = ruta.get(posRuta);

        // Calcular la proporción de tamaño entre el alto y el ancho de la pantalla
        proporcion = (float) pixelX / pixelY;

        // Fijamos la orientación y la inclinación inicial
        orientacionNorma = 0;
        inclinacionNorma = 0;
    }

    public void initialize(PanoramaView p) {
        this.ruta = p.ruta;
        this.posRuta = p.posRuta;
        this.indiceEscena = p.indiceEscena;
        this.mapa = p.mapa;
        this.proporcion = p.proporcion;

        this.widthImagen = p.widthImagen;
        this.heightImagen = p.heightImagen;

        this.heightScreen = p.heightScreen;
        this.widthScreen = p.widthScreen;

        this.widthScale = p.widthScale;
        this.heightScale = p.heightScale;

        this.scaleX = p.scaleX;
        this.scaleY = p.scaleY;

        this.matrix = p.matrix;

        this.desplaPorGrado = p.desplaPorGrado;
        this.desplaPorGradoVertical = p.desplaPorGradoVertical;

        this.orientacionNorma = p.orientacionNorma;

        this.inclinacionNorma = p.inclinacionNorma;

        this.hayHotSpot = p.hayHotSpot;
    }

    // Método que realiza el dibujo de la imagen, este método es llamado siempre que se
    // invoca al método invalidate(). Preguntar al profesor si dentro del onDraw es necesario
    // poner el invalidate() o se puede omitir
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.i("INFO", new String("-- X: " + listaEscenas.get(indiceEscena).X + " Y: " + listaEscenas.get(indiceEscena).Y));
        Log.i("INFO", new String("width: " + widthScale));
        Log.i("INFO", new String("widthMapa: " + mapa.getWidth()));

        // Normalizar el valor de X
        listaEscenas.get(indiceEscena).X = (listaEscenas.get(indiceEscena).X + mapa.getWidth()) % mapa.getWidth();

        // Crear el mapa de bits de la región a mostrar
        Bitmap region = createBitmap(mapa, listaEscenas.get(indiceEscena).X, listaEscenas.get(indiceEscena).Y, widthScale, heightScale, matrix, true);

        // Pintar la región
        canvas.drawBitmap(region, 0, 0, null);

        // Redibuja el lienzo
        invalidate();
    }

    public Integer getPosRuta() { return posRuta; }

    public Integer getLenRuta() { return ruta.size(); }

    public void cargarImagen() {
        // Crear mapa de bits a partir de la imagen
        Bitmap mapaImagen = BitmapFactory.decodeResource(getResources(), listaEscenas.get(indiceEscena).imagen360);

        // Hacemos que la imagen sea mutable
        Bitmap mapaImagenMutable = mapaImagen.copy(Bitmap.Config.ARGB_8888, true);

        // Creo el canvas para pintar los hotspots dentro de la imagen
        Canvas pintarPin = new Canvas(mapaImagenMutable);
        int siguientePos = posRuta + 1;
        if (siguientePos != ruta.size()) {
            // Se indica que hay hotspots de salto dibujados
            hayHotSpot = true;

            // Obtenemos la siguiente escena
            int siguienteEscena = ruta.get(siguientePos);

            // Hago bitmap del hotspot de salto
            Bitmap puntoInf = BitmapFactory.decodeResource(getResources(), listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).icono);

            // Lo transformo en mutable
            Bitmap puntoInfMutable = puntoInf.copy(Bitmap.Config.ARGB_8888, true);

            // Lo reescalo para que encaje mejor en la escena
            // Establezco el sitio en el que se va a dibujar
            Rect dstRectorForRender = new Rect(listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).x - 300 / 2,
                    listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).y - 300 / 2,
                    listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).x + 300 / 2,
                    listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).y + 300 / 2);
            Bitmap puntoInfRescale = Bitmap.createScaledBitmap(puntoInfMutable, 300, 300, true);

            // Pintamos el hotspot
            pintarPin.drawBitmap(puntoInfMutable, null, dstRectorForRender, null);
        } else {
            // Se indica que no hay hotspots de salto dibujados
            hayHotSpot = false;
        }

        for (int i = 0; i < listaEscenas.get(indiceEscena).listaHotspotInfo.size(); ++i) {
            // Hago bitmap del hotspot de informacion
            Bitmap puntoInf = BitmapFactory.decodeResource(getResources(), listaEscenas.get(indiceEscena).listaHotspotInfo.get(i).icono);

            // Lo transformo en mutable
            Bitmap puntoInfMutable = puntoInf.copy(Bitmap.Config.ARGB_8888, true);

            // Lo reescalo para que encaje mejor en la escena
            // Establezco el sitio en el que se va a dibujar
            Rect dstRectorForRender = new Rect(listaEscenas.get(indiceEscena).listaHotspotInfo.get(i).x - 300 / 2,
                    listaEscenas.get(indiceEscena).listaHotspotInfo.get(i).y - 300 / 2,
                    listaEscenas.get(indiceEscena).listaHotspotInfo.get(i).x + 300 / 2,
                    listaEscenas.get(indiceEscena).listaHotspotInfo.get(i).y + 300 / 2);
            Bitmap puntoInfRescale = Bitmap.createScaledBitmap(puntoInfMutable, 300, 300, true);

            // Pintamos el hotspot
            pintarPin.drawBitmap(puntoInfMutable, null, dstRectorForRender, null);
        }

        // Actualizar valores de las dimensiones
        widthImagen = mapaImagen.getWidth();
        heightImagen = mapaImagen.getHeight();

        Log.i("INFO", new String("widthImg: " + widthImagen + " heightImg: " + heightImagen));

        heightScreen = heightImagen;
        widthScreen = (int) (heightScreen * proporcion);

        widthScale = (int) (widthScreen * listaEscenas.get(indiceEscena).zoom);
        heightScale = (int) (heightScreen * listaEscenas.get(indiceEscena).zoom);

        scaleX = ((float) pixelX) / widthScale;
        scaleY = ((float) pixelY) / heightScale;

        // Se crea la matriz para aplicar las escalas
        matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);

        // Se calcula cuantos pixeles hay por grado
        desplaPorGrado = widthImagen / 360f;
        int alturaIni = (int) (heightImagen - heightScale) / 2;
        desplaPorGradoVertical = alturaIni / 45f;

        // Generamos el mapa con la extensión
        mapa = createBitmap(2 * widthImagen, heightImagen, Bitmap.Config.ARGB_8888);
        Canvas auxCanvas = new Canvas(mapa);
        auxCanvas.drawBitmap(mapaImagenMutable, 0, 0, null);
        // Se añade la extensión de la imagen
        auxCanvas.drawBitmap(mapaImagenMutable, widthImagen, 0, null);

        // Fijar el valor de X de la esquina superior izquierda de la región a mostrar
        listaEscenas.get(indiceEscena).X = (int) (listaEscenas.get(indiceEscena).norte + orientacionNorma * desplaPorGrado - widthScale / 2);

        // Fijar el valor de Y de la esquina superior izquierda de la región a mostrar
        listaEscenas.get(indiceEscena).Y = (int) (alturaIni + inclinacionNorma * desplaPorGradoVertical);
    }

    public String getTituloEscena() {
        return listaEscenas.get(indiceEscena).titulo;
    }

    // Método para modificar la orientación de la región visible de la imagen
    public void aplicarOrientacion(float orientacion) {
        // Normalizamos los grados, ya que originalmente están en el rango [-180, 180]
        orientacionNorma = (orientacion + 360) % 360;

        // El norte de una imagen es siempre el valor cero de la orientación
        // Calculamos el valor de X con la nueva orientación
        listaEscenas.get(indiceEscena).X = (int) (listaEscenas.get(indiceEscena).norte + orientacionNorma * desplaPorGrado - widthScale / 2);
    }

    // Método para modificar la inclinación de la región visible de la imagen
    public void aplicarInclinacion(float inclinacion) {
        inclinacionNorma = inclinacion;
        int alturaIni = (int) (heightImagen - heightScale) / 2;
        Log.i("INFO_Inclinacion", new String("Inclinacion: " + inclinacion + " alturaIni: " + alturaIni));

        // Calculamos el valor de Y con la nueva inclinación
        if (alturaIni + inclinacionNorma * desplaPorGradoVertical < 0) {
            listaEscenas.get(indiceEscena).Y = 0;
        } else {
            if (alturaIni + inclinacionNorma * desplaPorGradoVertical > heightImagen - heightScale) {
                listaEscenas.get(indiceEscena).Y = heightImagen - heightScale;
            } else {
                listaEscenas.get(indiceEscena).Y = (int) (alturaIni + inclinacionNorma * desplaPorGradoVertical);
            }
        }
    }

    public Hotspot pulsacion(float x, float y) {
        float factorX = (float) widthScale / pixelX;
        float factorY = (float) heightScale / pixelY;
        float X = (float) (x * factorX + listaEscenas.get(indiceEscena).X) % widthImagen;
        float Y = (float) ((y - (maxPantalla - pixelY)) * factorY + listaEscenas.get(indiceEscena).Y);
        float distancia;

        if (hayHotSpot) {
            Log.i("INFO_touch", new String("coordenadas esquina superior: " + ((listaEscenas.get(indiceEscena).X)) + " , " + (listaEscenas.get(indiceEscena).Y)));
            Log.i("INFO_touch", new String("coordenadas con operacion: " + X + " , " + Y));
            Log.i("INFO_touch", new String("coordenadas parametro: " + x + " , " + y));
            Log.i("INFO_touch", new String("factorX: " + factorX + " factorY " + factorY));

            int siguientePos = posRuta + 1;
            int siguienteEscena = ruta.get(siguientePos);

            distancia = (float) Math.sqrt(Math.pow(listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).x - X, 2) + Math.pow(listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).y - Y, 2));
            if (listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).radio > distancia) {
                return listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena);
            }
        }

        for (int i = 0; i < listaEscenas.get(indiceEscena).listaHotspotInfo.size(); ++i) {
            distancia = (float) Math.sqrt(Math.pow(listaEscenas.get(indiceEscena).listaHotspotInfo.get(i).x - X, 2) + Math.pow(listaEscenas.get(indiceEscena).listaHotspotInfo.get(i).y - Y, 2));
            if (listaEscenas.get(indiceEscena).listaHotspotInfo.get(i).radio > distancia) {
                HotspotInfo auxI = listaEscenas.get(indiceEscena).listaHotspotInfo.get(i);
                return auxI;
            }
        }

        return null;
    }

    public void cambiarEscena(HotspotJump hotspot) {
        indiceEscena = hotspot.getEscena();
        posRuta += 1;
        cargarImagen();
    }

    public void retrocederEscena() {
        if(posRuta > 0) {
            posRuta -= 1;
            indiceEscena = ruta.get(posRuta);
            cargarImagen();
        }
    }

    public int fijarFlecha() {
        if (hayHotSpot) {
            int siguientePos = posRuta + 1;
            int siguienteEscena = ruta.get(siguientePos);
            int posHotSpotX = listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).x;
            int posHotSpotY = listaEscenas.get(indiceEscena).listaHotspotJump.get(siguienteEscena).y;

            int dist_der = posHotSpotX - listaEscenas.get(indiceEscena).X;
            int dist_izq = listaEscenas.get(indiceEscena).X - posHotSpotX + widthScale;

            if (dist_der < 0)
                dist_der += widthImagen;
            else
                dist_izq += widthImagen;


            if (listaEscenas.get(indiceEscena).Y > posHotSpotY) {
                if (dist_der < widthScale || dist_izq < widthScale) {
                    return PanoramaController.Direcciones.FLECHA_ARRIBA;
                }

                if (dist_der <= dist_izq)
                    return PanoramaController.Direcciones.FLECHA_ARRIBA_DER;
                else
                    return PanoramaController.Direcciones.FLECHA_ARRIBA_IZQ;
            } else if (listaEscenas.get(indiceEscena).Y + heightScale < posHotSpotY) {
                if (dist_der < widthScale || dist_izq < widthScale) {
                    return PanoramaController.Direcciones.FLECHA_ABAJO;
                }

                if (dist_der <= dist_izq)
                    return PanoramaController.Direcciones.FLECHA_ABAJO_DER;
                else
                    return PanoramaController.Direcciones.FLECHA_ABAJO_IZQ;
            } else {
                if (dist_der < widthScale || dist_izq < widthScale) {
                    return PanoramaController.Direcciones.SIN_DIRE;
                }

                if (dist_der <= dist_izq)
                    return PanoramaController.Direcciones.FLECHA_DER;
                else
                    return PanoramaController.Direcciones.FLECHA_IZQ;
            }
        } else {
            return PanoramaController.Direcciones.SIN_DIRE;
        }

    }

    public float getZoom() {
        return listaEscenas.get(indiceEscena).zoom;
    }

    public void aplicarZoom(float scaleFactor) {
        listaEscenas.get(indiceEscena).zoom = scaleFactor;

        Log.i("INFO_Zoom", new String(" ZOOM: " + listaEscenas.get(indiceEscena).zoom));

        widthScale = (int) (widthScreen * listaEscenas.get(indiceEscena).zoom);
        heightScale = (int) (heightScreen * listaEscenas.get(indiceEscena).zoom);

        int alturaIni = (int) (heightImagen - heightScale) / 2;
        desplaPorGradoVertical = alturaIni / 45f;

        scaleX = ((float) pixelX) / widthScale;
        scaleY = ((float) pixelY) / heightScale;

        matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);

        listaEscenas.get(indiceEscena).X = (int) (listaEscenas.get(indiceEscena).norte + orientacionNorma * desplaPorGrado - widthScale / 2);
        if (alturaIni + inclinacionNorma * desplaPorGradoVertical < 0) {
            listaEscenas.get(indiceEscena).Y = 0;
        } else {
            if (alturaIni + inclinacionNorma * desplaPorGradoVertical > heightImagen - heightScale) {
                listaEscenas.get(indiceEscena).Y = heightImagen - heightScale;
            } else {
                listaEscenas.get(indiceEscena).Y = (int) (alturaIni + inclinacionNorma * desplaPorGradoVertical);
            }
        }
    }

    // Método para generar las rutas de prueba a usar en la APP
    private void crearRutaPrueba() {
        listaEscenas = new ArrayList<Escena>();
        /*
        Map<Integer, HotspotJump> listaHotspotsJ = new HashMap<>();
        Map<Integer, HotspotJump> listaHotspotsJ2 = new HashMap<>();
        ArrayList<HotspotInfo> listaHotspotsI = new ArrayList<>();
        ArrayList<HotspotInfo> listaHotspotsI2 = new ArrayList<>();
        HotspotJump h1 = new HotspotJump(1000, 500, 100, 1, R.drawable.pin);
        HotspotJump h2 = new HotspotJump(1000, 1000, 100, 0, R.drawable.pin);
        String title = "Info de prueba";
        String descrip = "Esta sería la descripción";
        HotspotInfo h3 = new HotspotInfo(2000, 500, 100, title, descrip, R.drawable.info);
        listaHotspotsJ.put(1, h1);
        listaHotspotsJ2.put(0, h2);
        listaHotspotsI.add(h3);
        */
        /*
        Escena escena1 = new Escena(R.drawable.escenario2, "Entrada ETSIIT", 0.8f, 2000, listaHotspotsJ, listaHotspotsI);
        Escena escena2 = new Escena(R.drawable.imagen1, "Despacho Marcelino", 0.8f, 2000, listaHotspotsJ2, listaHotspotsI2);
        */

        String title;
        String descrip;

        /*  Escenas  */
        Map<Integer, HotspotJump> listaHotspotsJ1 = new HashMap<>();
        HotspotJump h1_1 = new HotspotJump(3380, 2000, 100, 1, R.drawable.pin);
        listaHotspotsJ1.put(1, h1_1);
        ArrayList<HotspotInfo> listaHotspotsI1 = new ArrayList<>();
        title = "Servicios UGR";
        descrip = "Al entrar a mano al fondo de la sala encontrarás la Secretaría, donde puedes" +
                  " realizar trámites administrativos. Además al entrar a mano derecha encontrarás" +
                  " una escaleras que se llevarán a la biblioteca (primera planta), y a los despachos" +
                  " de los profesores (segunda planta).";
        HotspotInfo h1_2 = new HotspotInfo(2206, 1900, 100, title, descrip, R.drawable.info);
        listaHotspotsI1.add(h1_2);
        Escena escena1 = new Escena(R.drawable.imagen1, "Entrada ETSIIT", 0.8f, 2800, listaHotspotsJ1, listaHotspotsI1);
        /* ----------------------------- */
        Map<Integer, HotspotJump> listaHotspotsJ2 = new HashMap<>();
        HotspotJump h2_1 = new HotspotJump(4150, 1900, 100, 2, R.drawable.pin);
        listaHotspotsJ2.put(2, h2_1);
        ArrayList<HotspotInfo> listaHotspotsI2 = new ArrayList<>();
        title = "Zona de descanso";
        descrip = "En esta zona encontrarás bancos donde descansar o realizar tareas. Esta zona dispone" +
                  " de enchufes, una máquina expendedora, y de un futbolín.";
        HotspotInfo h2_2 = new HotspotInfo(1656, 2050, 100, title, descrip, R.drawable.info);
        listaHotspotsI2.add(h2_2);
        title = "Servicios UGR";
        descrip = "Al bajar las escaleras a mano derecha puedes encontrar servicios de la universidad" +
                  " como la copistería, el comedor, o la cafetería.";
        HotspotInfo h2_3 = new HotspotInfo(2986, 2150, 100, title, descrip, R.drawable.info);
        listaHotspotsI2.add(h2_3);
        Escena escena2 = new Escena(R.drawable.imagen2, "Zona Descanso", 0.8f, 3750, listaHotspotsJ2, listaHotspotsI2);
        /* ----------------------------- */
        Map<Integer, HotspotJump> listaHotspotsJ3 = new HashMap<>();
        HotspotJump h3_1 = new HotspotJump(2170, 2000, 100, 3, R.drawable.pin);
        listaHotspotsJ3.put(3, h3_1);
        ArrayList<HotspotInfo> listaHotspotsI3 = new ArrayList<>();
        Escena escena3 = new Escena(R.drawable.imagen3, "Pasillo central", 0.8f, 1700, listaHotspotsJ3, listaHotspotsI3);
        /* ----------------------------- */
        Map<Integer, HotspotJump> listaHotspotsJ4 = new HashMap<>();
        HotspotJump h4 = new HotspotJump(4150, 2000, 100, 4, R.drawable.pin);
        listaHotspotsJ4.put(4, h4);
        ArrayList<HotspotInfo> listaHotspotsI4 = new ArrayList<>();
        Escena escena4 = new Escena(R.drawable.imagen4, "Entrada Edf. Aulas", 0.8f, 3400, listaHotspotsJ4, listaHotspotsI4);
        /* ----------------------------- */
        Map<Integer, HotspotJump> listaHotspotsJ5 = new HashMap<>();
        HotspotJump h5 = new HotspotJump(2100, 2000, 100, 5, R.drawable.pin);
        listaHotspotsJ5.put(5, h5);
        ArrayList<HotspotInfo> listaHotspotsI5 = new ArrayList<>();
        Escena escena5 = new Escena(R.drawable.imagen5, "Planta Cero", 0.8f, 7000, listaHotspotsJ5, listaHotspotsI5);
        /* ----------------------------- */
        Map<Integer, HotspotJump> listaHotspotsJ6 = new HashMap<>();
        HotspotJump h6 = new HotspotJump(4300, 1900, 100, 6, R.drawable.pin);
        listaHotspotsJ6.put(6, h6);
        ArrayList<HotspotInfo> listaHotspotsI6 = new ArrayList<>();
        Escena escena6 = new Escena(R.drawable.imagen6, "Inicio Escaleras", 0.8f, 2850, listaHotspotsJ6, listaHotspotsI6);
        /* ----------------------------- */
        Map<Integer, HotspotJump> listaHotspotsJ7 = new HashMap<>();
        HotspotJump h7 = new HotspotJump(4100, 1950, 100, 7, R.drawable.pin);
        listaHotspotsJ7.put(7, h7);
        ArrayList<HotspotInfo> listaHotspotsI7 = new ArrayList<>();
        Escena escena7 = new Escena(R.drawable.imagen7, "Fin Escaleras", 0.8f, 5530, listaHotspotsJ7, listaHotspotsI7);
        /* ----------------------------- */
        Map<Integer, HotspotJump> listaHotspotsJ8 = new HashMap<>();
        HotspotJump h8 = new HotspotJump(1600, 2000, 100, 8, R.drawable.pin);
        listaHotspotsJ8.put(8, h8);
        ArrayList<HotspotInfo> listaHotspotsI8 = new ArrayList<>();
        Escena escena8 = new Escena(R.drawable.imagen8, "Primera Planta", 0.8f, 3660, listaHotspotsJ8, listaHotspotsI8);
        /* ----------------------------- */
        Map<Integer, HotspotJump> listaHotspotsJ9 = new HashMap<>();
        ArrayList<HotspotInfo> listaHotspotsI9 = new ArrayList<>();
        title = "Destino";
        descrip = "Este es tu destino, el aula 1.5.";
        HotspotInfo h9_1 = new HotspotInfo(4160, 1900, 100, title, descrip, R.drawable.info);
        listaHotspotsI9.add(h9_1);
        Escena escena9 = new Escena(R.drawable.imagen9, "Entrada Aula 1.5", 0.8f, 3800, listaHotspotsJ9, listaHotspotsI9);

        listaEscenas.add(escena1);
        listaEscenas.add(escena2);
        listaEscenas.add(escena3);
        listaEscenas.add(escena4);
        listaEscenas.add(escena5);
        listaEscenas.add(escena6);
        listaEscenas.add(escena7);
        listaEscenas.add(escena8);
        listaEscenas.add(escena9);
    }
}





