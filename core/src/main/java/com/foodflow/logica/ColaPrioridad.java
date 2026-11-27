package com.foodflow.logica;

import com.foodflow.modelo.Cliente;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Cola de prioridad implementada como monticulo binario min-heap sobre un ArrayList.
 *
 * El cliente con menor valor de prioridad (1 = VIP Gold) sale primero.
 * Entre clientes de igual prioridad se respeta el orden de llegada (numero de cliente).
 *
 * @param <T> tipo de elemento, debe ser Comparable (Cliente implementa Comparable<Cliente>)
 */
public class ColaPrioridad<T extends Comparable<T>> implements Iterable<T> {
    private final ArrayList<T> heap = new ArrayList<>();

    /** Inserta un cliente en el monticulo y lo burbujea hasta su posicion correcta. */
    public void offer(T elemento) {
        heap.add(elemento);
        subir(heap.size() - 1);
    }

    /** Extrae y devuelve el cliente de mayor prioridad (raiz del min-heap). Devuelve null si esta vacia. */
    public T poll() {
        if (heap.isEmpty()) return null;
        T raiz = heap.get(0);
        int ultimo = heap.size() - 1;
        heap.set(0, heap.get(ultimo));
        heap.remove(ultimo);
        if (!heap.isEmpty()) bajar(0);
        return raiz;
    }

    /** Devuelve el cliente de mayor prioridad sin eliminarlo. */
    public T peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    public boolean isEmpty() { return heap.isEmpty(); }
    public int size() { return heap.size(); }
    public void clear() { heap.clear(); }

    /** Elimina una instancia concreta de cliente (usado cuando un cliente se cansa de esperar). */
    public boolean remover(T elemento) {
        int idx = heap.indexOf(elemento);
        if (idx < 0) return false;
        int ultimo = heap.size() - 1;
        heap.set(idx, heap.get(ultimo));
        heap.remove(ultimo);
        if (idx < heap.size()) {
            subir(idx);
            bajar(idx);
        }
        return true;
    }

    /** Devuelve una copia de los elementos en orden de prioridad (para dibujar sin romper el monticulo). */
    public List<T> aLista() {
        List<T> copia = new ArrayList<>(heap);
        copia.sort(null);
        return copia;
    }

    private void subir(int i) {
        while (i > 0) {
            int p = (i - 1) / 2;
            if (heap.get(i).compareTo(heap.get(p)) < 0) {
                T tmp = heap.get(i);
                heap.set(i, heap.get(p));
                heap.set(p, tmp);
                i = p;
            } else {
                break;
            }
        }
    }

    private void bajar(int i) {
        int n = heap.size();
        while (true) {
            int min = i;
            int izq = 2 * i + 1;
            int der = 2 * i + 2;
            if (izq < n && heap.get(izq).compareTo(heap.get(min)) < 0) min = izq;
            if (der < n && heap.get(der).compareTo(heap.get(min)) < 0) min = der;
            if (min == i) break;
            T tmp = heap.get(i);
            heap.set(i, heap.get(min));
            heap.set(min, tmp);
            i = min;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return aLista().iterator();
    }
}
