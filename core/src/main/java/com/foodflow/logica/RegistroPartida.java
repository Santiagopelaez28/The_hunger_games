package com.foodflow.logica;

/**
 * Registro de una partida finalizada. Guarda las estadísticas del día
 * en que terminó el juego y su puntaje calculado.
 *
 * puntaje = 100 × entregados + 300 × (día − 1)
 */
public class RegistroPartida implements Comparable<RegistroPartida> {
    public final int dia;
    public final int entregados;
    public final int perdidos;
    public final int monedas;
    public final int puntaje;

    public RegistroPartida(int dia, int entregados, int perdidos, int monedas) {
        this.dia = dia;
        this.entregados = entregados;
        this.perdidos = perdidos;
        this.monedas = monedas;
        this.puntaje = calcularPuntaje(dia, entregados);
    }

    private static int calcularPuntaje(int dia, int entregados) {
        return 100 * entregados + 300 * (dia - 1);
    }

    /**
     * Orden descendente por puntaje (mayor primero).
     * Si hay empate en puntaje, gana el que llegó a un día mayor.
     */
    @Override
    public int compareTo(RegistroPartida otro) {
        if (this.puntaje != otro.puntaje) {
            return Integer.compare(otro.puntaje, this.puntaje); // mayor puntaje primero
        }
        return Integer.compare(otro.dia, this.dia); // día mayor primero (desempate)
    }

    @Override
    public String toString() {
        return "Día " + dia + " | Entregados: " + entregados + " | Perdidos: " + perdidos
                + " | Monedas: $" + monedas + " | Puntaje: " + puntaje;
    }
}
