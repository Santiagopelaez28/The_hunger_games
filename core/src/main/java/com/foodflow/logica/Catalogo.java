package com.foodflow.logica;

import com.foodflow.modelo.Ingrediente;

import java.util.ArrayList;
import java.util.List;

/** Todos los ingredientes que existen en el juego. El orden de TODOS es el orden en que aparecen en la tienda. */
public final class Catalogo {
    public static final Ingrediente PAN      = new Ingrediente("Pan",       2,  0, 0xD9A441);
    public static final Ingrediente CARNE    = new Ingrediente("Carne",     5,  0, 0x7B3F27);
    public static final Ingrediente QUESO    = new Ingrediente("Queso",     3, 30, 0xF5D142);
    public static final Ingrediente LECHUGA  = new Ingrediente("Lechuga",   2, 25, 0x5DBB63);
    public static final Ingrediente TOMATE   = new Ingrediente("Tomate",    2, 25, 0xE5484D);
    public static final Ingrediente CEBOLLA  = new Ingrediente("Cebolla",   2, 35, 0xC9A0DC);
    public static final Ingrediente HUEVO    = new Ingrediente("Huevo",     3, 50, 0xF4EFE6);
    public static final Ingrediente TOCINETA = new Ingrediente("Tocineta",  4, 60, 0xB5452F);
    public static final Ingrediente PINA     = new Ingrediente("Pina",      3, 70, 0xFFA94D);
    public static final Ingrediente AGUACATE = new Ingrediente("Aguacate",  4, 80, 0x7FA650);

    public static final List<Ingrediente> TODOS = List.of(
            PAN, CARNE, QUESO, LECHUGA, TOMATE, CEBOLLA, HUEVO, TOCINETA, PINA, AGUACATE);

    private Catalogo() { }

    /** Ingredientes con los que arranca el jugador. */
    public static ArrayList<Ingrediente> iniciales() {
        ArrayList<Ingrediente> lista = new ArrayList<>();
        lista.add(PAN);
        lista.add(CARNE);
        return lista;
    }
}
