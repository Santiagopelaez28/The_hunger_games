package com.foodflow.modelo;

/**
 * Representa una mejora que el jugador puede comprar entre dos dias.
 * La clase solo guarda los datos de la mejora; Mundo aplica sus efectos.
 */
public final class Mejora {
    public enum Tipo { COSTOSA, EQUILIBRADA }

    private final String id;
    private final String nombre;
    private final String descripcion;
    private final int costo;
    private final Tipo tipo;

    public Mejora(String id, String nombre, String descripcion, int costo, Tipo tipo) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("La mejora necesita un id");
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("La mejora necesita un nombre");
        if (costo <= 0) throw new IllegalArgumentException("El costo debe ser positivo");
        if (tipo == null) throw new IllegalArgumentException("La mejora necesita un tipo");

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.costo = costo;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getCosto() { return costo; }
    public Tipo getTipo() { return tipo; }
}
