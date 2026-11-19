package com.foodflow.modelo;

/** Tipo de plato que pide un cliente; define como se dibuja y como se arma. */
public enum TipoPlato {
    HAMBURGUESA("Hamburguesa", "BURGER"),
    PIZZA("Pizza", "PIZZA"),
    HOTDOG("Hotdog", "HOTDOG");

    private final String etiqueta;
    private final String etiquetaCorta;

    TipoPlato(String etiqueta, String etiquetaCorta) {
        this.etiqueta = etiqueta;
        this.etiquetaCorta = etiquetaCorta;
    }

    public String getEtiqueta() { return etiqueta; }
    public String getEtiquetaCorta() { return etiquetaCorta; }
}
