package com.foodflow.logica;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Lista doblemente enlazada: cada nodo guarda el dato y referencias al nodo anterior y al siguiente.
 * La despensa la usa para poder avanzar o retroceder por paginas cuando hay mas ingredientes
 * de los que caben en pantalla.
 */
public final class ListaDoble<T> implements Iterable<T> {
    /** Nodo de la lista; se expone solo para recorrerla, no para modificarla por fuera. */
    public static final class Nodo<T> {
        private final T dato;
        private Nodo<T> anterior;
        private Nodo<T> siguiente;

        private Nodo(T dato) { this.dato = dato; }

        public T getDato() { return dato; }
        public Nodo<T> getAnterior() { return anterior; }
        public Nodo<T> getSiguiente() { return siguiente; }
    }

    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int tamano;

    /** Agrega un dato al final. */
    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (primero == null) {
            primero = nuevo;
            ultimo = nuevo;
        } else {
            nuevo.anterior = ultimo;
            ultimo.siguiente = nuevo;
            ultimo = nuevo;
        }
        tamano++;
    }

    public boolean contiene(T dato) {
        for (Nodo<T> n = primero; n != null; n = n.siguiente) {
            if (Objects.equals(n.dato, dato)) return true;
        }
        return false;
    }

    public Nodo<T> getPrimero() { return primero; }
    public int getTamano() { return tamano; }
    public boolean estaVacia() { return tamano == 0; }

    /** Copia de los datos en orden; util para las reglas que trabajan con listas normales. */
    public List<T> aLista() {
        List<T> lista = new ArrayList<>(tamano);
        for (T dato : this) lista.add(dato);
        return lista;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> actual = primero;

            @Override public boolean hasNext() { return actual != null; }

            @Override public T next() {
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }
}
