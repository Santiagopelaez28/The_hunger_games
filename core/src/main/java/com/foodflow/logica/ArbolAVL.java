package com.foodflow.logica;

import java.util.ArrayList;
import java.util.List;

/**
 * Árbol AVL que almacena los registros de partidas finalizadas, ordenados por
 * puntaje descendente (el mayor primero). Se utiliza para mantener el Top 5
 * de las mejores partidas jugadas.
 *
 * <p>Clave de orden: puntaje (mayor primero). Desempate: día alcanzado (mayor primero).</p>
 */
public class ArbolAVL {
    private NodoAVL raiz;

    private static class NodoAVL {
        RegistroPartida dato;
        NodoAVL izquierda;
        NodoAVL derecha;
        int altura;

        NodoAVL(RegistroPartida dato) {
            this.dato = dato;
            this.altura = 1;
        }
    }

    /** Inserta un registro en el árbol manteniendo el balance AVL. */
    public void insertar(RegistroPartida registro) {
        raiz = insertar(raiz, registro);
    }

    private NodoAVL insertar(NodoAVL nodo, RegistroPartida registro) {
        if (nodo == null) {
            return new NodoAVL(registro);
        }

        int cmp = registro.compareTo(nodo.dato);
        if (cmp < 0) {
            nodo.izquierda = insertar(nodo.izquierda, registro);
        } else if (cmp > 0) {
            nodo.derecha = insertar(nodo.derecha, registro);
        } else {
            // Duplicado exacto: no insertar dos veces el mismo registro
            return nodo;
        }

        nodo.altura = 1 + Math.max(altura(nodo.izquierda), altura(nodo.derecha));

        int balance = balance(nodo);

        // Caso izquierda-izquierda
        if (balance > 1 && registro.compareTo(nodo.izquierda.dato) < 0) {
            return rotarDerecha(nodo);
        }
        // Caso derecha-derecha
        if (balance < -1 && registro.compareTo(nodo.derecha.dato) > 0) {
            return rotarIzquierda(nodo);
        }
        // Caso izquierda-derecha
        if (balance > 1 && registro.compareTo(nodo.izquierda.dato) > 0) {
            nodo.izquierda = rotarIzquierda(nodo.izquierda);
            return rotarDerecha(nodo);
        }
        // Caso derecha-izquierda
        if (balance < -1 && registro.compareTo(nodo.derecha.dato) < 0) {
            nodo.derecha = rotarDerecha(nodo.derecha);
            return rotarIzquierda(nodo);
        }

        return nodo;
    }

    private NodoAVL rotarDerecha(NodoAVL y) {
        NodoAVL x = y.izquierda;
        NodoAVL T2 = x.derecha;

        x.derecha = y;
        y.izquierda = T2;

        y.altura = 1 + Math.max(altura(y.izquierda), altura(y.derecha));
        x.altura = 1 + Math.max(altura(x.izquierda), altura(x.derecha));

        return x;
    }

    private NodoAVL rotarIzquierda(NodoAVL x) {
        NodoAVL y = x.derecha;
        NodoAVL T2 = y.izquierda;

        y.izquierda = x;
        x.derecha = T2;

        x.altura = 1 + Math.max(altura(x.izquierda), altura(x.derecha));
        y.altura = 1 + Math.max(altura(y.izquierda), altura(y.derecha));

        return y;
    }

    private int altura(NodoAVL nodo) {
        return nodo == null ? 0 : nodo.altura;
    }

    private int balance(NodoAVL nodo) {
        return nodo == null ? 0 : altura(nodo.izquierda) - altura(nodo.derecha);
    }

    /**
     * Devuelve los mejores registros en orden descendente (derecha, nodo, izquierda),
     * deteniéndose al alcanzar el límite especificado.
     */
    public List<RegistroPartida> top(int limite) {
        List<RegistroPartida> resultado = new ArrayList<>();
        top(raiz, resultado, limite);
        return resultado;
    }

    private void top(NodoAVL nodo, List<RegistroPartida> resultado, int limite) {
        if (nodo == null || resultado.size() >= limite) return;
        // Recorrido izquierda-nodo-derecha: como los mayores puntajes se insertan a la izquierda,
        // este recorrido visita de mayor a menor puntaje.
        top(nodo.izquierda, resultado, limite);
        if (resultado.size() < limite) {
            resultado.add(nodo.dato);
        }
        top(nodo.derecha, resultado, limite);
    }

    public boolean estaVacio() { return raiz == null; }
    public int tamaño() { return tamaño(raiz); }

    private int tamaño(NodoAVL nodo) {
        if (nodo == null) return 0;
        return 1 + tamaño(nodo.izquierda) + tamaño(nodo.derecha);
    }

    public void limpiar() { raiz = null; }
}
