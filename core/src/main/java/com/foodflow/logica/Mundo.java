package com.foodflow.logica;

import com.foodflow.modelo.Aspecto;
import com.foodflow.modelo.Cliente;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.modelo.Mejora;
import com.foodflow.modelo.NivelCliente;
import com.foodflow.modelo.Receta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

/**
 * Reglas del juego, sin nada de LibGDX (se puede probar sin abrir ventana).
 *
 *  - ColaPrioridad<Cliente> cola de llegada (min-heap por prioridad: VIP Gold=1, VIP Silver=2, Comun=3).
 *    Si se llena o la paciencia se agota, el cliente se va.
 *  - Stack<Ingrediente>    plato en construcción (LIFO). deshacer() quita el último ingrediente.
 *  - ListaDoble<Ingrediente> despensa; sus nodos permiten recorrerla hacia adelante y hacia atrás.
 *  - Recetario             diccionario de recetas; los clientes piden una receta completa.
 *  - ArbolMejoras          catalogo de mejoras ordenado por costo.
 */
public class Mundo {
    public enum Estado { JUGANDO, MEJORAS, DIA_TERMINADO, FIN_JUEGO }
    public enum Entrega { SIN_CLIENTE, CORRECTA, INCORRECTA }

    public static final int CAPACIDAD_COLA = 10;
    public static final int MAX_PLATO = 8;
    public static final int VIDAS_INICIALES = 5;
    public static final float DURACION_DIA = 70f;

    private final Random rnd;
    private final ListaDoble<Ingrediente> disponibles = Catalogo.iniciales();
    private final ColaPrioridad<Cliente> cola = new ColaPrioridad<>();
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

    // ---------------------------------------------------------------- clientes VIP
    private static final int VIP_MINIMO_DIA = 2;        // al menos 2 VIPs por dia
    private static final float PROBABILIDAD_VIP = 0.15f; // los VIPs son menos frecuentes que los comunes
    private static final float TIEMPO_MINIMO_VIP = 10f; // segundos que deben pasar antes de que aparezca el primer VIP del dia
    private int vipsEsteDia = 0;                        // cuantos VIPs se han generado hoy
    private boolean ultimoFueVIP = false;               // evita que dos VIPs lleguen uno tras otro

    // ---------------------------------------------------------------- sistema de mejoras
    private final ArbolMejoras arbolMejoras = new ArbolMejoras();
    private final List<Mejora> ofertasMejoras = new ArrayList<>();
    private final Set<String> mejorasCompradas = new HashSet<>();
    private final Set<String> mejorasActivas = new HashSet<>();

    public Mundo() { this(System.nanoTime()); }
    public Mundo(long semilla) {
        this.rnd = new Random(semilla);
        // El restaurante abre con tres clientes esperando: la cola puede pasar de cinco puestos.
        recibirCliente();
        recibirCliente();
        recibirCliente();
    }

    // ---------------------------------------------------------------- avance del tiempo

    private float tiempoTranscurridoDia = 0f;

    /** Avanza el juego dt segundos. Llamar una vez por frame. */
    public void actualizar(float dt) {
        if (estado != Estado.JUGANDO || dt <= 0f) return;
        if (tiempoMensaje > 0f) tiempoMensaje = Math.max(0f, tiempoMensaje - dt);

        // Llegadas (solo mientras dure el día)
        if (tiempoDia > 0) {
            tiempoDia = Math.max(0f, tiempoDia - dt);
            tiempoTranscurridoDia += dt;
            relojLlegada -= dt;
            while (relojLlegada <= 0f && estado == Estado.JUGANDO && tiempoDia > 0f) {
                relojLlegada += intervaloLlegada();
                recibirCliente();
            }
        }

        // Paciencia de quienes esperan en la cola (se recorre toda, por si las pacientes difieren)
        for (Cliente c : cola.aLista()) {
            c.esperar(dt);
            if (c.agotado() && cola.remover(c)) {
                perderCliente("Un cliente se canso de esperar");
                if (estado == Estado.FIN_JUEGO) return;
            }
        }

        // Paciencia del cliente que se está atendiendo
        if (enAtencion != null) {
            enAtencion.esperar(dt);
            if (enAtencion.agotado()) {
                enAtencion = null;
                plato.clear();
                perderCliente("Se agoto la paciencia del cliente en atencion");
                if (estado == Estado.FIN_JUEGO) return;
            }
        }

        // Al terminar el tiempo, se espera a que no queden clientes y se pasa a mejoras.
        if (estado == Estado.JUGANDO && tiempoDia <= 0f
                && cola.isEmpty() && enAtencion == null) {
            estado = Estado.MEJORAS;
            generarOfertasMejoras();
        }
    }

    private void recibirCliente() {
        if (cola.size() >= getCapacidadCola()) {
            perderCliente("Local lleno: un cliente se fue");
            return;
        }

        Receta receta = elegirReceta();

        // Los VIPs no aparecen hasta que pasen TIEMPO_MINIMO_VIP segundos del inicio del dia,
        // y nunca dos seguidos (uno detras del otro).
        boolean disponibleVIP = tiempoTranscurridoDia >= TIEMPO_MINIMO_VIP && !ultimoFueVIP;
        NivelCliente nivel = disponibleVIP ? decidirNivelVIP() : NivelCliente.COMUN;
        Aspecto aspecto = (nivel != NivelCliente.COMUN)
                ? Aspecto.vip(rnd, nivel)
                : Aspecto.aleatorio(rnd);

        float pacienciaBase = paciencia();
        float pacienciaCliente = pacienciaBase * multiplicadorPaciencia();
        if (nivel.esVIP()) pacienciaCliente *= 1.25f;

        Cliente c = new Cliente(siguienteNumero++, receta, pacienciaCliente, aspecto, nivel);
        cola.offer(c);
        ultimoFueVIP = (nivel != NivelCliente.COMUN);
        if (nivel != NivelCliente.COMUN) vipsEsteDia++;
    }

    /** Decide si el próximo cliente es VIP y de qué nivel.
     *  Los VIPs son menos frecuentes que los comunes, pero se garantiza al menos
     *  VIP_MINIMO_DIA por día subiendo la probabilidad mientras no se alcance el mínimo. */
    private NivelCliente decidirNivelVIP() {
        boolean forzarMinimo = vipsEsteDia < VIP_MINIMO_DIA;
        boolean esVIP = forzarMinimo
                ? rnd.nextFloat() < 0.60f
                : rnd.nextFloat() < PROBABILIDAD_VIP;
        if (!esVIP) return NivelCliente.COMUN;
        // Entre los VIPs, la probabilidad de ser Gold varía con el día para que
        // no sea siempre el mismo nivel: oscila entre ~20% y ~60% de Gold.
        float probGold = 0.40f + 0.20f * (float) Math.sin(dia * 0.7f);
        return rnd.nextFloat() < probGold ? NivelCliente.GOLD : NivelCliente.SILVER;
    }

    // ---------------------------------------------------------------- acciones del jugador

    /** Saca al primero de la cola (el que llegó antes) y lo pone en el mostrador. */
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
            int propina = Math.round(c.fraccionPaciencia() * 4f);
            int pagoBase = c.pagoBase() + propina;
            int pago = Math.round(pagoBase * multiplicadorPago());
            monedas += pago;
            entregados++;
            avisar("Pedido correcto: +$" + pago + " (propina incluida)");
            plato.clear();
            return Entrega.CORRECTA;
        }
        // Pedido incorrecto: si es VIP y hay monedas suficientes, se hace un reembolso
        // menor al valor de la receta y el cliente no se va enfadado.
        if (c.esVIP()) {
            int reembolso = c.pagoBase() / 2;   // la mitad del valor de la receta (menor al pago total)
            if (monedas >= reembolso && reembolso > 0) {
                monedas -= reembolso;
                plato.clear();
                avisar("Pedido incorrecto (VIP): reembolso de -$" + reembolso);
                return Entrega.INCORRECTA;
            }
        }
        plato.clear();
        perderCliente("Pedido incorrecto: el cliente se fue");
        return Entrega.INCORRECTA;
    }

    /** Compra un ingrediente nuevo (solo entre días). Se agrega al final de la lista doble. */
    public boolean comprar(Ingrediente ingrediente) {
        if (estado != Estado.DIA_TERMINADO || ingrediente == null) return false;
        int costo = getCostoIngrediente(ingrediente);
        if (disponibles.contiene(ingrediente) || monedas < costo) return false;
        monedas -= costo;
        disponibles.agregar(ingrediente);
        return true;
    }

    public boolean siguienteDia() {
        if (estado != Estado.DIA_TERMINADO) return false;
        dia++;
        tiempoDia = getDuracionDiaTotal();
        tiempoTranscurridoDia = 0f;
        relojLlegada = 1f;
        vipsEsteDia = 0;   // nuevo día: se reinicia el mínimo de VIPs
        ultimoFueVIP = false; // nuevo día: se puede volver a generar VIPs
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
    public List<Cliente> getCola() { return cola.aLista(); }   // copia, ordenada por prioridad del monticulo

    /** Clientes VIP en espera, ordenados por prioridad (para dibujar en fila separada). */
    public List<Cliente> getColaVIP() {
        List<Cliente> lista = new ArrayList<>();
        for (Cliente c : cola.aLista()) if (c.esVIP()) lista.add(c);
        return lista;
    }

    /** Clientes comunes en espera, ordenados por prioridad (para dibujar en fila separada). */
    public List<Cliente> getColaComun() {
        List<Cliente> lista = new ArrayList<>();
        for (Cliente c : cola.aLista()) if (!c.esVIP()) lista.add(c);
        return lista;
    }
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

    // ---------------------------------------------------------------- sistema de mejoras y arbol binario

    /** Compra una de las mejoras ofrecidas. Solo se permite una por dia. */
    public boolean comprarMejora(String id) {
        if (estado != Estado.MEJORAS || id == null) return false;

        for (Mejora mejora : ofertasMejoras) {
            if (!mejora.getId().equals(id)) continue;
            if (mejorasCompradas.contains(id) || monedas < mejora.getCosto()) return false;

            // El seguro recupera una vida inmediatamente; las otras mejoras son permanentes.
            if ("seguro_comercial".equals(id)) {
                if (vidas >= VIDAS_INICIALES) return false;
                vidas++;
            } else {
                mejorasActivas.add(id);
            }

            monedas -= mejora.getCosto();
            mejorasCompradas.add(id);
            ofertasMejoras.clear();
            estado = Estado.DIA_TERMINADO;
            avisar("Mejora adquirida: " + mejora.getNombre());
            return true;
        }

        return false;
    }

    /** Omite las mejoras y habilita la tienda. */
    public void omitirMejoras() {
        if (estado != Estado.MEJORAS) return;

        ofertasMejoras.clear();
        estado = Estado.DIA_TERMINADO;
        avisar("Mejoras omitidas. Puedes visitar la tienda.");
    }

    private void generarOfertasMejoras() {
        ofertasMejoras.clear();

        List<Mejora> costosas = arbolMejoras.obtenerEnRango(300, Integer.MAX_VALUE);
        List<Mejora> equilibradas = arbolMejoras.obtenerEnRango(1, 299);

        filtrarCompradas(costosas);
        filtrarCompradas(equilibradas);

        agregarOfertaAleatoria(costosas);
        agregarOfertaAleatoria(equilibradas);
        agregarOfertaAleatoria(equilibradas);

        // Si se agota una categoria, se completan las ofertas con las restantes.
        List<Mejora> restantes = arbolMejoras.obtenerEnOrden();
        filtrarCompradas(restantes);

        while (ofertasMejoras.size() < 3 && !restantes.isEmpty()) {
            agregarOfertaAleatoria(restantes);
        }

        Collections.shuffle(ofertasMejoras, rnd);
    }

    private void filtrarCompradas(List<Mejora> lista) {
        java.util.Iterator<Mejora> it = lista.iterator();
        while (it.hasNext()) {
            Mejora mejora = it.next();
            if (mejorasCompradas.contains(mejora.getId())
                    || contieneOferta(mejora.getId())) {
                it.remove();
            } else if ("seguro_comercial".equals(mejora.getId())
                    && vidas >= VIDAS_INICIALES) {
                it.remove();
            }
        }
    }

    private boolean contieneOferta(String id) {
        for (Mejora oferta : ofertasMejoras) {
            if (oferta.getId().equals(id)) return true;
        }
        return false;
    }

    private void agregarOfertaAleatoria(List<Mejora> candidatas) {
        if (candidatas.isEmpty()) return;
        int indice = rnd.nextInt(candidatas.size());
        Mejora elegida = candidatas.remove(indice);
        ofertasMejoras.add(elegida);
    }

    public List<Mejora> getOfertasMejoras() {
        return Collections.unmodifiableList(ofertasMejoras);
    }

    public boolean tieneMejora(String id) {
        return mejorasActivas.contains(id) || mejorasCompradas.contains(id);
    }

    public int getCantidadMejorasCompradas() {
        return mejorasCompradas.size();
    }

    private float multiplicadorPago() {
        float multiplicador = 1f;
        if (mejorasActivas.contains("caja_registradora")) multiplicador *= 1.20f;
        if (mejorasActivas.contains("publicidad_agresiva")) multiplicador *= 1.25f;
        if (mejorasActivas.contains("proveedor_economico")) multiplicador *= 0.90f;
        if (mejorasActivas.contains("menu_gourmet")) multiplicador *= 1.35f;
        return multiplicador;
    }

    private float multiplicadorPaciencia() {
        float multiplicador = 1f;
        if (mejorasActivas.contains("atencion_vip")) multiplicador *= 1.25f;
        if (mejorasActivas.contains("cafe_clientes")) multiplicador *= 1.30f;
        if (mejorasActivas.contains("horario_nocturno")) multiplicador *= 0.85f;
        if (mejorasActivas.contains("menu_gourmet")) multiplicador *= 0.80f;
        return multiplicador;
    }

    private float multiplicadorLlegadas() {
        float multiplicador = 1f;
        if (mejorasActivas.contains("publicidad_agresiva")) multiplicador *= 0.80f;
        if (mejorasActivas.contains("cafe_clientes")) multiplicador *= 0.85f;
        return multiplicador;
    }

    public int getCapacidadCola() {
        return CAPACIDAD_COLA
                + (mejorasActivas.contains("sala_ampliada") ? 2 : 0);
    }

    public float getDuracionDiaTotal() {
        float duracion = DURACION_DIA;
        if (mejorasActivas.contains("cocina_renovada")) duracion *= 1.20f;
        if (mejorasActivas.contains("horario_nocturno")) duracion *= 1.25f;
        return duracion;
    }

    /** Costo del ingrediente aplicando el descuento de Proveedor economico. */
    public int getCostoIngrediente(Ingrediente ingrediente) {
        if (ingrediente == null) return 0;
        if (mejorasActivas.contains("proveedor_economico")) {
            return Math.max(1, Math.round(ingrediente.getCosto() * 0.70f));
        }
        return ingrediente.getCosto();
    }

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
    private float intervaloLlegada() {
        float base = Math.max(4f, 10f - 0.8f * (dia - 1));
        return Math.max(2.5f, base * multiplicadorLlegadas());
    }
    private float paciencia() {
        float base = Math.max(20f, 45f - 4f * (dia - 1));
        return Math.max(10f, base * multiplicadorPaciencia());
    }

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
