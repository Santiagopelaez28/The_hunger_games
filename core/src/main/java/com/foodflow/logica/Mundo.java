package com.foodflow.logica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import com.foodflow.modelo.Aspecto;
import com.foodflow.modelo.Cliente;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.modelo.Mejora;
import com.foodflow.modelo.NivelCliente;
import com.foodflow.modelo.Receta;

/**
 * Reglas del juego, sin dependencias de LibGDX.
 *
 * ColaPrioridad<Cliente>: atiende primero VIP Gold, luego VIP Silver y finalmente
 * clientes comunes. Entre clientes del mismo nivel se respeta el orden de llegada.
 * Stack<Ingrediente>: plato en construccion; deshacer() quita el ultimo ingrediente.
 * ListaDoble<Ingrediente>: despensa del restaurante.
 * ArbolMejoras: catalogo de mejoras ordenado por costo.
 */
public class Mundo {
    public enum Estado { JUGANDO, MEJORAS, DIA_TERMINADO, FIN_JUEGO }
    public enum Entrega { SIN_CLIENTE, CORRECTA, INCORRECTA }

    public static final int CAPACIDAD_COLA = 10;
    public static final int MAX_PLATO = 8;
    public static final int VIDAS_INICIALES = 5;
    public static final float DURACION_DIA = 70f;

    private static final int PRECIO_VIDA_INICIAL = 100;
    private static final int AUMENTO_PRECIO_VIDA = 10;

    // Configuracion de clientes VIP.
    private static final int VIP_MINIMO_DIA = 2;
    private static final float PROBABILIDAD_VIP = 0.15f;
    private static final float TIEMPO_MINIMO_VIP = 10f;

    private final Random rnd;
    private final ListaDoble<Ingrediente> disponibles = Catalogo.iniciales();
    private final ColaPrioridad<Cliente> cola = new ColaPrioridad<>();
    private final Stack<Ingrediente> plato = new Stack<>();

    // Sistema de mejoras.
    private final ArbolMejoras arbolMejoras;
    private final List<Mejora> ofertasMejoras = new ArrayList<>();
    private final Set<String> mejorasCompradas = new HashSet<>();
    private final Set<String> mejorasActivas = new HashSet<>();

    private Cliente enAtencion;
    private Estado estado = Estado.JUGANDO;
    private int dia = 1;
    private int monedas = 0;
    private int vidas = VIDAS_INICIALES;
    private int entregados = 0;
    private int perdidos = 0;
    private int siguienteNumero = 1;
    private int comprasVida = 0;

    private float tiempoDia = DURACION_DIA;
    private float tiempoTranscurridoDia = 0f;
    private float relojLlegada = 1f;

    private String ultimaReceta;
    private String mensaje = "";
    private float tiempoMensaje = 0f;

    private int vipsEsteDia = 0;
    private boolean ultimoFueVIP = false;

    public Mundo() {
        this(System.nanoTime());
    }

    public Mundo(long semilla) {
        this.rnd = new Random(semilla);
        this.arbolMejoras = new ArbolMejoras();

        // El restaurante abre con tres clientes comunes esperando.
        recibirCliente();
        recibirCliente();
        recibirCliente();
    }

    /** Avanza el juego dt segundos. Se llama una vez por frame. */
    public void actualizar(float dt) {
        if (estado != Estado.JUGANDO || dt <= 0f) return;

        if (tiempoMensaje > 0f) {
            tiempoMensaje = Math.max(0f, tiempoMensaje - dt);
        }

        // El reloj y las llegadas solo avanzan mientras el dia siga abierto.
        if (tiempoDia > 0f) {
            tiempoDia = Math.max(0f, tiempoDia - dt);
            tiempoTranscurridoDia += dt;
            relojLlegada -= dt;

            while (relojLlegada <= 0f && estado == Estado.JUGANDO && tiempoDia > 0f) {
                relojLlegada += intervaloLlegada();
                recibirCliente();
            }
        }

        // ColaPrioridad.iterator() recorre una copia ordenada.
        // Por eso eliminamos al cliente de la cola real con cola.remover(c).
        for (Cliente c : cola.aLista()) {
            c.esperar(dt);
            if (c.agotado() && cola.remover(c)) {
                perderCliente("Un cliente se canso de esperar");
                if (estado == Estado.FIN_JUEGO) return;
            }
        }

        // Paciencia del cliente que se esta atendiendo.
        if (enAtencion != null) {
            enAtencion.esperar(dt);
            if (enAtencion.agotado()) {
                enAtencion = null;
                plato.clear();
                perderCliente("Se agoto la paciencia del cliente en atencion");
                if (estado == Estado.FIN_JUEGO) return;
            }
        }

        // Al terminar el tiempo, se espera a que no queden clientes.
        if (estado == Estado.JUGANDO && tiempoDia <= 0f
                && cola.isEmpty() && enAtencion == null) {
            estado = Estado.MEJORAS;
            generarOfertasMejoras();
        }
    }

    /** Genera un cliente, asignandole nivel VIP cuando corresponda. */
    private void recibirCliente() {
        if (cola.size() >= getCapacidadCola()) {
            perderCliente("Local lleno: un cliente se fue");
            return;
        }

        Receta receta = elegirReceta();

        boolean disponibleVIP = tiempoTranscurridoDia >= TIEMPO_MINIMO_VIP && !ultimoFueVIP;
        NivelCliente nivel = disponibleVIP ? decidirNivelVIP() : NivelCliente.COMUN;

        Aspecto aspecto = nivel.esVIP()
                ? Aspecto.vip(rnd, nivel)
                : Aspecto.aleatorio(rnd);

        float pacienciaCliente = paciencia();
        if (nivel.esVIP()) pacienciaCliente *= 1.25f;

        Cliente cliente = new Cliente(
                siguienteNumero++, receta, pacienciaCliente, aspecto, nivel);

        cola.offer(cliente);
        ultimoFueVIP = nivel.esVIP();

        if (nivel.esVIP()) {
            vipsEsteDia++;
        }
    }

    /**
     * Favorece la aparicion de los primeros VIP del dia.
     * La probabilidad posterior vuelve al valor normal del 15%.
     */
    private NivelCliente decidirNivelVIP() {
        boolean forzarMinimo = vipsEsteDia < VIP_MINIMO_DIA;
        boolean esVIP = forzarMinimo
                ? rnd.nextFloat() < 0.60f
                : rnd.nextFloat() < PROBABILIDAD_VIP;

        if (!esVIP) return NivelCliente.COMUN;

        float probGold = 0.40f + 0.20f * (float) Math.sin(dia * 0.7f);
        return rnd.nextFloat() < probGold
                ? NivelCliente.GOLD
                : NivelCliente.SILVER;
    }

    // -------------------------------------------------------------------------
    // Acciones del jugador
    // -------------------------------------------------------------------------

    /** Atiende al cliente con mayor prioridad disponible. */
    public boolean atenderSiguiente() {
        if (estado != Estado.JUGANDO) return false;

        if (enAtencion != null) {
            avisar("Termina primero el pedido actual");
            return false;
        }

        Cliente cliente = cola.poll();
        if (cliente == null) {
            avisar("No hay clientes esperando");
            return false;
        }

        enAtencion = cliente;
        plato.clear();
        return true;
    }

    public boolean agregarIngrediente(Ingrediente ingrediente) {
        if (estado != Estado.JUGANDO || enAtencion == null || ingrediente == null) {
            return false;
        }
        if (!disponibles.contiene(ingrediente)) return false;

        if (plato.size() >= MAX_PLATO) {
            avisar("El plato esta lleno");
            return false;
        }

        plato.push(ingrediente);
        return true;
    }

    /** Quita el ultimo ingrediente agregado al plato. */
    public boolean deshacer() {
        if (estado != Estado.JUGANDO || plato.isEmpty()) return false;
        plato.pop();
        return true;
    }

    /** Entrega el plato y aplica las reglas normales o VIP. */
    public Entrega entregar() {
        if (estado != Estado.JUGANDO || enAtencion == null) {
            return Entrega.SIN_CLIENTE;
        }

        Cliente cliente = enAtencion;
        boolean correcto = plato.size() == cliente.getReceta().size() && platoCoincide();
        enAtencion = null;

        if (correcto) {
            int propina = Math.round(cliente.fraccionPaciencia() * 4f);
            int pagoBase = cliente.pagoBase() + propina;
            int pago = Math.round(pagoBase * multiplicadorPago());

            monedas += pago;
            entregados++;
            avisar("Pedido correcto: +$" + pago + " (propina incluida)");
            plato.clear();
            return Entrega.CORRECTA;
        }

        // Un VIP tolera un error si el restaurante puede cubrir el reembolso.
        if (cliente.esVIP()) {
            int reembolso = cliente.pagoBase() / 2;
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

    // -------------------------------------------------------------------------
    // Tienda y vidas
    // -------------------------------------------------------------------------

    /** Compra un ingrediente durante la tienda, despues de las mejoras. */
    public boolean comprar(Ingrediente ingrediente) {
        if (estado != Estado.DIA_TERMINADO || ingrediente == null) return false;

        int costo = getCostoIngrediente(ingrediente);
        if (disponibles.contiene(ingrediente) || monedas < costo) return false;

        monedas -= costo;
        disponibles.agregar(ingrediente);
        return true;
    }

    /** Compra una vida. El costo aumenta 10 monedas por cada compra anterior. */
    public boolean comprarVida() {
        if (estado != Estado.DIA_TERMINADO || vidas >= VIDAS_INICIALES) return false;

        int costo = getCostoVida();
        if (monedas < costo) return false;

        monedas -= costo;
        vidas++;
        comprasVida++;
        avisar("Has recuperado una vida");
        return true;
    }

    public int getCostoVida() {
        return PRECIO_VIDA_INICIAL + comprasVida * AUMENTO_PRECIO_VIDA;
    }

    /** Costo del ingrediente aplicando el descuento de Proveedor economico. */
    public int getCostoIngrediente(Ingrediente ingrediente) {
        if (ingrediente == null) return 0;

        if (mejorasActivas.contains("proveedor_economico")) {
            return Math.max(1, Math.round(ingrediente.getCosto() * 0.70f));
        }
        return ingrediente.getCosto();
    }

    public boolean siguienteDia() {
        if (estado != Estado.DIA_TERMINADO) return false;

        dia++;
        tiempoDia = getDuracionDiaTotal();
        tiempoTranscurridoDia = 0f;
        relojLlegada = 1f;
        vipsEsteDia = 0;
        ultimoFueVIP = false;
        estado = Estado.JUGANDO;

        avisar("Dia " + dia);
        return true;
    }

    // -------------------------------------------------------------------------
    // Sistema de mejoras y arbol binario
    // -------------------------------------------------------------------------

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
        Iterator<Mejora> it = lista.iterator();

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

    // -------------------------------------------------------------------------
    // Consultas para las pantallas
    // -------------------------------------------------------------------------

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

        for (Ingrediente ingrediente : Catalogo.TODOS) {
            if (!disponibles.contiene(ingrediente)) lista.add(ingrediente);
        }

        return lista;
    }

    public List<Ingrediente> getDisponibles() {
        return disponibles.aLista();
    }

    public ListaDoble<Ingrediente> getDespensa() {
        return disponibles;
    }

    /** Devuelve una copia de la cola ordenada por prioridad. */
    public List<Cliente> getCola() {
        return cola.aLista();
    }

    public List<Cliente> getColaVIP() {
        List<Cliente> lista = new ArrayList<>();
        for (Cliente cliente : cola.aLista()) {
            if (cliente.esVIP()) lista.add(cliente);
        }
        return lista;
    }

    public List<Cliente> getColaComun() {
        List<Cliente> lista = new ArrayList<>();
        for (Cliente cliente : cola.aLista()) {
            if (!cliente.esVIP()) lista.add(cliente);
        }
        return lista;
    }

    public List<Ingrediente> getPlato() {
        return Collections.unmodifiableList(plato);
    }

    public Cliente getEnAtencion() { return enAtencion; }
    public Estado getEstado() { return estado; }
    public int getDia() { return dia; }
    public int getMonedas() { return monedas; }
    public int getVidas() { return vidas; }
    public int getEntregados() { return entregados; }
    public int getPerdidos() { return perdidos; }
    public float getTiempoDia() { return tiempoDia; }
    public String getMensaje() { return tiempoMensaje > 0f ? mensaje : ""; }

    // -------------------------------------------------------------------------
    // Metodos internos
    // -------------------------------------------------------------------------

    private void perderCliente(String motivo) {
        perdidos++;
        vidas--;
        avisar(motivo);

        if (vidas <= 0) {
            vidas = 0;
            estado = Estado.FIN_JUEGO;
        }
    }

    private void avisar(String texto) {
        mensaje = texto;
        tiempoMensaje = 3f;
    }

    private float intervaloLlegada() {
        float base = Math.max(4f, 10f - 0.8f * (dia - 1));
        return Math.max(2.5f, base * multiplicadorLlegadas());
    }

    private float paciencia() {
        float base = Math.max(20f, 45f - 4f * (dia - 1));
        return Math.max(10f, base * multiplicadorPaciencia());
    }

    private Receta elegirReceta() {
        List<Receta> posibles = Recetario.disponibles(disponibles.aLista());

        if (posibles.isEmpty()) {
            return Recetario.receta(Recetario.RECETA_BASICA);
        }

        if (posibles.size() > 1) {
            posibles.removeIf(receta -> receta.getNombre().equals(ultimaReceta));
        }

        Receta receta = posibles.get(rnd.nextInt(posibles.size()));
        ultimaReceta = receta.getNombre();
        return receta;
    }
}
