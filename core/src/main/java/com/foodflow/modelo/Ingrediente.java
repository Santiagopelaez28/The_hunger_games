package com.foodflow.modelo;

/** Ingrediente del juego. El color es RGB (0xRRGGBB) para no depender de LibGDX en la lógica. */
public final class Ingrediente {
    private final String nombre;
    private final int valor;   // cuánto aporta al pago de un pedido
    private final int costo;   // cuánto cuesta desbloquearlo en la tienda
    private final int color;

    public Ingrediente(String nombre, int valor, int costo, int color) {
        this.nombre = nombre;
        this.valor = valor;
        this.costo = costo;
        this.color = color;
    }

    public String getNombre() { return nombre; }
    public int getValor() { return valor; }
    public int getCosto() { return costo; }
    public int getColor() { return color; }

    @Override
    public String toString() { return nombre; }
}
