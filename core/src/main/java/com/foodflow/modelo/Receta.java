package com.foodflow.modelo;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/** Una receta del recetario: nombre, tipo de plato e ingredientes en el orden en que se arman. */
public final class Receta {
    private final String nombre;
    private final TipoPlato tipo;
    private final List<Ingrediente> ingredientes;

    public Receta(String nombre, TipoPlato tipo, List<Ingrediente> ingredientes) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ingredientes = Collections.unmodifiableList(new ArrayList<>(ingredientes));
    }

    public String getNombre() { return nombre; }
    public TipoPlato getTipo() { return tipo; }
    public List<Ingrediente> getIngredientes() { return ingredientes; }
}
