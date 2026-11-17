package com.foodflow.logica;

import com.foodflow.modelo.Aspecto;
import com.foodflow.modelo.Cliente;
import com.foodflow.modelo.Ingrediente;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

/**
 * Reglas del juego, sin nada de LibGDX (se puede probar sin abrir ventana).
 *
 *  - Queue<Cliente>        cola de llegada (FIFO). Si se llena o la paciencia se agota, el cliente se va.
 *  - Stack<Ingrediente>    plato en construcción (LIFO). deshacer() quita el último ingrediente.
 *  - ArrayList<Ingrediente> ingredientes disponibles; crece cuando el jugador compra en la tienda.
 */
public class Mundo {
    public enum Estado { JUGANDO, DIA_TERMINADO, FIN_JUEGO }
    public enum Entrega { SIN_CLIENTE, CORRECTA, INCORRECTA }

    public static final int CAPACIDAD_COLA = 5;
    public static final int MAX_PLATO = 8;
    public static final int VIDAS_INICIALES = 5;
    public static final float DURACION_DIA = 90f;

    private final Random rnd;
    private final ArrayList<Ingrediente> disponibles = Catalogo.iniciales();
    private final Queue<Cliente> cola = new LinkedList<>();
    private final Stack<Ingrediente> plato = new Stack<>();
    private Cliente enAtencion;

    private Estado estado = Estado.JUGANDO;
    private int dia = 1;
    private int monedas = 0;
    private int vidas = VIDAS_INICIALES;
    private int entregados = 0;
    private int perdidos = 0;
    private int siguienteNumero = 1;
    private float tiempoDia = DURACION_DIA;
    private float relojLlegada = 1f;   // el primer cliente llega al segundo
    private String mensaje = "";
    private float tiempoMensaje = 0f;

    public Mundo() { this(System.nanoTime()); }
    public Mundo(long semilla) { this.rnd = new Random(semilla); }

    // ---------------------------------------------------------------- avance del tiempo

    /** Avanza el juego dt segundos. Llamar una vez por frame. */
    public void actualizar(float dt) {
        if (estado != Estado.JUGANDO) return;
        if (tiempoMensaje > 0) tiempoMensaje -= dt;

        // Llegadas (solo mientras dure el día)
        if (tiempoDia > 0) {
            tiempoDia = Math.max(0f, tiempoDia - dt);
            relojLlegada -= dt;
            while (relojLlegada <= 0 && estado == Estado.JUGANDO) {
                relojLlegada += intervaloLlegada();
                recibirCliente();
            }
        }

        // Paciencia de quienes esperan en la cola (se recorre toda, por si las pacientes difieren)
        Iterator<Cliente> it = cola.iterator();
        while (it.hasNext()) {
            Cliente c = it.next();
            c.esperar(dt);
            if (c.agotado()) {
                it.remove();
                perderCliente("Un cliente se canso de esperar");
            }
        }

        // Paciencia del cliente que se está atendiendo
        if (enAtencion != null) {
            enAtencion.esperar(dt);
            if (enAtencion.agotado()) {
                enAtencion = null;
                plato.clear();
                perderCliente("Se agoto la paciencia del cliente en atencion");
            }
        }

        if (estado == Estado.JUGANDO && tiempoDia <= 0 && cola.isEmpty() && enAtencion == null) {
            estado = Estado.DIA_TERMINADO;
        }
    }

    private void recibirCliente() {
        if (cola.size() >= CAPACIDAD_COLA) {
            perderCliente("La cola esta llena: un cliente se fue");
        } else {
            cola.offer(new Cliente(siguienteNumero++, generarReceta(), paciencia(), Aspecto.aleatorio(rnd)));
        }
    }

    // ---------------------------------------------------------------- acciones del jugador

    /** Saca al primero de la cola (el que llegó antes) y lo pone en el mostrador. */
    public boolean atenderSiguiente() {
        if (estado != Estado.JUGANDO) return false;
        if (enAtencion != null) { avisar("Termina primero el pedido actual"); return false; }
        Cliente c = cola.poll();
        if (c == null) { avisar("No hay clientes en la cola"); return false; }
        enAtencion = c;
        plato.clear();
        return true;
    }

    /** Apila un ingrediente en el plato. */
    public boolean agregarIngrediente(Ingrediente ingrediente) {
        if (estado != Estado.JUGANDO || enAtencion == null) return false;
        if (!disponibles.contains(ingrediente)) return false;
        if (plato.size() >= MAX_PLATO) { avisar("El plato esta lleno"); return false; }
        plato.push(ingrediente);
        return true;
    }

    /** Quita el último ingrediente apilado. */
    public boolean deshacer() {
        if (estado != Estado.JUGANDO || plato.isEmpty()) return false;
        plato.pop();
        return true;
    }

    /** Entrega el plato al cliente en atención y lo compara con su receta. */
    public Entrega entregar() {
        if (estado != Estado.JUGANDO || enAtencion == null) return Entrega.SIN_CLIENTE;
        Cliente c = enAtencion;
        boolean correcto = plato.size() == c.getReceta().size() && platoCoincide();
        enAtencion = null;
        if (correcto) {
            int propina = Math.round(c.fraccionPaciencia() * 4);
            int pago = c.pagoBase() + propina;
            monedas += pago;
            entregados++;
            avisar("Pedido correcto: +$" + pago + " (propina $" + propina + ")");
            plato.clear();
            return Entrega.CORRECTA;
        }
        plato.clear();
        perderCliente("Pedido incorrecto: el cliente se fue");
        return Entrega.INCORRECTA;
    }

    /** Compra un ingrediente nuevo (solo entre días). Se agrega al arreglo dinámico. */
    public boolean comprar(Ingrediente ingrediente) {
        if (estado != Estado.DIA_TERMINADO) return false;
        if (disponibles.contains(ingrediente) || monedas < ingrediente.getCosto()) return false;
        monedas -= ingrediente.getCosto();
        disponibles.add(ingrediente);
        return true;
    }

    public boolean siguienteDia() {
        if (estado != Estado.DIA_TERMINADO) return false;
        dia++;
        tiempoDia = DURACION_DIA;
        relojLlegada = 1f;
        estado = Estado.JUGANDO;
        avisar("Dia " + dia);
        return true;
    }

    // ---------------------------------------------------------------- consultas

    /** true si lo apilado hasta ahora es el comienzo correcto de la receta. */
    public boolean platoCoincide() {
        if (enAtencion == null) return plato.isEmpty();
        List<Ingrediente> receta = enAtencion.getReceta();
        if (plato.size() > receta.size()) return false;
        for (int i = 0; i < plato.size(); i++) {
            if (plato.get(i) != receta.get(i)) return false;
        }
        return true;
    }

    public List<Ingrediente> ingredientesPorComprar() {
        List<Ingrediente> lista = new ArrayList<>();
        for (Ingrediente i : Catalogo.TODOS) {
            if (!disponibles.contains(i)) lista.add(i);
        }
        return lista;
    }

    public List<Ingrediente> getDisponibles() { return Collections.unmodifiableList(disponibles); }
    public List<Cliente> getCola() { return new ArrayList<>(cola); }   // copia, en orden de llegada
    public List<Ingrediente> getPlato() { return Collections.unmodifiableList(plato); } // de abajo hacia arriba
    public Cliente getEnAtencion() { return enAtencion; }
    public Estado getEstado() { return estado; }
    public int getDia() { return dia; }
    public int getMonedas() { return monedas; }
    public int getVidas() { return vidas; }
    public int getEntregados() { return entregados; }
    public int getPerdidos() { return perdidos; }
    public float getTiempoDia() { return tiempoDia; }
    public String getMensaje() { return tiempoMensaje > 0 ? mensaje : ""; }

    // ---------------------------------------------------------------- internos

    private void perderCliente(String motivo) {
        perdidos++;
        vidas--;
        avisar(motivo);
        if (vidas <= 0) estado = Estado.FIN_JUEGO;
    }

    private void avisar(String texto) {
        mensaje = texto;
        tiempoMensaje = 3f;
    }

    /** Dificultad: cada día llegan más rápido y con menos paciencia. */
    private float intervaloLlegada() { return Math.max(4f, 10f - 0.8f * (dia - 1)); }
    private float paciencia() { return Math.max(20f, 45f - 4f * (dia - 1)); }

    /** Pan abajo, 1 a 4 ingredientes (según el día) tomados del arreglo disponible, Pan arriba. */
    private List<Ingrediente> generarReceta() {
        List<Ingrediente> extras = new ArrayList<>(disponibles);
        extras.remove(Catalogo.PAN);
        int maxExtras = Math.min(1 + (dia + 1) / 2, 4);
        int n = 1 + rnd.nextInt(maxExtras);
        List<Ingrediente> receta = new ArrayList<>();
        receta.add(Catalogo.PAN);
        for (int i = 0; i < n; i++) receta.add(extras.get(rnd.nextInt(extras.size())));
        receta.add(Catalogo.PAN);
        return receta;
    }
}
