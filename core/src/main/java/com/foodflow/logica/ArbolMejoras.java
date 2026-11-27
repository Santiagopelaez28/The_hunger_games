package com.foodflow.logica;

import com.foodflow.modelo.Mejora;

import java.util.ArrayList;
import java.util.List;

/**
 * Arbol binario de busqueda de mejoras, ordenado por costo.
 *
 * Cada nodo guarda una mejora. Los costos menores quedan a la izquierda y los
 * mayores a la derecha. Si dos mejoras tienen el mismo costo, se desempata por id.
 */
public final class ArbolMejoras {
    private static final class Nodo {
        private final Mejora mejora;
        private Nodo izquierdo;
        private Nodo derecho;

        private Nodo(Mejora mejora) {
            this.mejora = mejora;
        }
    }

    private Nodo raiz;
    private int cantidad;

    public ArbolMejoras() {
        insertar(new Mejora("publicidad_agresiva", "Publicidad agresiva",
                "Los clientes llegan 20% mas rapido.\nCada pedido paga 25% mas.",
                180, Mejora.Tipo.EQUILIBRADA));
        insertar(new Mejora("proveedor_economico", "Proveedor economico",
                "Los ingredientes cuestan 30% menos.\nCada pedido paga 10% menos.",
                170, Mejora.Tipo.EQUILIBRADA));
        insertar(new Mejora("cafe_clientes", "Cafe para clientes",
                "La paciencia aumenta 30%.\nLos clientes llegan 15% mas rapido.",
                190, Mejora.Tipo.EQUILIBRADA));
        insertar(new Mejora("horario_nocturno", "Horario nocturno",
                "El dia dura 25% mas.\nLa paciencia baja 15%.",
                180, Mejora.Tipo.EQUILIBRADA));
        insertar(new Mejora("menu_gourmet", "Menu gourmet",
                "Cada pedido paga 35% mas.\nLa paciencia baja 20%.",
                200, Mejora.Tipo.EQUILIBRADA));

        insertar(new Mejora("seguro_comercial", "Seguro comercial",
                "Recuperas una vida inmediatamente.\nNo supera el maximo de cinco vidas.",
                380, Mejora.Tipo.COSTOSA));
        insertar(new Mejora("atencion_vip", "Atencion VIP",
                "Los clientes tienen 25%\nmas paciencia.",
                420, Mejora.Tipo.COSTOSA));
        insertar(new Mejora("cocina_renovada", "Cocina renovada",
                "La duracion de cada dia\naumenta 20%.",
                450, Mejora.Tipo.COSTOSA));
        insertar(new Mejora("caja_registradora", "Caja registradora",
                "Cada pedido genera\n20% mas monedas.",
                450, Mejora.Tipo.COSTOSA));
        insertar(new Mejora("sala_ampliada", "Sala de espera ampliada",
                "Puedes tener dos clientes\nmas esperando en la cola.",
                500, Mejora.Tipo.COSTOSA));
    }

    /** Inserta una mejora manteniendo el orden del arbol. */
    public void insertar(Mejora mejora) {
        if (mejora == null) throw new IllegalArgumentException("No se puede insertar una mejora nula");
        raiz = insertar(raiz, mejora);
    }

    private Nodo insertar(Nodo actual, Mejora mejora) {
        if (actual == null) {
            cantidad++;
            return new Nodo(mejora);
        }

        int comparacion = comparar(mejora, actual.mejora);
        if (comparacion < 0) actual.izquierdo = insertar(actual.izquierdo, mejora);
        else if (comparacion > 0) actual.derecho = insertar(actual.derecho, mejora);
        // Un mismo id no se duplica en el catalogo.
        else if (!mejora.getId().equals(actual.mejora.getId())) {
            actual.derecho = insertar(actual.derecho, mejora);
        }
        return actual;
    }

    /** Busca una mejora por su costo exacto. Puede haber mas de una con ese costo. */
    public List<Mejora> buscarPorCosto(int costo) {
        List<Mejora> resultado = new ArrayList<>();
        buscarPorCosto(raiz, costo, resultado);
        return resultado;
    }

    private void buscarPorCosto(Nodo nodo, int costo, List<Mejora> resultado) {
        if (nodo == null) return;
        if (costo < nodo.mejora.getCosto()) {
            buscarPorCosto(nodo.izquierdo, costo, resultado);
        } else if (costo > nodo.mejora.getCosto()) {
            buscarPorCosto(nodo.derecho, costo, resultado);
        } else {
            resultado.add(nodo.mejora);
            // Los costos iguales se ordenan por id; pueden estar a ambos lados.
            buscarCostoIgual(nodo.izquierdo, costo, resultado);
            buscarCostoIgual(nodo.derecho, costo, resultado);
        }
    }

    private void buscarCostoIgual(Nodo nodo, int costo, List<Mejora> resultado) {
        if (nodo == null) return;
        if (nodo.mejora.getCosto() == costo) resultado.add(nodo.mejora);
        if (costo <= nodo.mejora.getCosto()) buscarCostoIgual(nodo.izquierdo, costo, resultado);
        if (costo >= nodo.mejora.getCosto()) buscarCostoIgual(nodo.derecho, costo, resultado);
    }

    /** Devuelve las mejoras cuyo precio esta dentro del rango, inclusive. */
    public List<Mejora> obtenerEnRango(int minimo, int maximo) {
        List<Mejora> resultado = new ArrayList<>();
        if (minimo > maximo) return resultado;
        obtenerEnRango(raiz, minimo, maximo, resultado);
        return resultado;
    }

    private void obtenerEnRango(Nodo nodo, int minimo, int maximo, List<Mejora> resultado) {
        if (nodo == null) return;
        int costo = nodo.mejora.getCosto();
        if (costo >= minimo) obtenerEnRango(nodo.izquierdo, minimo, maximo, resultado);
        if (costo >= minimo && costo <= maximo) resultado.add(nodo.mejora);
        if (costo <= maximo) obtenerEnRango(nodo.derecho, minimo, maximo, resultado);
    }

    /** Recorrido inorden: devuelve todas las mejoras ordenadas de menor a mayor costo. */
    public List<Mejora> obtenerEnOrden() {
        List<Mejora> resultado = new ArrayList<>();
        obtenerEnOrden(raiz, resultado);
        return resultado;
    }

    private void obtenerEnOrden(Nodo nodo, List<Mejora> resultado) {
        if (nodo == null) return;
        obtenerEnOrden(nodo.izquierdo, resultado);
        resultado.add(nodo.mejora);
        obtenerEnOrden(nodo.derecho, resultado);
    }

    public int getCantidad() { return cantidad; }

    private int comparar(Mejora a, Mejora b) {
        int porCosto = Integer.compare(a.getCosto(), b.getCosto());
        return porCosto != 0 ? porCosto : a.getId().compareTo(b.getId());
    }
}
