package com.foodflow.logica;

import com.foodflow.modelo.Aspecto;
import com.foodflow.modelo.Cliente;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.modelo.Receta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

/**
 - Reglas del juego, sin nada de LibGDX (se puede probar sin abrir ventana).
 *
 -  - PriorityQueue<Cliente> cola de llegada con prioridad VIP. Si se llena o la paciencia se agota, el cliente se va.
 -  - Stack<Ingrediente>     plato en construcción (LIFO). deshacer() quita el último ingrediente.
 -  - ListaDoble<Ingrediente>  despensa; sus nodos permiten recorrerla hacia adelante y hacia atrás.
 -  - Recetario              diccionario de recetas; los clientes piden una receta completa.
 */
public class Mundo {
    public enum Estado { JUGANDO, DIA_TERMINADO, FIN_JUEGO }
    public enum Entrega { SIN_CLIENTE, CORRECTA, INCORRECTA }

    public static final int CAPACIDAD_COLA = 10;
    public static final int MAX_PLATO = 8;
    public static final int VIDAS_INICIALES = 5;
    public static final float DURACION_DIA = 110f;

    private final Random rnd;
    private final ListaDoble<Ingrediente> disponibles = Catalogo.iniciales();
    private final Queue<Cliente> cola = new PriorityQueue<>();
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
    private String ultimaReceta;       // para no repetir el mismo pedido dos veces seguidas
    private String mensaje = "";
    private float tiempoMensaje = 0f;

    public Mundo() { this(System.nanoTime()); }
    public Mundo(long semilla) {
        this.rnd = new Random(semilla);
        // El restaurante abre con tres clientes esperando: la cola puede pasar de cinco puestos.
        recibirCliente();
        recibirCliente();
        recibirCliente();
    }

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
            perderCliente("Local lleno: un cliente se fue");
        } else {
            Receta receta = elegirReceta();
            // Los primeros 2 clientes son normales; del 3ro en adelante pueden llegar VIP
            boolean esVip = siguienteNumero > 2 && rnd.nextFloat() < 0.37f;
            cola.offer(new Cliente(siguienteNumero++, receta, paciencia(), Aspecto.aleatorio(rnd), esVip));
            if (esVip) {
                avisar("Llego un cliente VIP!");
            }
        }
    }

    // ---------------------------------------------------------------- acciones del jugador

    /** Saca al de mayor prioridad de la cola (VIP primero, luego orden de llegada) y lo pone en el mostrador. */
    public boolean atenderSiguiente() {
        if (estado != Estado.JUGANDO) return false;
        if (enAtencion != null) { avisar("Termina primero el pedido actual"); return false; }
        Cliente c = cola.poll();
        if (c == null) { avisar("No hay clientes esperando"); return false; }
        enAtencion = c;
        plato.clear();
        return true;
    }

    /** Apila un ingrediente en el plato. */
    public boolean agregarIngrediente(Ingrediente ingrediente) {
        if (estado != Estado.JUGANDO || enAtencion == null) return false;
        if (!disponibles.contiene(ingrediente)) return false;
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

    /** Compra un ingrediente nuevo (solo entre días). Se agrega al final de la lista doble. */
    public boolean comprar(Ingrediente ingrediente) {
        if (estado != Estado.DIA_TERMINADO) return false;
        if (disponibles.contiene(ingrediente) || monedas < ingrediente.getCosto()) return false;
        monedas -= ingrediente.getCosto();
        disponibles.agregar(ingrediente);
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
            if (!disponibles.contiene(i)) lista.add(i);
        }
        return lista;
    }

    public List<Ingrediente> getDisponibles() { return disponibles.aLista(); }
    /** Lista enlazada de la despensa; la pantalla la recorre por nodos para paginar. */
    public ListaDoble<Ingrediente> getDespensa() { return disponibles; }
    public List<Cliente> getCola() {
        List<Cliente> ordenada = new ArrayList<>(cola);
        Collections.sort(ordenada);
        return ordenada;
    }   // copia ordenada por prioridad VIP y orden de llegada
    public List<Ingrediente> getPlato() { return Collections.unmodifiableList(plato); } // en orden de la receta
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
    private float intervaloLlegada() { return Math.max(5f, 12f - 0.8f * (dia - 1)); }
    private float paciencia() { return Math.max(40f, 80f - 3f * (dia - 1)); }

    /** Elige una receta del diccionario que se pueda preparar con la despensa actual. */
    private Receta elegirReceta() {
        List<Receta> posibles = Recetario.disponibles(disponibles.aLista());
        if (posibles.isEmpty()) return Recetario.receta(Recetario.RECETA_BASICA);
        if (posibles.size() > 1) {
            posibles.removeIf(receta -> receta.getNombre().equals(ultimaReceta));
        }
        Receta receta = posibles.get(rnd.nextInt(posibles.size()));
        ultimaReceta = receta.getNombre();
        return receta;
    }
}