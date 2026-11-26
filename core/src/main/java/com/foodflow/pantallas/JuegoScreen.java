package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foodflow.FoodFlowGame;
import com.foodflow.logica.Catalogo;
import com.foodflow.logica.ListaDoble;
import com.foodflow.logica.Mundo;
import com.foodflow.modelo.Cliente;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.modelo.TipoPlato;
import com.foodflow.ui.Dibujos;
import com.foodflow.ui.Pincel;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla principal con vista 2D tipo Pokemon: el chef (personaje fijo) atiende
 * a los clientes en un restaurante. La receta y sus ingredientes se revelan en un
 * globo de texto cuando el cliente llega al mostrador.
 */
public class JuegoScreen extends PantallaBase {
    private static final int MAX_BOTONES = 12;

    // Botones de ingredientes (6 por fila) y botones de accion
    private static final float BW = 124, BH = 40;
    private static final float AY = 16, AH = 46, AW = 190;
    private static final float ATENDER_X = 18, DESHACER_X = 248, RECETAS_X = 478, ENTREGAR_X = 708;

    // Boton para salir de la pausa
    private static final float PAUSA_X = 380, PAUSA_Y = 178, PAUSA_W = 200, PAUSA_H = 46;

    // Flechas para recorrer la despensa cuando hay mas de MAX_BOTONES ingredientes
    private static final float PAG_Y = 198, PAG_IZQ_X = 844, PAG_DER_X = 878, PAG_R = 14;

    // ---------------------------------------------------------------- escena
    private static final float ESCENA_Y = 220f;
    private static final float MOSTRADOR_Y = 220f;
    private static final float MOSTRADOR_ALTO = 52f;
    private static final float SUELO_Y = 430f;
    private static final float HUD_Y = 548f;

    private static final float CHEF_X = 370f;
    private static final float CHEF_PIES = 246f;
    private static final float CHEF_S = 21f;

    private static final float CLIENTE_X = 520f;
    private static final float CLIENTE_PIES = 290f;
    private static final float CLIENTE_S = 18f;

    private static final float BURBUJA_X = 252f, BURBUJA_Y = 436f;
    private static final float BURBUJA_W = 388f, BURBUJA_H = 108f;

    private static final float PLATO_X = 214f, PLATO_Y = 260f;
    private static final float TICKET_X = 24f, TICKET_Y = 276f, TICKET_W = 128f, TICKET_H = 146f;

    // Puestos de la cola de espera (hasta 8). El cliente en atencion va aparte.
    // Dos filas: los cuatro primeros esperan delante y los otros cuatro detras.
    private static final float[] COLA_X = {650, 712, 774, 836, 898, 650, 712, 774, 836, 898};
    private static final float[] COLA_Y = {318, 318, 318, 318, 318, 388, 388, 388, 388, 388};
    private static final float[] COLA_S = {16f, 16f, 16f, 16f, 16f, 14f, 14f, 14f, 14f, 14f};

    private boolean pausado = false;
    private float brinco = 0f;
    private int tamPlatoAnterior = 0;
    private String mensajeVisto = "";
    private float tiempoMensaje = 0f;

    // Animaciones de la escena
    private float animServir = 0f;
    private float animBurbuja = 0f;
    private int clienteVisible = -1;

    // Posicion actual (animada) del cliente en atencion. Al pulsar ATENDER el cliente
    // se acerca desde atrás hasta el mostrador frente al chef.
    private float clienteX = CLIENTE_X;
    private float clienteTargetX = CLIENTE_X;
    private float clienteApproachT = 0f;

    // Ventana visible de la lista doble: se mueve con las flechas o con las teclas IZQUIERDA/DERECHA.
    private final List<Ingrediente> visibles = new ArrayList<>(MAX_BOTONES);
    private ListaDoble.Nodo<Ingrediente> inicioPagina;
    private int paginaIngredientes = 0;

    public JuegoScreen(FoodFlowGame juego) { super(juego); }

    private static float bx(int i) { return 24 + (i % 6) * 152; }
    private static float by(int i) { return i < 6 ? 132 : 78; }

    @Override
    public void render(float dt) {
        Mundo m = juego.mundo;
        actualizarVisibles(m);

        if (pausado) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)
                    || (hayClick() && dentro(toque, PAUSA_X, PAUSA_Y, PAUSA_W, PAUSA_H))) {
                pausado = false;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            pausado = true;
        } else {
            if (leerEntrada(m)) return;
            m.actualizar(Math.min(dt, 0.05f));   // tope de 50 ms: un tiron de la ventana no rompe la partida

            // El cliente en atencion se desliza hacia el mostrador al ser atendido.
            if (m.getEnAtencion() != null) {
                clienteX += (clienteTargetX - clienteX) * dt * 5f;
            } else {
                clienteX = CLIENTE_X;
                clienteApproachT = 0f;
            }
        }

        if (m.getEstado() == Mundo.Estado.DIA_TERMINADO) { juego.setScreen(new TiendaScreen(juego)); return; }
        if (m.getEstado() == Mundo.Estado.FIN_JUEGO) { juego.setScreen(new FinScreen(juego)); return; }

        dibujar(m, dt);
    }

    // ------------------------------------------------------------------ entrada

    private void accionPrincipal(Mundo m) {
        if (m.getEnAtencion() == null) {
            if (m.atenderSiguiente()) {
                animBurbuja = 0f;
                // El cliente se acerca desde atras hasta el mostrador.
                clienteX = CLIENTE_X + 150f;
                clienteTargetX = CLIENTE_X;
                clienteApproachT = 0f;
                if (juego.campana != null) juego.campana.play(0.7f);
            }
        } else if (!m.getPlato().isEmpty()) {
            entregar(m);
        }
    }

    /** Entrega el plato y lanza la animacion del chef sirviendo (positiva si acerto). */
    private void entregar(Mundo m) {
        if (m.getEnAtencion() == null || m.getPlato().isEmpty()) return;
        Mundo.Entrega resultado = m.entregar();
        animServir = resultado == Mundo.Entrega.INCORRECTA ? -1f : 1f;
    }

    private boolean leerEntrada(Mundo m) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            abrirRecetario();
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) anteriorPagina();
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) siguientePagina();

        boolean hayPedido = m.getEnAtencion() != null;
        boolean sinMensaje = m.getMensaje().isEmpty();
        if (hayPedido) {
            for (int i = 0; i < visibles.size(); i++) {
                if (Gdx.input.isKeyJustPressed(teclaDe(i))) m.agregarIngrediente(visibles.get(i));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) accionPrincipal(m);
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) m.deshacer();

        if (hayClick()) {
            if (dentro(toque, RECETAS_X, AY, AW, AH)) {
                abrirRecetario();
                return true;
            }
            if (hayPedido && sinMensaje && paginaIngredientes > 0 && dentro(toque, PAG_IZQ_X - PAG_R,
                    PAG_Y - PAG_R, PAG_R * 2, PAG_R * 2)) {
                anteriorPagina();
                return false;
            }
            if (hayPedido && sinMensaje && paginaIngredientes + 1 < paginas(m) && dentro(toque,
                    PAG_DER_X - PAG_R, PAG_Y - PAG_R, PAG_R * 2, PAG_R * 2)) {
                siguientePagina();
                return false;
            }
            if (hayPedido) {
                for (int i = 0; i < visibles.size(); i++) {
                    if (dentro(toque, bx(i), by(i), BW, BH)) m.agregarIngrediente(visibles.get(i));
                }
            }
            if (dentro(toque, ATENDER_X, AY, AW, AH)) {
                if (m.atenderSiguiente()) animBurbuja = 0f;
            }
            if (dentro(toque, DESHACER_X, AY, AW, AH)) m.deshacer();
            if (dentro(toque, ENTREGAR_X, AY, AW, AH)) entregar(m);
        }
        return false;
    }

    private void actualizarVisibles(Mundo m) {
        ListaDoble<Ingrediente> despensa = m.getDespensa();
        if (despensa.estaVacia()) {
            inicioPagina = null;
            paginaIngredientes = 0;
            visibles.clear();
            return;
        }
        if (inicioPagina == null) inicioPagina = despensa.getPrimero();
        visibles.clear();
        ListaDoble.Nodo<Ingrediente> nodo = inicioPagina;
        for (int i = 0; i < MAX_BOTONES && nodo != null; i++) {
            visibles.add(nodo.getDato());
            nodo = nodo.getSiguiente();
        }
    }

    private int paginas(Mundo m) {
        return Math.max(1, (m.getDespensa().getTamano() + MAX_BOTONES - 1) / MAX_BOTONES);
    }

    /** Retrocede una pagina caminando por los enlaces anteriores de la lista doble. */
    private void anteriorPagina() {
        if (paginaIngredientes <= 0) return;
        ListaDoble.Nodo<Ingrediente> nodo = inicioPagina;
        for (int i = 0; i < MAX_BOTONES && nodo != null; i++) nodo = nodo.getAnterior();
        inicioPagina = nodo != null ? nodo : juego.mundo.getDespensa().getPrimero();
        paginaIngredientes--;
        actualizarVisibles(juego.mundo);
    }

    /** Avanza una pagina caminando por los enlaces siguientes de la lista doble. */
    private void siguientePagina() {
        if (paginaIngredientes + 1 >= paginas(juego.mundo)) return;
        ListaDoble.Nodo<Ingrediente> nodo = inicioPagina;
        for (int i = 0; i < MAX_BOTONES && nodo != null; i++) nodo = nodo.getSiguiente();
        if (nodo == null) return;
        inicioPagina = nodo;
        paginaIngredientes++;
        actualizarVisibles(juego.mundo);
    }

    private void abrirRecetario() {
        juego.setScreen(new RecetasScreen(juego, RecetasScreen.Origen.JUEGO));
    }

    /** Teclas 1-9 y 0 para los primeros diez ingredientes; Q y E para los dos ultimos. */
    private static int teclaDe(int i) {
        if (i < 9) return Input.Keys.NUM_1 + i;
        if (i == 9) return Input.Keys.NUM_0;
        return i == 10 ? Input.Keys.Q : Input.Keys.E;
    }

    private static String etiquetaTecla(int i) {
        if (i < 9) return String.valueOf(i + 1);
        if (i == 9) return "0";
        return i == 10 ? "Q" : "E";
    }

    // ------------------------------------------------------------------ dibujo

    private void dibujar(Mundo m, float dt) {
        Pincel p = juego.pincel;
        List<Cliente> cola = m.getCola();
        List<Ingrediente> plato = m.getPlato();
        Cliente actual = m.getEnAtencion();
        float t = p.getTiempo();

        if (!m.getMensaje().equals(mensajeVisto)) {
            mensajeVisto = m.getMensaje();
            tiempoMensaje = 0f;
        } else {
            tiempoMensaje += dt;
        }
        if (plato.size() > tamPlatoAnterior) brinco = 1f;
        tamPlatoAnterior = plato.size();
        brinco = Math.max(0f, brinco - dt * 5f);
        float salto = brinco * 6f;

        int numero = actual == null ? -1 : actual.getNumero();
        if (numero != clienteVisible) {
            clienteVisible = numero;
            animBurbuja = 0f;
        } else {
            animBurbuja = Math.min(1f, animBurbuja + dt * 4.5f);
        }
        if (animServir > 0f) animServir = Math.max(0f, animServir - dt * 2.2f);
        else if (animServir < 0f) animServir = Math.min(0f, animServir + dt * 2.2f);

        boolean caminando = false;

        empezarFrame();
        dibujarFondo(p, t);
        dibujarCola(p, cola, t);
        dibujarClienteActivo(p, actual, t);
        dibujarChef(p, actual, t, caminando);
        dibujarMostrador(p, actual, plato, t, salto);
        if (actual != null) dibujarPedido(p, actual);
        dibujarHud(p, m, cola, t);
        dibujarCocina(p, m, actual, plato, t);
        dibujarMensaje(p, m);

        if (m.getVidas() <= 2) {
            float a = 0.07f + 0.05f * (float) Math.sin(t * 5f);
            p.degradadoAlpha(0, 0, FoodFlowGame.ANCHO, 50, 0xFF3B3B, a, 0f);
            p.degradadoAlpha(0, 550, FoodFlowGame.ANCHO, 50, 0xFF3B3B, 0f, a);
        }

        if (pausado) dibujarPausa(p, t);
        terminarFrame();
    }

    /** Pared, ventana, tablones y suelo del restaurante. */
    private void dibujarFondo(Pincel p, float t) {
        p.degradado(0, SUELO_Y, FoodFlowGame.ANCHO, HUD_Y - SUELO_Y, 0xE7CDA6, 0xF9EDD3);
        p.rect(0, SUELO_Y, FoodFlowGame.ANCHO, 18, 0x7A4A26, 1f);
        p.rect(0, SUELO_Y + 18, FoodFlowGame.ANCHO, 2, 0xFFE1A6, 0.35f);
        for (int x = 40; x < FoodFlowGame.ANCHO; x += 80) {
            p.rect(x, SUELO_Y + 20, 1, HUD_Y - SUELO_Y - 20, 0x8A5B35, 0.08f);
        }
        p.halo(300, 520, 120, 0xFFE29A, 0.12f + 0.03f * (float) Math.sin(t * 1.8f));
        p.halo(840, 520, 120, 0xFFE29A, 0.10f + 0.03f * (float) Math.sin(t * 1.5f));

        dibujarMenu(p);
        dibujarLetrero(p);
        dibujarVentana(p);

        p.imagenCubriendo(juego.madera, 0, ESCENA_Y, FoodFlowGame.ANCHO, SUELO_Y - ESCENA_Y, 0.5f, 0.2f, 0x8A5B35);
        p.rect(0, ESCENA_Y, FoodFlowGame.ANCHO, SUELO_Y - ESCENA_Y, 0x2A1608, 0.30f);
        for (int i = 1; i < 7; i++) {
            float y = ESCENA_Y + i * (SUELO_Y - ESCENA_Y) / 7f;
            p.rect(0, y, FoodFlowGame.ANCHO, 1.5f, 0x1A0D08, 0.20f);
        }
        for (int x = 80; x < FoodFlowGame.ANCHO; x += 160) {
            p.rect(x, ESCENA_Y, 1.5f, SUELO_Y - ESCENA_Y, 0x1A0D08, 0.14f);
        }
        p.degradadoAlpha(0, SUELO_Y - 45, FoodFlowGame.ANCHO, 45, 0x000000, 0f, 0.20f);

        // alfombra de la zona de espera
        p.redondeado(624, 292, 326, 148, 30, 0x4E1C1C, 0.55f);
        p.redondeado(632, 298, 310, 136, 24, 0x8E3B3B, 0.55f);
        p.redondeado(642, 306, 290, 120, 20, 0x9C4646, 0.42f);
    }

    private void dibujarMenu(Pincel p) {
        float x = 36, y = 455, w = 208, h = 72;
        p.redondeado(x - 5, y - 5, w + 10, h + 10, 9, 0x6B4A26, 1f);
        p.redondeado(x, y, w, h, 6, 0x24312B, 1f);
        p.textoCentrado("MENU", x + w / 2f, y + h - 14, 0xFFD166, 0.60f);
        p.rect(x + 12, y + h - 22, w - 24, 1, 0xFFD166, 0.28f);
        p.texto("HAMBURGUESA  $8", x + 16, y + 36, 0xE8E2D2, 0.42f);
        p.texto("PIZZA        $9", x + 16, y + 23, 0xE8E2D2, 0.42f);
        p.texto("HOTDOG       $7", x + 16, y + 10, 0xE8E2D2, 0.42f);
    }

    /** Letrero del restaurante: logo completo centrado con el nombre a su alrededor (sin el texto FOODFLOW). */
    private void dibujarLetrero(Pincel p) {
        float cx = FoodFlowGame.ANCHO / 2f;
        float y = 455f;
        float w = 170f, h = 72f;
        float x = cx - w / 2f;

        // fondo del letrero
        p.redondeado(x - 5, y - 5, w + 10, h + 10, 10, 0x6B4A26, 1f);
        p.redondeado(x, y, w, h, 7, 0x2E1B10, 1f);
        p.rect(x + 10, y + h - 16, w - 20, 1, 0xF2B33D, 0.35f);

        // logo del restaurante completo dentro del recuadro (sin recortar ni deformar)
        p.imagenAjustando(juego.logoRestaurante, x + 8, y + 8, w - 16, h - 16, 0x8A5B35);

        // nombre del restaurante alrededor del logo, con fuente llamativa (contorno)
        String nombre = FoodFlowGame.NOMBRE_RESTAURANTE;
        p.textoCentradoContorno(nombre, cx, y + h + 17, 0xFFD166, 0.85f, 0x3A2410);
        p.textoCentradoContorno(nombre, cx, y - 12, 0xFFD166, 0.60f, 0x3A2410);
    }

    private void dibujarVentana(Pincel p) {
        float x = 702, y = 455, w = 182, h = 68;
        p.redondeado(x - 6, y - 6, w + 12, h + 12, 9, 0x6B4A26, 1f);
        p.imagenCubriendo(juego.nubes, x, y, w, h, 0.5f, 0.15f, 0x8EDBF9);
        p.rect(x + w / 2f - 1.5f, y, 3, h, 0x6B4A26, 1f);
        p.rect(x, y + h / 2f - 1.5f, w, 3, 0x6B4A26, 1f);
        p.redondeado(x - 16, y - 4, 16, h + 8, 7, 0xC0392B, 0.90f);
        p.redondeado(x + w, y - 4, 16, h + 8, 7, 0xC0392B, 0.90f);
        p.textoCentrado("ABIERTO", x + w / 2f, y + h + 10, 0x2E7D32, 0.45f);
    }

    /** Clientes que esperan. Su pedido permanece oculto: solo se ve "..." hasta pasar al mostrador. */
    private void dibujarCola(Pincel p, List<Cliente> cola, float t) {
        int n = Math.min(cola.size(), COLA_X.length);
        if (n == 0) {
            p.textoCentrado("Sin clientes en espera", 792, 360, 0xE8C9A0, 0.66f);
            return;
        }
        for (int i = n - 1; i >= 0; i--) {
            Cliente c = cola.get(i);
            float x = COLA_X[i], y = COLA_Y[i], s = COLA_S[i];
            float pac = c.fraccionPaciencia();
            float bob = (float) Math.sin(t * 3f + i * 0.9f) * 1.2f;

            if (i == 0) {
                p.halo(x, y + s * 2.4f, s * 2.6f, 0xFFC24A,
                        0.10f + 0.05f * (float) Math.sin(t * 3f));
            }
            if (pac < 0.25f) {
                p.halo(x, y + s * 2.4f, s * 2.8f, 0xFF6B6B,
                        0.12f + 0.08f * (float) Math.sin(t * 8f));
            }

            Dibujos.clienteCompleto(p, x, y + bob, s * 0.85f, c.getAspecto(), pac);
            float gy = y + s * 5.15f + 10f;
            dibujarGloboEspera(p, x, gy, pac);
            Dibujos.barra(p, x - 17f, y - 7f, 34f, 4f, pac);
            if (i == 0) {
                p.redondeado(x - 30, y - 25, 60, 14, 5, 0x1A120C, 0.70f);
                p.textoCentrado("SIGUIENTE", x, y - 15f, 0xFFD166, 0.46f);
            }
        }
    }

    private void dibujarGloboEspera(Pincel p, float x, float y, float pac) {
        boolean urgente = pac < 0.25f;
        int nube = urgente ? 0xFFE3E3 : 0xFFFFFF;
        p.circulo(x - 8, y, 6, nube, 0.95f);
        p.circulo(x, y + 2, 7, nube, 0.95f);
        p.circulo(x + 8, y, 6, nube, 0.95f);
        p.circulo(x - 14, y - 7, 2.5f, nube, 0.90f);
        p.circulo(x - 18, y - 11, 1.6f, nube, 0.85f);
        if (urgente) {
            p.textoCentrado("!", x, y + 2, 0xE5484D, 0.65f);
        } else {
            p.circulo(x - 4, y + 1, 1.2f, 0x6A6A78, 0.90f);
            p.circulo(x, y + 2, 1.2f, 0x6A6A78, 0.90f);
            p.circulo(x + 4, y + 1, 1.2f, 0x6A6A78, 0.90f);
        }
    }

    private void dibujarClienteActivo(Pincel p, Cliente actual, float t) {
        if (actual == null) return;
        float pac = actual.fraccionPaciencia();
        float bob = (float) Math.sin(t * 2.6f) * 1.4f;
        p.halo(clienteX, CLIENTE_PIES + 46, 105,
                pac < 0.25f ? 0xFF6B6B : 0xFFC24A, 0.12f);
        Dibujos.clienteCompleto(p, clienteX, CLIENTE_PIES + bob, CLIENTE_S, actual.getAspecto(), pac);
    }

    /** Chef fijo del jugador: se desplaza por detras del mostrador y extiende el brazo al atender. */
    private void dibujarChef(Pincel p, Cliente actual, float t, boolean caminando) {
        float bob = (float) Math.sin(t * (caminando ? 10f : 2.4f)) * (caminando ? 2.4f : 1f);
        float pies = CHEF_PIES + bob;
        Dibujos.cocinero(p, CHEF_X, pies, CHEF_S, actual != null, animServir);
        float etiquetaY = pies + CHEF_S * 5.9f + 8f;
        p.redondeado(CHEF_X - 24, etiquetaY - 8, 48, 17, 7, 0x1A120C, 0.80f);
        p.textoCentrado("TU", CHEF_X, etiquetaY, 0xFFD166, 0.55f);
    }

    private void dibujarMostrador(Pincel p, Cliente actual, List<Ingrediente> plato, float t, float salto) {
        float y = MOSTRADOR_Y, h = MOSTRADOR_ALTO;
        p.imagenCubriendo(juego.madera, 0, y, FoodFlowGame.ANCHO, h, 0.5f, 0.5f, 0x8A5B35);
        p.rect(0, y, FoodFlowGame.ANCHO, h, 0x1A0D08, 0.34f);
        p.rect(0, y + h - 12, FoodFlowGame.ANCHO, 12, 0xC08A4A, 1f);
        p.rect(0, y + h - 12, FoodFlowGame.ANCHO, 3, 0xFFE1A6, 0.55f);
        p.rect(0, y + h, FoodFlowGame.ANCHO, 3, 0x5A3A1E, 1f);
        for (int x = 40; x < FoodFlowGame.ANCHO; x += 120) {
            p.rect(x, y, 2, h - 12, 0x3A2412, 0.35f);
        }
        p.rect(0, y + h - 30, FoodFlowGame.ANCHO, 1.5f, 0xFFE1A6, 0.18f);
        p.rect(0, y + 8, FoodFlowGame.ANCHO, 3, 0xD9A441, 0.55f);

        // Mostrador vertical blanco que separa al chef (izquierda) de los clientes (derecha).
        float mostradorX = 452f;
        p.rect(mostradorX, y, 18f, SUELO_Y - y, 0xFFFFFF, 1f);
        p.rect(mostradorX - 2f, y, 3f, SUELO_Y - y, 0xF2B33D, 0.9f);
        p.rect(mostradorX + 18f, y, 3f, SUELO_Y - y, 0xF2B33D, 0.9f);

        dibujarTicket(p, plato);

        // Plato que se va armando sobre el mostrador
        p.elipse(PLATO_X, PLATO_Y + 2, 64, 9, 0x090502, 0.35f);
        p.elipse(PLATO_X, PLATO_Y + 5, 64, 9, 0xE8E4D8, 1f);
        p.elipse(PLATO_X, PLATO_Y + 7, 52, 6, 0xCBC5B6, 1f);
        if (!plato.isEmpty()) {
            TipoPlato tipo = actual == null ? TipoPlato.HAMBURGUESA : actual.getTipoPlato();
            Dibujos.plato(p, tipo, PLATO_X, PLATO_Y + 9 + salto, 120, 96, plato);
            Dibujos.vapor(p, PLATO_X, PLATO_Y + 78 + salto, 56, t, 0xFFFFFF, 0.16f);
        } else {
            p.textoCentrado(actual == null ? "Atiende a un cliente" : "Agrega ingredientes",
                    PLATO_X, PLATO_Y + 60, 0xBFAE86, 0.62f);
        }

        if (actual == null) {
            p.elipse(CLIENTE_X, 274, 30, 5, 0x090502, 0.30f);
            p.elipse(CLIENTE_X, 276, 30, 5, 0xE8E4D8, 1f);
            Dibujos.campana(p, CLIENTE_X, 288, 28, 0xD9A441, 0.95f);
            p.textoCentrado("Pulsa ATENDER", CLIENTE_X, 328, 0xFFD166, 0.68f);
        }
    }

    /** Comanda de papel con la pila del plato: el ultimo ingrediente va arriba. */
    private void dibujarTicket(Pincel p, List<Ingrediente> plato) {
        float x = TICKET_X, y = TICKET_Y, w = TICKET_W, h = TICKET_H;
        p.panel(x, y, w, h, 8, 0xFFF3D6, 0xD9B77A, 1f);
        p.redondeado(x + w / 2f - 14, y + h - 6, 28, 10, 4, 0x8A6A42, 1f);
        p.rect(x + w / 2f - 1, y + h + 2, 2, 7, 0x8A6A42, 0.85f);
        p.circulo(x + w / 2f, y + h + 10, 3.2f, 0x3A2412, 0.95f);
        p.textoCentrado("TU PLATO", x + w / 2f, y + h - 22, 0x4A3212, 0.56f);
        p.redondeado(x + w - 38, y + h - 31, 28, 14, 5, 0xF2B33D, 1f);
        p.textoCentrado(plato.size() + "/" + Mundo.MAX_PLATO, x + w - 24, y + h - 24, 0x3A2708, 0.48f);

        if (plato.isEmpty()) {
            p.textoCentrado("Sin", x + w / 2f, y + h / 2f + 6, 0x8A6A42, 0.60f);
            p.textoCentrado("ingredientes", x + w / 2f, y + h / 2f - 6, 0x8A6A42, 0.60f);
            return;
        }
        for (int k = 0; k < plato.size(); k++) {
            int idx = plato.size() - 1 - k;              // el ultimo ingrediente va arriba
            float ry = y + h - 52 - k * 12.5f;
            boolean tope = k == 0;
            p.redondeado(x + 8, ry, w - 16, 11.5f, 4, tope ? 0xF2B33D : 0xE8D7B8, 1f);
            p.redondeado(x + 11, ry + 2, 4, 7.5f, 2, plato.get(idx).getColor(), 1f);
            p.texto((idx + 1) + " " + plato.get(idx).getNombre(), x + 19, ry + 8.8f,
                    tope ? 0x3A2708 : 0x5A4222, 0.42f);
        }
    }

    /** Globo de dialogo del cliente: revela el nombre de la receta y sus ingredientes en orden. */
    private void dibujarPedido(Pincel p, Cliente c) {
        float pop = Math.min(1f, animBurbuja * 4.5f);
        float x = BURBUJA_X, w = BURBUJA_W, h = BURBUJA_H;
        float y = BURBUJA_Y + (1f - pop) * 10f;
        float pac = c.fraccionPaciencia();
        int borde = pac < 0.25f ? 0xFF6B6B : 0xFFC24A;

        p.rectRotado(CLIENTE_X, y + 5f, 20, 20, 45, borde, 1f);
        p.panel(x, y, w, h, 18, 0xFFF7E6, borde, 0.98f);
        p.rectRotado(CLIENTE_X, y + 7f, 14, 14, 45, 0xFFF7E6, 1f);

        p.texto("PEDIDO #" + c.getNumero(), x + 18, y + h - 12, 0x8A6A42, 0.55f);
        String tipo = c.getTipoPlato().getEtiqueta().toUpperCase();
        float tw = p.anchoTexto(tipo, 0.5f) + 18;
        p.redondeado(x + w - 18 - tw, y + h - 28, tw, 16, 6, 0xF2B33D, 1f);
        p.textoCentrado(tipo, x + w - 18 - tw / 2f, y + h - 20, 0x3A2708, 0.50f);
        p.texto(c.getNombreReceta(), x + 18, y + h - 31, 0x3A2708, 0.92f);

        // Solo nombre e imagen del plato: los ingredientes quedan en el recetario.
        Dibujos.plato(p, c.getTipoPlato(), x + w / 2f, y + 30, 96, 56, c.getReceta());

        Dibujos.corazon(p, x + 22, y + 12, 5.5f, pac < 0.25f ? 0xFF6B6B : 0xE5484D, 1f);
        Dibujos.barra(p, x + 34, y + 9, 150, 7, pac);
        String paga = "PAGA $" + c.pagoBase();
        p.texto(paga, x + w - 18 - p.anchoTexto(paga, 0.55f), y + 15, 0x8A6A42, 0.55f);
    }

    private void dibujarHud(Pincel p, Mundo m, List<Cliente> cola, float t) {
        p.degradado(0, HUD_Y, FoodFlowGame.ANCHO, 52, 0x141621, 0x2A2E44);
        p.rect(0, HUD_Y, FoodFlowGame.ANCHO, 2, 0xF2B33D, 0.75f);
        p.rect(0, HUD_Y - 2, FoodFlowGame.ANCHO, 2, 0x07080D, 0.75f);

        p.redondeado(16, 559, 92, 30, 10, 0x8A5B0D, 1f);
        p.redondeado(16, 561, 92, 28, 10, 0xF2B33D, 1f);
        p.redondeado(19, 572, 86, 14, 7, 0xFFFFFF, 0.17f);
        p.textoCentrado("DIA " + m.getDia(), 62, 575, 0x3A2708, 0.95f);

        Dibujos.moneda(p, 138, 574, 13);
        p.texto("$" + m.getMonedas(), 157, 581, 0xFFD166, 1.1f);

        if (m.getVidas() <= 2) {
            p.halo(325, 574, 78, 0xFF6B6B, 0.18f + 0.12f * (float) Math.sin(t * 5f));
        }
        for (int i = 0; i < Mundo.VIDAS_INICIALES; i++) {
            int color = i < m.getVidas() ? 0xFF6B6B : 0x3A3E50;
            Dibujos.corazon(p, 272 + i * 25, 574, 10.5f, color, 1f);
        }

        Dibujos.reloj(p, 414, 574, 12, 0xBFD7FF, 0x191C2B);
        float fracTiempo = Math.max(0f, Math.min(1f, m.getTiempoDia() / Mundo.DURACION_DIA));
        Dibujos.barra(p, 436, 568, 220, 12, m.getTiempoDia() > 0 ? fracTiempo : 0f);
        if (m.getTiempoDia() > 0) {
            p.textoCentrado((int) Math.ceil(m.getTiempoDia()) + " s", 700, 574, 0xFFFFFF, 0.78f);
        } else {
            p.textoCentrado("Ultimos clientes", 700, 574, 0x8EE39B, 0.62f);
        }

        p.redondeado(786, 561, 96, 27, 9, 0x1C1F2E, 0.85f);
        p.textoCentrado("COLA " + cola.size() + "/" + Mundo.CAPACIDAD_COLA, 834, 575,
                0xFFD166, 0.56f);
        p.tecla("P", 920, 574);
    }

    private void dibujarCocina(Pincel p, Mundo m, Cliente actual, List<Ingrediente> plato, float t) {
        p.degradado(0, 0, FoodFlowGame.ANCHO, ESCENA_Y, 0x17131A, 0x2A232B);
        p.rect(0, ESCENA_Y - 3, FoodFlowGame.ANCHO, 3, 0x07080D, 0.75f);
        p.rect(0, ESCENA_Y - 2, FoodFlowGame.ANCHO, 1.5f, 0xF2B33D, 0.28f);

        if (actual == null) {
            dibujarCocinaCerrada(p, m, t);
        } else {
            if (m.getMensaje().isEmpty()) {
                p.redondeado(20, 184, 920, 28, 9, 0x0B0D13, 0.55f);
                p.rect(32, 209, 896, 1, 0xF2B33D, 0.22f);
                p.texto("INGREDIENTES", 30, 197, 0xD8C39A, 0.56f);
                String cuenta = m.getDespensa().getTamano() + "/" + Catalogo.TODOS.size();
                p.texto(cuenta, 928 - p.anchoTexto(cuenta, 0.56f), 197, 0xD8C39A, 0.56f);
                int paginas = paginas(m);
                if (paginas > 1) {
                    String pagina = "PAG " + (paginaIngredientes + 1) + "/" + paginas;
                    p.texto(pagina, 820 - p.anchoTexto(pagina, 0.52f), 197, 0x9AA0B4, 0.52f);
                    dibujarFlechaPagina(p, PAG_IZQ_X, false, paginaIngredientes > 0);
                    dibujarFlechaPagina(p, PAG_DER_X, true, paginaIngredientes + 1 < paginas);
                }
            }

            p.redondeado(14, 68, 932, 114, 18, 0x0E0C12, 0.55f);
            boolean puedeAgregar = plato.size() < Mundo.MAX_PLATO;
            for (int i = 0; i < visibles.size(); i++) {
                Dibujos.botonIngrediente(p, visibles.get(i).getNombre(), bx(i), by(i), BW, BH,
                        visibles.get(i), puedeAgregar, etiquetaTecla(i));
            }
        }

        boolean listoParaEntregar = actual != null && !plato.isEmpty()
                && plato.size() == actual.getReceta().size() && m.platoCoincide();
        botonAccion(p, ATENDER_X, AY, AW, AH, "ATENDER", "ESPACIO", 0x4D96FF,
                actual == null && !m.getCola().isEmpty(), 0, false);
        botonAccion(p, DESHACER_X, AY, AW, AH, "DESHACER", "BORRAR", 0xF2994A,
                !plato.isEmpty(), 1, false);
        botonAccion(p, RECETAS_X, AY, AW, AH, "RECETAS", "R", 0xB69CF2,
                true, 2, false);
        botonAccion(p, ENTREGAR_X, AY, AW, AH, "ENTREGAR", "ESPACIO", 0x6BCB77,
                actual != null && !plato.isEmpty(), 3, listoParaEntregar);
    }

    /** Sin pedido activo no se muestran los ingredientes: solo una cocina cerrada con la pista. */
    private void dibujarCocinaCerrada(Pincel p, Mundo m, float t) {
        if (m.getMensaje().isEmpty()) {
            p.redondeado(20, 184, 920, 28, 9, 0x0B0D13, 0.55f);
            p.rect(32, 209, 896, 1, 0xF2B33D, 0.22f);
            p.texto("TU COCINA", 30, 197, 0xD8C39A, 0.56f);
            String estado = m.getCola().isEmpty() ? "Esperando clientes..."
                    : "Los ingredientes se revelan con el pedido";
            p.texto(estado, 928 - p.anchoTexto(estado, 0.52f), 197, 0x9AA0B4, 0.52f);
        }

        float x = 24, y = 70, w = 912, h = 114;
        p.panel(x, y, w, h, 18, 0x15121A, 0x3E4560, 0.96f);
        p.elipse(x + 92, y + 28, 54, 8, 0x090502, 0.50f);
        p.elipse(x + 92, y + 31, 54, 8, 0xE8E4D8, 1f);
        p.halo(x + 92, y + 62, 70, 0xFFC24A, 0.16f + 0.04f * (float) Math.sin(t * 2f));
        Dibujos.campana(p, x + 92, y + 62, 64, 0xD9A441, 0.90f);

        p.texto("La cocina espera un pedido", x + 176, y + 76, 0xFFD166, 0.92f);
        String linea = m.getCola().isEmpty()
                ? "Cuando llegue un cliente, pulsa ATENDER"
                : "Pulsa ATENDER para ver su receta y sus ingredientes";
        p.texto(linea, x + 176, y + 50, 0xB9BED0, 0.66f);
        p.textoCentrado("?   ?   ?", x + 760, y + 62, 0x4A4F63, 0.90f);
    }

    private void dibujarMensaje(Pincel p, Mundo m) {
        String msg = m.getMensaje();
        if (msg.isEmpty()) return;
        boolean malo = msg.contains("incorrecto") || msg.contains("canso") || msg.contains("lleno")
                || msg.contains("agoto") || msg.contains("perdio");
        boolean bueno = !malo && (msg.contains("correcto") || msg.contains("Dia") || msg.startsWith("Pedido"));
        int borde = bueno ? 0x6BD66B : malo ? 0xFF6B6B : 0xF2B33D;
        float esc = 0.72f + 0.13f * Math.min(1f, tiempoMensaje * 8f);
        float anchoPanel = Math.min(780f, p.anchoTexto(msg, esc) + 48f);
        float mx = 480 - anchoPanel / 2f;
        p.panel(mx, 182, anchoPanel, 31, 15, 0x16131E, borde, 0.97f);
        p.circulo(mx + 18, 197.5f, 8, borde, 0.95f);
        if (bueno) Dibujos.check(p, mx + 18, 197.5f, 9, 0x10220F, 1f);
        else if (malo) p.textoCentrado("!", mx + 18, 198, 0x1A0E0E, 0.62f);
        else Dibujos.destello(p, mx + 18, 197.5f, 11, 0x2A1B08, 0.90f);
        p.textoCentrado(msg, mx + anchoPanel / 2f + 8, 198, 0xFFF3D6, esc);
    }

    private void botonAccion(Pincel p, float x, float y, float w, float h, String texto, String tecla,
                             int color, boolean activo, int icono, boolean resaltado) {
        float alfa = activo ? 1f : 0.38f;
        boolean hover = activo && p.ratonDentro(x, y, w, h);
        float dy = hover ? 2f : 0f;
        float radio = 15f;
        if (resaltado) {
            float pulso = 0.5f + 0.5f * (float) Math.sin(p.getTiempo() * 4.5f);
            p.halo(x + w / 2f, y + h / 2f, w * 0.58f, color, 0.20f + 0.16f * pulso);
        }
        p.redondeado(x, y - 5 + dy, w, h, radio, 0x05060A, 0.45f * alfa);
        p.redondeado(x, y + dy - 1f, w, h + 2f, radio + 1, Pincel.mezclarRgb(color, 0x000000, 0.45f), alfa);
        p.redondeado(x, y + dy, w, h, radio, color, alfa);
        p.redondeado(x + 3, y + dy + h * 0.46f, w - 6, h * 0.48f, radio - 3,
                0xFFFFFF, (hover ? 0.15f : 0.08f) * alfa);
        p.redondeado(x + 3, y + dy + 2, w - 6, h * 0.26f, radio - 3,
                Pincel.mezclarRgb(color, 0x000000, 0.30f), 0.28f * alfa);
        p.rect(x + radio, y + dy + h - 2, w - 2 * radio, 1.4f, 0xFFFFFF, 0.30f * alfa);

        int tinta = Pincel.tinta(color);
        p.circulo(x + 34, y + dy + h / 2f, 17, 0x000000, 0.16f * alfa);
        if (icono == 0) Dibujos.campana(p, x + 34, y + dy + h / 2f, 30, tinta, alfa);
        else if (icono == 1) Dibujos.flechaIzquierda(p, x + 34, y + dy + h / 2f, 26, tinta, alfa);
        else if (icono == 2) Dibujos.libro(p, x + 34, y + dy + h / 2f, 28, tinta, alfa);
        else Dibujos.check(p, x + 34, y + dy + h / 2f, 25, tinta, alfa);

        p.textoCentrado(texto, x + w / 2f - 6, y + dy + h / 2f + 1, tinta, 0.96f, alfa);
        p.setAlfa(alfa);
        p.tecla(tecla, x + w - 33, y + dy + h / 2f);
        p.setAlfa(1f);
    }

    /** Flecha de la paginacion de ingredientes; se enciende cuando hay una pagina en esa direccion. */
    private void dibujarFlechaPagina(Pincel p, float cx, boolean derecha, boolean activa) {
        boolean hover = activa && p.ratonDentro(cx - PAG_R, PAG_Y - PAG_R, PAG_R * 2, PAG_R * 2);
        int color = activa ? (hover ? 0xFFE29A : 0xF2B33D) : 0x4A4F63;
        p.circulo(cx, PAG_Y, PAG_R, 0x0B0D13, activa ? 0.9f : 0.5f);
        p.circulo(cx, PAG_Y, PAG_R - 1.5f, color, activa ? 1f : 0.45f);
        if (derecha) Dibujos.flechaDerecha(p, cx, PAG_Y, 21, 0x2A1B08, activa ? 1f : 0.5f);
        else Dibujos.flechaIzquierda(p, cx, PAG_Y, 21, 0x2A1B08, activa ? 1f : 0.5f);
    }

    private void dibujarPausa(Pincel p, float t) {
        p.rect(0, 0, FoodFlowGame.ANCHO, FoodFlowGame.ALTO, 0x080A10, 0.78f);
        p.halo(480, 320, 220, 0xFFC24A, 0.12f);
        p.panel(260, 145, 440, 310, 22, 0x24283A, 0x596080, 0.99f);
        p.textoCentradoContorno("PAUSA", 480, 410, 0xFFD166, 2.2f, 0x1A1208);
        p.textoCentrado("La paciencia de tus clientes esta en espera", 480, 372, 0xB9BED0, 0.78f);

        float[] filas = {348, 320, 292, 264, 236};
        String[] teclas = {"ESPACIO", "BORRAR", "1-0, Q, E", "IZQ / DER", "R"};
        String[] textos = {"Atender / entregar", "Deshacer ingrediente", "Agregar ingrediente",
                "Cambiar pagina de ingredientes", "Ver recetas"};
        for (int i = 0; i < filas.length; i++) {
            p.tecla(teclas[i], 348, filas[i]);
            p.texto(textos[i], 390, filas[i] + 4, 0xFFF3D6, 0.78f);
        }

        p.boton("CONTINUAR", PAUSA_X, PAUSA_Y, PAUSA_W, PAUSA_H, 0xFFC24A, true, true);
        p.textoCentrado("Pulsa P para seguir", 480, 156, 0x9AA0B4, 0.65f);
    }
}
