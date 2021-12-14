package com.example.npiapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/*
Clase que contiene un atributo por cada columna de la tabla. Contiene también métodos para acceder
a estos atributos. Se utilizan ciertas etiquetas para indicar qué atributo corresponde con la clave
primaria (@PrimaryKey) o para aplicar restricciones (@NonNull).
*/
@Entity(tableName = "menu")
public class Menu {
    public Menu(@NonNull String date, @NonNull String first1, float price_first1, int ordered_first1,
                String allergens_first1, @NonNull String first2, float price_first2, int ordered_first2,
                String allergens_first2, @NonNull String main1, float price_main1, int ordered_main1,
                String allergens_main1, @NonNull String main2, float price_main2, int ordered_main2,
                String allergens_main2, @NonNull String dessert1, float price_dessert1, int ordered_dessert1,
                String allergens_dessert1, @NonNull String dessert2, float price_dessert2, int ordered_dessert2,
                String allergens_dessert2, int day_with_order) {
        this.date = date;
        this.first1 = first1;
        this.price_first1 = price_first1;
        this.ordered_first1 = ordered_first1;
        this.allergens_first1 = allergens_first1;
        this.first2 = first2;
        this.price_first2 = price_first2;
        this.ordered_first2 = ordered_first2;
        this.allergens_first2 = allergens_first2;
        this.main1 = main1;
        this.price_main1 = price_main1;
        this.ordered_main1 = ordered_main1;
        this.allergens_main1 = allergens_main1;
        this.main2 = main2;
        this.price_main2 = price_main2;
        this.ordered_main2 = ordered_main2;
        this.allergens_main2 = allergens_main2;
        this.dessert1 = dessert1;
        this.price_dessert1 = price_dessert1;
        this.ordered_dessert1 = ordered_dessert1;
        this.allergens_dessert1 = allergens_dessert1;
        this.dessert2 = dessert2;
        this.price_dessert2 = price_dessert2;
        this.ordered_dessert2 = ordered_dessert2;
        this.allergens_dessert2 = allergens_dessert2;
        this.day_with_order = day_with_order;
    }

    @PrimaryKey
    @NonNull
    private String date;

    @NonNull
    private String first1;
    private float price_first1;
    private int ordered_first1;
    @NonNull
    private String allergens_first1;

    @NonNull
    private String first2;
    private float price_first2;
    private int ordered_first2;
    @NonNull
    private String allergens_first2;

    @NonNull
    private String main1;
    private float price_main1;
    private int ordered_main1;
    @NonNull
    private String allergens_main1;

    @NonNull
    private String main2;
    private float price_main2;
    private int ordered_main2;
    @NonNull
    private String allergens_main2;

    @NonNull
    private String dessert1;
    private float price_dessert1;
    private int ordered_dessert1;
    @NonNull
    private String allergens_dessert1;

    @NonNull
    private String dessert2;
    private float price_dessert2;
    private int ordered_dessert2;
    @NonNull
    private String allergens_dessert2;

    private int day_with_order;


    public String[] getMealsNamesArray() {
        String[] array = {first1, first2, main1, main2, dessert1, dessert2};
        return array;
    }

    public String[] getMealsAllergensArray() {
        String[] array = {allergens_first1, allergens_first2, allergens_main1,
                allergens_main2, allergens_dessert1, allergens_dessert2};
        return array;
    }

    public float[] getMealsPricesArray() {
        float[] array = {price_first1, price_first2, price_main1, price_main2, price_dessert1, price_dessert2};
        return array;
    }

    public int[] getMealsOrderedArray() {
        int[] array = {ordered_first1, ordered_first2, ordered_main1, ordered_main2,
                ordered_dessert1, ordered_dessert2};
        return array;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    @NonNull
    public String getFirst1() {
        return first1;
    }

    public float getPrice_first1() {
        return price_first1;
    }

    public int getOrdered_first1() {
        return ordered_first1;
    }

    public String getAllergens_first1() {
        return allergens_first1;
    }

    @NonNull
    public String getFirst2() {
        return first2;
    }

    public float getPrice_first2() {
        return price_first2;
    }

    public int getOrdered_first2() {
        return ordered_first2;
    }

    public String getAllergens_first2() {
        return allergens_first2;
    }

    @NonNull
    public String getMain1() {
        return main1;
    }

    public float getPrice_main1() {
        return price_main1;
    }

    public int getOrdered_main1() {
        return ordered_main1;
    }

    public String getAllergens_main1() {
        return allergens_main1;
    }

    @NonNull
    public String getMain2() {
        return main2;
    }

    public float getPrice_main2() {
        return price_main2;
    }

    public int getOrdered_main2() {
        return ordered_main2;
    }

    public String getAllergens_main2() {
        return allergens_main2;
    }

    @NonNull
    public String getDessert1() {
        return dessert1;
    }

    public float getPrice_dessert1() {
        return price_dessert1;
    }

    public int getOrdered_dessert1() {
        return ordered_dessert1;
    }

    public String getAllergens_dessert1() {
        return allergens_dessert1;
    }

    @NonNull
    public String getDessert2() {
        return dessert2;
    }

    public float getPrice_dessert2() {
        return price_dessert2;
    }

    public int getOrdered_dessert2() {
        return ordered_dessert2;
    }

    public String getAllergens_dessert2() {
        return allergens_dessert2;
    }

    public int getDay_with_order() {
        return day_with_order;
    }
}
