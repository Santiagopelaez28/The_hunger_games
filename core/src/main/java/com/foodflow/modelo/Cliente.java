package com.foodflow.modelo;

import java.util.List;

/** Cliente que espera en la cola. La receta se arma en el orden en que aparece en la lista. */
public class Cliente implements Comparable<Cliente> {
    private final int numero;
    private final String nombreReceta;
    private final TipoPlato tipoPlato;
    private final List<Ingrediente> receta;
    private final Aspecto aspecto;
    private final float pacienciaMax;
    private final boolean vip;
    private float restante;

    public Cliente(int numero, Receta receta, float paciencia, Aspecto aspecto) {
        this(numero, receta, paciencia, aspecto, false);
    }

    /** Constructor con soporte para clientes VIP (cola de prioridad). */
    public Cliente(int numero, Receta receta, float paciencia, Aspecto aspecto, boolean vip) {
        this.numero = numero;
        this.nombreReceta = receta.getNombre();
        this.tipoPlato = receta.getTipo();
        this.receta = receta.getIngredientes();
        this.aspecto = aspecto;
        this.pacienciaMax = paciencia;
        this.restante = paciencia;
        this.vip = vip;
    }

    public int getNumero() { return numero; }
    public String getNombreReceta() { return nombreReceta; }
    public TipoPlato getTipoPlato() { return tipoPlato; }
    public List<Ingrediente> getReceta() { return receta; }
    public Aspecto getAspecto() { return aspecto; }
    public boolean isVip() { return vip; }

    public void esperar(float dt) { restante -= dt; }
    public boolean agotado() { return restante <= 0; }
    /** 1 = recién llegó, 0 = se va. */
    public float fraccionPaciencia() { return Math.max(0f, restante / pacienciaMax); }

    public int pagoBase() {
        int total = 0;
        for (Ingrediente i : receta) total += i.getValor();
        return vip ? total + 5 : total;
    }

    /** Prioriza clientes VIP; entre clientes de igual tipo, respeta el orden de llegada. */
    @Override
    public int compareTo(Cliente otro) {
        if (this.vip != otro.vip) {
            return this.vip ? -1 : 1;
        }
        return Integer.compare(this.numero, otro.numero);
    }
}
