package com.foodflow.logica;

import com.foodflow.modelo.Ingrediente;

import java.util.List;

/** Todos los ingredientes que existen en el juego. El orden de TODOS es el orden en que aparecen en la tienda. */
public final class Catalogo {
    public static final Ingrediente PAN        = new Ingrediente("Pan",        2,  0, 0xD9A441);
    public static final Ingrediente CARNE      = new Ingrediente("Carne",      5,  0, 0x7B3F27);
    public static final Ingrediente QUESO      = new Ingrediente("Queso",      3, 30, 0xF5D142);
    public static final Ingrediente LECHUGA    = new Ingrediente("Lechuga",    2, 25, 0x5DBB63);
    public static final Ingrediente TOMATE     = new Ingrediente("Tomate",     2, 25, 0xE5484D);
    public static final Ingrediente MASA       = new Ingrediente("Masa",       1,  0, 0xE8C98A);
    public static final Ingrediente SALSA      = new Ingrediente("Salsa",      2,  0, 0xC0392B);
    public static final Ingrediente SALCHICHA  = new Ingrediente("Salchicha",  4,  0, 0xA8432F);
    public static final Ingrediente CEBOLLA    = new Ingrediente("Cebolla",    2, 35, 0xC9A0DC);
    public static final Ingrediente PIMIENTO   = new Ingrediente("Pimiento",   3, 45, 0xE4572E);
    public static final Ingrediente HUEVO      = new Ingrediente("Huevo",      3, 50, 0xF4EFE6);
    public static final Ingrediente CHAMPINON  = new Ingrediente("Champinon",  4, 55, 0xC7A97B);
    public static final Ingrediente TOCINETA   = new Ingrediente("Tocineta",   4, 60, 0xB5452F);
    public static final Ingrediente PEPPERONI  = new Ingrediente("Pepperoni",  4, 65, 0xB8322A);
    public static final Ingrediente PINA       = new Ingrediente("Pina",       3, 70, 0xFFA94D);
    public static final Ingrediente AGUACATE   = new Ingrediente("Aguacate",   4, 80, 0x7FA650);

    public static final List<Ingrediente> TODOS = List.of(
            PAN, CARNE, QUESO, LECHUGA, TOMATE, MASA, SALSA, SALCHICHA,
            CEBOLLA, PIMIENTO, HUEVO, CHAMPINON, TOCINETA, PEPPERONI, PINA, AGUACATE);

    private Catalogo() { }

    /** Ingredientes basicos del primer dia: alcanzan para hamburguesas, pizzas y hotdogs. */
    public static ListaDoble<Ingrediente> iniciales() {
        ListaDoble<Ingrediente> lista = new ListaDoble<>();
        lista.agregar(PAN);
        lista.agregar(CARNE);
        lista.agregar(QUESO);
        lista.agregar(LECHUGA);
        lista.agregar(TOMATE);
        lista.agregar(MASA);
        lista.agregar(SALSA);
        lista.agregar(SALCHICHA);
        return lista;
    }
}
