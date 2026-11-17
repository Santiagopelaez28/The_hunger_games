package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foodflow.FoodFlowGame;
import com.foodflow.logica.Mundo;
import com.foodflow.modelo.Cliente;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.ui.Dibujos;
import com.foodflow.ui.Pincel;

import java.util.List;

/** Pantalla principal: cola de clientes, pedido en atención, plato (pila) y botones de ingredientes. */
public class JuegoScreen extends PantallaBase {
    private static final int[] COLORES_CLIENTE = {0x6FB1FC, 0xF08BA4, 0x9BE08F, 0xF2B66D, 0xB69CF2, 0x7FD6C9};
    private static final int MAX_BOTONES = 10;

    // Botones de ingredientes (6 por fila) y botones de acción
    private static final float BW = 140, BH = 44;
    private static final float AY = 26, AH = 44, AW = 200;
    private static final float ATENDER_X = 30, DESHACER_X = 250, ENTREGAR_X = 470;

    // Parte del cielo que se ve detrás de los clientes (0 = franja de arriba, donde está la nube grande)
    private static final float RECORTE_CIELO = 0.04f;

    private boolean pausado = false;

    public JuegoScreen(FoodFlowGame juego) { super(juego); }

    private static float bx(int i) { return 30 + (i % 6) * 150; }
    private static float by(int i) { return 140 - (i / 6) * 52; }

    @Override
    public void render(float dt) {
        Mundo m = juego.mundo;

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) pausado = !pausado;
        if (!pausado) {
            leerEntrada(m);
            m.actualizar(Math.min(dt, 0.05f));   // tope de 50 ms: un tirón de la ventana no rompe la partida
        }

        if (m.getEstado() == Mundo.Estado.DIA_TERMINADO) { juego.setScreen(new TiendaScreen(juego)); return; }
        if (m.getEstado() == Mundo.Estado.FIN_JUEGO) { juego.setScreen(new FinScreen(juego)); return; }

        dibujar(m);
    }

    // ------------------------------------------------------------------ entrada

    private void accionPrincipal(Mundo m) {
        if (m.getEnAtencion() == null) m.atenderSiguiente();
        else if (!m.getPlato().isEmpty()) m.entregar();
    }

    private void leerEntrada(Mundo m) {
        List<Ingrediente> ing = m.getDisponibles();

        for (int i = 0; i < ing.size() && i < MAX_BOTONES; i++) {
            int tecla = i < 9 ? Input.Keys.NUM_1 + i : Input.Keys.NUM_0;
            if (Gdx.input.isKeyJustPressed(tecla)) m.agregarIngrediente(ing.get(i));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) accionPrincipal(m);
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) m.deshacer();

        if (hayClick()) {
            for (int i = 0; i < ing.size() && i < MAX_BOTONES; i++) {
                if (dentro(toque, bx(i), by(i), BW, BH)) m.agregarIngrediente(ing.get(i));
            }
            if (dentro(toque, ATENDER_X, AY, AW, AH)) m.atenderSiguiente();
            if (dentro(toque, DESHACER_X, AY, AW, AH)) m.deshacer();
            if (dentro(toque, ENTREGAR_X, AY, AW, AH) && !m.getPlato().isEmpty()) m.entregar();
        }
    }

    // ------------------------------------------------------------------ dibujo

    private static void barra(Pincel p, float x, float y, float w, float h, float fraccion) {
        p.rect(x, y, w, h, 0x3A3E50);
        int color = fraccion > 0.5f ? 0x6BD66B : fraccion > 0.25f ? 0xF2C230 : 0xFF6B6B;
        p.rect(x, y, w * fraccion, h, color);
    }

    private void dibujar(Mundo m) {
        Pincel p = juego.pincel;
        List<Cliente> cola = m.getCola();
        List<Ingrediente> plato = m.getPlato();
        List<Ingrediente> ing = m.getDisponibles();
        Cliente actual = m.getEnAtencion();

        empezarFrame();

        // Fondos: cielo con nubes detrás de los clientes y madera en el mostrador
        p.imagenCubriendo(juego.nubes, 0, 424, 960, 136, 0.5f, RECORTE_CIELO, 0x8EDBF9);
        p.imagenCubriendo(juego.madera, 0, 205, 960, 219, 0.5f, 0f, 0xD9A441);
        p.rect(0, 419, 960, 6, 0x6B4A1E);   // borde del mostrador
        p.rect(0, 205, 960, 4, 0x4A3212);   // sombra de abajo

        // HUD
        p.rect(0, 560, 960, 40, 0x1B1D26);
        p.texto("Dia " + m.getDia(), 20, 588, 0xFFFFFF);
        p.texto("Monedas: $" + m.getMonedas(), 130, 588, 0xFFD166);
        p.texto("Vidas: " + m.getVidas(), 340, 588, m.getVidas() <= 2 ? 0xFF6B6B : 0xFFFFFF);
        if (m.getTiempoDia() > 0) {
            p.texto("Tiempo: " + (int) Math.ceil(m.getTiempoDia()) + " s", 480, 588, 0xFFFFFF);
        } else {
            p.texto("Atiende a los ultimos clientes", 480, 588, 0x8EE39B);
        }

        // Clientes en orden de llegada: el primero (el que se atiende) tiene borde dorado y la etiqueta SIGUIENTE
        for (int i = 0; i < Mundo.CAPACIDAD_COLA; i++) {
            float x = 30 + i * 150, y = 430;
            p.rect(x, y, 135, 95, 0xFFFFFF, 0.25f);
            if (i == 0 && !cola.isEmpty()) {
                p.rect(x - 3, y + 95, 141, 3, 0xFFD166);
                p.rect(x - 3, y - 3, 141, 3, 0xFFD166);
                p.rect(x - 3, y - 3, 3, 101, 0xFFD166);
                p.rect(x + 135, y - 3, 3, 101, 0xFFD166);
                p.rect(x - 3, y + 98, 72, 15, 0xFFD166);
                p.textoCentrado("SIGUIENTE", x + 33, y + 105.5f, 0x1B1D26, 0.6f);
            }
            if (i < cola.size()) {
                Cliente c = cola.get(i);
                Dibujos.cliente(p, x + 67, y + 62, 17, c.getAspecto(), c.fraccionPaciencia());
                p.texto("#" + c.getNumero(), x + 6, y + 91, 0x2B4A6B, 0.7f);
                barra(p, x + 10, y + 6, 115, 6, c.fraccionPaciencia());
            }
        }

        // Mostrador: el cliente en atencion y la hamburguesa que pidio
        p.rect(30, 215, 440, 200, 0x2B1B0E, 0.55f);
        if (actual == null) {
            p.textoCentrado("Nadie en el mostrador", 250, 340, 0x9AA0B4, 1f);
            p.textoCentrado("Pulsa ATENDER (ESPACIO)", 250, 305, 0xFFD166, 1f);
        } else {
            float animo = actual.fraccionPaciencia();
            List<Ingrediente> receta = actual.getReceta();
            p.texto("Cliente #" + actual.getNumero() + " pide:", 45, 405, 0xFFFFFF);
            barra(p, 45, 380, 410, 8, animo);
            Dibujos.cliente(p, 105, 325, 24, actual.getAspecto(), animo);

            p.rect(185, 256, 130, 6, 0xE8E8E8);
            Dibujos.hamburguesa(p, 250, 262, 120, 105, receta);

            int aciertos = 0;
            while (aciertos < plato.size() && aciertos < receta.size() && plato.get(aciertos) == receta.get(aciertos)) {
                aciertos++;
            }
            for (int i = 0; i < receta.size(); i++) {
                p.texto((i + 1) + ". " + receta.get(i).getNombre(), 335, 372 - i * 20,
                        i < aciertos ? 0x6BD66B : 0xB9BED0, 0.9f);
            }
            if (!m.platoCoincide()) {
                p.texto("Algo no coincide: usa DESHACER", 45, 245, 0xFF6B6B, 0.95f);
            } else if (plato.size() == receta.size()) {
                p.texto("Listo: pulsa ENTREGAR", 45, 245, 0x6BD66B, 0.95f);
            }
        }

        // Plato: se ve la hamburguesa y, a la derecha, la lista de arriba hacia abajo
        p.rect(545, 215, 390, 200, 0x2B1B0E, 0.55f);
        p.texto("PLATO " + plato.size() + "/" + Mundo.MAX_PLATO, 797, 402, 0xFFFFFF, 0.9f);
        p.elipse(670, 228, 118, 14, 0xE9E9EE);
        p.elipse(670, 230, 96, 9, 0xCFCFD8);
        Dibujos.hamburguesa(p, 670, 232, 190, 175, plato);
        if (plato.isEmpty() && actual != null) {
            p.textoCentrado("Plato vacio: agrega ingredientes", 670, 300, 0x9AA0B4, 0.9f);
        }
        for (int j = plato.size() - 1, fila = 0; j >= 0; j--, fila++) {
            boolean tope = j == plato.size() - 1;
            p.texto(plato.get(j).getNombre() + (tope ? " (tope)" : ""), 797, 378 - fila * 17,
                    tope ? 0xFFD166 : 0xB9BED0, 0.8f);
        }

        // Mensaje de eventos
        if (!m.getMensaje().isEmpty()) p.textoCentrado(m.getMensaje(), 480, 194, 0xFFD166, 1f);

        // Ingredientes disponibles (arreglo dinamico): un boton con icono por cada uno
        boolean puedeAgregar = actual != null && plato.size() < Mundo.MAX_PLATO;
        for (int i = 0; i < ing.size() && i < MAX_BOTONES; i++) {
            Dibujos.botonIngrediente(p, ing.get(i).getNombre(), bx(i), by(i), BW, BH, ing.get(i), puedeAgregar,
                    String.valueOf((i + 1) % 10));
        }

        // Acciones
        p.boton("ATENDER", ATENDER_X, AY, AW, AH, 0x4D96FF, actual == null && !cola.isEmpty());
        p.boton("DESHACER", DESHACER_X, AY, AW, AH, 0xF2994A, !plato.isEmpty());
        p.boton("ENTREGAR", ENTREGAR_X, AY, AW, AH, 0x6BCB77, actual != null && !plato.isEmpty());
        p.texto("ESPACIO atender/entregar   RETROCESO deshacer   1-9 y 0 ingredientes   P pausa",
                30, 18, 0x7C8197, 0.8f);

        if (pausado) {
            p.rect(0, 0, 960, 600, 0x000000, 0.65f);
            p.textoCentrado("PAUSA", 480, 330, 0xFFFFFF, 2.5f);
            p.textoCentrado("Pulsa P para continuar", 480, 270, 0xFFD166, 1f);
        }

        terminarFrame();
    }
}
