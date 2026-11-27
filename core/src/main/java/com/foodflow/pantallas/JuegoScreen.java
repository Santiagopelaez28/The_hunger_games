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

/** Pantalla principal: cola de clientes, pedido en atencion, plato (pila) y botones de ingredientes. */
public class JuegoScreen extends PantallaBase {
    private static final int MAX_BOTONES = 12;

    // Botones de ingredientes (6 por fila) y botones de accion
    private static final float BW = 144, BH = 46;
    private static final float AY = 14, AH = 52, AW = 218;
    private static final float ATENDER_X = 18, DESHACER_X = 248, RECETAS_X = 478, ENTREGAR_X = 708;

    // Boton para salir de la pausa
    private static final float PAUSA_X = 380, PAUSA_Y = 178, PAUSA_W = 200, PAUSA_H = 46;

    // Flechas para recorrer la despensa cuando hay mas de MAX_BOTONES ingredientes
    private static final float PAG_Y = 198, PAG_IZQ_X = 844, PAG_DER_X = 878, PAG_R = 14;

    // Parte del cielo que se ve detras de los clientes (0 = franja de arriba)
    private static final float RECORTE_CIELO = 0.04f;

    private boolean pausado = false;
    private float brinco = 0f;
    private int tamPlatoAnterior = 0;
    private String mensajeVisto = "";
    private float tiempoMensaje = 0f;

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
        }

        if (m.getEstado() == Mundo.Estado.MEJORAS) { juego.setScreen(new MejorasScreen(juego)); return; }
        if (m.getEstado() == Mundo.Estado.DIA_TERMINADO) { juego.setScreen(new TiendaScreen(juego)); return; }
        if (m.getEstado() == Mundo.Estado.FIN_JUEGO) { juego.setScreen(new FinScreen(juego)); return; }

        dibujar(m, dt);
    }

    // ------------------------------------------------------------------ entrada

    private void accionPrincipal(Mundo m) {
        if (m.getEnAtencion() == null) m.atenderSiguiente();
        else if (!m.getPlato().isEmpty()) m.entregar();
    }

    private boolean leerEntrada(Mundo m) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            abrirRecetario();
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) anteriorPagina();
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) siguientePagina();

        for (int i = 0; i < visibles.size(); i++) {
            if (Gdx.input.isKeyJustPressed(teclaDe(i))) m.agregarIngrediente(visibles.get(i));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) accionPrincipal(m);
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) m.deshacer();

        if (hayClick()) {
            if (dentro(toque, RECETAS_X, AY, AW, AH)) {
                abrirRecetario();
                return true;
            }
            if (paginaIngredientes > 0 && dentro(toque, PAG_IZQ_X - PAG_R, PAG_Y - PAG_R,
                    PAG_R * 2, PAG_R * 2)) {
                anteriorPagina();
                return false;
            }
            if (paginaIngredientes + 1 < paginas(m) && dentro(toque, PAG_DER_X - PAG_R,
                    PAG_Y - PAG_R, PAG_R * 2, PAG_R * 2)) {
                siguientePagina();
                return false;
            }
            for (int i = 0; i < visibles.size(); i++) {
                if (dentro(toque, bx(i), by(i), BW, BH)) m.agregarIngrediente(visibles.get(i));
            }
            if (dentro(toque, ATENDER_X, AY, AW, AH)) m.atenderSiguiente();
            if (dentro(toque, DESHACER_X, AY, AW, AH)) m.deshacer();
            if (dentro(toque, ENTREGAR_X, AY, AW, AH) && !m.getPlato().isEmpty()) m.entregar();
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

        empezarFrame();

        // ---------------------------------------------------------- escenario
        p.imagenCubriendo(juego.nubes, 0, 410, 960, 138, 0.5f, RECORTE_CIELO, 0x8EDBF9);
        p.halo(888, 518, 78, 0xFFE29A, 0.35f);
        p.imagenCubriendo(juego.madera, 0, 220, 960, 194, 0.5f, 0f, 0xD9A441);
        p.degradadoAlpha(0, 220, 960, 194, 0x2A1608, 0.24f, 0f);
        p.rect(0, 412, 960, 5, 0x4A2F12, 0.92f);
        p.rect(0, 412, 960, 2, 0xFFE1A6, 0.32f);
        p.rect(0, 218, 960, 4, 0x120B05, 0.65f);
        // zona de preparacion: madera oscura con el mismo tono del mostrador
        p.imagenCubriendo(juego.madera, 0, 0, 960, 218, 0.5f, 0f, 0x3A2412);
        p.rect(0, 0, 960, 218, 0x0E1018, 0.68f);
        p.degradadoAlpha(0, 0, 960, 218, 0x080A10, 0.46f, 0.04f);
        p.rect(0, 216, 960, 2, 0xF2B33D, 0.14f);

        // ---------------------------------------------------------- HUD
        p.degradado(0, 548, 960, 52, 0x141621, 0x2A2E44);
        p.rect(0, 548, 960, 2, 0xF2B33D, 0.75f);
        p.rect(0, 546, 960, 2, 0x07080D, 0.75f);

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

        Dibujos.reloj(p, 424, 574, 12, 0xBFD7FF, 0x191C2B);
        float fracTiempo = Math.max(0f, Math.min(1f, m.getTiempoDia() / m.getDuracionDiaTotal()));
        Dibujos.barra(p, 446, 568, 300, 12, m.getTiempoDia() > 0 ? fracTiempo : 0f);
        if (m.getTiempoDia() > 0) {
            p.textoCentrado((int) Math.ceil(m.getTiempoDia()) + " s", 770, 574, 0xFFFFFF, 0.78f);
        } else {
            p.textoCentrado("Ultimos clientes", 770, 574, 0x8EE39B, 0.66f);
        }
        p.tecla("P", 920, 574);

        // ---------------------------------------------------------- cola de clientes
        for (int i = 0; i < Mundo.CAPACIDAD_COLA; i++) {
            float x = 24 + i * 184, y = 418, w = 168, h = 114;
            Cliente c = i < cola.size() ? cola.get(i) : null;
            boolean siguiente = i == 0 && c != null;
            float animo = c == null ? 1f : c.fraccionPaciencia();

            if (siguiente) {
                p.halo(x + w / 2f, y + h / 2f, 100f, 0xFFC24A, 0.20f + 0.06f * (float) Math.sin(t * 3f));
            }
            if (c != null && animo < 0.25f) {
                p.halo(x + w / 2f, y + h / 2f, 92f, 0xFF6B6B, 0.16f + 0.10f * (float) Math.sin(t * 8f));
            }

            p.panel(x, y, w, h, 16, siguiente ? 0xFFF0CC : 0xFFF7E6,
                    siguiente ? 0xFFC24A : 0xD9C79B, 0.98f);

            if (siguiente) {
                p.redondeado(x + w - 94, y + h - 25, 86, 17, 6, 0xF2B33D, 1f);
                p.textoCentrado("SIGUIENTE", x + w - 51, y + h - 16.5f, 0x3A2708, 0.55f);
            }

            if (c == null) {
                p.circulo(x + w / 2f, y + 52, 22, 0xBFAE86, 0.16f);
                p.redondeado(x + w / 2f - 24, y + 22, 48, 28, 14, 0xBFAE86, 0.13f);
                p.textoCentrado("Libre", x + w / 2f, y + 32, 0x8B7B55, 0.78f);
                continue;
            }

            float bob = (float) Math.sin(t * 3f + i * 0.7f) * 1.5f;
            Dibujos.cliente(p, x + 48, y + 70 + bob, 18f, c.getAspecto(), animo);

            // Etiqueta para identificar a los clientes VIP
            if (c.getNivel() != null && c.getNivel().esVIP()) {
                p.textoCentrado(
                c.getNivel().etiqueta,
                x + 48,
                y + 96,
                c.getNivel().colorGlobo,
                0.48f
                );
            }

            // mini plato del pedido, con la forma de su tipo
            p.elipse(x + 121, y + 37, 27, 5, 0x1A1208, 0.18f);
            Dibujos.plato(p, c.getTipoPlato(), x + 121, y + 38, 55, 46, c.getReceta());

            Dibujos.barra(p, x + 12, y + 10, w - 24, 9, animo);
        }
        // La sala ampliada admite clientes adicionales; se indica cuántos esperan fuera de las cinco tarjetas visibles.
        if (cola.size() > Mundo.CAPACIDAD_COLA) {
            int extra = cola.size() - Mundo.CAPACIDAD_COLA;
            p.textoCentrado("+" + extra + " CLIENTE" + (extra == 1 ? "" : "S") + " EN ESPERA",
                    480, 405, 0xFFD166, 0.60f);
        }

        // ---------------------------------------------------------- mostrador: cliente y comanda
        float cx = 24, cy = 228, cw = 472, ch = 182;
        p.panel(cx, cy, cw, ch, 18, 0x2B1B11, 0x6B4A26, 0.97f);

        if (actual == null) {
            p.texto("MOSTRADOR", 40, 399, 0xD8B98A, 0.62f);
            p.circulo(100, 330, 46, 0x3B2A1C, 0.95f);
            p.circulo(100, 330, 41, 0x241811, 1f);
            Dibujos.campana(p, 100, 330, 38, 0xC9A86A, 0.35f);
            Dibujos.barra(p, 34, 250, 132, 10, 0f);
            p.textoCentrado("Nadie en el mostrador", 320, 338, 0xFFF3D6, 1.05f);
            p.textoCentrado("Pulsa ATENDER o ESPACIO", 320, 306, 0xFFD166, 0.82f);
        } else {
            p.texto("CLIENTE #" + actual.getNumero(), 40, 399, 0xFFD166, 0.72f);
            p.halo(100, 330, 60, 0xFFC24A, 0.18f);
            p.circulo(100, 330, 46, 0x3B2A1C, 0.95f);
            p.circulo(100, 330, 42, 0x241811, 1f);
            float bob = (float) Math.sin(t * 2.5f) * 1.5f;
            Dibujos.cliente(p, 100, 346 + bob, 25f, actual.getAspecto(), actual.fraccionPaciencia());
            p.texto("PACIENCIA", 36, 262, 0xD8B98A, 0.55f);
            Dibujos.barra(p, 34, 244, 132, 10, actual.fraccionPaciencia());

            // comanda de papel: solo el nombre de la receta y una imagen del plato;
            // la descripcion completa se busca en RECETAS para prepararla.
            float tx = 176, ty = 240, tw = 304, th = 158;
            p.panel(tx, ty, tw, th, 12, 0xFFF3D6, 0xD9B77A, 1f);
            p.redondeado(tx + 10, ty + th - 7, tw - 20, 2, 1, 0xFFFFFF, 0.40f);
            p.texto("RECETA: " + actual.getNombreReceta(), 190, 364, 0x4A3212, 0.95f);
            p.textoCentrado("Busca en RECETAS: " + actual.getNombreReceta(), tx + tw / 2f, 386,
                    0x8A6A42, 0.62f);
            Dibujos.plato(p, actual.getTipoPlato(), tx + tw / 2f, 298, 116, 70, actual.getReceta());
            p.redondeado(380, 369, 84, 21, 8, 0xF2B33D, 1f);
            p.textoCentrado("PAGA $" + actual.pagoBase(), 422, 379.5f, 0x3A2708, 0.62f);

            String estado;
            int colorEstado;
            if (plato.isEmpty()) {
                estado = "Agrega ingredientes";
                colorEstado = 0x8A6A42;
            } else if (!m.platoCoincide()) {
                estado = "Algo no coincide: usa DESHACER";
                colorEstado = 0xC0392B;
            } else if (plato.size() == actual.getReceta().size()) {
                estado = "Listo! Pulsa ENTREGAR";
                colorEstado = 0x2E7D32;
            } else {
                estado = "Vas bien, sigue la receta";
                colorEstado = 0x8A6A42;
            }
            p.circulo(194, 249, 4, colorEstado, 1f);
            p.texto(estado, 204, 252, colorEstado, 0.68f);
        }

        // ---------------------------------------------------------- plato y pila
        float px = 508, py = 228, pw = 428, ph = 182;
        p.panel(px, py, pw, ph, 18, 0x2B1B11, 0x6B4A26, 0.97f);
        p.texto("TU PLATO", px + 16, 399, 0xD8B98A, 0.64f);
        p.redondeado(px + pw - 56, 386, 40, 19, 7, 0x1A120C, 0.8f);
        p.textoCentrado(plato.size() + "/" + Mundo.MAX_PLATO, px + pw - 36, 395.5f,
                plato.size() >= Mundo.MAX_PLATO ? 0xFFB86B : 0xFFF3D6, 0.6f);

        p.halo(644, 312, 110, 0xFFB347, 0.18f);
        p.elipse(644, 248, 132, 20, 0x090502, 0.50f);
        p.elipse(644, 254, 132, 20, 0xE8E4D8);
        p.elipse(644, 257, 108, 14, 0xCBC5B6);
        TipoPlato tipoPlato = actual == null ? TipoPlato.HAMBURGUESA : actual.getTipoPlato();
        Dibujos.plato(p, tipoPlato, 644, 260 + salto, 176, 138, plato);

        if (plato.isEmpty()) {
            p.textoCentrado(actual == null ? "Atiende a un cliente" : "Agrega ingredientes",
                    644, 330, 0xB6A98C, 0.88f);
        }

        p.texto("ORDEN", 790, 399, 0xD8B98A, 0.55f);
        for (int k = 0; k < Mundo.MAX_PLATO; k++) {
            float sy = 250 + k * 16f;
            boolean llena = k < plato.size();
            boolean tope = k == plato.size() - 1;
            if (!llena) {
                p.redondeado(790, sy, 130, 13, 4, 0x1A120C, 0.42f);
                p.rect(798, sy + 6, 5, 1.5f, 0x7A6440, 0.35f);
                continue;
            }
            if (tope) p.redondeado(789, sy - 1, 132, 15, 5, 0xF2B33D, 0.92f);
            p.redondeado(790, sy, 130, 13, 4, 0x241811, 1f);
            p.redondeado(793, sy + 2, 5, 9, 2, plato.get(k).getColor(), 1f);
            p.texto(plato.get(k).getNombre(), 802, sy + 10.5f, tope ? 0xFFD166 : 0xE8DFC9, 0.62f);
        }

        // ---------------------------------------------------------- mensaje flotante
        String msg = m.getMensaje();
        if (!msg.isEmpty()) {
            boolean malo = msg.contains("incorrecto") || msg.contains("canso") || msg.contains("lleno")
                    || msg.contains("agoto") || msg.contains("perdio");
            boolean bueno = !malo && (msg.contains("correcto") || msg.contains("Dia") || msg.startsWith("Pedido"));
            int borde = bueno ? 0x6BD66B : malo ? 0xFF6B6B : 0xF2B33D;
            float esc = 0.72f + 0.13f * Math.min(1f, tiempoMensaje * 8f);
            float anchoPanel = Math.min(780f, p.anchoTexto(msg, esc) + 48f);
            float mx = 480 - anchoPanel / 2f;
            p.panel(mx, 184, anchoPanel, 31, 15, 0x16131E, borde, 0.97f);
            p.circulo(mx + 18, 199.5f, 8, borde, 0.95f);
            if (bueno) Dibujos.check(p, mx + 18, 199.5f, 9, 0x10220F, 1f);
            else if (malo) p.textoCentrado("!", mx + 18, 200, 0x1A0E0E, 0.62f);
            else Dibujos.destello(p, mx + 18, 199.5f, 11, 0x2A1B08, 0.9f);
            p.textoCentrado(msg, mx + anchoPanel / 2f + 8, 200, 0xFFF3D6, esc);
        }

        // ---------------------------------------------------------- ingredientes
        if (msg.isEmpty()) {
            p.redondeado(20, 185, 920, 27, 9, 0x0B0D13, 0.42f);
            p.rect(32, 210, 896, 1, 0xF2B33D, 0.22f);
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
            if (m.getDia() == 1) {
                String pista = "La tienda abre al terminar el dia";
                float anchoPista = p.anchoTexto(pista, 0.62f);
                float hx = 480 - anchoPista / 2f;
                Dibujos.destello(p, hx - 17, 101, 11, 0xE8B85C, 0.55f);
                p.texto(pista, hx, 105, 0x8E8470, 0.62f);
            }
        }
        boolean puedeAgregar = actual != null && plato.size() < Mundo.MAX_PLATO;
        for (int i = 0; i < visibles.size(); i++) {
            Dibujos.botonIngrediente(p, visibles.get(i).getNombre(), bx(i), by(i), BW, BH,
                    visibles.get(i), puedeAgregar, etiquetaTecla(i));
        }

        // ---------------------------------------------------------- acciones
        boolean listoParaEntregar = actual != null && !plato.isEmpty()
                && plato.size() == actual.getReceta().size() && m.platoCoincide();
        botonAccion(p, ATENDER_X, AY, AW, AH, "ATENDER", "ESPACIO", 0x4D96FF,
                actual == null && !cola.isEmpty(), 0, false);
        botonAccion(p, DESHACER_X, AY, AW, AH, "DESHACER", "RETROCESO", 0xF2994A,
                !plato.isEmpty(), 1, false);
        botonAccion(p, RECETAS_X, AY, AW, AH, "RECETAS", "R", 0xB69CF2,
                true, 2, false);
        botonAccion(p, ENTREGAR_X, AY, AW, AH, "ENTREGAR", "ESPACIO", 0x6BCB77,
                actual != null && !plato.isEmpty(), 3, listoParaEntregar);

        // ---------------------------------------------------------- aviso de pocas vidas
        if (m.getVidas() <= 2) {
            float a = 0.07f + 0.05f * (float) Math.sin(t * 5f);
            p.degradadoAlpha(0, 0, 960, 50, 0xFF3B3B, a, 0f);
            p.degradadoAlpha(0, 550, 960, 50, 0xFF3B3B, 0f, a);
        }

        if (pausado) dibujarPausa(p, t);

        terminarFrame();
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
        p.rect(0, 0, 960, 600, 0x080A10, 0.78f);
        p.halo(480, 320, 220, 0xFFC24A, 0.12f);
        p.panel(260, 145, 440, 310, 22, 0x24283A, 0x596080, 0.99f);
        p.textoCentradoContorno("PAUSA", 480, 410, 0xFFD166, 2.2f, 0x1A1208);
        p.textoCentrado("La paciencia de tus clientes esta en espera", 480, 372, 0xB9BED0, 0.78f);

        float[] filas = {348, 320, 292, 264, 236};
        String[] teclas = {"ESPACIO", "RETROCESO", "1-0, Q, E", "IZQ / DER", "R"};
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
