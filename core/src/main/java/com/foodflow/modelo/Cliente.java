package com.foodflow.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cliente que espera en la cola. La receta se lee de abajo hacia arriba (igual que la pila del plato). */
public class Cliente {
    private final int numero;
    private final List<Ingrediente> receta;
    private final Aspecto aspecto;
    private final float pacienciaMax;
    private float restante;

    public Cliente(int numero, List<Ingrediente> receta, float paciencia, Aspecto aspecto) {
        this.numero = numero;
        this.aspecto = aspecto;
        this.receta = Collections.unmodifiableList(new ArrayList<>(receta));
        this.pacienciaMax = paciencia;
        this.restante = paciencia;
    }

    public int getNumero() { return numero; }
    public List<Ingrediente> getReceta() { return receta; }
    public Aspecto getAspecto() { return aspecto; }

    public void esperar(float dt) { restante -= dt; }
    public boolean agotado() { return restante <= 0; }
    /** 1 = recién llegó, 0 = se va. */
    public float fraccionPaciencia() { return Math.max(0f, restante / pacienciaMax); }

    public int pagoBase() {
        int total = 0;
        for (Ingrediente i : receta) total += i.getValor();
        return total;
    }
}
