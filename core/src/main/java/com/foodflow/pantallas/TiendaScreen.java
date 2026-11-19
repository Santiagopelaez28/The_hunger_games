package com.foodflow.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.foodflow.FoodFlowGame;
import com.foodflow.logica.Mundo;
import com.foodflow.modelo.Ingrediente;
import com.foodflow.ui.Dibujos;
import com.foodflow.ui.Pincel;

import java.util.List;

/** Entre un dia y otro: se compran ingredientes que se agregan a la lista doble de disponibles. */
public class TiendaScreen extends PantallaBase {
    private static final float TARJETA_W = 214, TARJETA_H = 84;
    private static final float SIGUIENTE_X = 330, SIGUIENTE_Y = 136, SIGUIENTE_W = 300, SIGUIENTE_H = 56;
    private static final float RECETAS_X = 690, RECETAS_Y = 136, RECETAS_W = 220, RECETAS_H = 56;

    private String aviso = "";
    private float tiempoAviso = 0f;

    public TiendaScreen(FoodFlowGame juego) { super(juego); }

    private static float tx(int i) { return 32 + (i % 4) * 232; }
    private static float ty(int i) { return 300 - (i / 4) * 94; }

    private void siguienteDia() {
        juego.mundo.siguienteDia();
        juego.setScreen(new JuegoScreen(juego));
    }

    private void abrirRecetario() {
        juego.setScreen(new RecetasScreen(juego, RecetasScreen.Origen.TIENDA));
    }

    @Override
    public void render(float dt) {
        Mundo m = juego.mundo;
        List<Ingrediente> venta = m.ingredientesPorComprar();
        if (tiempoAviso > 0) tiempoAviso -= dt;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { siguienteDia(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) { abrirRecetario(); return; }
        if (hayClick()) {
            if (dentro(toque, RECETAS_X, RECETAS_Y, RECETAS_W, RECETAS_H)) {
                abrirRecetario();
                return;
            }
            if (dentro(toque, SIGUIENTE_X, SIGUIENTE_Y, SIGUIENTE_W, SIGUIENTE_H)) {
                siguienteDia();
                return;
            }
            for (int i = 0; i < venta.size(); i++) {
                if (dentro(toque, tx(i), ty(i), TARJETA_W, TARJETA_H)) {
                    Ingrediente ing = venta.get(i);
                    if (m.comprar(ing)) {
                        aviso = ing.getNombre() + " agregado a tu despensa!";
                    } else {
                        aviso = "Te faltan monedas para " + ing.getNombre();
                    }
                    tiempoAviso = 2.4f;
                    break;   // la lista cambio: se vuelve a leer en el proximo frame
                }
            }
        }
        venta = m.ingredientesPorComprar();

        empezarFrame();
        Pincel p = juego.pincel;
        float t = p.getTiempo();

        // ---------------------------------------------------------- fondo
        p.imagenCubriendo(juego.fondoTienda, 0, 0, 960, 600, 0.5f, 0.35f, 0x2C2233);
        p.rect(0, 0, 960, 600, 0x1A1020, 0.12f);
        p.degradadoAlpha(0, 330, 960, 270, 0x120E18, 0f, 0.80f);
        p.degradadoAlpha(0, 0, 960, 110, 0x120E18, 0.68f, 0f);
        p.halo(480, 540, 210, 0xFFB347, 0.24f + 0.04f * (float) Math.sin(t * 1.6f));

        // ---------------------------------------------------------- cabecera
        p.textoCentradoContorno("DIA " + m.getDia() + " COMPLETADO", 480, 560, 0xFFD166, 1.85f, 0x3A2410);
        p.textoCentradoSombra("Compra ingredientes nuevos para pedidos mas variados", 480, 522, 0xFFF3D6, 0.82f);

        // ---------------------------------------------------------- estadisticas
        dibujarEstadistica(p, 50, 460, "ENTREGADOS", String.valueOf(m.getEntregados()), 0, 0x8EE39B);
        dibujarEstadistica(p, 270, 460, "PERDIDOS", String.valueOf(m.getPerdidos()), 1, 0xFF9A9A);
        dibujarEstadistica(p, 490, 460, "VIDAS", String.valueOf(m.getVidas()), 2, 0xFF6B6B);
        dibujarEstadistica(p, 710, 460, "MONEDAS", "$" + m.getMonedas(), 3, 0xFFD166);

        // ---------------------------------------------------------- despensa
        // Hasta 16 ingredientes en dos filas de 8; la lista doble crece al comprar.
        List<Ingrediente> tienes = m.getDisponibles();
        for (int i = 0; i < tienes.size(); i++) {
            float x = 20 + (i % 8) * 116, y = 422 - (i / 8) * 30;
            p.panel(x, y, 112, 26, 9, 0x1B1F2E, 0x3E4560, 0.96f);
            Dibujos.icono(p, tienes.get(i), x + 13, y + 13, 18);
            p.texto(tienes.get(i).getNombre(), x + 24, y + 17, 0xE8E2D2, 0.42f);
        }

        // ---------------------------------------------------------- tienda
        if (venta.isEmpty()) {
            p.panel(280, 286, 400, 76, 18, 0x1B1F2E, 0x3E4560, 0.97f);
            p.circulo(314, 324, 18, 0x2E7D32, 1f);
            Dibujos.check(p, 314, 324, 22, 0xFFF3D6, 1f);
            p.textoCentrado("Ya tienes todos los ingredientes!", 500, 332, 0x8EE39B, 1.05f);
            p.textoCentrado("Tu cocina esta completa", 500, 308, 0xB9BED0, 0.72f);
        }
        for (int i = 0; i < venta.size(); i++) {
            dibujarOferta(p, m, venta.get(i), tx(i), ty(i));
        }

        // ---------------------------------------------------------- boton siguiente dia
        float pulso = 0.5f + 0.5f * (float) Math.sin(t * 2.8f);
        p.halo(480, SIGUIENTE_Y + SIGUIENTE_H / 2f, 170 + pulso * 8f, 0x6BD66B, 0.16f + 0.10f * pulso);
        p.boton("COMENZAR DIA " + (m.getDia() + 1), SIGUIENTE_X, SIGUIENTE_Y, SIGUIENTE_W, SIGUIENTE_H,
                0x6BCB77, true, false);
        p.tecla("ENTER", SIGUIENTE_X + SIGUIENTE_W - 42, SIGUIENTE_Y + SIGUIENTE_H / 2f);
        p.boton("RECETAS", RECETAS_X, RECETAS_Y, RECETAS_W, RECETAS_H, 0xB69CF2, true, false);
        p.tecla("R", RECETAS_X + RECETAS_W - 38, RECETAS_Y + RECETAS_H / 2f);

        // ---------------------------------------------------------- aviso / ayuda
        if (tiempoAviso > 0 && !aviso.isEmpty()) {
            float ancho = p.anchoTexto(aviso, 0.8f) + 40;
            p.panel(480 - ancho / 2f, 94, ancho, 30, 15, 0x16131E, 0xFFC24A, 0.97f);
            p.textoCentrado(aviso, 480, 110, 0xFFF3D6, 0.8f);
        } else {
            p.textoCentrado("Click en un ingrediente para comprarlo   |   R para ver recetas   |   ENTER para seguir",
                    480, 100, 0xB9BED0, 0.72f);
        }

        terminarFrame();
    }

    private void dibujarEstadistica(Pincel p, float x, float y, String etiqueta, String valor, int icono, int color) {
        p.panel(x, y, 200, 48, 13, 0x1B1F2E, 0x3E4560, 0.94f);
        float cx = x + 27, cy = y + 24;
        if (icono == 0) {
            p.circulo(cx, cy, 14, color, 0.18f);
            Dibujos.check(p, cx, cy, 17, color, 1f);
        } else if (icono == 1) {
            p.circulo(cx, cy, 14, color, 0.18f);
            Dibujos.cruz(p, cx, cy, 12, color, 1f);
        } else if (icono == 2) {
            p.circulo(cx, cy, 14, color, 0.18f);
            Dibujos.corazon(p, cx, cy, 7.5f, color, 1f);
        } else {
            Dibujos.moneda(p, cx, cy, 11);
        }
        p.texto(etiqueta, x + 50, y + 34, 0x9AA0B4, 0.55f);
        p.texto(valor, x + 50, y + 15, color, 0.95f);
    }

    private void dibujarOferta(Pincel p, Mundo m, Ingrediente ing, float x, float y) {
        boolean puede = m.getMonedas() >= ing.getCosto();
        boolean hover = puede && p.ratonDentro(x, y, TARJETA_W, TARJETA_H);
        float dy = hover ? 2f : 0f;

        int relleno = puede ? 0xFFF5E0 : 0xE2D8C5;
        int borde = puede ? (hover ? 0xFFC24A : 0xD9C79B) : 0xBFAF92;
        int tinta = puede ? 0x3A2A18 : 0x7A6A50;

        if (hover) p.halo(x + TARJETA_W / 2f, y + TARJETA_H / 2f, 120, 0xFFC24A, 0.20f);
        p.panel(x, y + dy, TARJETA_W, TARJETA_H, 14, relleno, borde, 1f);

        p.circulo(x + 38, y + dy + TARJETA_H / 2f, 24, 0x241811, 0.92f);
        p.circulo(x + 38, y + dy + TARJETA_H / 2f, 21, ing.getColor(), puede ? 0.25f : 0.12f);
        p.setAlfa(puede ? 1f : 0.55f);
        Dibujos.icono(p, ing, x + 38, y + dy + TARJETA_H / 2f, 45);
        p.setAlfa(1f);

        p.texto(ing.getNombre(), x + 72, y + dy + 61, tinta, 0.95f);
        p.texto("Aporta $" + ing.getValor(), x + 72, y + dy + 44, puede ? 0x8A6A42 : 0x9A8A70, 0.62f);

        p.redondeado(x + 72, y + dy + 9, 96, 20, 8, puede ? 0xF2B33D : 0xC9B99C, 1f);
        Dibujos.moneda(p, x + 84, y + dy + 18.5f, 7);
        p.texto("$" + ing.getCosto(), x + 95, y + dy + 23, puede ? 0x3A2708 : 0x7A6A50, 0.68f);

        int colorBoton = puede ? 0x2E7D32 : 0x9A8A70;
        float botonX = x + TARJETA_W - 23, botonY = y + dy + 23;
        p.circulo(botonX, botonY - 1.5f, 14, 0x241811, 0.22f);
        p.circulo(botonX, botonY, 14, 0xFFF5E0, 1f);
        p.circulo(botonX, botonY, 12, colorBoton, 1f);
        p.textoCentrado("+", botonX, botonY + 1, 0xFFFFFF, 1.35f);
    }
}
