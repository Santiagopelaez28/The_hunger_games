package com.foodflow.modelo;

/**
 * Nivel de prioridad del cliente en la cola.
 * 1 = VIP Gold (el mas prioritario), 2 = VIP Silver, 3 = Comun.
 * Mientras menor sea el valor de prioridad, antes se atiende al cliente.
 */
public enum NivelCliente {
    GOLD(1, "VIP GOLD", 0xF2B33D),       // dorado
    SILVER(2, "VIP SILVER", 0xBFC9D1),   // plata
    COMUN(3, "COMUN", 0xFFFFFF);         // blanco / normal

    public final int prioridad;
    public final String etiqueta;
    public final int colorGlobo;

    NivelCliente(int prioridad, String etiqueta, int colorGlobo) {
        this.prioridad = prioridad;
        this.etiqueta = etiqueta;
        this.colorGlobo = colorGlobo;
    }

    public boolean esVIP() { return this != COMUN; }
}
